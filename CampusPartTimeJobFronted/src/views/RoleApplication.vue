<template>
  <div class="role-application-page">
    <van-form @submit="onSubmit" @failed="onFailed">
      <van-cell-group inset>
        <van-cell title="当前角色" :value="getRoleText(currentRole)" />
        <van-cell title="申请成为" is-link @click="showRolePicker = true">
          <template #value>
            <span :class="{ 'text-placeholder': !form.role }">
              {{ form.role !== null && form.role !== undefined ? getRoleText(form.role) : '请选择角色' }}
            </span>
          </template>
        </van-cell>
        <van-cell title="选择管理员" is-link @click="showAdminPicker = true">
          <template #value>
            <span :class="{ 'text-placeholder': !form.adminId }">
              {{ selectedAdminName || '请选择管理员' }}
            </span>
          </template>
        </van-cell>
        <van-field
          v-model="form.image"
          name="image"
          label="资质图片"
          placeholder="请输入图片URL"
          clearable
        />
        <van-field
          v-model="form.information"
          name="information"
          type="textarea"
          label="申请信息"
          placeholder="请输入申请信息（最多1000字）"
          rows="5"
          autosize
          maxlength="1000"
          show-word-limit
        />
      </van-cell-group>

      <div class="submit-btn-wrapper">
        <van-button
          round
          block
          type="primary"
          native-type="submit"
          :loading="loading"
          :disabled="loading"
        >
          提交申请
        </van-button>
      </div>
    </van-form>

    <!-- 角色选择器 -->
    <van-popup v-model:show="showRolePicker" position="bottom" round>
      <van-picker
        :columns="roleColumns"
        @confirm="onRoleConfirm"
        @cancel="showRolePicker = false"
      />
    </van-popup>

    <!-- 管理员选择器 -->
    <van-popup v-model:show="showAdminPicker" position="bottom" round>
      <van-picker
        :columns="adminColumns"
        @confirm="onAdminConfirm"
        @cancel="showAdminPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { submitRoleApplication, getAdminList } from '@/api/roleApplication'
import { getCurrentUser } from '@/api/user'
import { showSuccessToast, showFailToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const currentRole = ref(null)
const form = reactive({
  adminId: null,
  role: null,
  information: '',
  image: ''
})

const showRolePicker = ref(false)
const showAdminPicker = ref(false)
const adminList = ref([])
const selectedAdminName = ref('')

const roleColumns = [
  { text: '用户', value: 0 },
  { text: '商家', value: 1 },
  { text: '管理员', value: 2 }
]

const adminColumns = ref([])

// 获取角色文本
const getRoleText = (role) => {
  const roleMap = {
    0: '用户',
    1: '商家',
    2: '管理员'
  }
  return roleMap[role] || '未知'
}

// 获取当前用户信息
const fetchCurrentUser = async () => {
  try {
    const res = await getCurrentUser()
    if (res && res.code === 200 && res.data) {
      currentRole.value = userStore.userInfo?.role || 0
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

// 获取管理员列表
const fetchAdminList = async () => {
  try {
    const res = await getAdminList()
    if (res && res.code === 200 && res.data) {
      adminList.value = res.data
      adminColumns.value = res.data.map(admin => ({
        text: admin.username || `管理员${admin.id}`,
        value: admin.id
      }))
    }
  } catch (error) {
    console.error('获取管理员列表失败', error)
  }
}

// 角色选择确认
const onRoleConfirm = ({ selectedOptions }) => {
  const role = selectedOptions[0].value
  if (role === currentRole.value) {
    showFailToast('申请的身份和现身份不能一致')
    return
  }
  form.role = role
  showRolePicker.value = false
}

// 管理员选择确认
const onAdminConfirm = ({ selectedOptions }) => {
  const adminId = selectedOptions[0].value
  form.adminId = adminId
  const admin = adminList.value.find(a => a.id === adminId)
  selectedAdminName.value = admin ? (admin.username || `管理员${admin.id}`) : ''
  showAdminPicker.value = false
}

// 表单提交
const onSubmit = async () => {
  if (!form.role && form.role !== 0) {
    showFailToast('请选择申请角色')
    return
  }
  if (!form.adminId) {
    showFailToast('请选择管理员')
    return
  }
  if (form.role === currentRole.value) {
    showFailToast('申请的身份和现身份不能一致')
    return
  }
  if (!form.information.trim()) {
    showFailToast('请输入申请信息')
    return
  }

  loading.value = true
  try {
    const res = await submitRoleApplication(form)
    if (res && res.code === 200) {
      showSuccessToast('申请提交成功')
      setTimeout(() => {
        router.back()
      }, 1500)
    }
  } catch (error) {
    console.error('提交申请失败', error)
  } finally {
    loading.value = false
  }
}

// 表单校验失败
const onFailed = (errorInfo) => {
  console.log('表单校验失败', errorInfo)
}

onMounted(() => {
  fetchCurrentUser()
  fetchAdminList()
  if (userStore.userInfo) {
    currentRole.value = userStore.userInfo.role
  }
})
</script>

<style scoped>
.role-application-page {
  min-height: calc(100vh - 46px);
  padding: 20px;
  background-color: #f5f5f5;
}

.submit-btn-wrapper {
  margin: 30px 16px 0;
}

.text-placeholder {
  color: #969799;
}
</style>

