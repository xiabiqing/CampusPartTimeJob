<template>
  <div class="home-page">
    <!-- 搜索栏 -->
    <div class="search-section">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索岗位描述关键词"
        @search="onSearch"
        @clear="onClear"
        @input="onSearchInput"
      />
    </div>

    <!-- 兼职列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="job-list">
        <van-empty
          v-if="!loading && filteredJobList.length === 0"
          :description="isSearching ? '未找到相关兼职' : '暂无兼职信息'"
          :image="isSearching ? 'search' : 'default'"
        />
        
        <div v-else class="job-cards">
          <van-skeleton
            v-if="loading"
            title
            avatar
            :row="3"
            :loading="loading"
          />
          
          <div v-for="job in filteredJobList" :key="job.id" class="job-card">
            <div class="job-header">
              <h3 class="job-title">{{ job.title }}</h3>
              <span class="job-salary">{{ job.salary ? job.salary + '元/小时' : '-' }}</span>
            </div>
            <div class="job-info" v-if="job.tags && Array.isArray(job.tags) && job.tags.length > 0">
              <van-tag
                v-for="(tag, index) in job.tags"
                :key="index"
                type="primary"
                size="small"
                style="margin-right: 4px;"
              >
                {{ tag }}
              </van-tag>
            </div>
            <div class="job-details">
              <div class="detail-item">
                <span class="label">公司：</span>
                <span class="value">{{ job.company || '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">招聘人数：</span>
                <span class="value">{{ job.count ? job.count + '人' : '-' }}</span>
              </div>
              <div class="detail-item">
                <span class="label">岗位描述：</span>
                <span class="value">{{ job.jobDetails || '-' }}</span>
              </div>
              <div v-if="job.createTime" class="detail-item">
                <span class="label">发布时间：</span>
                <span class="value">{{ formatDate(job.createTime) }}</span>
              </div>
            </div>
            <div class="job-footer" v-if="userStore.userDetail?.role === 0">
              <van-button
                round
                block
                type="primary"
                @click="showApplyPopup(job)"
              >
                申请兼职
              </van-button>
            </div>
          </div>
        </div>
      </div>
    </van-pull-refresh>

    <!-- 申请弹窗 -->
    <van-popup
      v-model:show="showApplyDialog"
      position="bottom"
      round
      :style="{ minHeight: '40%' }"
    >
      <div class="apply-popup">
        <div class="popup-header">
          <span class="cancel" @click="showApplyDialog = false">取消</span>
          <span class="title">申请兼职</span>
          <span class="confirm" @click="confirmApply">提交</span>
        </div>
        <div class="popup-content">
          <van-cell-group inset>
            <van-cell title="职位" :value="currentJob?.title" />
            <van-cell title="公司" :value="currentJob?.company" />
            <van-cell title="薪资" :value="currentJob?.salary ? currentJob.salary + '元/小时' : '-'" />
          </van-cell-group>
          <van-field
            v-model="applyForm.information"
            label="申请信息"
            type="textarea"
            placeholder="请输入您的申请信息，如：个人简介、相关经验等"
            rows="5"
            autosize
            maxlength="500"
            show-word-limit
            style="margin-top: 12px;"
          />
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getRecruitmentList, sendJobApplication } from '@/api/student'
import { searchRecruitment } from '@/api/user'
import { showSuccessToast, showFailToast } from 'vant'

const userStore = useUserStore()

const searchKeyword = ref('')
const loading = ref(false)
const refreshing = ref(false)
const jobList = ref([])
const searchResultList = ref([]) // 搜索结果列表
const isSearching = ref(false) // 是否正在搜索
const showApplyDialog = ref(false)
const currentJob = ref(null)
const applyForm = ref({
  information: ''
})

// 防抖定时器
let searchTimer = null

// 显示列表：如果有搜索关键词且正在搜索，显示搜索结果；否则显示全部列表
const filteredJobList = computed(() => {
  if (isSearching.value && searchKeyword.value.trim()) {
    return searchResultList.value
  }
  return jobList.value
})

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

// 加载兼职列表
const loadJobList = async () => {
  loading.value = true
  try {
    const res = await getRecruitmentList()
    if (res && res.code === 200) {
      // 处理tags字段，确保是数组格式
      const list = (res.data || []).map(job => {
        // 如果tags是字符串，尝试解析为数组
        if (job.tags && typeof job.tags === 'string') {
          try {
            job.tags = JSON.parse(job.tags)
          } catch (e) {
            console.warn('解析tags失败:', job.tags, e)
            job.tags = []
          }
        }
        // 确保tags是数组
        if (!Array.isArray(job.tags)) {
          job.tags = []
        }
        return job
      })
      jobList.value = list
    } else {
      jobList.value = []
      showFailToast('获取兼职列表失败')
    }
  } catch (error) {
    console.error('获取兼职列表失败', error)
    jobList.value = []
    showFailToast('获取兼职列表失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  if (isSearching.value && searchKeyword.value.trim()) {
    // 如果正在搜索，刷新搜索结果
    onSearch(searchKeyword.value)
  } else {
    // 否则刷新全部列表
    loadJobList()
  }
}

// 搜索兼职（调用后端接口）
const onSearch = async (value) => {
  if (!value || !value.trim()) {
    // 如果搜索关键词为空，显示全部列表
    isSearching.value = false
    searchResultList.value = []
    return
  }
  
  loading.value = true
  isSearching.value = true
  
  try {
    const res = await searchRecruitment(value.trim())
    if (res && res.code === 200) {
      // 处理tags字段，确保是数组格式
      const list = (res.data || []).map(job => {
        // 如果tags是字符串，尝试解析为数组
        if (job.tags && typeof job.tags === 'string') {
          try {
            job.tags = JSON.parse(job.tags)
          } catch (e) {
            console.warn('解析tags失败:', job.tags, e)
            job.tags = []
          }
        }
        // 确保tags是数组
        if (!Array.isArray(job.tags)) {
          job.tags = []
        }
        return job
      })
      searchResultList.value = list
      
      if (list.length === 0) {
        showFailToast('未找到相关兼职')
      }
    } else {
      searchResultList.value = []
      showFailToast(res.msg || '搜索失败')
    }
  } catch (error) {
    console.error('搜索失败', error)
    searchResultList.value = []
    showFailToast('搜索失败，请稍后重试')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

// 搜索输入（防抖处理）
const onSearchInput = (value) => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 如果输入为空，立即清空搜索
  if (!value || !value.trim()) {
    onClear()
    return
  }
  
  // 设置防抖，500ms后执行搜索
  searchTimer = setTimeout(() => {
    onSearch(value)
  }, 500)
}

// 清空搜索
const onClear = () => {
  // 清除防抖定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
    searchTimer = null
  }
  
  searchKeyword.value = ''
  isSearching.value = false
  searchResultList.value = []
  // 清空后重新加载全部列表
  loadJobList()
}

// 显示申请弹窗
const showApplyPopup = (job) => {
  currentJob.value = job
  applyForm.value.information = ''
  showApplyDialog.value = true
}

// 确认申请
const confirmApply = async () => {
  if (!applyForm.value.information.trim()) {
    showFailToast('请输入申请信息')
    return
  }

  try {
    const res = await sendJobApplication({
      recruitmentId: currentJob.value.id,
      information: applyForm.value.information
    })
    if (res && res.code === 200) {
      showSuccessToast('申请提交成功')
      showApplyDialog.value = false
      applyForm.value.information = ''
    } else {
      showFailToast(res.msg || '申请提交失败')
    }
  } catch (error) {
    console.error('申请提交失败', error)
    showFailToast('申请提交失败')
  }
}

// 初始化加载
onMounted(() => {
  loadJobList()
})
</script>

<style scoped>
.home-page {
  min-height: calc(100vh - 46px - 50px);
  background-color: #f5f5f5;
}

.search-section {
  background: #fff;
  padding: 10px 0;
}

.job-list {
  padding: 10px;
}

.job-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.job-card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.job-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.job-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  flex: 1;
  margin: 0;
}

.job-salary {
  font-size: 18px;
  font-weight: bold;
  color: #ff6b6b;
  margin-left: 10px;
}

.job-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.job-details {
  margin-bottom: 12px;
}

.detail-item {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.detail-item .label {
  color: #999;
}

.detail-item .value {
  color: #333;
}

.job-footer {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #eee;
}

.apply-popup {
  padding: 0 16px 20px;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #eee;
}

.popup-header .title {
  font-size: 16px;
  font-weight: bold;
}

.popup-header .cancel {
  color: #999;
  font-size: 14px;
  cursor: pointer;
}

.popup-header .confirm {
  color: #1989fa;
  font-size: 14px;
  cursor: pointer;
}

.popup-content {
  padding-top: 16px;
}
</style>

