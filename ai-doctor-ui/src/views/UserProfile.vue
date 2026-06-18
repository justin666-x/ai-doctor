<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <p>请完善您的个人信息，以便我们为您提供更精准的服务</p>
        </div>
      </template>
      <el-form 
        :model="userProfileData" 
        :rules="rules" 
        ref="formRef" 
        label-position="top"
        class="profile-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="userProfileData.username" disabled></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="nickname">
              <el-input v-model="userProfileData.nickname" placeholder="请输入您的真实姓名"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身高(cm)" prop="height">
              <el-input v-model="userProfileData.height" placeholder="请输入身高"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="体重(kg)" prop="weight">
              <el-input v-model="userProfileData.weight" placeholder="请输入体重"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="性别" prop="sex">
                    <el-radio-group v-model="userProfileData.sex">
                        <el-radio label="男">男</el-radio>
                        <el-radio label="女">女</el-radio>
                    </el-radio-group>
                </el-form-item>
            </el-col>
            <el-col :span="12">
                <el-form-item label="血型" prop="bloodType">
                    <el-select v-model="userProfileData.bloodType" placeholder="请选择血型" style="width: 100%;">
                        <el-option label="A型" value="A"></el-option>
                        <el-option label="B型" value="B"></el-option>
                        <el-option label="AB型" value="AB"></el-option>
                        <el-option label="O型" value="O"></el-option>
                        <el-option label="其他" value="其他"></el-option>
                    </el-select>
                </el-form-item>
            </el-col>
        </el-row>

        <el-row :gutter="20">
            <el-col :span="12">
                <el-form-item label="年龄" prop="age">
                    <el-input v-model.number="userProfileData.age" placeholder="请输入年龄"></el-input>
                </el-form-item>
            </el-col>
             <el-col :span="12">
                <el-form-item label="身份证号" prop="idCardNumber">
                    <el-input v-model="userProfileData.idCardNumber" placeholder="请输入身份证号"></el-input>
                </el-form-item>
            </el-col>
        </el-row>
        
        <el-form-item label="邮箱" prop="email">
            <el-input v-model="userProfileData.email" placeholder="请输入邮箱"></el-input>
        </el-form-item>
        
        <el-form-item label="联系电话" prop="contactPhone">
            <el-input v-model="userProfileData.contactPhone" placeholder="请输入联系电话"></el-input>
        </el-form-item>
        
        <el-form-item label="紧急联系人" prop="emergencyContact">
            <el-input v-model="userProfileData.emergencyContact" placeholder="请输入紧急联系人"></el-input>
        </el-form-item>
        
        <el-form-item class="form-buttons">
            <el-button type="primary" @click="saveUserProfile" :loading="isSaving">保存信息</el-button>
            <el-button @click="resetForm" v-if="!isFirstLogin">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getUserProfile, updateUserProfile } from '@/api/user'

const router = useRouter()
const formRef = ref(null)
const isFirstLogin = ref(false)
const isSaving = ref(false)

const userProfileData = reactive({
  username: '',
  height: '',
  weight: '',
  sex: '',
  bloodType: '',
  age: null,
  idCardNumber: '',
  email: '',
  contactPhone: '',
  emergencyContact: '',
  nickname: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  height: [{ required: true, message: '请输入身高', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入体重', trigger: 'blur' }],
  sex: [{ required: true, message: '请选择性别', trigger: 'change' }],
  bloodType: [{ required: true, message: '请选择血型', trigger: 'change' }],
  age: [
    { required: true, message: '请输入年龄', trigger: 'blur' },
    { type: 'number', message: '年龄必须为数字', trigger: 'blur' }
  ],
  idCardNumber: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: ['blur', 'change'] }
  ],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  emergencyContact: [{ required: true, message: '请输入紧急联系人', trigger: 'blur' }]
}

onMounted(async () => {
  const userInfo = localStorage.getItem('userInfo')
  if (userInfo) {
    const { username } = JSON.parse(userInfo)
    userProfileData.username = username
  }
  
  const queryParams = new URLSearchParams(window.location.search)
  isFirstLogin.value = queryParams.get('first') === 'true'
  
  try {
    const result = await getUserProfile()
    if (result && result.code === 0 && result.data) {
      Object.assign(userProfileData, result.data)
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败，请检查网络连接')
    console.error('获取用户信息失败:', error)
  }
})

const saveUserProfile = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (valid) {
      isSaving.value = true
      try {
        const result = await updateUserProfile(userProfileData)
        if (result && result.code === 0) {
          ElMessage.success('个人信息保存成功')
          if (isFirstLogin.value) {
            router.push('/main')
          }
        } else {
          ElMessage.error(result?.msg || '保存失败')
        }
      } catch (error) {
        ElMessage.error('保存失败，请稍后重试')
        console.error('保存用户信息失败:', error)
      } finally {
        isSaving.value = false
      }
    } else {
      ElMessage.warning('请检查并完成所有必填信息')
      return false
    }
  })
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}
</script>

<style lang="scss" scoped>
.user-profile-container {
  padding: 24px;
  background-color: #f0f2f5;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
}

.profile-card {
  max-width: 800px;
  width: 100%;
}

.card-header {
  span {
    font-size: 20px;
    font-weight: 500;
  }
  p {
    font-size: 14px;
    color: #909399;
    margin-top: 8px;
    margin-bottom: 0;
  }
}

.profile-form {
  margin-top: 20px;
}

.form-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 