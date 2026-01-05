import request from '@/utils/request'

/**
 * 获取聊天联系人列表
 * 根据用户的申请记录获取联系人（学生可以看到申请过的商家，商家可以看到申请自己职位的学生）
 * @param {Number} role 用户角色 0-学生 1-商家
 */
export const getChatContacts = (role) => {
  // 根据角色调用不同的接口
  let url = ''
  if (role === 0) {
    // 学生调用学生接口
    url = '/student/chat/contacts'
  } else if (role === 1) {
    // 商家调用商家接口
    url = '/business/chat/contacts'
  } else {
    return Promise.reject(new Error('无效的用户角色'))
  }
  
  console.log('请求联系人列表:', { role, url, fullUrl: '/api' + url })
  
  return request({
    url,
    method: 'get'
  })
}

/**
 * 获取与某个用户的聊天记录
 * @param {Number} targetUserId 目标用户ID
 */
export const getChatHistory = (targetUserId) => {
  const url = `/chat/history/${targetUserId}`
  console.log('请求聊天历史:', { targetUserId, url, fullUrl: '/api' + url })
  
  return request({
    url,
    method: 'get'
  })
}

