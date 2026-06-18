<template>
  <div class="reminder-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用药提醒</span>
          <el-button type="primary" @click="showDialog = true">添加用药计划</el-button>
        </div>
      </template>
      <el-table :data="planList" stripe style="width: 100%">
        <el-table-column prop="drugName" label="药品名称" width="180" />
        <el-table-column prop="purpose" label="用途" width="200" />
        <el-table-column prop="dosageCount" label="用药次数/日" width="120" />
        <el-table-column prop="timePoints" label="用药时间点" />
        <el-table-column label="操作" width="160">
          <template #default="scope">
            <el-button size="small" @click="editPlan(scope.row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deletePlan(scope.row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="editMode ? '编辑用药计划' : '添加用药计划'" v-model="showDialog" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="药品名称">
          <el-input v-model="form.drugName" />
        </el-form-item>
        <el-form-item label="用途">
          <el-input v-model="form.purpose" />
        </el-form-item>
        <el-form-item label="每日次数">
          <el-input-number v-model="form.dosageCount" :min="1" :max="6" />
        </el-form-item>
        <el-form-item label="时间点(HH:mm)">
          <div v-for="(t,i) in timeList" :key="i" style="margin-bottom:6px;">
            <el-time-picker v-model="timeList[i]" format="HH:mm" value-format="HH:mm" placeholder="选择时间" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog=false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const planList = ref([])
const showDialog = ref(false)
const editMode = ref(false)
const form = ref({ id:null, drugName:'', purpose:'', dosageCount:1, timePoints:'' })
const timeList = ref([])

const fetchPlans = async () => {
  const res = await request.get('/medication/list')
  if(res.code===0){
    planList.value = res.data
  }
}

// utility to sync timeList length with dosageCount
const syncTimeList = (count)=>{
  if(count>timeList.value.length){
    for(let i=timeList.value.length;i<count;i++) timeList.value.push('')
  }else if(count<timeList.value.length){
    timeList.value.splice(count)
  }
}

const submitForm = async () => {
  if(!form.value.drugName){
    ElMessage.error('请填写药品名称')
    return
  }
  if(timeList.value.some(t=>!t)){
    ElMessage.error('请为每次用药选择时间')
    return
  }
  form.value.timePoints = timeList.value.join(',')
  if(editMode.value){
    await request.put('/medication/update', JSON.stringify(form.value), {
      headers:{'Content-Type':'application/json'}
    })
  }else{
    await request.post('/medication/add', JSON.stringify(form.value), {
      headers:{'Content-Type':'application/json'}
    })
  }
  ElMessage.success('保存成功')
  showDialog.value=false
  fetchPlans()
}

const editPlan = (row) => {
  form.value={...row}
  timeList.value = row.timePoints.split(',')
  editMode.value=true
  showDialog.value=true
}

const deletePlan = (id) => {
  ElMessageBox.confirm('确认删除该用药计划?','提示',{type:'warning'}).then(async ()=>{
    await request.delete(`/medication/delete/${id}`)
    ElMessage.success('删除成功')
    fetchPlans()
  })
}

// watch dosageCount to trim timeList when次数减少
watch(() => form.value.dosageCount, (newVal, oldVal) => {
  syncTimeList(newVal)
})

// initialise list when dialog open
watch(showDialog,(val)=>{if(val){syncTimeList(form.value.dosageCount)}})

// 当弹窗关闭时重置表单
watch(showDialog, (val) => {
  if (!val) {
    editMode.value = false
    form.value = { id:null, drugName:'', purpose:'', dosageCount:1, timePoints:'' }
    timeList.value = []
  }
})

onMounted(()=>{
  fetchPlans()
})
</script>

<style scoped>
.reminder-container{padding:24px;}
.card-header{display:flex;justify-content:space-between;align-items:center;}
.time-tag{margin-right:4px;margin-bottom:4px;}
</style> 