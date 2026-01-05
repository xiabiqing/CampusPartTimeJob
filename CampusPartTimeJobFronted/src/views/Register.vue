<template>
  <div class="register-page">
    <van-nav-bar title="注册" />
    
    <div class="register-container">
      <div class="logo-section">
        <h1 class="title">校园兼职系统</h1>
        <p class="subtitle">创建新账号</p>
      </div>

      <van-form @submit="onSubmit" @failed="onFailed">
        <van-cell-group inset>
          <van-field
            v-model="form.userAccount"
            name="userAccount"
            label="账号"
            placeholder="请输入账号（6-20位）"
            :rules="accountRules"
            clearable
          />
          <van-field
            v-model="form.userPassword"
            type="password"
            name="userPassword"
            label="密码"
            placeholder="请输入密码（8-20位）"
            :rules="passwordRules"
            clearable
          />
          <van-field
            v-model="form.checkPassword"
            type="password"
            name="checkPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="checkPasswordRules"
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
            注册
          </van-button>
        </div>

        <div class="link-section">
          <span class="link-text">已有账号？</span>
          <span class="link-btn" @click="goToLogin">立即登录</span>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/user'
import { showSuccessToast, showFailToast } from 'vant'

const router = useRouter()

const form = reactive({
  userAccount: '',
  userPassword: '',
  checkPassword: ''
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

// 确认密码校验规则
const checkPasswordRules = [
  { required: true, message: '请再次输入密码' },
  {
    validator: (value) => {
      if (!value) return true
      if (value !== form.userPassword) {
        return '两次输入的密码不一致'
      }
      return true
    },
    message: '两次输入的密码不一致'
  }
]

// 表单提交
const onSubmit = async () => {
  if (loading.value) return
  
  // 再次校验确认密码
  if (form.userPassword !== form.checkPassword) {
    showFailToast('两次输入的密码不一致')
    return
  }
  
  loading.value = true
  
  try {
    const res = await register({
      userAccount: form.userAccount,
      userPassword: form.userPassword,
      checkPassword: form.checkPassword
    })
    
    // 注册成功
    if (res.code === 200) {
      showSuccessToast('注册成功，请登录')
      
      // 跳转到登录页
      setTimeout(() => {
        router.push('/login')
      }, 1500)
    }
  } catch (error) {
    console.error('注册失败', error)
    // 错误已在响应拦截器中处理
  } finally {
    loading.value = false
  }
}

// 表单校验失败
const onFailed = (errorInfo) => {
  console.log('表单校验失败', errorInfo)
}

// 跳转到登录页
const goToLogin = () => {
  router.push('/login')
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-container {
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

