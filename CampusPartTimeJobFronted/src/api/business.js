import request, { requestWithLoading } from '@/utils/request'

/**
 * 创建兼职订单
 * @param {Object} data - 兼职信息
 * @param {string} data.title - 标题
 * @param {number} data.salary - 薪水
 * @param {string} data.company - 公司
 * @param {number} data.count - 招聘人数
 * @param {Array<string>} data.tags - 标签列表
 * @param {string} data.jobDetails - 岗位描述
 * @returns {Promise}
 */
export const createRecruitment = (data) => {
  return requestWithLoading({
    url: '/business/createMenu',
    method: 'post',
    data
  })
}

/**
 * 获取标签列表
 * @returns {Promise}
 */
export const getTagsList = () => {
  return request({
    url: '/business/getTags',
    method: 'get'
  })
}

/**
 * 获取商家的招聘列表
 * @returns {Promise}
 */
export const getBusinessRecruitmentList = () => {
  return request({
    url: '/business/getRecruList',
    method: 'get'
  })
}

/**
 * 删除招聘
 * @param {Object} data - 招聘信息
 * @param {number} data.id - 招聘ID
 * @param {number} data.busIdRecru - 商家ID
 * @returns {Promise}
 */
export const deleteRecruitment = (data) => {
  return requestWithLoading({
    url: '/business/deleteMenu',
    method: 'delete',
    data
  })
}

/**
 * 更新招聘信息
 * @param {Object} data - 招聘信息（必须包含id和busIdRecru）
 * @param {number} data.id - 招聘ID
 * @param {number} data.busIdRecru - 商家ID
 * @param {string} data.title - 标题
 * @param {number} data.salary - 薪资
 * @param {string} data.company - 公司
 * @param {number} data.count - 招聘人数
 * @param {Array<string>} data.tags - 标签列表
 * @param {string} data.jobDetails - 岗位描述
 * @returns {Promise}
 */
export const updateRecruitment = (data) => {
  return requestWithLoading({
    url: '/business/updateMenu',
    method: 'put',
    data
  })
}

/**
 * 获取商家收到的兼职申请列表
 * @returns {Promise}
 */
export const getJobApplicationList = () => {
  return request({
    url: '/business/listJobApp',
    method: 'get'
  })
}

/**
 * 处理兼职申请
 * @param {Object} data - 处理信息
 * @param {Boolean} data.choose - true同意 false拒绝
 * @param {Object} data.jobApplication - 申请对象（需要包含id）
 * @returns {Promise}
 */
export const dealJobApplication = (data) => {
  return requestWithLoading({
    url: '/business/dealJobApp',
    method: 'post',
    params: {
      choose: data.choose
    },
    data: data.jobApplication
  })
}

