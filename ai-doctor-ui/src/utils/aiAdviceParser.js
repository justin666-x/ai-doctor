/**
 * AI建议内容解析工具
 * 将AI生成的建议文本解析为结构化数组，支持多种格式
 */

/**
 * 解析AI建议内容为结构化数组
 * @param {string} content - AI建议文本内容
 * @returns {Array} 解析后的建议数组，每个元素包含id、text、completed等属性
 */
export function parseAiAdvice(content) {
  if (!content || typeof content !== 'string') {
    return []
  }

  // 清理内容，移除多余空格和换行
  const cleanContent = content.trim()
  
  // 尝试多种解析模式
  const patterns = [
    // 模式1：数字序号 + 内容 (如 "1. 多喝水")
    /^\s*(\d+)[.、]\s*(.+)$/gm,
    // 模式2：换行分割 (如 "多喝水\n每天锻炼")
    /^(.+)$/gm,
    // 模式3：破折号分割 (如 "- 多喝水")
    /^\s*[-－]\s*(.+)$/gm,
    // 模式4：星号分割 (如 "* 多喝水")
    /^\s*[*＊]\s*(.+)$/gm
  ]

  let suggestions = []
  
  // 尝试每种模式
  for (let i = 0; i < patterns.length; i++) {
    const pattern = patterns[i]
    const matches = cleanContent.match(pattern)
    
    if (matches && matches.length > 0) {
      // 根据模式类型处理匹配结果
      if (i === 0) {
        // 数字序号模式
        suggestions = matches.map((match, index) => {
          const [, number, text] = match.match(/^\s*(\d+)[.、]\s*(.+)$/)
          return {
            id: index + 1,
            originalNumber: parseInt(number),
            text: text.trim(),
            completed: false
          }
        })
      } else {
        // 其他模式
        suggestions = matches.map((match, index) => {
          let text = match
          if (i === 1) {
            // 换行分割模式，直接使用匹配内容
            text = match.trim()
          } else {
            // 破折号或星号模式，提取内容部分
            text = match.replace(/^\s*[-－*＊]\s*/, '').trim()
          }
          
          if (text && text.length > 0) {
            return {
              id: index + 1,
              originalNumber: null,
              text: text,
              completed: false
            }
          }
          return null
        }).filter(item => item !== null)
      }
      
      if (suggestions.length > 0) {
        break
      }
    }
  }

  if (suggestions.length === 0 && cleanContent.length > 0) {
    suggestions = [{
      id: 1,
      originalNumber: null,
      text: cleanContent,
      completed: false
    }]
  }

  return suggestions
}

/**
 * 将结构化建议数组转换回文本格式
 * @param {Array} suggestions - 建议数组
 * @param {string} format - 输出格式 ('numbered', 'dash', 'simple')
 * @returns {string} 格式化后的文本
 */
export function formatAiAdvice(suggestions, format = 'numbered') {
  if (!Array.isArray(suggestions) || suggestions.length === 0) {
    return ''
  }

  switch (format) {
    case 'numbered':
      return suggestions.map(item => `${item.id}. ${item.text}`).join('\n')
    case 'dash':
      return suggestions.map(item => `- ${item.text}`).join('\n')
    case 'simple':
      return suggestions.map(item => item.text).join('\n')
    default:
      return suggestions.map(item => `${item.id}. ${item.text}`).join('\n')
  }
}

/**
 * 获取建议完成统计
 * @param {Array} suggestions - 建议数组
 * @returns {Object} 包含总数、已完成数、完成率等统计信息
 */
export function getAdviceStats(suggestions) {
  if (!Array.isArray(suggestions)) {
    return { total: 0, completed: 0, percentage: 0 }
  }

  const total = suggestions.length
  const completed = suggestions.filter(item => item.completed).length
  const percentage = total > 0 ? Math.round((completed / total) * 100) : 0

  return { total, completed, percentage }
}

/**
 * 更新建议完成状态
 * @param {Array} suggestions - 建议数组
 * @param {number} id - 建议ID
 * @param {boolean} completed - 完成状态
 * @returns {Array} 更新后的建议数组
 */
export function updateAdviceStatus(suggestions, id, completed) {
  if (!Array.isArray(suggestions)) {
    return []
  }

  return suggestions.map(item => 
    item.id === id ? { ...item, completed } : item
  )
} 