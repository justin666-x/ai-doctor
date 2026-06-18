<script setup>
import { ref } from 'vue'
import eventBus from '@/utils/eventBus'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const showReport = ref(false)
const reportData = ref({})
const saving = ref(false)

eventBus.on('health-report', (data) => {
  reportData.value = data
  showReport.value = true
})

// 新增工具函数：去除think和markdown
function cleanReportText(text) {
  let cleaned = text.replace(/<think>[\s\S]*?<\/think>/g, '');
  cleaned = cleaned
    .replace(/\*\*(.*?)\*\*/g, '$1')
    .replace(/\*(.*?)\*/g, '$1')
    .replace(/__(.*?)__/g, '$1')
    .replace(/^#+\s*(.*)$/gm, '$1')
    .replace(/^[-*]\s+/gm, '')
    .replace(/^\d+\.\s+/gm, '')
    .replace(/`([^`]+)`/g, '$1');
  return cleaned.trim();
}

const saveReport = async () => {
  // 保存前处理AI报告内容
  if (reportData.value && reportData.value.overallHealthReport) {
    reportData.value.overallHealthReport = cleanReportText(reportData.value.overallHealthReport);
  }
  if (saving.value) return
  saving.value = true
  try {
    await request.post('/healthData/save', JSON.stringify(reportData.value), {
      headers: { 'Content-Type': 'application/json' }
    })
    ElMessage.success('保存成功')
    showReport.value = false
    eventBus.emit('health-saved')
  } catch (e) {
    console.error(e)
  }
  saving.value = false
}
</script>

<template>
  <router-view />

  <!-- 全局健康报告弹窗 -->
  <el-dialog v-model="showReport" title="AI健康报告" width="600px" class="global-report-dialog">
    <pre class="report-text">{{ reportData.overallHealthReport }}</pre>
    <template #footer>
      <el-button @click="showReport=false">关闭</el-button>
      <el-button type="primary" @click="saveReport" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.global-report-dialog :deep(.el-dialog__body){
  max-height:60vh;
  overflow-y:auto;
}
.report-text{
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>
