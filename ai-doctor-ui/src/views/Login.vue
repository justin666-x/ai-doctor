<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useRegisterService, useLoginService, checkUserProfileComplete } from '@/api/user.js'

const isRegister = ref(false)
const loading = ref(false)
const loginData = reactive({ username: '', password: '' })
const registerData = reactive({ username: '', password: '', rePassword: '' })
const lf = ref(null); const rf = ref(null)
const router = useRouter()

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 3, max: 10, message: '3-10个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 5, max: 16, message: '5-16个字符', trigger: 'blur' }],
  rePassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: (_, v, cb) => v !== registerData.password ? cb(new Error('两次不一致')) : cb(), trigger: 'blur' }]
}

const doLogin = async () => { if (!lf.value) return; await lf.value.validate(async ok => { if (!ok) return; loading.value = true; try { const r = await useLoginService(loginData); if (r?.code === 0) { localStorage.setItem('userInfo', JSON.stringify({ username: loginData.username, token: r.data })); try { const p = await checkUserProfileComplete(); router.push(p?.data === true ? '/main' : '/profile?first=true') } catch { router.push('/profile?first=true') } ElMessage.success('登录成功') } } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { loading.value = false } }) }
const doRegister = async () => { if (!rf.value) return; await rf.value.validate(async ok => { if (!ok) return; loading.value = true; try { const r = await useRegisterService(registerData); if (r?.code === 0) { ElMessage.success('注册成功，请登录'); isRegister.value = false } } catch (e) { if (e?.message) ElMessage.error(e.message) } finally { loading.value = false } }) }
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="logo-icon"><span class="material-symbols-outlined">health_and_safety</span></div>
      <h1>AI Doctor</h1>
      <p class="sub">智能健康助手</p>

      <template v-if="isRegister">
        <el-form ref="rf" :model="registerData" :rules="rules" class="form">
          <el-form-item prop="username"><el-input v-model="registerData.username" placeholder="用户名" size="large" /></el-form-item>
          <el-form-item prop="password"><el-input v-model="registerData.password" type="password" placeholder="密码" size="large" /></el-form-item>
          <el-form-item prop="rePassword"><el-input v-model="registerData.rePassword" type="password" placeholder="确认密码" size="large" /></el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doRegister">注册</el-button>
        </el-form>
      </template>
      <template v-else>
        <el-form ref="lf" :model="loginData" :rules="rules" class="form">
          <el-form-item prop="username"><el-input v-model="loginData.username" placeholder="用户名" size="large" /></el-form-item>
          <el-form-item prop="password"><el-input v-model="loginData.password" type="password" placeholder="密码" size="large" show-password @keyup.enter="doLogin" /></el-form-item>
          <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="doLogin">登录</el-button>
        </el-form>
      </template>
      <p class="switch"><a @click="isRegister = !isRegister">{{ isRegister ? '已有账户？登录' : '没有账户？注册' }}</a></p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #e8f4fd 0%, #f0f2f5 100%); }
.login-card { width: 400px; background: var(--white); border-radius: var(--radius-lg); padding: 44px 40px 36px; box-shadow: var(--shadow-md); }
.logo-icon { width: 52px; height: 52px; margin: 0 auto 16px; background: var(--blue); border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.logo-icon .material-symbols-outlined { font-size: 28px; color: #fff; }
h1 { font-size: 24px; font-weight: 700; color: var(--text); text-align: center; }
.sub { font-size: 14px; color: var(--text2); text-align: center; margin: 4px 0 28px; }
.form { display: flex; flex-direction: column; gap: 2px; }
.submit-btn { width: 100%; height: 44px; font-size: 15px; margin-top: 12px; border-radius: var(--radius) !important; }
.switch { text-align: center; margin-top: 20px; }
.switch a { font-size: 13px; color: var(--blue); font-weight: 500; cursor: pointer; &:hover { text-decoration: underline; } }
</style>
