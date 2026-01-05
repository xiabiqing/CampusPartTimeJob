import request, { requestWithLoading } from '@/utils/request'

/**
 * 提交角色申请
 * @param {Object} data - 申请信息
 * @param {Long} data.adminId - 管理员ID
 * @param {number} data.role - 申请成为的角色 0用户 1商家 2管理员
 * @param {string} data.information - 申请信息
 * @param {string} data.image - 申请资质图片
 * @returns {Promise}
 */
export const submitRoleApplication = (data) => {
  return requestWithLoading({
    url: '/portRole/role',
    method: 'post',
    data
  })
}

/**
 * 获取申请列表（管理员查看）
 * @returns {Promise}
 */
export const getApplicationList = () => {
  return request({
    url: '/admin/application',
    method: 'get'
  })
}

/**
 * 处理申请（管理员）
 * @param {Object} data - 处理信息
 * @param {Boolean} data.choose - true同意 false拒绝
 * @param {Object} data.roleApplication - 申请对象
 * @returns {Promise}
 */
export const dealApplication = (data) => {
  return requestWithLoading({
    url: '/admin/dealApplication',
    method: 'post',
    params: {
      choose: data.choose
    },
    data: data.roleApplication
  })
}

/**
 * 获取管理员列表
 * @returns {Promise}
 */
export const getAdminList = () => {
  return request({
    url: '/admin/listAdmin',
    method: 'get'
  })
}

/**
 * 获取用户自己的申请状态列表
 * @returns {Promise}
 */
export const getUserApplicationList = () => {
  return request({
    url: '/user/application',
    method: 'get'
  })
}

