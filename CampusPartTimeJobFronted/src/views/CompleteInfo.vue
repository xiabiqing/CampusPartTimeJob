<template>
  <div class="complete-info-page">
    <van-nav-bar 
      title="完善信息" 
      fixed 
      placeholder 
      safe-area-inset-top
      left-arrow
      @click-left="handleBack"
    />
    
    <div class="form-container">
      <van-form @submit="onSubmit">
        <!-- 学生角色表单 -->
        <template v-if="userDetail.role === 0 || !userDetail.role">
          <van-cell-group inset>
            <van-field
              v-model="formData.username"
              name="username"
              label="用户名"
              placeholder="请输入用户名（1-10字）"
              :rules="[{ required: true, message: '请输入用户名' }]"
              maxlength="10"
              show-word-limit
            />
            <van-field
              v-model="formData.image"
              name="image"
              label="头像URL"
              placeholder="请输入头像URL地址（可选）"
            />
            <van-field
              v-model="formData.age"
              name="age"
              label="年龄"
              type="digit"
              placeholder="请输入年龄（0-150岁）"
              :rules="[{ required: true, message: '请输入年龄' }]"
            />
            <van-field
              v-model="genderText"
              name="gender"
              label="性别"
              placeholder="请选择性别"
              readonly
              is-link
              @click="showGenderPicker = true"
              :rules="[{ required: true, message: '请选择性别' }]"
            />
            <van-field
              v-model="formData.major"
              name="major"
              label="专业"
              placeholder="请输入专业（必填，最多20字）"
              :rules="[{ required: true, message: '请输入专业' }]"
              maxlength="20"
              show-word-limit
            />
            <van-field
              v-model="formData.email"
              name="email"
              label="邮箱"
              placeholder="请输入邮箱（必填，最多20字）"
              :rules="[{ required: true, message: '请输入邮箱' }, { validator: validateEmail }]"
              maxlength="20"
            />
            <van-field
              v-model="formData.selfIntroduction"
              name="selfIntroduction"
              label="自我介绍"
              type="textarea"
              placeholder="请输入自我介绍（可选，最多200字）"
              rows="4"
              autosize
              maxlength="200"
              show-word-limit
            />
          </van-cell-group>
        </template>

        <!-- 商家角色表单 -->
        <template v-if="userDetail.role === 1">
          <van-cell-group inset>
            <van-field
              v-model="formData.username"
              name="username"
              label="用户名"
              placeholder="请输入用户名（1-10字）"
              :rules="[{ required: true, message: '请输入用户名' }]"
              maxlength="10"
              show-word-limit
            />
            <van-field
              v-model="formData.image"
              name="image"
              label="头像URL"
              placeholder="请输入头像URL地址（可选）"
            />
            <van-field
              v-model="formData.position"
              name="position"
              label="职位"
              placeholder="请输入职位（1-10字）"
              :rules="[{ required: true, message: '请输入职位' }]"
              maxlength="10"
              show-word-limit
            />
            <van-field
              v-model="formData.location"
              name="location"
              label="位置"
              placeholder="请输入位置（1-30字）"
              :rules="[{ required: true, message: '请输入位置' }]"
              maxlength="30"
              show-word-limit
            />
            <van-field
              v-model="formData.company"
              name="company"
              label="公司"
              placeholder="请输入公司名称（1-30字）"
              :rules="[{ required: true, message: '请输入公司名称' }]"
              maxlength="30"
              show-word-limit
            />
          </van-cell-group>
        </template>

        <div class="submit-section">
          <van-button round block type="primary" native-type="submit" :loading="submitting">
            保存并完成
          </van-button>
          <van-button 
            round 
            block 
            type="default" 
            style="margin-top: 12px;"
            @click="handleLogout"
          >
            退出登录
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- 性别选择器 -->
    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker
        :columns="genderColumns"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentUser, updateUserInfo, updateBusinessInfo } from '@/api/user'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const submitting = ref(false)
const showGenderPicker = ref(false)
const userDetail = ref({
  id: null,
  role: null
})

const formData = ref({
  id: null,
  username: '',
  image: '',
  role: null,
  // 学生字段
  age: '',
  gender: null,
  major: '',
  email: '',
  selfIntroduction: '',
  // 商家字段
  position: '',
  location: '',
  company: ''
})

const genderColumns = [
  { text: '男', value: 0 },
  { text: '女', value: 1 }
]

const genderText = computed(() => {
  if (formData.value.gender === 0) return '男'
  if (formData.value.gender === 1) return '女'
  return ''
})

// 邮箱校验
const validateEmail = (value) => {
  if (!value) return '请输入邮箱'
  if (value.length > 20) return '邮箱长度不能超过20字'
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(value)) {
    return '请输入有效的邮箱地址'
  }
  return true
}

// 性别选择确认
const onGenderConfirm = ({ selectedOptions }) => {
  formData.value.gender = selectedOptions[0].value
  showGenderPicker.value = false
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    if (res && res.code === 200 && res.data) {
      userDetail.value = res.data
      // 填充表单数据
      formData.value.id = res.data.id
      formData.value.username = res.data.username || ''
      formData.value.image = res.data.image || ''
      formData.value.role = res.data.role !== undefined && res.data.role !== null ? res.data.role : 0
      
      if (formData.value.role === 0 || !formData.value.role) {
        // 学生字段
        formData.value.age = res.data.age !== undefined && res.data.age !== null ? res.data.age.toString() : ''
        formData.value.gender = res.data.gender !== undefined && res.data.gender !== null ? res.data.gender : null
        formData.value.major = res.data.major || ''
        formData.value.email = res.data.email || ''
        formData.value.selfIntroduction = res.data.selfIntroduction || ''
      } else if (formData.value.role === 1) {
        // 商家字段
        formData.value.position = res.data.position || ''
        formData.value.location = res.data.location || ''
        formData.value.company = res.data.company || ''
      }
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    showFailToast('获取用户信息失败')
  }
}

// 验证表单
const validateForm = () => {
  if (!formData.value.username || formData.value.username.length < 1 || formData.value.username.length > 10) {
    showFailToast('用户名长度应在1-10字之间')
    return false
  }

  const role = formData.value.role !== undefined && formData.value.role !== null ? formData.value.role : 0

  if (role === 0 || !role) {
    // 学生必填字段校验
    if (!formData.value.age) {
      showFailToast('请输入年龄')
      return false
    }
    const age = parseInt(formData.value.age)
    if (isNaN(age) || age < 0 || age > 150) {
      showFailToast('年龄必须在0-150岁之间')
      return false
    }
    if (formData.value.gender === null || formData.value.gender === undefined) {
      showFailToast('请选择性别')
      return false
    }
    if (formData.value.email && formData.value.email.length > 20) {
      showFailToast('邮箱长度不能超过20字')
      return false
    }
    if (formData.value.major && formData.value.major.length > 20) {
      showFailToast('专业长度不能超过20字')
      return false
    }
    if (formData.value.selfIntroduction && formData.value.selfIntroduction.length > 200) {
      showFailToast('自我介绍长度不能超过200字')
      return false
    }
  } else if (role === 1) {
    // 商家必填字段校验
    if (!formData.value.position || formData.value.position.length < 1 || formData.value.position.length > 10) {
      showFailToast('职位长度应在1-10字之间')
      return false
    }
    if (!formData.value.location || formData.value.location.length < 1 || formData.value.location.length > 30) {
      showFailToast('位置长度应在1-30字之间')
      return false
    }
    if (!formData.value.company || formData.value.company.length < 1 || formData.value.company.length > 30) {
      showFailToast('公司名称长度应在1-30字之间')
      return false
    }
  }

  return true
}

// 提交表单
const onSubmit = async () => {
  if (!validateForm()) {
    return
  }

  submitting.value = true
  try {
    const role = formData.value.role !== undefined && formData.value.role !== null ? formData.value.role : 0

    if (role === 1) {
      // 商家角色
      const updateData = {
        id: formData.value.id,
        username: formData.value.username,
        image: formData.value.image || '',
        role: role,
        position: formData.value.position,
        location: formData.value.location,
        company: formData.value.company
      }
      
      const res = await updateBusinessInfo(updateData)
      if (res && res.code === 200) {
        showSuccessToast('信息完善成功')
        // 刷新用户信息
        await userStore.refreshUserDetail()
        router.push('/home')
      }
    } else {
      // 学生角色
      const updateData = {
        id: formData.value.id,
        username: formData.value.username,
        image: formData.value.image || '',
        age: parseInt(formData.value.age),
        gender: formData.value.gender,
        major: formData.value.major || '',
        email: formData.value.email || '',
        selfIntroduction: formData.value.selfIntroduction || ''
      }
      
      const res = await updateUserInfo(updateData)
      if (res && res.code === 200) {
        showSuccessToast('信息完善成功')
        // 刷新用户信息
        await userStore.refreshUserDetail()
        router.push('/home')
      }
    }
  } catch (error) {
    console.error('保存失败', error)
  } finally {
    submitting.value = false
  }
}

// 返回处理
const handleBack = () => {
  showConfirmDialog({
    title: '提示',
    message: '您还未完善信息，无法返回。是否退出登录？'
  })
    .then(() => {
      handleLogout()
    })
    .catch(() => {})
}

// 退出登录
const handleLogout = () => {
  showConfirmDialog({
    title: '确认退出',
    message: '退出登录后需要重新登录，确定要退出吗？'
  })
    .then(() => {
      userStore.logout()
      router.push('/login')
    })
    .catch(() => {})
}

onMounted(() => {
  fetchUserInfo()
})
</script>

<style scoped>
.complete-info-page {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.form-container {
  padding: 20px;
}

.submit-section {
  margin-top: 30px;
  padding: 0 16px 20px;
}
</style>

