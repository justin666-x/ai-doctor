import request from '@/utils/request'

// 新增症状记录
export const addSymptom = async (data) => {
  return await request.post('/symptom/add', data, {
    headers: { 'Content-Type': 'application/json' }
  })
}

// 获取当前用户全部症状记录
export const listSymptom = async () => {
  return await request.get('/symptom/list')
}

// 删除症状记录
export const deleteSymptom = async (id) => {
  return await request.delete(`/symptom/delete/${id}`)
} 