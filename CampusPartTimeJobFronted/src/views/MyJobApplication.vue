<template>
  <div class="my-job-application-page">
    <van-nav-bar title="我的兼职申请" fixed placeholder safe-area-inset-top />
    
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-empty
          v-if="!loading && applicationList.length === 0"
          description="暂无申请记录"
          image="search"
        />
        
        <div v-for="item in applicationList" :key="item.id" class="application-card">
          <div class="card-header">
            <div class="job-title-section">
              <h3 class="job-title">{{ getRecruitmentTitle(item.recruId) }}</h3>
              <van-tag :type="getStatusType(item.status)" size="medium" class="status-tag">
                {{ getStatusText(item.status) }}
              </van-tag>
            </div>
            <div class="salary-info">
              <span class="salary-label">薪资</span>
              <span class="salary-value">{{ getRecruitmentSalary(item.recruId) }}元/小时</span>
            </div>
          </div>
          
          <div class="card-content">
            <div class="info-row">
              <van-icon name="shop-o" class="info-icon" />
              <span class="info-text">{{ getRecruitmentCompany(item.recruId) }}</span>
            </div>
            <div class="info-row" v-if="item.information">
              <van-icon name="description" class="info-icon" />
              <span class="info-text">{{ item.information }}</span>
            </div>
            <div class="tags-section" v-if="getRecruitmentTags(item.recruId) && getRecruitmentTags(item.recruId).length > 0">
              <van-tag
                v-for="(tag, index) in getRecruitmentTags(item.recruId)"
                :key="index"
                type="primary"
                size="small"
                class="job-tag"
              >
                {{ tag }}
              </van-tag>
            </div>
          </div>
          
          <div class="card-footer">
            <div class="time-info">
              <div class="time-item">
                <van-icon name="clock-o" class="time-icon" />
                <span class="time-label">申请时间：</span>
                <span class="time-value">{{ formatDate(item.createTime) }}</span>
              </div>
              <div class="time-item" v-if="item.updateTime && item.status !== 0">
                <van-icon name="checked" class="time-icon" />
                <span class="time-label">处理时间：</span>
                <span class="time-value">{{ formatDate(item.updateTime) }}</span>
              </div>
            </div>
            <div class="action-buttons" v-if="getRecruitmentBusinessId(item.recruId)">
              <van-button
                type="primary"
                size="small"
                icon="chat-o"
                @click="goToChat(item)"
                block
              >
                联系商家
              </van-button>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyJobApplicationList } from '@/api/student'
import { getRecruitmentList } from '@/api/student'
import { showFailToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const applicationList = ref([])
const recruitmentMap = ref({}) // 存储招聘信息映射

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '申请中',
    1: '已通过',
    2: '已拒绝',
    '-1': '已拒绝'  // 兼容-1拒绝状态
  }
  return statusMap[status] || statusMap[String(status)] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === 2 || status === -1) return 'danger'  // 兼容-1和2两种拒绝状态
  return 'warning'
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  try {
    const date = new Date(dateStr)
    return date.toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    return dateStr
  }
}

// 获取招聘标题
const getRecruitmentTitle = (recruId) => {
  const recruitment = recruitmentMap.value[recruId]
  return recruitment?.title || `招聘ID: ${recruId}`
}

// 获取招聘薪资
const getRecruitmentSalary = (recruId) => {
  const recruitment = recruitmentMap.value[recruId]
  return recruitment?.salary || '-'
}

// 获取招聘公司
const getRecruitmentCompany = (recruId) => {
  const recruitment = recruitmentMap.value[recruId]
  return recruitment?.company || '-'
}

// 获取招聘标签
const getRecruitmentTags = (recruId) => {
  const recruitment = recruitmentMap.value[recruId]
  return recruitment?.tags || []
}

// 获取招聘的商家ID
const getRecruitmentBusinessId = (recruId) => {
  const recruitment = recruitmentMap.value[recruId]
  return recruitment?.busIdRecru || null
}

// 跳转到聊天页面
const goToChat = (item) => {
  const businessId = getRecruitmentBusinessId(item.recruId)
  if (!businessId) {
    showFailToast('无法获取商家信息')
    return
  }
  
  const recruitment = recruitmentMap.value[item.recruId]
  const businessName = recruitment?.company || `商家${businessId}`
  
  router.push({
    name: 'Chat',
    params: {
      userId: businessId,
      role: 1 // 商家角色
    },
    query: {
      name: businessName
    }
  })
}

// 加载招聘信息
const loadRecruitmentData = async () => {
  try {
    const res = await getRecruitmentList()
    if (res && res.code === 200 && res.data) {
      const map = {}
      res.data.forEach(recruitment => {
        // 处理tags字段，确保是数组格式
        if (recruitment.tags && typeof recruitment.tags === 'string') {
          try {
            recruitment.tags = JSON.parse(recruitment.tags)
          } catch (e) {
            console.warn('解析tags失败:', recruitment.tags, e)
            recruitment.tags = []
          }
        }
        // 确保tags是数组
        if (!Array.isArray(recruitment.tags)) {
          recruitment.tags = []
        }
        map[recruitment.id] = recruitment
      })
      recruitmentMap.value = map
    }
  } catch (error) {
    console.error('获取招聘信息失败', error)
  }
}

// 加载申请数据
const loadData = async () => {
  loading.value = true
  try {
    // 先加载招聘信息
    await loadRecruitmentData()
    
    // 再加载申请列表
    const res = await getMyJobApplicationList()
    if (res && res.code === 200) {
      const list = res.data || []
      // 按创建时间倒序排列，申请中的排在前面
      applicationList.value = list.sort((a, b) => {
        // 先按状态排序：申请中(0)在前
        if (a.status === 0 && b.status !== 0) return -1
        if (a.status !== 0 && b.status === 0) return 1
        // 再按创建时间排序：新的在前
        if (a.createTime && b.createTime) {
          return new Date(b.createTime) - new Date(a.createTime)
        }
        return 0
      })
      finished.value = true
    } else {
      applicationList.value = []
      finished.value = true
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
    applicationList.value = []
    finished.value = true
    showFailToast('获取申请列表失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  finished.value = false
  loadData()
}

// 上拉加载
const onLoad = () => {
  loadData()
}

onMounted(() => {
  if (!userStore.isLoggedIn()) {
    userStore.initUserInfo()
    if (!userStore.isLoggedIn()) {
      router.push('/login')
      return
    }
  }
  loadData()
})
</script>

<style scoped>
.my-job-application-page {
  min-height: calc(100vh - 46px);
  background: linear-gradient(180deg, #f5f7fa 0%, #f5f5f5 100%);
  padding-bottom: 20px;
}

.application-card {
  margin: 12px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.application-card:active {
  transform: scale(0.98);
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.12);
}

.card-header {
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.job-title-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.job-title {
  flex: 1;
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: #fff;
  line-height: 1.4;
}

.status-tag {
  margin-left: 8px;
  flex-shrink: 0;
}

.salary-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.salary-label {
  font-size: 12px;
  opacity: 0.9;
}

.salary-value {
  font-size: 20px;
  font-weight: bold;
}

.card-content {
  padding: 16px;
}

.info-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
  line-height: 1.6;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-icon {
  margin-right: 8px;
  margin-top: 2px;
  color: #1989fa;
  flex-shrink: 0;
}

.info-text {
  flex: 1;
  font-size: 14px;
  color: #333;
  word-break: break-word;
}

.tags-section {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
}

.job-tag {
  margin: 0;
}

.card-footer {
  padding: 12px 16px;
  background-color: #fafafa;
  border-top: 1px solid #eee;
}

.time-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.time-item {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.time-icon {
  margin-right: 6px;
  color: #999;
}

.time-label {
  color: #999;
}

.time-value {
  color: #333;
  margin-left: 4px;
}

.action-buttons {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}
</style>

