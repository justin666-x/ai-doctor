import request from '@/utils/request'

export const addHistory = async (data) => request.post('/chatHistory/add', data, { headers:{'Content-Type':'application/json'} })

export const listHistory = async () => request.get('/chatHistory/list')

export const getHistory = async (id) => request.get(`/chatHistory/${id}`)

export const deleteHistory = async (id) => request.delete(`/chatHistory/${id}`)

export const updateHistory = async (id, data) => request.put(`/chatHistory/${id}`, data, { headers: { 'Content-Type': 'application/json' } }) 