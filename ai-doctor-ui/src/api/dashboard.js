import request from '@/utils/request'

export const getHealthScore = async () => {
  return await request.get('/dashboard/health-score')
}

export const getDailyAdvice = async (location = '') => {
  const params = location ? { location } : {}
  return await request.get('/dashboard/daily-advice', { params })
}
