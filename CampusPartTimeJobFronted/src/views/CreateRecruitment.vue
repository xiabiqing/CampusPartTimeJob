<template>
  <div class="create-recruitment-page">
    <van-nav-bar title="发布兼职" left-arrow @click-left="router.back()" fixed placeholder safe-area-inset-top />
    
    <van-form @submit="onSubmit">
      <van-cell-group inset>
        <van-field
          v-model="formData.title"
          name="title"
          label="标题"
          placeholder="请输入兼职标题（1-30字）"
          :rules="[{ required: true, message: '请输入标题' }]"
          maxlength="30"
          show-word-limit
        />
        <van-field
          v-model="formData.salary"
          name="salary"
          label="薪资"
          type="digit"
          placeholder="请输入薪资（元/小时）"
          :rules="[{ required: true, message: '请输入薪资' }]"
        />
        <van-field
          v-model="formData.company"
          name="company"
          label="公司"
          placeholder="请输入公司名称（1-30字）"
          :rules="[{ required: true, message: '请输入公司名称' }]"
          maxlength="30"
        />
        <van-field
          v-model="formData.count"
          name="count"
          label="招聘人数"
          type="digit"
          placeholder="请输入招聘人数"
          :rules="[{ required: true, message: '请输入招聘人数' }]"
        />
        <van-field
          v-model="formData.jobDetails"
          name="jobDetails"
          label="岗位描述"
          type="textarea"
          placeholder="请输入岗位详细描述（至少10字）"
          :rules="[{ required: true, message: '请输入岗位描述' }]"
          rows="5"
          autosize
          maxlength="1000"
          show-word-limit
        />
      </van-cell-group>

      <!-- 标签选择 -->
      <div class="tags-section">
        <div class="section-title">选择标签</div>
        <van-cell-group inset>
          <div class="tags-container">
            <van-tag
              v-for="tag in tagsList"
              :key="tag.id"
              :type="selectedTags.includes(tag.name) ? 'primary' : 'default'"
              size="medium"
              class="tag-item"
              @click="toggleTag(tag.name)"
            >
              {{ tag.name }}
            </van-tag>
          </div>
        </van-cell-group>
      </div>

      <div class="submit-section">
        <van-button round block type="primary" native-type="submit" :loading="submitting">
          发布兼职
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createRecruitment, getTagsList } from '@/api/business'
import { showSuccessToast, showFailToast } from 'vant'

const router = useRouter()

const formData = ref({
  title: '',
  salary: '',
  company: '',
  count: '',
  jobDetails: '',
  tags: []
})

const tagsList = ref([])
const selectedTags = ref([])
const submitting = ref(false)

// 获取标签列表
const fetchTags = async () => {
  try {
    const res = await getTagsList()
    if (res && res.code === 200) {
      tagsList.value = res.data || []
    }
  } catch (error) {
    console.error('获取标签列表失败', error)
  }
}

// 切换标签
const toggleTag = (tagName) => {
  const index = selectedTags.value.indexOf(tagName)
  if (index > -1) {
    selectedTags.value.splice(index, 1)
  } else {
    selectedTags.value.push(tagName)
  }
}

// 提交表单
const onSubmit = async () => {
  // 验证
  if (!formData.value.title || formData.value.title.length < 1 || formData.value.title.length > 30) {
    showFailToast('标题长度应在1-30字之间')
    return
  }
  
  if (!formData.value.salary || parseFloat(formData.value.salary) < 0) {
    showFailToast('请输入有效薪资')
    return
  }
  
  if (!formData.value.company || formData.value.company.length < 1 || formData.value.company.length > 30) {
    showFailToast('公司名称长度应在1-30字之间')
    return
  }
  
  if (!formData.value.count || parseInt(formData.value.count) < 1) {
    showFailToast('招聘人数至少为1人')
    return
  }
  
  if (!formData.value.jobDetails || formData.value.jobDetails.length < 10) {
    showFailToast('岗位描述至少10字')
    return
  }

  submitting.value = true
  try {
    const data = {
      title: formData.value.title,
      salary: parseFloat(formData.value.salary),
      company: formData.value.company,
      count: parseInt(formData.value.count),
      jobDetails: formData.value.jobDetails,
      tags: selectedTags.value
    }
    
    const res = await createRecruitment(data)
    if (res && res.code === 200) {
      showSuccessToast('发布成功')
      router.back()
    }
  } catch (error) {
    console.error('发布失败', error)
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchTags()
})
</script>

<style scoped>
.create-recruitment-page {
  min-height: calc(100vh - 46px);
  padding: 20px;
  background-color: #f5f5f5;
}

.tags-section {
  margin-top: 20px;
}

.section-title {
  padding: 12px 16px;
  font-size: 14px;
  color: #666;
}

.tags-container {
  padding: 12px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  cursor: pointer;
  user-select: none;
}

.submit-section {
  margin-top: 30px;
  padding: 0 16px 20px;
}
</style>

