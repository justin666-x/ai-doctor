<template>
  <el-card>
    <template #header>
      <span>问诊历史</span>
    </template>
    <el-table :data="histories" stripe style="width:100%">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="updateTime" label="最后时间" width="180">
        <template #default="scope">{{ formatTime(scope.row.updateTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="160" align="center">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="openChat(scope.row)">打开</el-button>
          <el-divider direction="vertical"></el-divider>
          <el-button type="danger" link size="small" @click="remove(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { listHistory, deleteHistory } from '@/api/history'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

const histories = ref([])
const router = useRouter()

const formatTime = ts => new Date(ts).toLocaleString('zh-CN', { hour12:false })

const load = async ()=>{
  try {
    const res = await listHistory()
    if(res.code===0){ histories.value = res.data }
  } catch(e){ ElMessage.error('加载失败') }
}

const openChat = (row)=>{
  router.push({ path:'/main/chat', query:{ sessionId: row.id }})
}

const remove = (row)=>{
  ElMessageBox.confirm('确定删除该会话历史？','警告',{ type:'warning' }).then(async()=>{
    try { await deleteHistory(row.id); ElMessage.success('已删除'); load() } catch(e){ ElMessage.error('删除失败') }
  }).catch(()=>{})
}

onMounted(load)
</script>
<style scoped></style> 