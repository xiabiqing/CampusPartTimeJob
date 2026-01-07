<template>
  <div class="my-recruitment-page">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-empty
          v-if="!loading && recruitmentList.length === 0"
          description="暂无招聘信息"
        />
        
        <div v-for="item in recruitmentList" :key="item.id" class="recruitment-card">
          <van-cell-group inset>
            <van-cell title="标题" :value="item.title" />
            <van-cell title="薪资" :value="item.salary ? item.salary + '元/小时' : '-'" />
            <van-cell title="公司" :value="item.company || '-'" />
            <van-cell title="招聘人数" :value="item.count ? item.count + '人' : '-'" />
            <van-cell title="岗位描述" :value="item.jobDetails || '-'" />
            <van-cell
              v-if="item.tags && item.tags.length > 0"
              title="标签"
            >
              <template #value>
                <van-tag
                  v-for="(tag, index) in item.tags"
                  :key="index"
                  type="primary"
                  size="small"
                  style="margin-right: 4px;"
                >
                  {{ tag }}
                </van-tag>
              </template>
            </van-cell>
            <van-cell
              v-if="item.createTime"
              title="发布时间"
              :value="formatDate(item.createTime)"
            />
            <van-cell title="操作">
              <template #value>
                <van-button
                  size="small"
                  type="primary"
                  @click="editRecruitment(item)"
                  style="margin-right: 8px;"
                >
                  编辑
                </van-button>
                <van-button
                  size="small"
                  type="danger"
                  @click="deleteRecruitment(item)"
                >
                  删除
                </van-button>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 编辑弹窗 -->
    <van-popup
      v-model:show="showEditPopup"
      position="bottom"
      round
      :style="{ minHeight: '60%' }"
    >
      <div class="edit-popup">
        <div class="popup-header">
          <span class="cancel" @click="showEditPopup = false">取消</span>
          <span class="title">编辑招聘</span>
          <span class="confirm" @click="confirmEdit">确定</span>
        </div>
        <van-form>
          <van-cell-group inset>
            <van-field
              v-model="editForm.title"
              label="标题"
              placeholder="请输入兼职标题"
              maxlength="30"
            />
            <van-field
              v-model="editForm.salary"
              label="薪资"
              type="digit"
              placeholder="请输入薪资（元/小时）"
            />
            <van-field
              v-model="editForm.company"
              label="公司"
              placeholder="请输入公司名称"
              maxlength="30"
            />
            <van-field
              v-model="editForm.count"
              label="招聘人数"
              type="digit"
              placeholder="请输入招聘人数"
            />
            <van-field
              v-model="editForm.jobDetails"
              label="岗位描述"
              type="textarea"
              placeholder="请输入岗位详细描述"
              rows="5"
              autosize
              maxlength="1000"
            />
          </van-cell-group>
        </van-form>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBusinessRecruitmentList, deleteRecruitment as deleteRecruitmentApi, updateRecruitment } from '@/api/business'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const router = useRouter()

const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const recruitmentList = ref([])
const showEditPopup = ref(false)
const editForm = ref({
  id: null,
  title: '',
  salary: '',
  company: '',
  count: '',
  jobDetails: '',
  tags: []
})
const currentRecruitment = ref(null)

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
    const res = await getBusinessRecruitmentList()
    if (res && res.code === 200) {
      // 处理tags字段，确保是数组格式
      const list = (res.data || []).map(item => {
        // 如果tags是字符串，尝试解析为数组
        if (item.tags && typeof item.tags === 'string') {
          try {
            item.tags = JSON.parse(item.tags)
          } catch (e) {
            console.warn('解析tags失败:', item.tags, e)
            item.tags = []
          }
        }
        // 确保tags是数组
        if (!Array.isArray(item.tags)) {
          item.tags = []
        }
        return item
      })
      recruitmentList.value = list
      finished.value = true
    } else {
      recruitmentList.value = []
      finished.value = true
    }
  } catch (error) {
    console.error('获取招聘列表失败', error)
    recruitmentList.value = []
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

// 编辑招聘
const editRecruitment = (item) => {
  currentRecruitment.value = item
  editForm.value = {
    id: item.id,
    title: item.title || '',
    salary: item.salary ? item.salary.toString() : '',
    company: item.company || '',
    count: item.count ? item.count.toString() : '',
    jobDetails: item.jobDetails || '',
    tags: item.tags || []
  }
  showEditPopup.value = true
}

// 确认编辑
const confirmEdit = async () => {
  if (!editForm.value.title || editForm.value.title.length < 1 || editForm.value.title.length > 30) {
    showFailToast('标题长度应在1-30字之间')
    return
  }
  
  if (!editForm.value.salary || parseFloat(editForm.value.salary) < 0) {
    showFailToast('请输入有效薪资')
    return
  }
  
  if (!editForm.value.company || editForm.value.company.length < 1 || editForm.value.company.length > 30) {
    showFailToast('公司名称长度应在1-30字之间')
    return
  }
  
  if (!editForm.value.count || parseInt(editForm.value.count) < 1) {
    showFailToast('招聘人数至少为1人')
    return
  }
  
  if (!editForm.value.jobDetails || editForm.value.jobDetails.length < 10) {
    showFailToast('岗位描述至少10字')
    return
  }

  try {
    const data = {
      id: currentRecruitment.value.id,
      busIdRecru: currentRecruitment.value.busIdRecru,
      title: editForm.value.title,
      salary: parseFloat(editForm.value.salary),
      company: editForm.value.company,
      count: parseInt(editForm.value.count),
      jobDetails: editForm.value.jobDetails,
      tags: editForm.value.tags || []
    }
    
    const res = await updateRecruitment(data)
    if (res && res.code === 200) {
      showSuccessToast('修改成功')
      showEditPopup.value = false
      await loadData()
    }
  } catch (error) {
    console.error('修改失败', error)
  }
}

// 删除招聘
const deleteRecruitment = (item) => {
  showConfirmDialog({
    title: '确认删除',
    message: '确定要删除该招聘信息吗？'
  })
    .then(async () => {
      try {
        const res = await deleteRecruitmentApi({
          id: item.id,
          busIdRecru: item.busIdRecru
        })
        if (res && res.code === 200) {
          showSuccessToast('删除成功')
          await loadData()
        }
      } catch (error) {
        console.error('删除失败', error)
      }
    })
    .catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.my-recruitment-page {
  min-height: calc(100vh - 46px);
  padding: 20px;
  background-color: #f5f5f5;
}

.recruitment-card {
  margin-bottom: 12px;
}

.edit-popup {
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
}

.popup-header .confirm {
  color: #1989fa;
  font-size: 14px;
}
</style>

