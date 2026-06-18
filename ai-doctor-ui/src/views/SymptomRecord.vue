<template>
  <el-card shadow="hover">
    <template #header>
      <span>症状记录</span>
    </template>
    <el-table :data="records" stripe style="width: 100%">
      <el-table-column prop="createTime" label="时间" width="170">
        <template #default="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="症状描述">
        <template #default="scope">
          {{ truncate(scope.row.symptomText) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="showDetail(scope.row)">查看</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="danger" link size="small" @click="confirmDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 详情弹窗 -->
    <el-dialog v-model="dialogVisible" width="600px" :title="'详情 (' + (selectedRecord ? formatTime(selectedRecord.createTime) : '') + ')'" destroy-on-close>
      <div v-if="selectedRecord">
        <h4>症状描述：</h4>
        <p style="white-space: pre-wrap;">{{ selectedRecord.symptomText }}</p>
        <h4 style="margin-top:20px;">AI 分析结果：</h4>
        <div style="white-space: pre-wrap;" v-html="renderMarkdown(selectedRecord.aiAnalysis)"></div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listSymptom, deleteSymptom } from '@/api/symptom'
import { ElMessage, ElMessageBox } from 'element-plus'

const records = ref([])
const dialogVisible = ref(false)
const selectedRecord = ref(null)

const formatTime = (ts) => new Date(ts).toLocaleString('zh-CN', { hour12: false })

const truncate = (text, len = 30) => {
  if (!text) return '-'
  return text.length > len ? text.slice(0, len) + '...' : text
}

const showDetail = (rec) => {
  selectedRecord.value = rec
  dialogVisible.value = true
}

const confirmDelete = (rec) => {
  ElMessageBox.confirm('确认删除该条症状记录？', '警告', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  }).then(async () => {
    try {
      const res = await deleteSymptom(rec.id)
      if (res.code === 0) {
        ElMessage.success('删除成功')
        records.value = records.value.filter(r => r.id !== rec.id)
      }
    } catch (e) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 优化后的markdown转HTML，确保-、*、数字列表都能转为ul>li
function renderMarkdown(text) {
  if (!text) return '';
  let html = text
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>') // **加粗**
    .replace(/\*(.*?)\*/g, '<i>$1</i>') // *斜体*
    .replace(/__(.*?)__/g, '<u>$1</u>') // __下划线__
    .replace(/^\s*[-\*][\s]?(.*)$/gm, '<li>$1</li>') // - 或 * 列表（更宽松，允许无空格）
    .replace(/^\s*\d+\.\s+(.*)$/gm, '<li>$1</li>') // 1. 列表
    .replace(/^#+\s*(.*)$/gm, '<b>$1</b>'); // # 标题
  // 连续<li>包裹为<ul>
  html = html.replace(/(<li>.*?<\/li>)(?:(?:<br>)?\s*)+/gs, function(match) {
    // 只包裹连续li
    if (/^(<li>.*?<\/li>)+$/.test(match.replace(/\s+/g, ''))) {
      return '<ul>' + match.replace(/<br>/g, '') + '</ul>';
    }
    return match;
  });
  // 剩余换行
  html = html.replace(/\n/g, '<br>');
  return html;
}

onMounted(async () => {
  try {
    const res = await listSymptom()
    if (res.code === 0 && Array.isArray(res.data)) {
      records.value = res.data
    }
  } catch (e) {
    ElMessage.error('获取症状记录失败')
  }
})
</script>

<style scoped>
.el-dialog__body ul {
  margin: 8px 0 8px 24px;
  padding-left: 18px;
  list-style-type: disc;
}
.el-dialog__body li {
  margin: 2px 0;
  padding-left: 2px;
}
</style> 