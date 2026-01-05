import axios from 'axios'
import { showFailToast, showLoadingToast, closeToast } from 'vant'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  },
  withCredentials: true // 支持session，携带cookie
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从localStorage获取token（如果有的话）
    const token = localStorage.getItem('token')
    if (token && token !== 'session-token') {
      config.headers.Authorization = `Bearer ${token}`
    }
    // 后端使用session，withCredentials已在axios实例中配置
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果返回的状态码为200，说明接口请求成功
    if (res.code === 200) {
      return res
    } else {
      // 接口返回错误信息（后端业务错误）
      const errorMsg = res.msg || res.message || '请求失败'
      showFailToast(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
  },
  (error) => {
    // 处理HTTP错误
    let message = '网络错误，请稍后重试'
    
    if (error.response) {
      // 服务器返回了错误状态码
      const { status, data } = error.response
      
      if (status === 401) {
        message = '未授权，请重新登录'
        // 清除用户信息，跳转到登录页
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        // 使用router跳转，避免硬刷新
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      } else if (status === 403) {
        message = '拒绝访问'
      } else if (status === 404) {
        message = '请求地址不存在'
      } else if (status === 500) {
        message = '服务器内部错误'
      } else if (data) {
        // 优先使用后端返回的错误信息
        message = data.msg || data.message || message
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    } else {
      message = error.message || '请求失败'
    }
    
    showFailToast(message)
    return Promise.reject(error)
  }
)

// 封装请求方法，支持loading
export const requestWithLoading = (config) => {
  const loadingToast = showLoadingToast({
    message: '加载中...',
    forbidClick: true,
    duration: 0
  })
  
  return request(config)
    .finally(() => {
      closeToast()
    })
}

export default request

