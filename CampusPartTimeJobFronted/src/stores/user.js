import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCurrentUser } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  // 用户基本信息（登录时返回）
  const userInfo = ref(null)
  // 用户详细信息（包含学生信息）
  const userDetail = ref(null)
  // token
  const token = ref('')

  // 设置用户信息
  const setUserInfo = (info) => {
    userInfo.value = info
    if (info) {
      localStorage.setItem('userInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('userInfo')
    }
  }

  // 设置用户详细信息
  const setUserDetail = (detail) => {
    userDetail.value = detail
  }

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  // 初始化用户信息（从localStorage读取）
  const initUserInfo = () => {
    const storedUserInfo = localStorage.getItem('userInfo')
    const storedToken = localStorage.getItem('token')
    
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败', e)
        userInfo.value = null
      }
    }
    
    if (storedToken) {
      token.value = storedToken
    }
  }

  // 刷新用户详细信息
  const refreshUserDetail = async () => {
    try {
      const res = await getCurrentUser()
      if (res.code === 200 && res.data) {
        userDetail.value = res.data
      }
    } catch (error) {
      console.error('获取用户详细信息失败', error)
    }
  }

  // 退出登录
  const logout = () => {
    userInfo.value = null
    userDetail.value = null
    token.value = ''
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
  }

  // 是否已登录（后端使用session，只要有userInfo就算登录）
  const isLoggedIn = () => {
    return !!userInfo.value
  }

  return {
    userInfo,
    userDetail,
    token,
    setUserInfo,
    setUserDetail,
    setToken,
    initUserInfo,
    refreshUserDetail,
    logout,
    isLoggedIn
  }
})

