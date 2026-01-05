<template>
  <div class="job-application-list-page">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-empty
          v-if="!loading && applicationList.length === 0"
          description="暂无申请信息"
        />
        
        <div v-for="item in applicationList" :key="item.id" class="application-card">
          <van-cell-group inset>
            <van-cell title="申请ID" :value="item.id" />
            <van-cell title="招聘ID" :value="item.recruId" />
            <van-cell title="学生ID" :value="item.stuId" />
            <van-cell title="申请信息" :value="item.information || '-'" />
            <van-cell title="申请状态">
              <template #value>
                <van-tag :type="getStatusType(item.status)">
                  {{ getStatusText(item.status) }}
                </van-tag>
              </template>
            </van-cell>
            <van-cell
              v-if="item.createTime"
              title="申请时间"
              :value="formatDate(item.createTime)"
            />
            <van-cell title="操作">
              <template #value>
                <div style="display: flex; gap: 8px; flex-wrap: wrap;">
                  <van-button
                    v-if="item.status === 0"
                    size="small"
                    type="success"
                    @click="dealApplication(item, true)"
                  >
                    同意
                  </van-button>
                  <van-button
                    v-if="item.status === 0"
                    size="small"
                    type="danger"
                    @click="dealApplication(item, false)"
                  >
                    拒绝
                  </van-button>
                  <van-button
                    v-if="item.stuId"
                    size="small"
                    type="primary"
                    icon="chat-o"
                    @click="goToChat(item)"
                  >
                    联系学生
                  </van-button>
                </div>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getJobApplicationList, dealJobApplication } from '@/api/business'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const router = useRouter()

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const applicationList = ref([])

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

// 获取状态文本
const getStatusText = (status) => {
  if (status === 0) return '申请中'
  if (status === 1) return '已同意'
  if (status === 2) return '已拒绝'
  return '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  if (status === 0) return 'warning'
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'default'
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getJobApplicationList()
    if (res && res.code === 200) {
      // 将新申请放在前面
      const list = res.data || []
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

// 处理申请
const dealApplication = (item, choose) => {
  const action = choose ? '同意' : '拒绝'
  showConfirmDialog({
    title: '确认操作',
    message: `确定要${action}该申请吗？`
  })
    .then(async () => {
      try {
        const res = await dealJobApplication({
          choose: choose,
          jobApplication: {
            id: item.id,
            recruId: item.recruId,
            stuId: item.stuId,
            information: item.information,
            status: item.status
          }
        })
        if (res && res.code === 200) {
          showSuccessToast(`${action}成功`)
          await loadData()
        } else {
          showFailToast(res.msg || `${action}失败`)
        }
      } catch (error) {
        console.error(`${action}失败`, error)
        showFailToast(`${action}失败`)
      }
    })
    .catch(() => {})
}

// 跳转到聊天页面
const goToChat = (item) => {
  if (!item.stuId) {
    showFailToast('无法获取学生信息')
    return
  }
  
  router.push({
    name: 'Chat',
    params: {
      userId: item.stuId,
      role: 0 // 学生角色
    },
    query: {
      name: `学生${item.stuId}`
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.job-application-list-page {
  min-height: calc(100vh - 46px);
  padding: 20px;
  background-color: #f5f5f5;
}

.application-card {
  margin-bottom: 12px;
}
</style>

