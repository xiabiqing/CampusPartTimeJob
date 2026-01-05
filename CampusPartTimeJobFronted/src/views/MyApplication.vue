<template>
  <div class="my-application-page">
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
            <van-cell title="申请角色" :value="getRoleText(item.role)">
              <template #value>
                <van-tag type="primary">{{ getRoleText(item.role) }}</van-tag>
              </template>
            </van-cell>
            <van-cell title="申请信息" :value="item.information || '无'" />
            <van-cell
              v-if="item.image"
              title="资质图片"
            >
              <template #value>
                <van-image
                  :src="item.image"
                  width="60"
                  height="60"
                  fit="cover"
                  @click="previewImage(item.image)"
                />
              </template>
            </van-cell>
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
              v-if="item.updateTime"
              title="处理时间"
              :value="formatDate(item.updateTime)"
            />
            <van-cell title="操作">
              <template #value>
                <van-button
                  size="small"
                  type="primary"
                  icon="chat-o"
                  @click="goToChatAdmin"
                >
                  联系管理员
                </van-button>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 图片预览 -->
    <van-image-preview v-model:show="showImagePreview" :images="previewImages" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getUserApplicationList, getAdminList } from '@/api/roleApplication'
import { showFailToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const applicationList = ref([])
const showImagePreview = ref(false)
const previewImages = ref([])
const adminId = ref(null) // 存储管理员ID

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

// 预览图片
const previewImage = (imageUrl) => {
  previewImages.value = [imageUrl]
  showImagePreview.value = true
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserApplicationList()
    if (res && res.code === 200) {
      const list = res.data || []
      // 按创建时间倒序排列
      applicationList.value = list.sort((a, b) => {
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

// 加载管理员信息
const loadAdminInfo = async () => {
  try {
    const res = await getAdminList()
    if (res && res.code === 200 && res.data && res.data.length > 0) {
      // 获取第一个管理员的ID
      adminId.value = res.data[0].id
    }
  } catch (error) {
    console.error('获取管理员信息失败', error)
  }
}

// 跳转到聊天页面（联系管理员）
const goToChatAdmin = () => {
  if (!adminId.value) {
    showFailToast('无法获取管理员信息，请稍后再试')
    return
  }
  
  router.push({
    name: 'Chat',
    params: {
      userId: adminId.value,
      role: 2 // 管理员角色
    },
    query: {
      name: '管理员'
    }
  })
}

onMounted(() => {
  if (!userStore.isLoggedIn()) {
    userStore.initUserInfo()
    if (!userStore.isLoggedIn()) {
      router.push('/login')
      return
    }
  }
  loadAdminInfo() // 先加载管理员信息
  loadData()
})
</script>

<style scoped>
.my-application-page {
  min-height: calc(100vh - 46px);
  padding: 20px;
  background-color: #f5f5f5;
}

.application-card {
  margin-bottom: 12px;
}
</style>

