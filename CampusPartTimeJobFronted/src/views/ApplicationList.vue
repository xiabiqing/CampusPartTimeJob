<template>
  <div class="application-list-page">
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
        />
        
        <div v-for="item in applicationList" :key="item.id" class="application-card">
          <van-cell-group inset>
            <van-cell title="申请ID" :value="item.id" />
            <van-cell title="申请角色" :value="getRoleText(item.role)" />
            <van-cell title="申请信息" :value="item.information || '无'" />
            <van-cell title="状态" :value="getStatusText(item.status)">
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
            <van-cell
              v-if="item.status === 0"
              title="操作"
            >
              <template #value>
                <van-button
                  size="small"
                  type="success"
                  @click="handleDeal(item, true)"
                  style="margin-right: 8px;"
                >
                  同意
                </van-button>
                <van-button
                  size="small"
                  type="danger"
                  @click="handleDeal(item, false)"
                >
                  拒绝
                </van-button>
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
import { getApplicationList, dealApplication } from '@/api/roleApplication'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const applicationList = ref([])

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    0: '用户',
    1: '商家',
    2: '管理员'
  }
  return roleMap[role] || '未知'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    0: '处理中',
    1: '已同意',
    '-1': '已拒绝'
  }
  return statusMap[status] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  if (status === 1) return 'success'
  if (status === -1) return 'danger'
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

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getApplicationList()
    if (res && res.code === 200 && res.data) {
      applicationList.value = res.data
      finished.value = true
    }
  } catch (error) {
    console.error('获取申请列表失败', error)
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
const handleDeal = (application, choose) => {
  const action = choose ? '同意' : '拒绝'
  showConfirmDialog({
    title: '确认操作',
    message: `确定要${action}该申请吗？`
  })
    .then(async () => {
      try {
        await dealApplication({
          choose: choose,
          roleApplication: application
        })
        showSuccessToast(`${action}成功`)
        await loadData()
      } catch (error) {
        console.error('处理申请失败', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.application-list-page {
  min-height: calc(100vh - 46px);
  padding: 20px;
  background-color: #f5f5f5;
}

.application-card {
  margin-bottom: 12px;
}
</style>

