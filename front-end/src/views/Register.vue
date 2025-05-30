<template>
  <div class="register-container">
    <div class="register-box">
      <h2>註冊</h2>
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        label-position="top"
      >
        <el-form-item label="用戶名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="請輸入用戶名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="電子郵件" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="請輸入電子郵件"
            :prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item label="密碼" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="請輸入密碼"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="確認密碼" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="請再次輸入密碼"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <div class="form-footer">
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            class="register-button"
          >
            註冊
          </el-button>
          <div class="login-link">
            已有帳號？
            <router-link to="/login">立即登入</router-link>
          </div>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { User, Lock, Message } from '@element-plus/icons-vue';
import { userApi } from '@/services/api';

const router = useRouter();
const registerFormRef = ref(null);
const loading = ref(false);

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
});

const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('請輸入密碼'));
  } else {
    if (registerForm.value.confirmPassword !== '') {
      registerFormRef.value?.validateField('confirmPassword');
    }
    callback();
  }
};

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('請再次輸入密碼'));
  } else if (value !== registerForm.value.password) {
    callback(new Error('兩次輸入密碼不一致'));
  } else {
    callback();
  }
};

const registerRules = {
  username: [
    { required: true, message: '請輸入用戶名', trigger: 'blur' },
    { min: 3, message: '用戶名至少需要 3 個字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '請輸入電子郵件', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的電子郵件格式', trigger: 'blur' }
  ],
  password: [
    { required: true, validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
};

const handleRegister = async () => {
  if (!registerFormRef.value) return;
  
  try {
    await registerFormRef.value.validate();
    loading.value = true;
    
    await userApi.register({
      username: registerForm.value.username,
      email: registerForm.value.email,
      password: registerForm.value.password
    });
    
    ElMessage.success('註冊成功');
    router.push('/login');
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '註冊失敗');
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
}

.register-box {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.register-box h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.form-footer {
  margin-top: 20px;
}

.register-button {
  width: 100%;
  margin-bottom: 15px;
}

.login-link {
  text-align: center;
  color: #606266;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
}

.login-link a:hover {
  text-decoration: underline;
}

@media (max-width: 768px) {
  .register-box {
    margin: 20px;
    padding: 20px;
  }
}
</style> 