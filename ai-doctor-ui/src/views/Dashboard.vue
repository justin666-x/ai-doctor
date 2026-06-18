<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserProfile } from '@/api/user'
import { getHealthScore, getDailyAdvice } from '@/api/dashboard'
import request from '@/utils/request'

const router = useRouter()
const displayName = ref('用户')
const avatar = ref('')
const healthScore = ref(72)
const scoreTag = ref('')
const hasHealthData = ref(false)
const medNext = ref(null)
const recentConsultations = ref([])
const aiTip = ref('')
const aiTipTag = ref('')
const weatherText = ref('今日晴 · 气温 25°C 🌤️')
const planCount = ref(0)
const tipsLoading = ref(true)
const scoreLoading = ref(true)

onMounted(async () => {
  const ui = localStorage.getItem('userInfo')
  if (ui) { try { const r = await getUserProfile(); if (r.code === 0 && r.data) { if (r.data.userPic) avatar.value = r.data.userPic; if (r.data.nickname) displayName.value = r.data.nickname } } catch {} }

  // AI 健康评分
  try {
    const r = await getHealthScore()
    if (r.code === 0 && r.data) {
      healthScore.value = r.data.score || 72
      scoreTag.value = r.data.tag || ''
      hasHealthData.value = r.data.hasData !== false
    }
  } catch {} finally { scoreLoading.value = false }

  // AI 每日建议 + 天气
  try {
    let loc = ''
    try {
      const pos = await new Promise((resolve, reject) => {
        navigator.geolocation.getCurrentPosition(resolve, reject, { timeout: 5000 })
      })
      loc = `${pos.coords.longitude.toFixed(2)},${pos.coords.latitude.toFixed(2)}`
    } catch {}
    const r = await getDailyAdvice(loc)
    if (r.code === 0 && r.data) {
      aiTip.value = r.data.advice || ''
      aiTipTag.value = r.data.tag || ''
      const w = r.data
      weatherText.value = `今日${w.weather || '晴'} · 气温 ${w.temperature || '25°C'} ${w.weatherIcon || '🌤️'}`
    }
  } catch {} finally { tipsLoading.value = false }

  // 用药提醒
  try { const r = await request.get('/medication/list'); if (r.code === 0 && r.data?.length) { const now = new Date(); const hhmm = now.getHours() * 60 + now.getMinutes(); const upcoming = r.data.flatMap(p => p.timePoints.split(',').map(t => { const [h,m] = t.trim().split(':').map(Number); return { name: p.drugName, minutes: h * 60 + m } })).filter(x => x.minutes > hhmm).sort((a,b) => a.minutes - b.minutes); if (upcoming.length) { const d = upcoming[0]; const diff = d.minutes - hhmm; medNext.value = diff < 60 ? `${diff} 分钟后` : `${Math.floor(diff/60)} 小时 ${diff%60} 分钟` } } } catch {}

  // 今日待办数
  try { const r = await request.get('/healthPlan/list'); if (r.code === 0 && r.data) planCount.value = r.data.length } catch {}

  // 最近问诊
  try { const r = await request.get('/symptom/list'); if (r.code === 0 && r.data) recentConsultations.value = r.data.slice(0, 3) } catch {}
})

const scoreColor = computed(() => {
  const s = healthScore.value
  if (s >= 85) return '#10b981'
  if (s >= 70) return '#3b82f6'
  if (s >= 55) return '#f59e0b'
  return '#ef4444'
})

const fmtDate = s => { if (!s) return ''; const d = new Date(s); return `${d.getMonth()+1}/${d.getDate()}` }

const shortcuts = [
  { label: '头痛', icon: '🤕' },
  { label: '感冒发烧', icon: '🤒' },
  { label: '肠胃不适', icon: '🤢' },
  { label: '失眠', icon: '😴' },
  { label: '皮肤问题', icon: '🌟' },
  { label: '体检解读', icon: '📋' },
]

const features = [
  { title: '健康数据', desc: '体征指标记录分析', icon: 'monitoring', color: '#3b82f6', bg: '#eff6ff', path: '/main/health' },
  { title: '健康计划', desc: '定制饮食运动目标', icon: 'event_note', color: '#8b5cf6', bg: '#f5f3ff', path: '/main/plan' },
  { title: '用药提醒', desc: '准时服药不遗漏', icon: 'medication', color: '#ec4899', bg: '#fdf2f8', path: '/main/reminder' },
  { title: '症状记录', desc: '问诊历史随时查', icon: 'description', color: '#f59e0b', bg: '#fffbeb', path: '/main/symptom' },
]

const period = new Date().getHours() < 12 ? '早上好' : new Date().getHours() < 18 ? '下午好' : '晚上好'

const tagLabel = (t) => {
  const m = { '运动': '🏃 运动', '饮食': '🥗 饮食', '作息': '😴 作息', '防护': '🛡 防护', '出行': '🚗 出行', '优秀': '优秀', '良好': '良好', '一般': '一般', '需关注': '需关注', '待完善': '待完善' }
  return m[t] || t
}
</script>

<template>
  <div class="dash">
    <!-- 医疗背景装饰 -->
    <div class="bg-decor">
      <svg class="ecg-line" viewBox="0 0 1200 200" preserveAspectRatio="none"><polyline fill="none" stroke="rgba(59,130,246,0.04)" stroke-width="2" points="0,100 50,100 70,100 75,40 80,160 85,100 120,100 135,100 140,60 145,140 150,100 200,100 250,100 255,30 260,170 265,100 300,100 350,100 355,50 360,150 365,100 400,100 450,100 455,35 460,165 465,100 500,100 550,100 555,45 560,155 565,100 600,100 650,100 655,40 660,160 665,100 700,100 750,100 755,55 760,145 765,100 800,100 850,100 855,30 860,170 865,100 900,100 950,100 955,50 960,150 965,100 1000,100 1050,100 1055,45 1060,155 1065,100 1100,100 1150,100 1155,35 1160,165 1165,100 1200,100"/></svg>
    </div>
    <div class="bg-blob blob-1"></div>

    <!-- 欢迎区 -->
    <div class="greeting">
      <div class="greet-avatar">
        <img v-if="avatar" :src="avatar" />
        <span v-else class="material-symbols-outlined">person</span>
      </div>
      <div class="greet-text">
        <h1>{{ period }}，{{ displayName }}</h1>
        <p>{{ weatherText }}</p>
      </div>
    </div>

    <!-- 健康评分 + 三卡片 -->
    <div class="top-row">
      <div class="score-card">
        <svg class="score-ring" viewBox="0 0 100 100">
          <circle cx="50" cy="50" r="40" fill="none" stroke="var(--border)" stroke-width="6" />
          <circle cx="50" cy="50" r="40" fill="none" :stroke="scoreColor" stroke-width="6"
            stroke-linecap="round" :stroke-dasharray="2 * Math.PI * 40"
            :stroke-dashoffset="2 * Math.PI * 40 * (1 - healthScore / 100)"
            transform="rotate(-90 50 50)" style="transition: stroke-dashoffset 1s ease" />
        </svg>
        <div class="score-center">
          <div class="score-val" :style="{ color: scoreColor }">{{ healthScore }}</div>
          <div class="score-label">健康评分</div>
          <span v-if="scoreTag" class="score-tag" :class="'tag-' + scoreTag">{{ scoreTag }}</span>
        </div>
      </div>
      <div v-if="medNext" class="stat-mini" @click="router.push('/main/reminder')">
        <div class="stat-mini-icon" style="background:#fdf2f8;color:#ec4899"><span class="material-symbols-outlined">alarm</span></div>
        <div class="stat-mini-body"><div class="stat-mini-val">{{ medNext }}</div><div class="stat-mini-label">距离下次用药</div></div>
      </div>
      <div v-else class="stat-mini stat-mini-empty" @click="router.push('/main/reminder')">
        <div class="stat-mini-icon" style="background:#fdf2f8;color:#ec4899"><span class="material-symbols-outlined">add_circle</span></div>
        <div class="stat-mini-body"><div class="stat-mini-val">未设置</div><div class="stat-mini-label">添加用药提醒</div></div>
      </div>
      <div class="stat-mini" @click="router.push('/main/plan')">
        <div class="stat-mini-icon" style="background:#f5f3ff;color:#8b5cf6"><span class="material-symbols-outlined">task_alt</span></div>
        <div class="stat-mini-body"><div class="stat-mini-val">{{ planCount }} 项</div><div class="stat-mini-label">今日待办</div></div>
      </div>
      <div class="stat-mini" @click="router.push('/main/health')">
        <div class="stat-mini-icon" style="background:#e6f7f5;color:#0d9488"><span class="material-symbols-outlined">directions_run</span></div>
        <div class="stat-mini-body"><div class="stat-mini-val">0 分钟</div><div class="stat-mini-label">本周运动</div></div>
      </div>
    </div>

    <!-- 主入口 -->
    <div class="hero" @click="router.push('/main/chat')">
      <div class="hero-inner">
        <div class="hero-icon-wrap"><span class="material-symbols-outlined">smart_toy</span></div>
        <div class="hero-text"><h2>开始智能问诊</h2><p>描述症状，AI 医生为你分析</p></div>
      </div>
      <div class="hero-arrow"><span class="material-symbols-outlined">arrow_forward</span></div>
    </div>

    <!-- 症状快捷入口 -->
    <div class="section-header"><span class="section-label">哪里不舒服？</span></div>
    <div class="symptom-grid">
      <button v-for="s in shortcuts" :key="s.label" class="symptom-chip" @click="router.push('/main/chat')">
        <span class="symptom-emoji">{{ s.icon }}</span>{{ s.label }}
      </button>
    </div>

    <!-- 功能入口 -->
    <div class="section-header"><span class="section-label">健康管理</span></div>
    <div class="card-grid">
      <div v-for="f in features" :key="f.title" class="fcard" @click="router.push(f.path)">
        <div class="fc-icon" :style="{ background: f.bg, color: f.color }"><span class="material-symbols-outlined">{{ f.icon }}</span></div>
        <div class="fc-body"><h3>{{ f.title }}</h3><p>{{ f.desc }}</p></div>
        <span class="material-symbols-outlined fc-arrow">chevron_right</span>
      </div>
    </div>

    <!-- AI 每日小贴士 -->
    <div class="ai-tip-card">
      <div class="ai-tip-top">
        <div class="ai-tip-icon"><span class="material-symbols-outlined">auto_awesome</span></div>
        <span class="ai-tip-title">AI 健康提醒</span>
        <span v-if="aiTipTag" class="ai-tip-chip">{{ tagLabel(aiTipTag) }}</span>
      </div>
      <div class="ai-tip-body">
        <span v-if="tipsLoading" class="ai-tip-loading">正在生成...</span>
        <p v-else>{{ aiTip || '今天保持良好作息，关注身体状态变化' }}</p>
      </div>
    </div>

    <!-- 最近问诊 -->
    <div v-if="recentConsultations.length" class="section-header"><span class="section-label">最近问诊</span></div>
    <div v-if="recentConsultations.length" class="recent-list">
      <div v-for="c in recentConsultations" :key="c.id" class="recent-item" @click="router.push('/main/symptom')">
        <div class="recent-left">
          <span class="material-symbols-outlined recent-icon">description</span>
          <div>
            <div class="recent-title">{{ c.symptomText?.slice(0, 30) }}...</div>
            <div class="recent-date">{{ fmtDate(c.createTime) }}</div>
          </div>
        </div>
        <span class="material-symbols-outlined recent-arrow">chevron_right</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.dash { max-width: 1100px; margin: 0 auto; padding: 32px 28px 60px; position: relative; }
.bg-blob { position: absolute; width: 400px; height: 400px; border-radius: 50%; filter: blur(120px); opacity: 0.035; pointer-events: none; z-index: 0; }
.blob-1 { background: var(--blue); top: -60px; right: -120px; }
.bg-decor { position: absolute; inset: 0; overflow: hidden; pointer-events: none; z-index: 0; }
.ecg-line { position: absolute; top: 80px; left: 0; width: 100%; opacity: 0.5; }

/* 欢迎 */
.greeting { display: flex; align-items: center; gap: 16px; margin-bottom: 24px; position: relative; z-index: 1; }
.greet-avatar { width: 52px; height: 52px; min-width: 52px; border-radius: 50%; overflow: hidden; background: var(--bg); display: flex; align-items: center; justify-content: center; border: 2px solid var(--blue-light); }
.greet-avatar img { width: 100%; height: 100%; object-fit: cover; }
.greet-avatar .material-symbols-outlined { font-size: 28px; color: var(--blue); }
.greet-text h1 { font-size: 26px; font-weight: 700; letter-spacing: -0.3px; }
.greet-text p { font-size: 14px; color: var(--text2); margin-top: 4px; }

/* 评分 + 用药 */
.top-row { display: grid; grid-template-columns: 160px 1fr 1fr 1fr; gap: 12px; margin-bottom: 28px; position: relative; z-index: 1; }
.score-card { position: relative; background: var(--white); border: 1px solid var(--border); border-radius: 14px; padding: 16px; display: flex; align-items: center; justify-content: center; }
.stat-mini { background: var(--white); border: 1px solid var(--border); border-radius: 14px; padding: 18px 20px; cursor: pointer; transition: all 0.2s; display: flex; flex-direction: column; gap: 12px; }
.stat-mini:hover { box-shadow: var(--shadow-md); transform: translateY(-1px); }
.stat-mini-empty { opacity: 0.6; }
.stat-mini-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.stat-mini-icon .material-symbols-outlined { font-size: 20px; }
.stat-mini-body .stat-mini-val { font-size: 22px; font-weight: 700; color: var(--text); }
.stat-mini-body .stat-mini-label { font-size: 12px; color: var(--text2); font-weight: 500; }
.score-ring { width: 90px; height: 90px; }
.score-center { position: absolute; text-align: center; }
.score-val { font-size: 28px; font-weight: 700; line-height: 1; }
.score-label { font-size: 11px; color: var(--text3); margin-top: 2px; }
.score-delta { font-size: 10px; color: var(--text3); margin-top: 2px; max-width: 90px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.score-tag { display: inline-block; font-size: 9px; padding: 2px 8px; border-radius: 9999px; font-weight: 600; margin-top: 4px; letter-spacing: 0.04em; }
.tag-优秀 { background: #d1fae5; color: #059669; }
.tag-良好 { background: #dbeafe; color: #2563eb; }
.tag-一般, .tag-待完善 { background: #fef3c7; color: #d97706; }
.tag-需关注 { background: #fee2e2; color: #dc2626; }

/* 主入口 */
.hero { display: flex; align-items: center; justify-content: space-between; background: linear-gradient(135deg, #3b82f6 0%, #6366f1 50%, #8b5cf6 100%); border-radius: 16px; padding: 26px 28px; color: #fff; cursor: pointer; transition: all 0.25s; margin-bottom: 28px; position: relative; z-index: 1; box-shadow: 0 4px 20px rgba(59,130,246,0.25); }
.hero:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(59,130,246,0.35); }
.hero:hover .hero-arrow { transform: translateX(4px); }
.hero-inner { display: flex; align-items: center; gap: 16px; }
.hero-icon-wrap { width: 48px; height: 48px; background: rgba(255,255,255,0.2); border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.hero-icon-wrap .material-symbols-outlined { font-size: 26px; }
.hero-text h2 { font-size: 20px; font-weight: 700; }
.hero-text p { font-size: 13px; opacity: 0.8; margin-top: 3px; font-weight: 400; }
.hero-arrow { transition: transform 0.25s ease; }
.hero-arrow .material-symbols-outlined { font-size: 28px; opacity: 0.6; }

/* 区块标题 */
.section-header { margin-bottom: 10px; position: relative; z-index: 1; }
.section-label { font-size: 13px; font-weight: 600; color: var(--text2); text-transform: uppercase; letter-spacing: 0.06em; }

/* 症状快捷 */
.symptom-grid { display: grid; grid-template-columns: repeat(6, 1fr); gap: 8px; margin-bottom: 28px; position: relative; z-index: 1; }
.symptom-chip { display: flex; align-items: center; gap: 7px; padding: 10px 14px; border: 1px solid var(--border); border-radius: 12px; background: var(--white); font-size: 13px; font-weight: 500; color: var(--text); cursor: pointer; transition: all 0.18s; font-family: var(--font); }
.symptom-chip:hover { border-color: var(--blue); background: var(--blue-light); color: var(--blue); transform: translateY(-1px); box-shadow: var(--shadow-md); }
.symptom-emoji { font-size: 18px; }

/* 功能卡片 */
.card-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; position: relative; z-index: 1; }
.fcard { background: var(--white); border: 1px solid var(--border); border-radius: 14px; padding: 16px 18px; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; gap: 12px; }
.fcard:hover { border-color: transparent; box-shadow: 0 4px 16px rgba(0,0,0,0.08); transform: translateY(-2px); }
.fc-icon { width: 40px; height: 40px; min-width: 40px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.fc-icon .material-symbols-outlined { font-size: 20px; }
.fc-body h3 { font-size: 14px; font-weight: 600; }
.fc-body p { font-size: 12px; color: var(--text3); margin-top: 2px; }
.fc-arrow { font-size: 18px; color: var(--text3); transition: all 0.2s; }
.fcard:hover .fc-arrow { color: var(--blue); transform: translateX(2px); }

/* AI 小贴士 */
.ai-tip-card { background: linear-gradient(135deg, #eff6ff 0%, #fdf2f8 100%); border: 1px solid rgba(59,130,246,0.08); border-radius: 16px; padding: 20px 24px; margin: 28px 0; position: relative; z-index: 1; overflow: hidden; }
.ai-tip-card::before { content: ''; position: absolute; top: -30px; right: -30px; width: 80px; height: 80px; background: radial-gradient(circle, rgba(59,130,246,0.06) 0%, transparent 70%); border-radius: 50%; }
.ai-tip-top { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.ai-tip-icon { width: 34px; height: 34px; min-width: 34px; border-radius: 10px; background: linear-gradient(135deg, #3b82f6, #8b5cf6); display: flex; align-items: center; justify-content: center; color: #fff; }
.ai-tip-icon .material-symbols-outlined { font-size: 18px; animation: sparkle 2s ease-in-out infinite; }
@keyframes sparkle { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
.ai-tip-title { font-size: 13px; font-weight: 600; color: #3b82f6; letter-spacing: 0.02em; }
.ai-tip-chip { font-size: 11px; padding: 3px 10px; border-radius: 9999px; background: rgba(139,92,246,0.1); color: #8b5cf6; font-weight: 600; white-space: nowrap; }
.ai-tip-body p { font-size: 15px; color: var(--text); line-height: 1.7; font-weight: 500; margin: 0; }
.ai-tip-loading { font-size: 14px; color: var(--text3); font-style: italic; }

/* 最近问诊 */
.recent-list { display: flex; flex-direction: column; gap: 6px; position: relative; z-index: 1; }
.recent-item { display: flex; align-items: center; justify-content: space-between; background: var(--white); border: 1px solid var(--border); border-radius: 12px; padding: 12px 16px; cursor: pointer; transition: all 0.15s; }
.recent-item:hover { border-color: var(--blue); box-shadow: var(--shadow); }
.recent-left { display: flex; align-items: center; gap: 10px; }
.recent-icon { font-size: 20px; color: var(--text3); }
.recent-title { font-size: 13px; font-weight: 500; }
.recent-date { font-size: 11px; color: var(--text3); margin-top: 2px; }
.recent-arrow { font-size: 16px; color: var(--text3); }
</style>
