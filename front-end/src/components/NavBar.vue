<template>
  <div class="navbar">
    <el-menu
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      router
    >
      <div class="left-menu">
        <el-menu-item index="/">首頁</el-menu-item>
        <el-menu-item index="/restaurants">餐廳列表</el-menu-item>
      </div>

      <div class="right-menu">
        <template v-if="!userStore.isInitialized">
          <el-skeleton :rows="1" animated />
        </template>
        <template v-else-if="!userStore.isLoggedIn">
          <el-button type="primary" @click="$router.push('/login')">登入</el-button>
          <el-button @click="$router.push('/register')">註冊</el-button>
        </template>
        <template v-else>
          <template v-if="userStore.user && !userStore.isAdmin">
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              個人資料
            </el-menu-item>
          </template>
          <el-menu-item @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            登出
          </el-menu-item>
        </template>
      </div>
    </el-menu>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import { User, SwitchButton } from '@element-plus/icons-vue';

const router = useRouter();
const userStore = useUserStore();
const activeIndex = ref('/');

onMounted(async () => {
  if (!userStore.isInitialized) {
    await userStore.initialize();
  }
});

const handleLogout = () => {
  userStore.logout();
  ElMessage.success('已登出');
  router.push('/login');
};
</script>

<style scoped>
.navbar {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.el-menu-demo {
  display: flex;
  justify-content: space-between;
  padding: 0 20px;
}

.left-menu {
  display: flex;
}

.right-menu {
  display: flex;
  align-items: center;
  gap: 10px;
}

.right-menu .el-button {
  margin-left: 10px;
  height: 32px;
  line-height: 32px;
  padding: 0 15px;
  transition: all 0.3s ease;
}

.right-menu .el-button:hover {
  transform: translateY(-1px);
}
</style> 