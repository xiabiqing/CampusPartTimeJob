<template>
  <div class="login-page">
    <van-nav-bar title="登录" />
    
    <div class="login-container">
      <div class="logo-section">
        <h1 class="title">校园兼职系统</h1>
        <p class="subtitle">欢迎回来</p>
      </div>

      <!-- 登录类型切换 -->
      <div class="login-type-tabs">
        <van-tabs v-model:active="loginType" @change="onLoginTypeChange">
          <van-tab title="用户登录" name="user"></van-tab>
          <van-tab title="管理员登录" name="admin"></van-tab>
        </van-tabs>
      </div>

      <van-form @submit="onSubmit" @failed="onFailed">
        <van-cell-group inset>
          <van-field
            v-model="form.userAccount"
            name="userAccount"
            label="账号"
            placeholder="请输入账号"
            :rules="accountRules"
            clearable
          />
          <van-field
            v-model="form.userPassword"
            type="password"
            name="userPassword"
            label="密码"
            placeholder="请输入密码"
            :rules="passwordRules"
            clearable
          />
        </van-cell-group>

        <div class="submit-btn-wrapper">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
            :disabled="loading"
          >
            {{ loginType === 'admin' ? '管理员登录' : '登录' }}
          </van-button>
        </div>

        <div v-if="loginType === 'user'" class="link-section">
          <span class="link-text">还没有账号？</span>
          <span class="link-btn" @click="goToRegister">立即注册</span>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAdminStore } from '@/stores/admin'
import { login } from '@/api/user'
import { adminLogin } from '@/api/admin'
import { showSuccessToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const adminStore = useAdminStore()

const loginType = ref('user')
const form = reactive({
  userAccount: '',
  userPassword: ''
})

const loading = ref(false)

// 账号校验规则
const accountRules = [
  { required: true, message: '请输入账号' },
  {
    validator: (value) => {
      if (!value) return true
      if (value.length < 6 || value.length > 20) {
        return '账号长度为6-20位'
      }
      if (!/^[a-zA-Z0-9_.]+$/.test(value)) {
        return '账号仅支持字母、数字、下划线和点'
      }
      return true
    },
    message: '账号格式不正确'
  }
]

// 密码校验规则
const passwordRules = [
  { required: true, message: '请输入密码' },
  {
    validator: (value) => {
      if (!value) return true
      if (value.length < 8 || value.length > 20) {
        return '密码长度为8-20位'
      }
      if (!/^[a-zA-Z0-9_.]+$/.test(value)) {
        return '密码仅支持字母、数字、下划线和点'
      }
      return true
    },
    message: '密码格式不正确'
  }
]

// 登录类型切换
const onLoginTypeChange = () => {
  form.userAccount = ''
  form.userPassword = ''
}

// 表单提交
const onSubmit = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    if (loginType.value === 'admin') {
      // 管理员登录
      const res = await adminLogin({
        userAccount: form.userAccount,
        userPassword: form.userPassword
      })
      
      if (res.code === 200 && res.data) {
        adminStore.setAdminInfo(res.data)
        adminStore.setToken('session-token')
        showSuccessToast('管理员登录成功')
        setTimeout(() => {
          router.push('/admin-home')
        }, 500)
      }
    } else {
      // 用户登录
      const res = await login({
        userAccount: form.userAccount,
        userPassword: form.userPassword
      })
      
      if (res.code === 200 && res.data) {
        userStore.setUserInfo(res.data)
        userStore.setToken('session-token')
        showSuccessToast('登录成功')
        
        // 检查用户信息是否完整
        try {
          const { getCurrentUser } = await import('@/api/user')
          const userRes = await getCurrentUser()
          if (userRes && userRes.code === 200 && userRes.data) {
            userStore.setUserDetail(userRes.data)
            // 判断信息是否完整
            const role = userRes.data.role !== undefined && userRes.data.role !== null ? userRes.data.role : 0
            let isComplete = false
            
            if (role === 0 || !role) {
              // 学生角色必填字段检查
              isComplete = userRes.data.username && 
                          userRes.data.username.trim() !== '' &&
                          userRes.data.age !== null && 
                          userRes.data.age !== undefined &&
                          userRes.data.gender !== null && 
                          userRes.data.gender !== undefined &&
                          userRes.data.major && 
                          userRes.data.major.trim() !== '' &&
                          userRes.data.email && 
                          userRes.data.email.trim() !== ''
            } else if (role === 1) {
              // 商家角色必填字段检查
              isComplete = userRes.data.username && 
                          userRes.data.username.trim() !== '' &&
                          userRes.data.position && 
                          userRes.data.position.trim() !== '' &&
                          userRes.data.location && 
                          userRes.data.location.trim() !== '' &&
                          userRes.data.company && 
                          userRes.data.company.trim() !== ''
            }
            
            setTimeout(() => {
              router.push(isComplete ? '/home' : '/complete-info')
            }, 500)
          } else {
            setTimeout(() => {
              router.push('/complete-info')
            }, 500)
          }
        } catch (error) {
          console.error('获取用户信息失败', error)
          setTimeout(() => {
            router.push('/complete-info')
          }, 500)
        }
      }
    }
  } catch (error) {
    console.error('登录失败', error)
  } finally {
    loading.value = false
  }
}

// 表单校验失败
const onFailed = (errorInfo) => {
  console.log('表单校验失败', errorInfo)
}

// 跳转到注册页
const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  padding: 60px 20px 20px;
}

.logo-section {
  text-align: center;
  margin-bottom: 50px;
  color: #fff;
}

.title {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 10px;
}

.subtitle {
  font-size: 16px;
  opacity: 0.9;
}

.login-type-tabs {
  margin-bottom: 20px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 10px;
}

.submit-btn-wrapper {
  margin: 30px 0;
  padding: 0 16px;
}

.link-section {
  text-align: center;
  margin-top: 20px;
  color: #fff;
  font-size: 14px;
}

.link-text {
  opacity: 0.8;
}

.link-btn {
  color: #fff;
  font-weight: bold;
  margin-left: 5px;
  cursor: pointer;
  text-decoration: underline;
}
</style>

