import axios from 'axios';
import { ElMessage } from 'element-plus';
import { useUserStore } from '@/stores/user';
import router from '@/router';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  timeout: 15000,
  withCredentials: false
});

// 請求攔截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    console.log('Request Interceptor:', {
      url: config.url,
      method: config.method,
      hasToken: !!token,
      headers: config.headers
    });
    
    // 如果是登入請求，不需要添加 token
    if (config.url.includes('/users/login')) {
      return config;
    }
    
    // 如果是公開的評論列表請求，不需要添加 token
    if (config.url.includes('/reviews/restaurant/') && config.method === 'get') {
      return config;
    }
    
    // 其他所有請求都需要添加 token
    if (token) {
      const tokenValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      config.headers.Authorization = tokenValue;
      console.log('Added token to request:', {
        url: config.url,
        token: tokenValue.substring(0, 20) + '...'
      });
    } else {
      // 如果是需要認證的請求，直接返回錯誤
      if (config.url.includes('/favorite') || 
          config.url.includes('/reviews/create') || 
          config.url.includes('/reviews/update') ||
          config.url.includes('/reviews/delete') ||
          config.url.includes('/users/me') ||
          config.url.includes('/restaurants/new') ||
          config.url.includes('/restaurants/edit')) {
        console.log('Auth required but no token:', {
          url: config.url,
          method: config.method
        });
        return Promise.reject(new Error('請先登入'));
      }
    }
    return config;
  },
  (error) => {
    console.error('Request Interceptor Error:', error);
    return Promise.reject(error);
  }
);

// 響應攔截器
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      
      // 記錄詳細的錯誤信息
      console.error('API Error:', {
        url: error.config.url,
        method: error.config.method,
        status,
        data,
        headers: error.config.headers,
        requestData: error.config.data // 添加請求數據
      });

      // 如果是評論列表請求，直接返回空數據
      if (error.config.url.includes('/reviews/restaurant/') && error.config.method === 'get') {
        return Promise.resolve({
          data: {
            content: [],
            totalElements: 0,
            currentPage: 0,
            size: 10,
            starDistribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
          }
        });
      }

      // 如果是熱門餐廳或最新餐廳請求，返回空數組
      if ((error.config.url.includes('/restaurants/popular') || 
           error.config.url.includes('/restaurants/latest')) && 
          error.config.method === 'get') {
        console.warn('Failed to fetch restaurants, returning empty array:', {
          url: error.config.url,
          error: data
        });
        return Promise.resolve({
          data: []
        });
      }
      
      switch (status) {
        case 400:
          // 顯示具體的錯誤訊息
          if (data && data.message) {
            ElMessage.error(data.message);
          } else if (data && data.error) {
            ElMessage.error(data.error);
          } else {
            ElMessage.error('請求格式錯誤，請檢查輸入');
          }
          return Promise.reject(error);
          
        case 401:
          const userStore = useUserStore();
          // 只有在非登入頁面且需要認證的請求時才清除用戶狀態
          if (!window.location.pathname.includes('/login') && 
              (error.config.url.includes('/favorite') || 
               error.config.url.includes('/reviews/create') || 
               error.config.url.includes('/reviews/update') ||
               error.config.url.includes('/reviews/delete'))) {
            // 避免重複清除用戶狀態
            if (userStore.isLoggedIn) {
              userStore.clearUser();
              localStorage.removeItem('token');
              router.push('/login');
            }
          }
          return Promise.reject(error);
          
        case 403:
          ElMessage.error('權限不足');
          return Promise.reject(error);
          
        case 500:
          console.error('Server Error:', {
            url: error.config.url,
            data: data,
            message: data?.message || 'Internal Server Error'
          });
          if (data?.error === 'Access Denied') {
            const userStore = useUserStore();
            return Promise.resolve({ data: false });
          }
          return Promise.resolve({ data: [] });
          
        default:
          ElMessage.error('操作失敗，請稍後再試');
          return Promise.reject(error);
      }
    }
    
    return Promise.reject(error);
  }
);

// 用戶相關 API
const userApi = {
  // 用戶註冊
  register: (userData) => api.post('/users/register', userData),
  
  // 用戶登入
  login: (credentials) => {
    return api.post('/users/login', credentials)
      .then(response => {
        if (response.data) {
          const token = response.data;
          // 確保 token 格式正確
          const tokenValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
          // 保存到 localStorage
          localStorage.setItem('token', tokenValue);
          // 設置到 axios 實例
          api.defaults.headers.common['Authorization'] = tokenValue;
        }
        return response;
      });
  },
  
  // 獲取當前用戶信息
  getCurrentUser: () => api.get('/users/me'),
  
  // 更新用戶資料
  updateProfile: (userData) => api.put('/users/me', userData),
  
  // 獲取用戶收藏的餐廳
  getFavorites: (userId) => api.get(`/users/${userId}/favorites`),
  
  // 獲取用戶的評論
  getUserReviews: (userId) => {
    return api.get(`/users/${userId}/reviews`).catch(error => {
      // 如果是 DTO 轉換錯誤，返回空數組
      if (error.response?.status === 500 && 
          error.response?.data?.error?.includes('Cannot instantiate class')) {
        return Promise.resolve({ data: [] });
      }
      throw error;
    });
  },
  
  // 獲取用戶收藏的餐廳列表
  getUserFavorites: (userId) => api.get(`/users/${userId}/favorites`),

  // 獲取用戶創建的餐廳
  getCreatedRestaurants: (userId) => api.get(`/users/${userId}/restaurants`),

  // 更新用戶信息
  updateUser: (userData) => api.put('/users/me', userData),

  // 添加收藏
  addFavorite: (restaurantId) => api.post(`/restaurants/${restaurantId}/favorite`),
  
  // 移除收藏
  removeFavorite: (restaurantId) => api.delete(`/restaurants/${restaurantId}/favorite`),
};

// 餐廳相關 API
const restaurantApi = {
  // 獲取餐廳列表
  getRestaurants: (params) => {
    return api.get('/restaurants', { params })
      .catch(error => {
        console.error('Error fetching restaurants:', error);
        // 返回空數據
        return Promise.resolve({
          data: {
            content: [],
            totalElements: 0,
            currentPage: 0,
            size: 10
          }
        });
      });
  },
  
  // 獲取熱門餐廳
  getPopular: () => {
    return api.get('/restaurants/popular')
      .then(response => {
        // 確保返回的數據是數組
        if (!Array.isArray(response.data)) {
          console.warn('Popular restaurants data is not an array:', response.data);
          return { data: [] };
        }
        return response;
      })
      .catch(error => {
        console.error('Error fetching popular restaurants:', error);
        // 直接返回空數組，不拋出錯誤
        return { data: [] };
      });
  },
  
  // 獲取最新餐廳
  getLatest: () => {
    return api.get('/restaurants/latest')
      .then(response => {
        // 確保返回的數據是數組
        if (!Array.isArray(response.data)) {
          console.warn('Latest restaurants data is not an array:', response.data);
          return { data: [] };
        }
        return response;
      })
      .catch(error => {
        console.error('Error fetching latest restaurants:', error);
        // 直接返回空數組，不拋出錯誤
        return { data: [] };
      });
  },
  
  // 獲取餐廳詳情
  getRestaurantById: (id) => api.get(`/restaurants/${id}`),
  
  // 檢查是否已收藏
  checkFavorite: async (restaurantId) => {
    try {
      const token = localStorage.getItem('token');
      if (!token) {
        return Promise.resolve({ data: false });
      }

      // 確保 token 格式正確
      const tokenValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
      
      const response = await api.post(`/restaurants/${restaurantId}/favorite/check`, {}, {
        headers: {
          'Authorization': tokenValue
        }
      });
      return response;
    } catch (error) {
      return Promise.resolve({ data: false });
    }
  },
  
  // 創建餐廳
  create: (restaurantData) => {
    const token = localStorage.getItem('token');
    if (!token) {
      return Promise.reject(new Error('請先登入'));
    }
    const tokenValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    const headers = { 'Authorization': tokenValue };
    return api.post('/restaurants', restaurantData, { headers });
  },
  
  // 更新餐廳
  updateRestaurant: (id, restaurantData) => {
    const token = localStorage.getItem('token');
    if (!token) {
      return Promise.reject(new Error('請先登入'));
    }
    const tokenValue = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    const headers = { 'Authorization': tokenValue };
    return api.put(`/restaurants/${id}`, restaurantData, { headers });
  },
  
  // 刪除餐廳
  deleteRestaurant: (id) => api.delete(`/restaurants/${id}`),
  
  // 獲取餐廳評論
  getReviews: (restaurantId, page = 0, size = 10) => 
    api.get(`/reviews/restaurant/${restaurantId}`, { params: { page, size } }),
  
  // 創建評論
  createReview: (restaurantId, formData) => {
    const token = localStorage.getItem('token');
    const headers = {
      'Authorization': token ? `Bearer ${token}` : '',
      'Content-Type': 'multipart/form-data',
      'Accept': 'application/json'
    };
    return api.post(`/reviews/${restaurantId}`, formData, { headers });
  },
  
  // 刪除評論
  deleteReview: (reviewId) => api.delete(`/reviews/${reviewId}`),
  
  // 獲取收藏列表
  getFavorites: () => api.get('/restaurants/favorites'),
  
  // 添加收藏
  addFavorite: (restaurantId) => api.post(`/restaurants/${restaurantId}/favorite`),
  
  // 移除收藏
  removeFavorite: (restaurantId) => api.delete(`/restaurants/${restaurantId}/favorite`),
};

// 評論相關 API
const reviewApi = {
  // 獲取餐廳評論
  getRestaurantReviews: (restaurantId, params) => {
    const token = localStorage.getItem('token');
    const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
    return api.get(`/reviews/restaurant/${restaurantId}/page`, { 
      params,
      headers 
    });
  },
  
  // 創建評論
  createReview: (restaurantId, reviewData) => {
    const token = localStorage.getItem('token');
    const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
    return api.post(`/reviews/${restaurantId}`, reviewData, { headers });
  },
  
  // 更新評論
  updateReview: (reviewId, reviewData) => {
    const token = localStorage.getItem('token');
    if (!token) {
      return Promise.reject(new Error('請先登入'));
    }
    
    const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
    return api.put(`/reviews/${reviewId}`, reviewData, { headers });
  },
  
  // 刪除評論
  deleteReview: (reviewId) => {
    const token = localStorage.getItem('token');
    const headers = token ? { 'Authorization': `Bearer ${token}` } : {};
    return api.delete(`/reviews/${reviewId}`, { headers });
  },
  
  // 點讚評論
  likeReview: (reviewId) => 
    api.post(`/reviews/${reviewId}/like`),
  
  // 取消點讚
  unlikeReview: (reviewId) => 
    api.delete(`/reviews/${reviewId}/like`),
};

export { userApi, restaurantApi, reviewApi };