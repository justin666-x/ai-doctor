<template>
  <div class="health-data-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康数据管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="generatePreview" :loading="loading">生成健康报告</el-button>
          </div>
        </div>
      </template>

      <el-form :model="form" label-position="top" class="health-form">
        <!-- 生命体征 -->
        <h4 class="section-title">生命体征</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="体温 (°C) 正常范围: 36.3-37.3">
              <el-input-number v-model="form.temperature" :step="0.1" :min="30" :max="45" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="心率 (次/分) 正常范围: 60-100">
              <el-input-number v-model="form.heartRate" :step="1" :min="30" :max="220" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="呼吸频率 (次/分) 正常范围: 12-20">
              <el-input-number v-model="form.respiratoryRate" :step="1" :min="5" :max="60" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="血压 (mmHg) 正常范围: 90-140/60-90">
              <el-input v-model="form.bloodPressure" placeholder="如 120/80" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 血常规 -->
        <h4 class="section-title">血常规</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="红细胞 (10^12/L) 正常范围: 男4.0-5.5, 女3.5-5.0">
              <el-input-number v-model="form.redBloodCell" :step="0.01" :precision="2" :min="0.5" :max="8" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血红蛋白 (g/L) 正常范围: 男130-175, 女115-150">
              <el-input-number v-model="form.hemoglobin" :step="1" :precision="1" :min="20" :max="250" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="白细胞 (10^9/L) 正常范围: 4.0-10.0">
              <el-input-number v-model="form.whiteBloodCell" :step="0.01" :precision="2" :min="0.1" :max="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="血小板 (10^9/L) 正常范围: 100-300">
              <el-input-number v-model="form.platelet" :step="1" :precision="1" :min="10" :max="800" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 血糖 -->
        <h4 class="section-title">血糖</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="空腹血糖 (mmol/L) 正常范围: 3.9-6.1">
              <el-input-number v-model="form.fastingBloodGlucose" :step="0.1" :precision="1" :min="1" :max="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="餐后2小时血糖 (mmol/L) 正常范围: <7.8">
              <el-input-number v-model="form.postprandialBloodGlucose" :step="0.1" :precision="1" :min="1" :max="30" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 血脂 -->
        <h4 class="section-title">血脂</h4>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="总胆固醇 (mmol/L) 正常范围: <5.2">
              <el-input-number v-model="form.totalCholesterol" :step="0.01" :precision="2" :min="0" :max="15" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="甘油三酯 (mmol/L) 正常范围: <1.7">
              <el-input-number v-model="form.triglycerides" :step="0.01" :precision="2" :min="0" :max="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="高密度脂蛋白胆固醇 (mmol/L) 正常范围: >1.0">
              <el-input-number v-model="form.hdlCholesterol" :step="0.01" :precision="2" :min="0" :max="5" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="低密度脂蛋白胆固醇 (mmol/L) 正常范围: <3.4">
              <el-input-number v-model="form.ldlCholesterol" :step="0.01" :precision="2" :min="0" :max="10" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>

      <!-- 健康报告展示 -->
      <div v-if="report" class="report-section">
        <h4>AI健康报告</h4>
        <pre>{{ report }}</pre>
      </div>

      <!-- 已保存数据列表 -->
      <h4 v-if="savedList.length" class="section-title">已保存数据</h4>
      <el-table v-if="savedList.length" :data="savedList" stripe style="width: 100%">
        <el-table-column label="记录时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.recordTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="temperature" label="体温" width="80" />
        <el-table-column prop="heartRate" label="心率" width="80" />
        <el-table-column prop="bloodPressure" label="血压" width="110" />
        <el-table-column prop="fastingBloodGlucose" label="空腹血糖" width="110" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">查看</el-button>
            <el-button size="small" type="danger" @click="deleteRow(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 详情弹窗 -->
      <el-dialog v-model="detailDialog" title="健康数据详情" width="600px" class="detail-dialog">
        <el-descriptions :column="2" border class="health-desc-table">
          <el-descriptions-item label="记录时间">{{ formatTime(detail.recordTime) }}</el-descriptions-item>
          <el-descriptions-item label="体温">{{ detail.temperature }}</el-descriptions-item>
          <el-descriptions-item label="心率">{{ detail.heartRate }}</el-descriptions-item>
          <el-descriptions-item label="呼吸频率">{{ detail.respiratoryRate }}</el-descriptions-item>
          <el-descriptions-item label="血压">{{ detail.bloodPressure }}</el-descriptions-item>
          <el-descriptions-item label="空腹血糖">{{ detail.fastingBloodGlucose }}</el-descriptions-item>
          <el-descriptions-item label="餐后血糖">{{ detail.postprandialBloodGlucose }}</el-descriptions-item>
          <el-descriptions-item label="总胆固醇">{{ detail.totalCholesterol }}</el-descriptions-item>
          <el-descriptions-item label="甘油三酯">{{ detail.triglycerides }}</el-descriptions-item>
          <el-descriptions-item label="HDL">{{ detail.hdlCholesterol }}</el-descriptions-item>
          <el-descriptions-item label="LDL" :span="2">{{ detail.ldlCholesterol }}</el-descriptions-item>
        </el-descriptions>
        <h4 style="margin-top:20px;">AI健康报告</h4>
        <div class="report-text" v-html="renderMarkdown(combineReport(detail))"></div>
        <template #footer>
          <el-button @click="detailDialog=false">关闭</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import eventBus from '@/utils/eventBus'

const loading = ref(false)
const report = ref('')
const savedList = ref([])
const detailDialog = ref(false)
const detail = ref({})

const form = reactive({
  temperature: null,
  heartRate: null,
  respiratoryRate: null,
  bloodPressure: '',
  redBloodCell: null,
  hemoglobin: null,
  whiteBloodCell: null,
  platelet: null,
  fastingBloodGlucose: null,
  postprandialBloodGlucose: null,
  totalCholesterol: null,
  triglycerides: null,
  hdlCholesterol: null,
  ldlCholesterol: null
})

const generatePreview = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await request.post('/healthData/preview', JSON.stringify(form), {
      headers: { 'Content-Type': 'application/json' },
      timeout: 120000
    })
    if (res.code === 0 && res.data) {
      eventBus.emit('health-report', res.data)
    }
  } catch (e) {
    console.error(e)
  }
  loading.value = false
}

const fetchList = async () => {
  const res = await request.get('/healthData/list')
  if (res.code === 0) {
    savedList.value = res.data || []
  }
}

const deleteRow = (id) => {
  ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' }).then(async () => {
    await request.delete(`/healthData/delete/${id}`)
    ElMessage.success('已删除')
    fetchList()
  })
}

const viewDetail = (row) => {
  detail.value = row
  detailDialog.value = true
}

const combineReport = (item) => {
  return (
    (item.overallHealthReport || '') ||
    [item.vitalSignsAnalysis, item.bloodRoutineAnalysis, item.bloodGlucoseAnalysis, item.bloodLipidAnalysis]
      .filter(Boolean)
      .join('\n')
  )
}

// 新增工具函数：去除think和markdown
function cleanReportText(text) {
  // 去除<think>内容
  let cleaned = text.replace(/<think>[\s\S]*?<\/think>/g, '');
  // 去除markdown符号
  cleaned = cleaned
    .replace(/\*\*(.*?)\*\*/g, '$1')      // **加粗**
    .replace(/\*(.*?)\*/g, '$1')            // *斜体*
    .replace(/__(.*?)__/g, '$1')              // __下划线__
    .replace(/^#+\s*(.*)$/gm, '$1')          // # 标题
    .replace(/^[-*]\s+/gm, '')               // - 列表项 或 * 列表项
    .replace(/^\d+\.\s+/gm, '')            // 1. 序号
    .replace(/`([^`]+)`/g, '$1');             // `行内代码`
  return cleaned.trim();
}

// 时间格式化函数，统一YYYY-MM-DD HH:mm:ss
function formatTime(ts) {
  if (!ts) return '-';
  const d = new Date(ts)
  const pad = n => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// markdown转HTML，和症状记录一致
function renderMarkdown(text) {
  if (!text) return '';
  let html = text
    .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>') // **加粗**
    .replace(/\*(.*?)\*/g, '<i>$1</i>') // *斜体*
    .replace(/__(.*?)__/g, '<u>$1</u>') // __下划线__
    .replace(/^\s*[-\*][\s]?(.*)$/gm, '<li>$1</li>') // - 或 * 列表（允许无空格）
    .replace(/^\s*\d+\.\s+(.*)$/gm, '<li>$1</li>') // 1. 列表
    .replace(/^#+\s*(.*)$/gm, '<b>$1</b>'); // # 标题
  // 连续<li>包裹为<ul>
  html = html.replace(/(<li>.*?<\/li>)(?:(?:<br>)?\s*)+/gs, function(match) {
    if (/^(<li>.*?<\/li>)+$/.test(match.replace(/\s+/g, ''))) {
      return '<ul>' + match.replace(/<br>/g, '') + '</ul>';
    }
    return match;
  });
  html = html.replace(/\n/g, '<br>');
  return html;
}

onMounted(() => {
  fetchList()
  eventBus.on('health-saved', fetchList)
})
</script>

<style scoped>
.health-data-container {
  padding: 24px 48px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  gap: 12px;
}
.section-title {
  margin: 24px 0 12px;
  font-size: 16px;
  font-weight: 600;
}
.report-section {
  margin-top: 32px;
  background: var(--color-surface-container);
  padding: 16px;
  border-radius: 8px;
  white-space: pre-wrap;
}

/* 表单统一样式 */
.health-form .el-form-item__label {
  font-weight: 500;
}

/* 统一限制输入框最大宽度，保持简洁 */
.health-form :deep(.el-input-number),
.health-form :deep(.el-input) {
  max-width: 260px;
}

.detail-dialog {
  /* Add your styles here */
}

.detail-dialog :deep(.el-dialog__body){
  max-height:60vh;
  overflow-y:auto;
}

.report-text{
  white-space: pre-wrap;
  line-height:1.6;
  word-break: break-word;
}
.health-desc-table {
  background: var(--color-surface-container-low);
  border-radius: var(--radius-xl);
  padding: 12px 0 4px 0;
  font-size: 15px;
  box-shadow: 0 2px 8px 0 rgba(0,0,0,0.04);
}
.health-desc-table :deep(.el-descriptions__header) {
  display: none;
}
.health-desc-table :deep(.el-descriptions__body) {
  border-radius: 12px;
  overflow: hidden;
}
.health-desc-table :deep(.el-descriptions__table) {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}
.health-desc-table :deep(td),
.health-desc-table :deep(th) {
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 14px;
  font-size: 15px;
}
.health-desc-table :deep(tr:last-child) td {
  border-bottom: none;
}
.health-desc-table :deep(.el-descriptions-item__label) {
  color: #888;
  font-weight: 500;
  background: var(--color-surface-container-low);
}
.health-desc-table :deep(.el-descriptions-item__content) {
  color: #222;
  font-weight: 500;
}
.health-desc-table :deep(tr:hover) td {
  background: var(--color-primary-container);
}
.health-desc-table :deep(.el-descriptions-item__content[colspan="2"]) {
  text-align: center;
}
.health-desc-table :deep(.el-descriptions__label) {
  flex-basis: 35% !important;
  max-width: 35%;
  min-width: 80px;
}
.health-desc-table :deep(.el-descriptions__content) {
  flex-basis: 65% !important;
  max-width: 65%;
  min-width: 100px;
}
.report-text ul {
  margin: 8px 0 8px 24px;
  padding-left: 18px;
  list-style-type: disc;
}
.report-text li {
  margin: 2px 0;
  padding-left: 2px;
}
</style> 