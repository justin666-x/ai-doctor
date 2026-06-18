<template>
  <div class="health-plan-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>健康计划</span>
          <el-button type="primary" @click="showAIDialog = true">AI生成健康计划</el-button>
          <el-button @click="showAddDialog = true">手动添加</el-button>
        </div>
      </template>
      <el-table :data="planList" style="width: 100%" stripe>
        <el-table-column prop="planName" label="计划名称" width="150" />
        <el-table-column prop="goal" label="目标" width="180" />
        <el-table-column prop="cycle" label="周期" width="100" />
        <el-table-column prop="totalDays" label="总天数" width="80" />
        <el-table-column prop="remainingDays" label="剩余天数" width="90" />
        <el-table-column prop="dailyStatus" label="今日状态" width="90" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column prop="emailReminder" label="邮箱提醒" width="120">
          <template #default="scope">
            <el-tag
              :type="scope.row.emailReminder === 1 ? 'success' : 'info'"
              effect="light"
              style="cursor:pointer;user-select:none;display:flex;align-items:center;font-size:15px;"
              @click="toggleEmailReminder(scope.row)"
            >
              <svg v-if="scope.row.emailReminder === 1" width="18" height="18" viewBox="0 0 24 24" style="margin-right:6px;vertical-align:middle;" fill="#21b66f">
                <path d="M2 6.5A2.5 2.5 0 0 1 4.5 4h15A2.5 2.5 0 0 1 22 6.5v11A2.5 2.5 0 0 1 19.5 20h-15A2.5 2.5 0 0 1 2 17.5v-11Zm2.5-.5a.5.5 0 0 0-.5.5v.217l8 5.333 8-5.333V6.5a.5.5 0 0 0-.5-.5h-15Zm15 13a.5.5 0 0 0 .5-.5v-8.217l-7.5 5-7.5-5V17.5a.5.5 0 0 0 .5.5h15Z"/>
              </svg>
              <svg v-else width="18" height="18" viewBox="0 0 24 24" style="margin-right:6px;vertical-align:middle;" fill="#bfbfbf">
                <path d="M2 6.5A2.5 2.5 0 0 1 4.5 4h15A2.5 2.5 0 0 1 22 6.5v11A2.5 2.5 0 0 1 19.5 20h-15A2.5 2.5 0 0 1 2 17.5v-11Zm2.5-.5a.5.5 0 0 0-.5.5v.217l8 5.333 8-5.333V6.5a.5.5 0 0 0-.5-.5h-15Zm15 13a.5.5 0 0 0 .5-.5v-8.217l-7.5 5-7.5-5V17.5a.5.5 0 0 0 .5.5h15Z"/>
              </svg>
              <span :style="scope.row.emailReminder === 1 ? 'color:#21b66f;font-weight:600;' : 'color:#888;'">
                {{ scope.row.emailReminder === 1 ? '已开启' : '已关闭' }}
              </span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="操作" width="220">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" @click="editPlan(scope.row)">编辑</el-button>
              <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
              <el-button size="small" type="danger" @click="deletePlan(scope.row.id)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- AI生成健康计划弹窗 -->
    <el-dialog
      title="AI生成健康计划"
      v-model="showAIDialog"
      @keyup.enter="generateAIPlan"
    >
      <el-form :model="aiForm" label-width="100px">
        <el-form-item label="健康目标">
          <el-input
            v-model="aiForm.goal"
            placeholder="如减重5kg、控制血压等"
            @keyup.enter="generateAIPlan"
            @keydown.enter.prevent="generateAIPlan"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAIDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="loading"
          @click="generateAIPlan"
        >
          生成
        </el-button>
      </template>
    </el-dialog>

    <!-- 添加/编辑健康计划弹窗 -->
    <el-dialog
      :key="form.id || 'add'"
      :title="editMode ? '编辑健康计划' : '添加健康计划'"
      v-model="showAddDialog"
    >
      <el-form :model="form" label-width="100px">
        <el-form-item label="计划名称">
          <el-input v-model="form.planName" />
        </el-form-item>
        <el-form-item label="目标">
          <el-input v-model="form.goal" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" rows="3" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="form.startDate" type="date" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="form.endDate" type="date" />
        </el-form-item>
        <el-form-item label="周期">
          <el-select v-model="form.cycle" placeholder="请选择周期" style="width: 100%">
            <el-option label="每天" value="每天" />
            <el-option label="每周" value="每周" />
            <el-option label="每月" value="每月" />
          </el-select>
        </el-form-item>
        <el-form-item label="提醒">
          <el-time-picker
            v-model="form.reminder"
            placeholder="选择时间"
            format="HH:mm"
            value-format="HH:mm"
            :picker-options="{ selectableRange: '00:00:00 - 23:59:59', minuteStep: 1 }"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="邮箱提醒">
          <el-select v-model="form.emailReminder" style="width: 100px">
            <el-option :value="1" label="已开启" />
            <el-option :value="0" label="已关闭" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option label="进行中" value="进行中" />
            <el-option label="已完成" value="已完成" />
            <el-option label="已终止" value="已终止" />
          </el-select>
        </el-form-item>
        <el-form-item label="AI建议">
          <div v-if="form.aiAdvice" class="ai-advice-edit-section">
            <AiAdviceDisplay
              :content="form.aiAdvice"
              :editable="false"
              :show-mode-selector="true"
            />
          </div>
          <el-input v-model="form.aiAdvice" type="textarea" rows="2" placeholder="AI建议内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 计划详情弹窗 -->
    <el-dialog title="健康计划详情" v-model="showDetailDialog" width="800px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="计划名称">{{ detail.planName }}</el-descriptions-item>
        <el-descriptions-item label="目标">{{ detail.goal }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ detail.content }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ detail.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ detail.endDate }}</el-descriptions-item>
        <el-descriptions-item label="周期">{{ detail.cycle }}</el-descriptions-item>
        <el-descriptions-item label="提醒">{{ detail.reminder }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
      </el-descriptions>
      
      <!-- AI建议展示区域 -->
      <div v-if="detail.aiAdvice" class="ai-advice-section">
        <h4>AI建议</h4>
        <AiAdviceDisplay
          :content="detail.aiAdvice"
          :editable="true"
          :show-mode-selector="true"
          @status-change="handleAdviceStatusChange"
        />
      </div>
      
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import AiAdviceDisplay from '@/components/AiAdviceDisplay.vue'
import { parseAiAdvice } from '@/utils/aiAdviceParser'

const planList = ref([])
const showAddDialog = ref(false)
const showAIDialog = ref(false)
const showDetailDialog = ref(false)
const editMode = ref(false)
const form = ref({ id: null, planName: '', goal: '', content: '', startDate: '', endDate: '', cycle: '', reminder: '', status: '进行中', aiAdvice: '', emailReminder: 1 })
const aiForm = ref({ goal: '' })
const detail = ref({})
const loading = ref(false)

// 已提醒记录，避免同一计划同一天重复提醒
const remindedPlans = ref({})

// 解析提醒时间（如"每天08:00提醒"）
function parseReminderTime(reminderStr) {
  if (!reminderStr) return null
  // 支持"每天08:00提醒"或"08:00提醒"
  const match = reminderStr.match(/(\d{1,2}):(\d{2})/)
  if (match) {
    return { hour: parseInt(match[1]), minute: parseInt(match[2]) }
  }
  return null
}

// 检查并弹窗提醒
function checkAndNotifyPlans() {
  const now = new Date()
  // 仅在整秒=0时判断，避免同一分钟多次提醒
  if (now.getSeconds() !== 0) return
  const today = now.toISOString().slice(0, 10)
  planList.value.forEach(plan => {
    if (!plan.reminder) return
    const time = parseReminderTime(plan.reminder)
    if (!time) return
    const key = `${plan.id || plan.planName}_${today}`
    if (remindedPlans.value[key]) return
    if (
      now.getHours() === time.hour &&
      now.getMinutes() === time.minute
    ) {
      // 构造 HTML 列表形式的 AI 建议
      let adviceHtml = ''
      if (plan.aiAdvice) {
        const suggestions = parseAiAdvice(plan.aiAdvice)
        adviceHtml = '<ol style="padding-left:20px;">' + suggestions.map(s => `<li style=\"margin:4px 0;\">${s.text}</li>`).join('') + '</ol>'
      }

      const msgHtml =
        `【${plan.planName || '健康计划'}】<br/>${plan.goal || ''}` +
        (adviceHtml ? `<br/>AI建议：${adviceHtml}` : '')

      ElMessageBox.alert(
        msgHtml,
        '健康计划提醒',
        {
          confirmButtonText: '知道了',
          dangerouslyUseHTMLString: true,
          customClass: 'plan-alert-box'
        }
      )
      remindedPlans.value[key] = true
    }
  })
}

// 定时器句柄
let reminderTimer = null

// 保留通知授权函数备用（当前未使用）
function requestNotificationPermission() {}

const fetchPlans = async () => {
  const res = await request.get('/healthPlan/list')
  if (res.code === 0) {
    const today = new Date().toISOString().slice(0,10)
    planList.value = res.data.map(p=>{
      const totalDaysRaw = Math.floor((new Date(p.endDate) - new Date(p.startDate))/86400000)+1
      const totalDays = Math.max(0,totalDaysRaw)
      const remainingRaw = Math.ceil((new Date(p.endDate) - new Date(today))/86400000)+1
      const remainingDays = Math.max(0, remainingRaw)
      // 读取今日状态
      const dailyStatus = getDailyStatus(p.id, today)
      // 重新计算整体状态
      let status = p.status
      if (remainingDays === 0 && dailyStatus === '已完成') {
        status = '已完成'
      }
      return {
        ...p,
        totalDays,
        remainingDays,
        dailyStatus,
        status,
        emailReminder: p.emailReminder ?? 1
      }
    })
  }
}

const submitForm = async () => {
  if (!form.value.planName || !form.value.goal) {
    ElMessage.error('计划名称和目标不能为空')
    return
  }
  // 转换 emailReminder 保证为数字 0/1
  const payload = { ...form.value, emailReminder: form.value.emailReminder ? 1 : 0 }

  if (editMode.value) {
    await request.put('/healthPlan/update', JSON.stringify(payload), {
      headers: { 'Content-Type': 'application/json' }
    })
    ElMessage.success('修改成功')
  } else {
    await request.post('/healthPlan/add', JSON.stringify(payload), {
      headers: { 'Content-Type': 'application/json' }
    })
    ElMessage.success('添加成功')
  }
  showAddDialog.value = false
  fetchPlans()
}

const editPlan = (row) => {
  form.value = { ...row }
  editMode.value = true
  showAddDialog.value = true
}

const deletePlan = (id) => {
  ElMessageBox.confirm('确定要删除该健康计划吗？', '提示', { type: 'warning' })
    .then(async () => {
      await request.delete(`/healthPlan/delete/${id}`)
      ElMessage.success('删除成功')
      fetchPlans()
    })
}

const generateAIPlan = async () => {
  if (!aiForm.value.goal) {
    ElMessage.error('请填写健康目标')
    return
  }
  if (loading.value) return
  loading.value = true
  const res = await request.post('/healthPlan/generateAI', JSON.stringify(aiForm.value), {
    headers: { 'Content-Type': 'application/json' }
  })
  if (res.code === 0 && res.data) {
    form.value = {
      id: null,
      planName: res.data.planName || '',
      goal: res.data.goal || aiForm.value.goal || '',
      content: res.data.content || '',
      startDate: res.data.startDate || '',
      endDate: res.data.endDate || '',
      cycle: res.data.cycle || '',
      reminder: res.data.reminder || '',
      status: res.data.status || '进行中',
      aiAdvice: res.data.content || res.data.aiAdvice || '',
      emailReminder: 1
    }
    editMode.value = false
    showAIDialog.value = false
    // 异步打开新增弹窗，避免状态不同步
    setTimeout(() => {
      showAddDialog.value = true
    }, 100)
    ElMessage.success('AI已生成健康计划，请确认后保存')
  } else {
    ElMessage.error(res.message || 'AI生成失败')
  }
  loading.value = false
}

const viewDetail = (row) => {
  // 直接引用原对象，保持与列表同步
  detail.value = row
  showDetailDialog.value = true
}

// 读取/保存每日完成状态
function getDailyStatus(id, day){
  const key = `plan_daily_${id}_${day}`
  return localStorage.getItem(key) || '未完成'
}
function setDailyStatus(id, day, status){
  const key = `plan_daily_${id}_${day}`
  localStorage.setItem(key,status)
}

const handleAdviceStatusChange = (data) => {
  console.log('AI建议状态变化:', data)
  const today = new Date().toISOString().slice(0,10)
  const allDone = data.suggestions.every(s=>s.completed)
  const newDailyStatus = allDone ? '已完成' : '未完成'
  setDailyStatus(detail.value.id, today, newDailyStatus)
  detail.value.dailyStatus = newDailyStatus

  // 更新列表中的对应项
  const idx = planList.value.findIndex(p=>p.id===detail.value.id)
  if(idx>-1){
    const row = planList.value[idx]
    row.dailyStatus = newDailyStatus

    // 重新计算剩余天数基于日期
    const remaining = Math.max(0, Math.ceil((new Date(row.endDate) - new Date(today))/86400000)+1)
    row.remainingDays = remaining

    if(allDone){
      // 如果之前未完成，弹祝贺
      if(row.status!=='已完成' && remaining===0){
        ElMessage.success('你已完成全部计划！')
      } else {
        ElMessage.success('恭喜你完成今日计划！')
      }
    }

    // 更新整体状态
    if(remaining===0 && newDailyStatus==='已完成'){
      row.status='已完成'
    }else if(row.status==='已完成' && newDailyStatus!=='已完成'){
      row.status='进行中'
    }

    // 触发响应式刷新
    planList.value[idx] = { ...row }
  }
}

const toggleEmailReminder = async (row) => {
  try {
    await request.patch('/healthPlan/emailReminder', {
      id: row.id,
      emailReminder: row.emailReminder
    }, {
      headers: { 'Content-Type': 'application/json' }
    })
    ElMessage.success(row.emailReminder === 1 ? '邮箱提醒已开启' : '邮箱提醒已关闭')
  } catch (e) {
    ElMessage.error('更新失败')
    // 回滚
    row.emailReminder = row.emailReminder === 1 ? 0 : 1
  }
}

onMounted(() => {
  fetchPlans()
  // 每秒检测，确保到点准时提醒
  reminderTimer = setInterval(() => {
    checkAndNotifyPlans()
  }, 1000)
})

onUnmounted(() => {
  if (reminderTimer) clearInterval(reminderTimer)
})
</script>

<style scoped>
.health-plan-container {
  padding: 24px 48px; /* 加大左右缩进 */
}
.card-header {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  gap: 12px;
}
.ai-advice-section {
  margin-top: 20px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #e9ecef;
}
.ai-advice-section h4 {
  margin: 0 0 16px 0;
  color: #495057;
  font-size: 16px;
  font-weight: 600;
}
.ai-advice-edit-section {
  margin-bottom: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

/* 操作按钮布局 */
.action-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .health-plan-container {
    padding: 16px 20px;
  }
  
  .card-header {
    flex-direction: column;
    gap: 8px;
    align-items: stretch;
  }
  
  .ai-advice-section {
    padding: 12px;
  }
  
  .ai-advice-edit-section {
    padding: 8px;
  }
}

/* 表格单元格内边距 */
:deep(.el-table .cell) {
  padding-left: 16px;
  padding-right: 16px;
}
</style> 