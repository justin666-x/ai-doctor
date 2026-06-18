<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getUserProfile } from '@/api/user'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const displayName = ref('用户')
const avatar = ref('')
const unreadCount = ref(0)

const isDashboard = computed(() => route.path === '/main' || route.path === '/main/')
const pageTitle = computed(() => {
  const m = { '/main/chat': '智能问诊', '/main/health': '健康数据', '/main/plan': '健康计划', '/main/reminder': '用药提醒', '/main/symptom': '症状记录', '/main/profile': '个人信息', '/main/settings': '设置' }
  return m[route.path] || ''
})

onMounted(async () => {
  const ui = localStorage.getItem('userInfo')
  if (ui) { try { const r = await getUserProfile(); if (r.code === 0 && r.data) { if (r.data.userPic) avatar.value = r.data.userPic; if (r.data.nickname) displayName.value = r.data.nickname } } catch {} }
  try { const r = await request.get('/medication/list'); if (r.code === 0 && r.data?.length) unreadCount.value = r.data.length } catch {}
})

const go = (p) => router.push(p)
const logout = () => { localStorage.removeItem('userInfo'); router.push('/login') }
</script>

<template>
  <div class="layout">
    <!-- 顶栏 -->
    <header class="topbar">
      <div class="topbar-left" @click="go('/main')">
        <div class="topbar-logo"><span class="material-symbols-outlined">health_and_safety</span></div>
        <div class="topbar-brand">
          <strong>AI Doctor</strong>
          <small>智能健康助手</small>
        </div>
      </div>

      <div class="topbar-right">
        <button class="tb-btn" :class="{ 'has-dot': unreadCount > 0 }" @click="go('/main/reminder')">
          <span class="material-symbols-outlined">notifications</span>
          <span v-if="unreadCount > 0" class="notif-dot">{{ unreadCount }}</span>
        </button>
        <div class="user-menu" @click="go('/main/profile')">
          <div class="user-avatar">
            <img v-if="avatar" :src="avatar" />
            <span v-else class="material-symbols-outlined">person</span>
          </div>
          <span class="user-name">{{ displayName }}</span>
          <span class="material-symbols-outlined user-chevron">expand_more</span>
        </div>
        <button class="tb-btn" @click="go('/main/settings')">
          <span class="material-symbols-outlined">settings</span>
        </button>
      </div>
    </header>

    <!-- 子页返回栏 -->
    <div v-if="!isDashboard" class="subbar">
      <button class="back-link" @click="go('/main')"><span class="material-symbols-outlined">arrow_back</span>返回</button>
      <span class="subbar-title">{{ pageTitle }}</span>
      <span></span>
    </div>

    <!-- 内容区 -->
    <main class="content">
      <router-view v-slot="{ Component, route: r }">
        <keep-alive><component v-if="r.meta?.keepAlive" :is="Component" :key="r.fullPath" /></keep-alive>
        <component v-if="!r.meta?.keepAlive" :is="Component" :key="r.fullPath" />
      </router-view>
    </main>
  </div>
</template>

<style lang="scss" scoped>
.layout { min-height: 100vh; display: flex; flex-direction: column; }

/* 顶栏 */
.topbar { display: flex; align-items: center; justify-content: space-between; height: 60px; padding: 0 24px; background: var(--white); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 50; }
.topbar-left { display: flex; align-items: center; gap: 10px; cursor: pointer; }
.topbar-logo { width: 34px; height: 34px; background: linear-gradient(135deg, #3b82f6, #6366f1); border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.topbar-logo .material-symbols-outlined { font-size: 20px; color: #fff; }
.topbar-brand strong { display: block; font-size: 16px; font-weight: 700; color: var(--text); line-height: 1.2; letter-spacing: -0.3px; }
.topbar-brand small { font-size: 11px; color: var(--text3); font-weight: 500; }

.topbar-right { display: flex; align-items: center; gap: 4px; }
.tb-btn { position: relative; width: 36px; height: 36px; border: none; border-radius: var(--radius); background: transparent; color: var(--text2); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.15s; }
.tb-btn:hover { background: var(--bg); color: var(--text); }
.tb-btn .material-symbols-outlined { font-size: 20px; }
.notif-dot { position: absolute; top: 3px; right: 3px; min-width: 17px; height: 17px; border-radius: 9999px; background: var(--red); color: #fff; font-size: 10px; font-weight: 700; display: flex; align-items: center; justify-content: center; padding: 0 3px; line-height: 1; }
.user-menu { display: flex; align-items: center; gap: 7px; padding: 4px 10px 4px 4px; border-radius: 9999px; cursor: pointer; transition: all 0.15s; }
.user-menu:hover { background: var(--bg); }
.user-avatar { width: 30px; height: 30px; border-radius: 50%; overflow: hidden; background: var(--bg); display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.user-avatar img { width: 100%; height: 100%; object-fit: cover; }
.user-avatar .material-symbols-outlined { font-size: 18px; color: var(--blue); }
.user-name { font-size: 13px; font-weight: 600; color: var(--text); }
.user-chevron { font-size: 18px; color: var(--text3); }

/* 返回栏 */
.subbar { display: flex; align-items: center; justify-content: space-between; height: 44px; padding: 0 20px; background: var(--white); border-bottom: 1px solid var(--border); }
.back-link { display: flex; align-items: center; gap: 4px; border: none; background: none; color: var(--blue); font-size: 13px; font-weight: 500; cursor: pointer; font-family: var(--font); }
.back-link:hover { opacity: 0.7; }
.back-link .material-symbols-outlined { font-size: 18px; }
.subbar-title { font-size: 14px; font-weight: 600; color: var(--text); }

/* 内容 */
.content { flex: 1; background: var(--bg); overflow-y: auto; }
</style>
