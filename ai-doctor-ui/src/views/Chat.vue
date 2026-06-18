<template>
  <ChatDoctor ref="chatRef" :session-id="sessionId" />
</template>
<script setup>
import ChatDoctor from '@/components/ChatDoctor.vue'
import { useRoute } from 'vue-router'
import { ref } from 'vue'
const route = useRoute()
const sessionId = route.query.sessionId || ''
const chatRef = ref(null)
</script>
