import request, { requestWithLoading } from '@/utils/request'

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @param {string} data.userAccount - 账号
 * @param {string} data.userPassword - 密码
 * @param {string} data.checkPassword - 确认密码
 * @returns {Promise}
 */
export const register = (data) => {
  return requestWithLoading({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 * @param {string} data.userAccount - 账号
 * @param {string} data.userPassword - 密码
 * @returns {Promise}
 */
export const login = (data) => {
  return requestWithLoading({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 获取当前用户信息（包含学生详细信息）
 * @returns {Promise}
 */
export const getCurrentUser = () => {
  return request({
    url: '/user/current',
    method: 'get'
  })
}

/**
 * 修改用户默认信息（学生）
 * @param {Object} data - 用户信息
 * @param {Long} data.id - 用户ID
 * @param {string} data.username - 用户名
 * @param {string} data.image - 头像
 * @param {number} data.age - 年龄
 * @param {number} data.gender - 性别 0男 1女
 * @param {string} data.major - 专业
 * @param {string} data.email - 邮箱
 * @param {string} data.selfIntroduction - 自我介绍
 * @returns {Promise}
 */
export const updateUserInfo = (data) => {
  return request({
    url: '/user/updateDefaultInfo',
    method: 'post',
    data
  })
}

/**
 * 修改商家信息
 * @param {Object} data - 商家信息
 * @param {Long} data.id - 用户ID
 * @param {string} data.username - 用户名
 * @param {string} data.image - 头像
 * @param {number} data.role - 角色
 * @param {string} data.position - 职位
 * @param {string} data.location - 位置
 * @param {string} data.company - 公司
 * @returns {Promise}
 */
export const updateBusinessInfo = (data) => {
  return request({
    url: '/user/updateBusInfo',
    method: 'post',
    data
  })
}

