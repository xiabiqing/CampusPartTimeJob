import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAdminStore = defineStore('admin', () => {
  // 管理员信息
  const adminInfo = ref(null)
  // token
  const token = ref('')

  // 设置管理员信息
  const setAdminInfo = (info) => {
    adminInfo.value = info
    if (info) {
      localStorage.setItem('adminInfo', JSON.stringify(info))
    } else {
      localStorage.removeItem('adminInfo')
    }
  }

  // 设置token
  const setToken = (newToken) => {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('adminToken', newToken)
    } else {
      localStorage.removeItem('adminToken')
    }
  }

  // 初始化管理员信息（从localStorage读取）
  const initAdminInfo = () => {
    const storedAdminInfo = localStorage.getItem('adminInfo')
    const storedToken = localStorage.getItem('adminToken')
    
    if (storedAdminInfo) {
      try {
        adminInfo.value = JSON.parse(storedAdminInfo)
      } catch (e) {
        console.error('解析管理员信息失败', e)
        adminInfo.value = null
      }
    }
    
    if (storedToken) {
      token.value = storedToken
    }
  }

  // 退出登录
  const logout = () => {
    adminInfo.value = null
    token.value = ''
    localStorage.removeItem('adminInfo')
    localStorage.removeItem('adminToken')
  }

  // 是否已登录
  const isLoggedIn = () => {
    return !!adminInfo.value
  }

  return {
    adminInfo,
    token,
    setAdminInfo,
    setToken,
    initAdminInfo,
    logout,
    isLoggedIn
  }
})

