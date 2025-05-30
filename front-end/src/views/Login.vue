<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>登入</h2>
      <el-form
        ref="formRef"
        :model="loginForm"
        :rules="rules"
        label-position="top"
      >
        <el-form-item label="帳號" prop="username">
          <el-input v-model="loginForm.username" />
        </el-form-item>
        
        <el-form-item label="密碼" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            show-password
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
          >
            登入
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import { handleError } from '@/utils/errorHandler'

const router = useRouter()
const userStore = useUserStore()

// 響應式表單數據
const loginForm = ref({
  username: '',
  password: ''
})

// 表單驗證規則
const rules = {
  username: [
    { required: true, message: '請輸入帳號', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' }
  ]
}

const loading = ref(false)
const formRef = ref(null)

const handleLogin = async () => {
  try {
    loading.value = true;
    await userStore.login(loginForm.value.username, loginForm.value.password);
    
    // 等待 store 初始化完成
    if (!userStore.isInitialized) {
      await userStore.initialize();
    }
    
    // 檢查登入狀態
    if (!userStore.isLoggedIn) {
      throw new Error('登入狀態未正確初始化');
    }
    
    ElMessage.success('登入成功');
    router.push('/');
  } catch (error) {
    console.error('Login error:', error);
    if (error.response?.status === 401) {
      ElMessage.error('用戶名或密碼錯誤');
    } else if (error.message === '登入狀態未正確初始化') {
      ElMessage.error('登入失敗，請重試');
      userStore.clearUser();  // 清除無效的狀態
    } else {
      ElMessage.error('登入失敗，請稍後重試');
    }
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.login-card {
  width: 100%;
  max-width: 400px;
  padding: 20px;
}

h2 {
  text-align: center;
  margin-bottom: 20px;
}
</style> 