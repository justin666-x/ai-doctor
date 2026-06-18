<template>
  <div class="settings-container">
    <el-card class="settings-card">
      <template #header>
        <span>账户设置</span>
      </template>

      <!-- Avatar upload -->
      <section class="avatar-section">
        <h3>上传头像</h3>
        <el-upload
          class="avatar-uploader"
          action=""
          :show-file-list="false"
          :before-upload="beforeAvatarUpload"
          :http-request="uploadRequest"
        >
          <img v-if="avatarUrl" :src="avatarUrl" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><User /></el-icon>
        </el-upload>
      </section>

      <el-divider />

      <!-- change password -->
      <h3>修改密码</h3>
      <el-form :model="pwdForm" :rules="rules" ref="formRef" label-width="120px" class="pwd-form">
        <el-form-item label="原密码" prop="old_pwd">
          <el-input v-model="pwdForm.old_pwd" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="new_pwd">
          <el-input v-model="pwdForm.new_pwd" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="re_pwd">
          <el-input v-model="pwdForm.re_pwd" type="password" placeholder="请再次输入新密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="isSubmitting" @click="submitPwd">保存</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>

      <el-divider />

      <!-- API Keys -->
      <section class="api-section">
        <h3>API 密钥设置</h3>
        <el-form :model="apiForm" label-width="120px">
          <el-form-item label="DeepSeek Key">
            <el-input v-model="apiForm.deepseekKey" placeholder="输入 DeepSeek API Key，留空则使用默认" show-password />
          </el-form-item>
          <el-form-item label="Tavily Key">
            <el-input v-model="apiForm.tavilyKey" placeholder="输入 Tavily API Key，留空则关闭联网搜索" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingApi" @click="saveApiKeys">保存 API 密钥</el-button>
          </el-form-item>
        </el-form>
      </section>

      <el-divider />
      <!-- delete account -->
      <section class="delete-section">
        <h3>注销账号</h3>
        <p class="danger-text">警告：此操作不可恢复，将删除账户及所有数据。</p>
        <el-button type="danger" @click="handleDelete">注销账号</el-button>
      </section>

    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { updatePassword, uploadAvatar, deleteAccount } from '@/api/user'
import { User } from '@element-plus/icons-vue'
import request from '@/utils/request'

const avatarUrl = ref('')
const apiForm = reactive({ deepseekKey: '', tavilyKey: '' })
const savingApi = ref(false)

const loadApiSettings = async () => {
  try {
    const res = await request.get('/api-settings')
    if (res.code === 0 && res.data) {
      apiForm.deepseekKey = res.data.deepseekKey || ''
      apiForm.tavilyKey = res.data.tavilyKey || ''
    }
  } catch (e) { console.error('加载API设置失败', e) }
}

const saveApiKeys = async () => {
  savingApi.value = true
  try {
    const res = await request.put('/api-settings', {
      deepseekKey: apiForm.deepseekKey || '',
      tavilyKey: apiForm.tavilyKey || ''
    })
    if (res.code === 0) {
      ElMessage.success('API 密钥已保存，重启后端后生效')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    savingApi.value = false
  }
}

onMounted(async () => {
  const stored = localStorage.getItem('avatar')
  if (stored) { avatarUrl.value = stored }
  loadApiSettings()
})

const beforeAvatarUpload = (file)=>{
  const isImg = ['image/jpeg','image/png','image/jpg','image/gif'].includes(file.type)
  const isLt2M = file.size / 1024 /1024 < 2
  if(!isImg){ElMessage.error('只能上传图片文件');return false}
  if(!isLt2M){ElMessage.error('图片大小不能超过2MB');return false}
  return true
}

const uploadRequest = async (options)=>{
  try{
    const res = await uploadAvatar(options.file)
    if(res.code===0){
      avatarUrl.value = res.data
      localStorage.setItem('avatar',res.data)
      ElMessage.success('头像上传成功')
    }
    options.onSuccess(res)
  }catch(e){
    ElMessage.error('上传失败')
    options.onError(e)
  }
}

const pwdForm = reactive({
  old_pwd: '',
  new_pwd: '',
  re_pwd: ''
})
const formRef = ref(null)
const isSubmitting = ref(false)

const validateConfirm = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入新密码'))
  } else if (value !== pwdForm.new_pwd) {
    callback(new Error('两次输入的新密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  old_pwd: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 5, max: 16, message: '密码长度应为5-16位', trigger: 'blur' }
  ],
  new_pwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { pattern: /^\S{5,16}$/, message: '密码必须是5-16位非空字符', trigger: 'blur' }
  ],
  re_pwd: [
    { validator: validateConfirm, trigger: 'blur' }
  ]
}

const submitPwd = () => {
  if (!formRef.value) return
  formRef.value.validate(async (valid) => {
    if (valid) {
      isSubmitting.value = true
      try {
        const res = await updatePassword(pwdForm)
        if (res.code === 0) {
          ElMessage.success('密码修改成功')
          resetForm()
        }
      } catch (error) {
        ElMessage.error(error.msg || '修改密码失败')
      } finally {
        isSubmitting.value = false
      }
    }
  })
}

const resetForm = () => {
  if (formRef.value) formRef.value.resetFields()
}

const handleDelete = ()=>{
  ElMessageBox.confirm('确定要注销账号？此操作不可恢复！','警告',{type:'warning'}).then(async()=>{
    try{
      await deleteAccount()
      ElMessage.success('账号已注销')
      localStorage.removeItem('userInfo')
      window.location.href = '/login'
    }catch(e){
      ElMessage.error('注销失败')
    }
  }).catch(()=>{})
}
</script>

<style lang="scss" scoped>
.settings-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}
.settings-card {
  width: 500px;
}
.pwd-form {
  margin-top: 20px;
}
.avatar-section{margin-bottom:20px;text-align:center}
.avatar{width:80px;height:80px;border-radius:50%;}
.avatar-uploader-icon{font-size:80px;color:#8c939d;}
.danger-text{color:#f56c6c;margin-bottom:10px;}
</style> 