<template>
  <div class="profile-page">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <van-skeleton title avatar :row="4" />
    </div>

    <template v-else>
      <!-- 用户信息卡片 -->
      <div class="user-card">
        <van-cell-group inset>
          <van-cell>
            <template #title>
              <div class="user-avatar" @click="editField('image')">
                <van-image
                  v-if="userDetail.image"
                  :src="userDetail.image"
                  round
                  width="70"
                  height="70"
                  fit="cover"
                />
                <van-icon
                  v-else
                  name="user-circle-o"
                  size="70"
                  color="#1989fa"
                />
                <div class="edit-tip">点击编辑头像</div>
              </div>
            </template>
          </van-cell>
          <van-cell title="用户名" :value="userDetail.username || '未设置'" is-link @click="editField('username')" />
          
          <!-- 学生角色信息 -->
          <template v-if="userDetail.role === 0">
            <van-cell title="年龄" :value="userDetail.age ? userDetail.age + '岁' : '未设置'" is-link @click="editField('age')" />
            <van-cell title="性别" :value="getGenderText(userDetail.gender)" is-link @click="showGenderPicker = true" />
            <van-cell title="专业" :value="userDetail.major || '未设置'" is-link @click="editField('major')" />
            <van-cell title="邮箱" :value="userDetail.email || '未设置'" is-link @click="editField('email')" />
            <van-cell title="自我介绍" :value="userDetail.selfIntroduction || '未设置'" is-link @click="editField('selfIntroduction')" />
          </template>
          
          <!-- 商家角色信息 -->
          <template v-if="userDetail.role === 1">
            <van-cell title="职位" :value="userDetail.position || '未设置'" is-link @click="editField('position')" />
            <van-cell title="位置" :value="userDetail.location || '未设置'" is-link @click="editField('location')" />
            <van-cell title="公司" :value="userDetail.company || '未设置'" is-link @click="editField('company')" />
          </template>
        </van-cell-group>
      </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <van-cell-group inset>
        <van-cell
          v-if="userDetail.role === 0"
          title="角色申请"
          is-link
          icon="user-o"
          @click="goToRoleApplication"
        />
            <van-cell
              v-if="userDetail.role === 0"
              title="我的申请"
              is-link
              icon="orders-o"
              @click="goToMyApplication"
            />
            <van-cell
              v-if="userDetail.role === 0"
              title="我的兼职申请"
              is-link
              icon="briefcase-o"
              @click="goToMyJobApplication"
            />
        <van-cell
          v-if="userDetail.role === 1"
          title="发布兼职"
          is-link
          icon="plus"
          @click="goToCreateRecruitment"
        />
            <van-cell
              v-if="userDetail.role === 1"
              title="我的招聘"
              is-link
              icon="orders-o"
              @click="goToMyRecruitment"
            />
            <van-cell
              v-if="userDetail.role === 1"
              title="处理申请"
              is-link
              icon="orders-o"
              @click="goToJobApplicationList"
            />
            <van-cell
              v-if="userStore.userInfo?.role === 2"
              title="申请列表"
              is-link
              icon="orders-o"
              @click="goToApplicationList"
            />
        <van-cell title="聊天" is-link icon="chat-o" @click="goToChatList" />
        <van-cell title="我的收藏" is-link icon="star-o" />
        <van-cell title="设置" is-link icon="setting-o" />
      </van-cell-group>
    </div>

      <!-- 退出登录 -->
      <div class="actions">
        <van-button
          round
          block
          type="danger"
          @click="handleLogout"
          style="margin: 20px 16px;"
        >
          退出登录
        </van-button>
      </div>
    </template>

    <!-- 编辑弹窗 -->
    <van-popup
      v-model:show="showEditPopup"
      position="bottom"
      round
      :style="{ minHeight: '30%' }"
    >
      <div class="edit-popup">
        <div class="popup-header">
          <span class="cancel" @click="showEditPopup = false">取消</span>
          <span class="title">{{ editTitle }}</span>
          <span class="confirm" @click="confirmEdit">确定</span>
        </div>
        <div class="popup-content">
          <van-field
            v-if="editFieldName === 'selfIntroduction'"
            v-model="editValue"
            type="textarea"
            :placeholder="'请输入' + editTitle"
            rows="4"
            autosize
            maxlength="200"
            show-word-limit
          />
          <van-field
            v-else-if="editFieldName === 'age'"
            v-model="editValue"
            type="digit"
            :placeholder="'请输入' + editTitle"
          />
          <van-field
            v-else-if="editFieldName === 'image'"
            v-model="editValue"
            :placeholder="'请输入头像URL地址'"
            label="头像URL"
          />
          <van-field
            v-else
            v-model="editValue"
            :placeholder="'请输入' + editTitle"
          />
        </div>
      </div>
    </van-popup>

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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getCurrentUser, updateUserInfo, updateBusinessInfo } from '@/api/user'
import { showConfirmDialog, showSuccessToast, showFailToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const userDetail = ref({
  id: null,
  username: '',
  image: '',
  role: null,
  // 学生字段
  age: null,
  gender: null,
  major: '',
  email: '',
  selfIntroduction: '',
  // 商家字段
  position: '',
  location: '',
  company: ''
})

// 编辑相关
const showEditPopup = ref(false)
const editFieldName = ref('')
const editTitle = ref('')
const editValue = ref('')

// 性别选择器
const showGenderPicker = ref(false)
const genderColumns = [
  { text: '男', value: 0 },
  { text: '女', value: 1 }
]

// 字段名称映射
const fieldTitleMap = {
  image: '头像',
  username: '用户名',
  age: '年龄',
  major: '专业',
  email: '邮箱',
  selfIntroduction: '自我介绍',
  position: '职位',
  location: '位置',
  company: '公司'
}

// 获取性别文本
const getGenderText = (gender) => {
  if (gender === 0) return '男'
  if (gender === 1) return '女'
  return '未设置'
}

// 获取用户详细信息
const fetchUserDetail = async () => {
  loading.value = true
  try {
    const res = await getCurrentUser()
    if (res && res.code === 200 && res.data) {
      userDetail.value = res.data
    } else {
      showFailToast('获取用户信息失败')
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
    if (error.response && error.response.status === 401) {
      userStore.logout()
      router.push('/login')
    }
  } finally {
    loading.value = false
  }
}

// 编辑字段
const editField = (fieldName) => {
  editFieldName.value = fieldName
  editTitle.value = fieldTitleMap[fieldName]
  editValue.value = userDetail.value[fieldName] || ''
  showEditPopup.value = true
}

// 确认编辑
const confirmEdit = async () => {
  if (!editValue.value && editFieldName.value !== 'selfIntroduction' && editFieldName.value !== 'image') {
    showFailToast('请输入内容')
    return
  }

  // 年龄校验
  if (editFieldName.value === 'age') {
    const age = parseInt(editValue.value)
    if (isNaN(age) || age < 1 || age > 150) {
      showFailToast('请输入有效年龄')
      return
    }
  }

  // 邮箱校验
  if (editFieldName.value === 'email' && editValue.value) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(editValue.value)) {
      showFailToast('请输入有效的邮箱地址')
      return
    }
  }

  try {
    // 根据角色选择不同的更新接口
    if (userDetail.value.role === 1) {
      // 商家角色
      const updateData = {
        id: userDetail.value.id,
        username: userDetail.value.username || '',
        image: userDetail.value.image || '',
        role: userDetail.value.role,
        position: userDetail.value.position || '',
        location: userDetail.value.location || '',
        company: userDetail.value.company || ''
      }
      
      updateData[editFieldName.value] = editValue.value || ''
      
      const res = await updateBusinessInfo(updateData)
      if (res && res.code === 200) {
        userDetail.value[editFieldName.value] = editValue.value || ''
        showSuccessToast('修改成功')
        showEditPopup.value = false
        await fetchUserDetail()
      }
    } else {
      // 学生角色
      const updateData = {
        id: userDetail.value.id,
        username: userDetail.value.username || '',
        image: userDetail.value.image || '',
        age: userDetail.value.age,
        gender: userDetail.value.gender,
        major: userDetail.value.major || '',
        email: userDetail.value.email || '',
        selfIntroduction: userDetail.value.selfIntroduction || ''
      }
      
      if (editFieldName.value === 'age') {
        updateData.age = editValue.value ? parseInt(editValue.value) : null
      } else {
        updateData[editFieldName.value] = editValue.value || ''
      }
      
      const res = await updateUserInfo(updateData)
      if (res && res.code === 200) {
        if (editFieldName.value === 'age') {
          userDetail.value.age = editValue.value ? parseInt(editValue.value) : null
        } else {
          userDetail.value[editFieldName.value] = editValue.value || ''
        }
        showSuccessToast('修改成功')
        showEditPopup.value = false
        await fetchUserDetail()
      }
    }
  } catch (error) {
    console.error('修改失败', error)
    if (error.response && error.response.status === 401) {
      userStore.logout()
      router.push('/login')
    }
  }
}

// 性别选择确认
const onGenderConfirm = async ({ selectedOptions }) => {
  const gender = selectedOptions[0].value
  try {
    const updateData = {
      id: userDetail.value.id,
      username: userDetail.value.username || '',
      image: userDetail.value.image || '',
      age: userDetail.value.age,
      gender: gender,
      major: userDetail.value.major || '',
      email: userDetail.value.email || '',
      selfIntroduction: userDetail.value.selfIntroduction || ''
    }
    
    const res = await updateUserInfo(updateData)
    if (res && res.code === 200) {
      userDetail.value.gender = gender
      showSuccessToast('修改成功')
      await fetchUserDetail()
    }
  } catch (error) {
    console.error('修改失败', error)
    if (error.response && error.response.status === 401) {
      userStore.logout()
      router.push('/login')
    }
  }
  showGenderPicker.value = false
}

// 跳转到发布兼职
const goToCreateRecruitment = () => {
  router.push('/create-recruitment')
}

// 跳转到我的招聘
const goToMyRecruitment = () => {
  router.push('/my-recruitment')
}

// 跳转到角色申请
const goToRoleApplication = () => {
  router.push('/role-application')
}

// 跳转到我的申请
const goToMyApplication = () => {
  router.push('/my-application')
}

// 跳转到我的兼职申请
const goToMyJobApplication = () => {
  router.push('/my-job-application')
}

// 跳转到申请列表
const goToApplicationList = () => {
  router.push('/application-list')
}

// 跳转到处理申请（商家）
const goToJobApplicationList = () => {
  router.push('/job-application-list')
}

// 跳转到聊天列表
const goToChatList = () => {
  router.push('/chat-list')
}

// 退出登录
const handleLogout = () => {
  showConfirmDialog({
    title: '提示',
    message: '确定要退出登录吗？'
  })
    .then(() => {
      userStore.logout()
      router.push('/login')
    })
    .catch(() => {})
}

onMounted(() => {
  fetchUserDetail()
})
</script>

<style scoped>
.profile-page {
  min-height: calc(100vh - 46px - 50px);
  padding: 20px 0;
  background-color: #f5f5f5;
}

.loading-container {
  padding: 20px;
}

.user-card {
  margin-bottom: 12px;
}

.user-avatar {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  cursor: pointer;
}

.edit-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.menu-section {
  margin-bottom: 20px;
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

.popup-content {
  padding-top: 16px;
}
</style>
