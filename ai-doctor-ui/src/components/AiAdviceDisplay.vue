<template>
  <div class="ai-advice-display">
    <!-- 模式选择器 -->
    <div v-if="showModeSelector" class="mode-selector">
      <el-radio-group v-model="currentMode" size="small">
        <el-radio-button label="text">文本模式</el-radio-button>
        <el-radio-button label="list">清单模式</el-radio-button>
        <el-radio-button label="table">表格模式</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 统计信息 -->
    <div v-if="currentMode !== 'text' && stats.total > 0" class="stats-bar">
      <el-progress 
        :percentage="stats.percentage" 
        :stroke-width="8"
        :show-text="false"
        color="#67C23A"
      />
      <span class="stats-text">
        已完成 {{ stats.completed }}/{{ stats.total }} ({{ stats.percentage }}%)
      </span>
    </div>

    <!-- 文本模式 -->
    <div v-if="currentMode === 'text'" class="text-mode">
      <el-input
        v-model="displayContent"
        type="textarea"
        :rows="Math.min(6, Math.max(3, suggestions.length + 1))"
        readonly
        class="advice-textarea"
      />
    </div>

    <!-- 清单模式 -->
    <div v-else-if="currentMode === 'list'" class="list-mode">
      <div v-if="suggestions.length === 0" class="empty-state">
        <el-empty description="暂无AI建议内容" />
      </div>
      <div v-else class="suggestion-list">
        <div
          v-for="suggestion in suggestions"
          :key="suggestion.id"
          class="suggestion-item"
          :class="{ 'completed': suggestion.completed }"
        >
          <el-checkbox
            v-model="suggestion.completed"
            :disabled="!editable"
            @change="handleStatusChange(suggestion.id, $event)"
          />
          <span class="suggestion-text">{{ suggestion.text }}</span>
        </div>
      </div>
    </div>

    <!-- 表格模式 -->
    <div v-else-if="currentMode === 'table'" class="table-mode">
      <div v-if="suggestions.length === 0" class="empty-state">
        <el-empty description="暂无AI建议内容" />
      </div>
      <el-table
        v-else
        :data="suggestions"
        style="width: 100%"
        size="small"
        stripe
      >
        <el-table-column prop="id" label="序号" width="60" align="center" />
        <el-table-column prop="text" label="建议内容" min-width="200" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.completed ? 'success' : 'info'"
              size="small"
            >
              {{ scope.row.completed ? '已完成' : '待完成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button
              v-if="editable"
              :type="scope.row.completed ? 'warning' : 'success'"
              size="small"
              @click="toggleStatus(scope.row.id)"
            >
              {{ scope.row.completed ? '取消完成' : '标记完成' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { parseAiAdvice, formatAiAdvice, getAdviceStats, updateAdviceStatus } from '@/utils/aiAdviceParser'

// Props
const props = defineProps({
  content: {
    type: String,
    default: ''
  },
  mode: {
    type: String,
    default: 'text',
    validator: (value) => ['text', 'list', 'table'].includes(value)
  },
  editable: {
    type: Boolean,
    default: false
  },
  showModeSelector: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:content', 'status-change'])

const currentMode = ref(props.mode)
const suggestions = ref([])

const parseContent = (content) => {
  suggestions.value = parseAiAdvice(content)
}

const displayContent = computed({
  get: () => formatAiAdvice(suggestions.value, 'numbered'),
  set: (value) => {
    emit('update:content', value)
  }
})

const stats = computed(() => getAdviceStats(suggestions.value))

watch(() => props.content, (newContent) => {
  parseContent(newContent)
}, { immediate: true })

watch(currentMode, (newMode) => {
  emit('update:mode', newMode)
})

const handleStatusChange = (id, completed) => {
  suggestions.value = updateAdviceStatus(suggestions.value, id, completed)
  emit('status-change', { id, completed, suggestions: suggestions.value })
  saveToLocalStorage()
  ElMessage.success(completed ? '已标记为完成' : '已取消完成状态')
}

const toggleStatus = (id) => {
  const suggestion = suggestions.value.find(item => item.id === id)
  if (suggestion) {
    handleStatusChange(id, !suggestion.completed)
  }
}

const saveToLocalStorage = () => {
  try {
    const key = `ai_advice_${props.content?.substring(0, 20) || 'default'}`
    localStorage.setItem(key, JSON.stringify(suggestions.value))
  } catch (error) {
    console.warn('保存AI建议状态失败:', error)
  }
}

const loadFromLocalStorage = () => {
  try {
    const key = `ai_advice_${props.content?.substring(0, 20) || 'default'}`
    const saved = localStorage.getItem(key)
    if (saved) {
      const savedSuggestions = JSON.parse(saved)
      const currentSuggestions = parseAiAdvice(props.content)
      suggestions.value = currentSuggestions.map(current => {
        const savedItem = savedSuggestions.find(s => s.id === current.id)
        return savedItem ? { ...current, completed: savedItem.completed } : current
      })
    }
  } catch (error) {
    console.warn('加载AI建议状态失败:', error)
  }
}

onMounted(() => {
  loadFromLocalStorage()
})
</script>

<style scoped>
.ai-advice-display {
  width: 100%;
}

.mode-selector {
  margin-bottom: 16px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.stats-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px;
  background: #f0f9ff;
  border-radius: 6px;
  border: 1px solid #bae6fd;
}

.stats-text {
  font-size: 14px;
  color: #0369a1;
  font-weight: 500;
}

.text-mode .advice-textarea {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.list-mode .suggestion-list {
  max-height: 400px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.suggestion-item:hover {
  border-color: #67C23A;
  box-shadow: 0 2px 8px rgba(103, 194, 58, 0.1);
}

.suggestion-item.completed {
  background: #f0f9ff;
  border-color: #67C23A;
}

.suggestion-item.completed .suggestion-text {
  text-decoration: line-through;
  color: #6b7280;
}

.suggestion-text {
  flex: 1;
  line-height: 1.6;
  word-break: break-word;
}

.table-mode {
  margin-top: 8px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

@media (max-width: 768px) {
  .mode-selector {
    padding: 8px;
  }
  
  .stats-bar {
    padding: 8px;
    flex-direction: column;
    gap: 8px;
  }
  
  .suggestion-item {
    padding: 8px;
    gap: 8px;
  }
}
</style> 