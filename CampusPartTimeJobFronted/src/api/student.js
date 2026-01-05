import request, { requestWithLoading } from '@/utils/request'

/**
 * 获取所有兼职列表（学生查看）
 * @returns {Promise}
 */
export const getRecruitmentList = () => {
  return request({
    url: '/student/displayRecruitment',
    method: 'get'
  })
}

/**
 * 发送兼职申请
 * @param {Object} data - 申请信息
 * @param {number} data.recruitmentId - 招聘ID
 * @param {string} data.information - 申请信息
 * @returns {Promise}
 */
export const sendJobApplication = (data) => {
  return requestWithLoading({
    url: `/student/sendJobApplication?id=${data.recruitmentId}`,
    method: 'post',
    data: {
      information: data.information
    }
  })
}

/**
 * 获取用户申请的兼职订单列表
 * @returns {Promise}
 */
export const getMyJobApplicationList = () => {
  return request({
    url: '/student/listJobApp',
    method: 'get'
  })
}

