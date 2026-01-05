<template>
  <div class="main-layout">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      :title="currentTitle"
      fixed
      placeholder
      safe-area-inset-top
    />
    
    <!-- 页面内容 -->
    <div class="content">
      <router-view />
    </div>
    
    <!-- 底部Tab栏 -->
    <van-tabbar v-model="activeTab" fixed placeholder safe-area-inset-bottom>
      <van-tabbar-item icon="home-o" to="/home">兼职列表</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">个人中心</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeTab = ref(0)

// 根据路由设置当前标题和激活的tab
const currentTitle = computed(() => {
  return route.meta.title || '校园兼职系统'
})

// 监听路由变化，更新activeTab
watch(() => route.path, (newPath) => {
  if (newPath === '/home') {
    activeTab.value = 0
  } else if (newPath === '/profile') {
    activeTab.value = 1
  }
}, { immediate: true })
</script>

<style scoped>
.main-layout {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.content {
  padding-bottom: 50px;
  min-height: calc(100vh - 46px - 50px);
}
</style>

