<template>
  <div class="chat-root">
    <div class="chat-body" ref="mc">
      <div class="chat-topbar">
        <div class="topbar-left">
          <span class="material-symbols-outlined topbar-icon">smart_toy</span>
          <span class="topbar-title">AI 智能问诊</span>
        </div>
        <button class="topbar-btn" @click="openHistoryDrawer" title="问诊历史">
          <span class="material-symbols-outlined">history</span>
          <span>历史记录</span>
        </button>
      </div>
      <div v-if="messages.length <= 1" class="welcome">
        <div class="welcome-icon"><span class="material-symbols-outlined">health_and_safety</span></div>
        <h3>AI 智能问诊</h3>
        <p>请描述您的症状或健康问题，或点击下方快速开始</p>
        <div class="symptom-cards">
          <button v-for="s in symptomShortcuts" :key="s.label" class="symptom-card" @click="sendQuick(s.prompt)">
            <span class="symptom-emoji">{{ s.icon }}</span>
            <span class="symptom-label">{{ s.label }}</span>
          </button>
        </div>
        <div class="quick-chips">
          <button v-for="p in quickPrompts" :key="p" @click="sendQuick(p)">{{ p }}</button>
        </div>
      </div>
      <div v-for="(msg, i) in messages.slice(1)" :key="i" :class="['msg', msg.role === 'user' ? 'msg-you' : 'msg-ai']">
        <div class="bubble" v-html="msg.content || '<i>思考中...</i>'"></div>
      </div>
      <div v-if="isLoading && !aiMsgAdded" class="msg msg-ai">
        <div class="bubble typing"><span></span><span></span><span></span></div>
      </div>
    </div>
    <div class="chat-foot">
      <div class="input-box">
        <textarea v-model="inputMsg" placeholder="描述您的症状或健康问题..." :disabled="isLoading" rows="1" @keyup.enter.exact.prevent="send" @input="resize" ref="ta"></textarea>
        <div class="input-bar">
          <button class="chip" @click="inputMsg='我的症状是: '">描述症状</button>
          <button class="btn-send" :disabled="isLoading || !inputMsg.trim()" @click="send">
            <span class="material-symbols-outlined">send</span>
          </button>
        </div>
      </div>
    </div>
  </div>
  <el-drawer v-model="drawerVisible" title="问诊历史" direction="rtl" size="350px">
    <div v-for="item in historyList" :key="item.id" class="hist-row">
      <div class="hist-left" @click="openHistory(item)">
        <div class="hist-title">{{ item.title }}</div>
        <div class="hist-time">{{ fmtTime(item.updateTime) }}</div>
      </div>
      <div class="hist-actions">
        <button class="hist-btn hist-btn-open" @click.stop="openHistory(item)"><span class="material-symbols-outlined">visibility</span></button>
        <button class="hist-btn hist-btn-del" @click.stop="removeHistory(item)"><span class="material-symbols-outlined">delete</span></button>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { addSymptom } from '@/api/symptom'
import { addHistory, getHistory, listHistory, deleteHistory, updateHistory } from '@/api/history'
import { useRoute, useRouter } from 'vue-router'

const props = defineProps({ sessionId: { default: '' } })
const msg = (role, content) => ({ role, content, timestamp: new Date() })
const messages = ref([msg('doctor', '<p class="md-p">您好，我是AI Doctor。请描述您的症状，我会为您提供专业的分析建议。</p>')])
const inputMsg = ref('')
const isLoading = ref(false)
const aiMsgAdded = ref(false)
const mc = ref(null)
const ta = ref(null)
const needRecord = ref(false)
const drawerVisible = ref(false)
const historyList = ref([])
const currentSessionId = ref(props.sessionId)
const route = useRoute()
const router = useRouter()

const symptomShortcuts = [
  { label: '头痛', icon: '🤕', prompt: '我的症状是: 头痛' },
  { label: '感冒发烧', icon: '🤒', prompt: '我的症状是: 感冒发烧' },
  { label: '肠胃不适', icon: '🤢', prompt: '我的症状是: 肠胃不适' },
  { label: '失眠', icon: '😴', prompt: '我的症状是: 失眠' },
  { label: '皮肤问题', icon: '🌟', prompt: '我的症状是: 皮肤问题' },
  { label: '体检解读', icon: '📋', prompt: '帮我看看体检报告' },
]
const quickPrompts = ['我的症状是: 头痛、乏力', '制定健康饮食计划']

const resize = () => { const e = ta.value; if (e) { e.style.height = 'auto'; e.style.height = Math.min(e.scrollHeight, 120) + 'px' } }
const sendQuick = (p) => { inputMsg.value = p; send() }

const loadHistoryList = async () => { try { const r = await listHistory(); if (r.code === 0) historyList.value = r.data } catch {} }
watch(drawerVisible, v => { if (v) loadHistoryList() })

const loadHistory = async (sid) => { if (!sid) return; try { const r = await getHistory(sid); if (r.code === 0 && r.data) { const a = JSON.parse(r.data.messages); if (Array.isArray(a) && a.length) { messages.value = a.map(m => m.role === 'doctor' ? { ...m, content: fmt(m.content) } : m) } } } catch {} }

const fmtTime = ts => { const d = new Date(ts); const p = n => String(n).padStart(2, '0'); return `${d.getFullYear()}-${p(d.getMonth()+1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}` }

const fmt = text => {
  if (!text) return ''
  let html = text
  // 转义 HTML
  html = html.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  // 思考块（AI 内心独白）
  html = html.replace(/&lt;think&gt;([\s\S]*?)&lt;\/think&gt;/g, '<div class="think">$1</div>')
  // 代码块
  html = html.replace(/```(\w*)\n?([\s\S]*?)```/g, '<pre class="md-code"><code>$2</code></pre>')
  // 行内代码
  html = html.replace(/`([^`]+)`/g, '<code class="md-inline">$1</code>')
  // 分隔线
  html = html.replace(/^---$/gm, '<hr class="md-hr">')
  // 标题 (兼容有无空格：###标题 或 ### 标题)
  html = html.replace(/^####\s*(.+)$/gm, '<h4 class="md-h4">$1</h4>')
  html = html.replace(/^###\s*(.+)$/gm, '<h3 class="md-h3">$1</h3>')
  html = html.replace(/^##\s*(.+)$/gm, '<h2 class="md-h2">$1</h2>')
  // 加粗 / 斜体
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  // 引用块
  html = html.replace(/^&gt;\s?(.+)$/gm, '<blockquote class="md-quote">$1</blockquote>')
  html = html.replace(/((?:<blockquote class="md-quote">.*<\/blockquote>\n?)+)/g, '<div class="md-quote-group">$1</div>')
  // 无序列表
  html = html.replace(/^-\s*(.+)$/gm, '<li class="md-li">$1</li>')
  html = html.replace(/((?:<li class="md-li">.*<\/li>\n?)+)/g, '<ul class="md-ul">$1</ul>')
  // 有序列表
  html = html.replace(/^(\d+)\.\s*(.+)$/gm, '<li class="md-ol-li">$2</li>')
  html = html.replace(/((?:<li class="md-ol-li">.*<\/li>\n?)+)/g, '<ol class="md-ol">$1</ol>')
  // 换行
  html = html.replace(/\n\n/g, '</p><p class="md-p">')
  html = html.replace(/\n/g, '<br>')
  // 包裹段落
  if (!/^<(h[2-4]|ul|ol|pre|div|blockquote|hr|p)/.test(html)) {
    html = '<p class="md-p">' + html + '</p>'
  }
  return html
}

const scroll = async () => { await nextTick(); if (mc.value) mc.value.scrollTop = mc.value.scrollHeight }

const send = async () => {
  if (!inputMsg.value.trim() || isLoading.value) return
  const um = msg('user', inputMsg.value)
  messages.value.push(um)
  needRecord.value = inputMsg.value.includes('我的症状是:')
  inputMsg.value = ''
  isLoading.value = true
  aiMsgAdded.value = false
  await scroll()
  try {
    let url = `/api/chat/with-sse?message=${encodeURIComponent(um.content)}`
    const u = localStorage.getItem('userInfo'); if (u) { try { const { token } = JSON.parse(u); if (token) url += `&token=${encodeURIComponent(token)}` } catch {} }
    if (currentSessionId.value) url += `&sessionId=${currentSessionId.value}`
    const es = new EventSource(url)
    let full = ''
    es.addEventListener('message', e => {
      full = e.data.startsWith(full) ? e.data : full + e.data
      if (!aiMsgAdded.value) { messages.value.push(msg('doctor', '')); aiMsgAdded.value = true }
      const am = messages.value[messages.value.length - 1]
      if (am && am.role === 'doctor') am.content = fmt(full)
      scroll()
    })
    es.addEventListener('done', async () => {
      es.close()
      isLoading.value = false; aiMsgAdded.value = false
      const am = messages.value[messages.value.length - 1]
      if (am && am.role === 'doctor') { am.rawContent = full; am.content = fmt(full) }
      if (needRecord.value) {
        try { await ElMessageBox.confirm('保存到症状记录？', '保存记录', { confirmButtonText: '保存', cancelButtonText: '取消', type: 'info' }); await addSymptom({ symptomText: um.content, aiAnalysis: full.replace(/<think>[\s\S]*?<\/think>/g, '').trim() }); ElMessage.success('已保存') } catch (e) { if (e !== 'cancel') ElMessage.error('保存失败') }
        needRecord.value = false
      }
      const sm = messages.value.map(m => m.role === 'doctor' && m.rawContent ? { ...m, content: m.rawContent } : m)
      const p = { title: sm.find(m => m.role === 'user')?.content?.slice(0, 20) || '新会话', messages: JSON.stringify(sm) }
      if (!currentSessionId.value) { try { const r = await addHistory(p); if (r.code === 0) currentSessionId.value = r.data } catch {} } else { try { await updateHistory(currentSessionId.value, p) } catch {} }
      scroll()
    })
    es.onerror = () => { if (es.readyState === EventSource.CLOSED) return; es.close(); isLoading.value = false; aiMsgAdded.value = false; scroll() }
  } catch (e) { isLoading.value = false; aiMsgAdded.value = false; scroll() }
}

onMounted(() => { if (currentSessionId.value) loadHistory(currentSessionId.value); scroll() })
watch(() => props.sessionId, v => { currentSessionId.value = v; messages.value = [msg('doctor', '<p class="md-p">您好，我是AI Doctor。</p>')]; loadHistory(v) })
const openHistory = async (item) => { drawerVisible.value = false; await loadHistory(item.id); currentSessionId.value = item.id; router.replace({ query: { ...route.query, sessionId: item.id } }) }
const removeHistory = (item) => { ElMessageBox.confirm('确认删除？', '警告', { type: 'warning' }).then(async () => { try { await deleteHistory(item.id); ElMessage.success('已删除'); loadHistoryList(); if (currentSessionId.value == item.id) { currentSessionId.value = ''; messages.value = [msg('doctor', '<p class="md-p">您好，我是AI Doctor。</p>')]; router.replace({ query: { ...route.query, sessionId: '' } }) } } catch { ElMessage.error('删除失败') } }).catch(() => {}) }
const openHistoryDrawer = () => { drawerVisible.value = true }
defineExpose({ openHistoryDrawer })
</script>

<style lang="scss" scoped>
.chat-root { display: flex; flex-direction: column; height: calc(100vh - 108px); }
.chat-body { flex: 1; overflow-y: auto; padding: 0 24px 16px; }
.chat-topbar { display: flex; align-items: center; justify-content: space-between; padding: 16px 0 12px; border-bottom: 1px solid var(--border); margin-bottom: 16px; }
.topbar-left { display: flex; align-items: center; gap: 8px; }
.topbar-icon { font-size: 22px; color: var(--blue); }
.topbar-title { font-size: 15px; font-weight: 600; color: var(--text); }
.topbar-btn { display: flex; align-items: center; gap: 6px; padding: 6px 14px; border: 1px solid var(--border); border-radius: var(--radius); background: var(--white); color: var(--text2); cursor: pointer; font-size: 13px; font-family: var(--font); transition: all 0.15s; }
.topbar-btn:hover { border-color: var(--blue); color: var(--blue); background: var(--blue-light); }
.topbar-btn .material-symbols-outlined { font-size: 18px; }
.welcome { text-align: center; padding: 80px 20px 40px; }
.welcome-icon { width: 56px; height: 56px; margin: 0 auto 16px; background: var(--blue); border-radius: 16px; display: flex; align-items: center; justify-content: center; }
.welcome-icon .material-symbols-outlined { font-size: 28px; color: #fff; }
.welcome h3 { font-size: 20px; font-weight: 700; color: var(--text); margin-bottom: 4px; }
.welcome p { font-size: 14px; color: var(--text2); margin-bottom: 24px; }
.quick-chips { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.quick-chips button { padding: 8px 18px; border: 1px solid var(--border); border-radius: 9999px; background: var(--white); font-size: 13px; color: var(--text2); cursor: pointer; transition: all 0.15s; font-family: var(--font); }
.quick-chips button:hover { border-color: var(--blue); color: var(--blue); background: var(--blue-light); }
.symptom-cards { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; max-width: 420px; margin: 0 auto 20px; }
.symptom-card { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 14px 10px; border: 1px solid var(--border); border-radius: var(--radius-lg); background: var(--white); cursor: pointer; transition: all 0.2s; font-family: var(--font); }
.symptom-card:hover { border-color: var(--blue); background: var(--blue-light); transform: translateY(-2px); box-shadow: 0 4px 12px rgba(26,115,232,0.12); }
.symptom-emoji { font-size: 28px; line-height: 1; }
.symptom-label { font-size: 13px; color: var(--text); font-weight: 500; white-space: nowrap; }
.msg { margin-bottom: 16px; display: flex; }
.msg-you { justify-content: flex-end; }
.bubble { max-width: 72%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.65; word-break: break-word; }
.msg-you .bubble { background: var(--blue); color: #fff; border-bottom-right-radius: 4px; }
.msg-ai .bubble { background: var(--white); color: var(--text); border: 1px solid var(--border); border-bottom-left-radius: 4px; max-width: 85%; padding: 14px 18px; font-size: 14px; line-height: 1.6; }
.typing { display: flex; gap: 4px; padding: 10px 14px; }
.typing span { width: 6px; height: 6px; border-radius: 50%; background: var(--text3); animation: b 1.2s infinite; }
.typing span:nth-child(2) { animation-delay: 0.2s; }
.typing span:nth-child(3) { animation-delay: 0.4s; }
.chat-foot { padding: 0 24px 16px; }
.input-box { background: var(--white); border: 1px solid var(--border); border-radius: 12px; padding: 8px 8px 8px 16px; box-shadow: var(--shadow); }
.input-box:focus-within { border-color: var(--blue); box-shadow: 0 0 0 3px rgba(26,115,232,0.1); }
textarea { width: 100%; border: none; outline: none; resize: none; font-family: var(--font); font-size: 14px; line-height: 1.6; color: var(--text); background: transparent; }
textarea::placeholder { color: var(--text3); }
.input-bar { display: flex; align-items: center; justify-content: space-between; margin-top: 6px; }
.chip { padding: 4px 14px; border: 1px solid var(--border); border-radius: 9999px; background: var(--bg); font-size: 12px; color: var(--text2); cursor: pointer; transition: all 0.15s; font-family: var(--font); }
.chip:hover { border-color: var(--blue); color: var(--blue); background: var(--blue-light); }
.btn-send { width: 34px; height: 34px; border: none; border-radius: 8px; background: var(--blue); color: #fff; cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.15s; }
.btn-send:hover:not(:disabled) { background: var(--blue-dark); transform: scale(1.05); }
.btn-send:disabled { opacity: 0.3; cursor: not-allowed; }
.btn-send .material-symbols-outlined { font-size: 18px; }
.hist-row { display: flex; align-items: center; justify-content: space-between; padding: 12px; border-bottom: 1px solid var(--border); }
.hist-left { flex: 1; cursor: pointer; min-width: 0; }
.hist-title { font-size: 14px; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.hist-time { font-size: 12px; color: var(--text2); margin-top: 2px; }
.hist-actions { display: flex; gap: 4px; margin-left: 8px; flex-shrink: 0; }
.hist-btn { width: 32px; height: 32px; border: 1px solid var(--border); border-radius: 8px; background: var(--white); cursor: pointer; display: flex; align-items: center; justify-content: center; transition: all 0.15s; }
.hist-btn:hover { border-color: var(--blue); background: var(--blue-light); color: var(--blue); }
.hist-btn-del:hover { border-color: var(--red); background: var(--red-light); color: var(--red); }
.hist-btn .material-symbols-outlined { font-size: 16px; }
@keyframes b { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(-4px); opacity: 0.5; } }
</style>
<style>.think { display: block; color: var(--text2); font-style: italic; font-size: 12px; padding: 4px 0 4px 10px; border-left: 2px solid var(--border); margin: 4px 0; }

/* Markdown 美化样式 */
.md-p { margin: 0 0 4px; line-height: 1.65; }
.md-h2 { font-size: 15px; font-weight: 700; color: #1e3a5f; margin: 16px 0 10px; padding: 8px 12px; background: #f0f5ff; border-left: 3px solid #3b82f6; border-radius: 0 8px 8px 0; }
.md-h3 { font-size: 14px; font-weight: 700; color: #2563eb; margin: 14px 0 8px; padding: 6px 0 6px 12px; border-left: 3px solid #93c5fd; }
.md-h4 { font-size: 13px; font-weight: 600; color: #374151; margin: 10px 0 6px; }
.md-ul { margin: 6px 0 10px; padding-left: 16px; list-style: none; }
.md-li { position: relative; padding: 2px 0 2px 18px; line-height: 1.6; }
.md-li::before { content: '•'; position: absolute; left: 2px; color: #2563eb; font-weight: 700; font-size: 13px; }
.md-ol { margin: 6px 0 10px; padding-left: 18px; list-style: none; counter-reset: md-ol; }
.md-ol-li { position: relative; padding: 2px 0 2px 22px; line-height: 1.6; counter-increment: md-ol; }
.md-ol-li::before { content: counter(md-ol) '.'; position: absolute; left: 2px; color: #2563eb; font-weight: 700; font-size: 12px; }
.md-hr { border: none; border-top: 1px solid var(--border); margin: 12px 0; }
.md-quote-group { margin: 10px 0; }
.md-quote { margin: 6px 0; padding: 10px 14px; border-left: 3px solid #818cf8; background: #eef2ff; border-radius: 0 8px 8px 0; font-size: 13px; color: #4338ca; line-height: 1.7; }
strong { color: #1e3a5f; font-weight: 700; }
em { color: #6b7280; }
.md-code { margin: 8px 0; padding: 12px 16px; background: #1e293b; color: #e2e8f0; border-radius: 8px; font-size: 12px; line-height: 1.6; overflow-x: auto; white-space: pre-wrap; }
.md-inline { padding: 2px 6px; background: #f1f5f9; border-radius: 4px; font-size: 12px; color: #334155; font-family: 'Consolas', 'Monaco', monospace; }

/* 警告与紧急提示 */
.bubble .md-warn { display: flex; gap: 8px; margin: 10px 0; padding: 10px 14px; background: #fffbeb; border: 1px solid #fde68a; border-radius: 10px; }
.bubble .md-warn-icon { font-size: 16px; flex-shrink: 0; margin-top: 1px; }
.bubble .md-warn-body { font-size: 13px; line-height: 1.65; color: #92400e; }
.bubble .md-emergency { display: flex; gap: 8px; margin: 10px 0; padding: 10px 14px; background: #fef2f2; border: 1px solid #fecaca; border-radius: 10px; }
.bubble .md-emergency-icon { font-size: 16px; flex-shrink: 0; margin-top: 1px; }
.bubble .md-emergency-body { font-size: 13px; line-height: 1.65; color: #991b1b; }</style>
