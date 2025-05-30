import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import zhTw from 'element-plus/dist/locale/zh-tw.mjs';
import App from './App.vue';
import router from './router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';

// 設置 Element Plus 的繁體中文
const elementPlusConfig = {
  locale: zhTw,
  messages: {
    'zh-tw': {
      el: {
        pagination: {
          goto: '前往',
          pagesize: '項/頁',
          total: '共 {total} 項',
          pageClassifier: '頁'
        }
      }
    }
  }
};

// 添加全局錯誤處理
window.onerror = function(message, source, lineno, colno, error) {
  console.error('全局錯誤:', { message, source, lineno, colno, error });
  return true;
};

// 創建 axios 實例
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 請求攔截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('Request error:', error);
    return Promise.reject(error);
  }
);

// 響應攔截器
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          localStorage.removeItem('token');
          router.push('/login');
          ElMessage.error('請重新登入');
          break;
        case 403:
          ElMessage.error('權限不足');
          break;
        case 404:
          ElMessage.error('請求的資源不存在');
          break;
        case 500:
          ElMessage.error('服務器錯誤');
          break;
        default:
          ElMessage.error('發生錯誤');
      }
    }
    return Promise.reject(error);
  }
);

const app = createApp(App);
const pinia = createPinia();

// 全局配置
app.config.globalProperties.$http = api;

// 使用插件
app.use(pinia);
app.use(router);
app.use(ElementPlus, elementPlusConfig);

// 全域註冊所有 Element Plus 圖標
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

// 掛載應用
app.mount('#app'); 