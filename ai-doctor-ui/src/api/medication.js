import request from '@/utils/request'

export const addMedicationPlan = async (data) => {
  return await request.post('/medication/add', JSON.stringify(data), {
    headers:{'Content-Type':'application/json'}
  })
}

export const listMedicationPlans = async () => {
  return await request.get('/medication/list')
}

export const updateMedicationPlan = async (data) => {
  return await request.put('/medication/update', JSON.stringify(data), {
    headers:{'Content-Type':'application/json'}
  })
}

export const deleteMedicationPlan = async (id) => {
  return await request.delete(`/medication/delete/${id}`)
} 