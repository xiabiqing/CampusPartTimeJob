import request, { requestWithLoading } from '@/utils/request'

/**
 * 管理员登录
 * @param {Object} data - 登录信息
 * @param {string} data.userAccount - 账号
 * @param {string} data.userPassword - 密码
 * @returns {Promise}
 */
export const adminLogin = (data) => {
  return requestWithLoading({
    url: '/admin/login',
    method: 'post',
    data
  })
}

