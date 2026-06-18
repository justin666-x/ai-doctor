import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/UserProfile.vue')
    },
    {
      path: '/main',
      component: () => import('@/views/Main.vue'),
      children: [
        {
          path: '',
          name: 'dashboard',
          component: () => import('@/views/Dashboard.vue')
        },
        {
          path: 'chat',
          name: 'chat',
          component: () => import('@/views/Chat.vue'),
          meta: { keepAlive: true }
        },
        {
          path: 'profile',
          name: 'userProfile',
          component: () => import('@/views/UserProfile.vue')
        },
        {
          path: 'settings',
          name: 'settings',
          component: () => import('@/views/Settings.vue')
        },
        {
          path: 'health',
          name: 'healthData',
          component: () => import('@/views/HealthData.vue')
        },
        {
          path: 'plan',
          name: 'healthPlan',
          component: () => import('@/views/HealthPlan.vue')
        },
        {
          path: 'reminder',
          name: 'medReminder',
          component: () => import('@/views/Reminder.vue')
        },
        {
          path: 'symptom',
          name: 'symptomRecord',
          component: () => import('@/views/SymptomRecord.vue')
        }
      ]
    }
  ]
})

export default router 