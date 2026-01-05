import request from '@/utils/request'

/**
 * 获取所有兼职列表（用户查看）
 * 注意：如果后端没有这个接口，需要添加
 * @returns {Promise}
 */
export const getAllRecruitmentList = () => {
  return request({
    url: '/recruitment/list',
    method: 'get'
  })
}

