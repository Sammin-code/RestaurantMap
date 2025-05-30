import { ElMessage } from 'element-plus';
import router from '@/router';

// 統一的錯誤訊息
const ERROR_MESSAGES = {
  NETWORK_ERROR: '網路連線失敗，請檢查網路設定',
  TIMEOUT_ERROR: '請求超時，請稍後再試',
  SERVER_ERROR: '伺服器發生錯誤，請稍後再試',
  VALIDATION_ERROR: '請檢查輸入的資料是否正確',
  AUTH_REQUIRED: '請先登入後再繼續操作',
  PERMISSION_DENIED: '您沒有權限執行此操作',
  RESOURCE_NOT_FOUND: '找不到請求的資源',
  CONFLICT: '此操作與現有資料衝突',
  VALIDATION_FAILED: '資料驗證失敗',
  UNKNOWN_ERROR: '發生錯誤，請稍後再試'
};

export const handleError = (error) => {
  console.error('API Error:', error);
  
  if (error.response) {
    switch (error.response.status) {
      case 401:
        ElMessage.error('請先登入');
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
        ElMessage.error(error.response.data?.message || '發生錯誤');
    }
  } else {
    ElMessage.error('網絡錯誤，請稍後再試');
  }
};

// 導出錯誤訊息常量，方便其他組件使用
export { ERROR_MESSAGES };
