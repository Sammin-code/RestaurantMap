import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { userApi } from '@/services/api';
import { restaurantApi } from '@/services/api';
import router from '@/router';

export const useUserStore = defineStore('user', {
  state: () => ({
    user: null,
    token: localStorage.getItem('token'),
    isInitialized: false,
    isLoading: true,
    favorites: [],
    favoriteStatusCache: new Map()
  }),

  getters: {
    isLoggedIn: (state) => {
      const token = localStorage.getItem('token');
      console.log('Checking isLoggedIn:', {
        hasToken: !!token,
        hasUser: !!state.user,
        role: state.user?.role
      });
      return !!token && !!state.user;  // 同時檢查 token 和 user
    },
    
    isAdmin: (state) => {
      if (!state.isInitialized) {
        return false;
      }
      console.log('Checking isAdmin:', {
        user: state.user,
        role: state.user?.role,
        isAdmin: state.user?.role === 'ROLE_ADMIN'
      });
      return state.user?.role === 'ROLE_ADMIN';
    },
    
    hasRole: (state) => (role) => {
      if (!state.isInitialized) {
        return false;
      }
      return state.user?.role === role;
    },

    isFavorite: (state) => (restaurantId) => {
      const favorites = Array.isArray(state.favorites) ? state.favorites : [];
      return favorites.some(fav => fav.id === restaurantId);
    }
  },

  actions: {
    async login(username, password) {
      try {
        const response = await userApi.login({ username, password });
        const token = response.data;
        
        // 解析 token 獲取用戶信息
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        const user = {
          id: tokenPayload.userId,
          username: tokenPayload.username,
          role: tokenPayload.role.startsWith('ROLE_') ? tokenPayload.role : `ROLE_${tokenPayload.role}`  // 檢查是否已有 ROLE_ 前綴
        };
        console.log('[user.js] 登入時解析到的 user:', user);

        // 設置 token 和用戶信息
        localStorage.setItem('token', token);
        this.token = token;
        this.user = user;
        this.isInitialized = true;
        this.isLoading = false;
        
        return response;
      } catch (error) {
        this.clearUser();
        throw error;
      }
    },

    async logout() {
      this.token = null;
      this.user = null;
      this.favorites = [];
      this.isLoading = false;
      localStorage.removeItem('token');
      ElMessage.success('已登出');
    },

    async initialize() {
      console.log('Initializing user store...');
      if (this.isInitialized) {
        console.log('User store already initialized');
        return;
      }
      
      this.isLoading = true;
      
      try {
        const token = localStorage.getItem('token');
        console.log('Token found:', !!token);
        
        if (!token) {
          console.log('No token found, setting initialized state');
          this.clearUser();
          this.isInitialized = true;  // 即使沒有 token 也要設置為已初始化
          this.isLoading = false;
          return;
        }

        // 解析 token 獲取用戶信息
        const tokenPayload = JSON.parse(atob(token.split('.')[1]));
        const user = {
          id: tokenPayload.userId,
          username: tokenPayload.username,
          role: tokenPayload.role.startsWith('ROLE_') ? tokenPayload.role : `ROLE_${tokenPayload.role}`
        };
        console.log('[user.js] initialize 解析到的 user:', user);

        this.user = user;
        this.token = token;
        this.isInitialized = true;
        this.isLoading = false;
        
        console.log('User store initialized:', {
          user: this.user,
          isLoggedIn: this.isLoggedIn,
          role: this.user?.role
        });
      } catch (error) {
        console.error('Failed to initialize user store:', error);
        this.clearUser();
        this.isInitialized = true;  // 即使出錯也要設置為已初始化
        this.isLoading = false;
        if (error.response?.status === 403) {
          ElMessage.warning('登入已過期，請重新登入');
          router.push('/login');
        }
      }
    },

    async updateProfile(userData) {
      try {
        const response = await userApi.updateProfile(userData);
        this.user = response.data;
        ElMessage.success('個人資料更新成功');
        return response.data;
      } catch (error) {
        throw error;
      }
    },

    async checkLogin() {
      const token = localStorage.getItem('token');
      console.log('Check Login:', {
        hasToken: !!token,
        user: this.user,
        isLoggedIn: this.isLoggedIn,
        role: this.user?.role
      });
      
      if (!token) {
        ElMessage.warning('請先登入');
        router.push('/login');
        return false;
      }
      
      // 檢查用戶信息是否存在
      if (!this.user) {
        console.log('User info not found, trying to fetch...');
        // 嘗試重新獲取用戶信息
        try {
          const response = await userApi.getCurrentUser();
          this.user = response.data;
        } catch (error) {
          console.error('Failed to fetch user info:', error);
          this.clearUser();
          ElMessage.warning('請重新登入');
          router.push('/login');
          return false;
        }
      }
      
      // 檢查用戶角色
      if (!this.user.role) {
        console.log('User role not found');
        this.clearUser();
        ElMessage.warning('請重新登入');
        router.push('/login');
        return false;
      }
      
      return true;
    },

    clearUser() {
      this.user = null;
      this.token = null;
      this.favorites = [];
      this.isInitialized = false;
      localStorage.removeItem('token');  // 清除 localStorage 中的 token
    },

    // 獲取收藏列表
    async fetchFavorites() {
      const token = localStorage.getItem('token');
      if (!token) {
        this.favorites = [];
        return [];
      }
      try {
        const response = await restaurantApi.getFavorites();
        if (!Array.isArray(response.data)) {
          console.warn('Favorites response is not an array:', response.data);
          this.favorites = [];
          return [];
        }
        this.favorites = response.data;
        return response.data;
      } catch (error) {
        console.error('Failed to fetch favorites:', error);
        // 如果是 500 錯誤，可能是後端問題，不要清除收藏列表
        if (error.response?.status === 500) {
          console.warn('Server error while fetching favorites, keeping existing list');
          return this.favorites;
        }
        // 其他錯誤才清空列表
        this.favorites = [];
        return [];
      }
    },

    // 添加收藏
    async addFavorite(restaurantId) {
      const token = localStorage.getItem('token');
      if (!token) {
        console.log('No token found for addFavorite');
        return;
      }
      
      try {
        console.log('Adding favorite for restaurant:', restaurantId);
        await restaurantApi.addFavorite(restaurantId);
        // 不立即重新獲取列表，而是直接添加到本地列表
        if (!this.favorites.some(fav => fav.id === restaurantId)) {
          this.favorites.push({ id: restaurantId });
        }
        console.log('Favorite added successfully');
      } catch (error) {
        console.error('Failed to add favorite:', error);
        throw error;
      }
    },

    // 取消收藏
    async removeFavorite(restaurantId) {
      const token = localStorage.getItem('token');
      if (!token) {
        console.log('No token found for removeFavorite');
        return;
      }
      
      try {
        console.log('Removing favorite for restaurant:', restaurantId);
        await restaurantApi.removeFavorite(restaurantId);
        // 不立即重新獲取列表，而是直接從本地列表移除
        this.favorites = this.favorites.filter(fav => fav.id !== restaurantId);
        console.log('Favorite removed successfully');
      } catch (error) {
        console.error('Failed to remove favorite:', error);
        throw error;
      }
    },

    async checkFavoriteStatus(restaurantId) {
      const token = localStorage.getItem('token');
      if (!token) {
        console.log('No token found for checkFavoriteStatus');
        return false;
      }
      
      try {
        console.log('Checking favorite status for restaurant:', restaurantId);
        const response = await restaurantApi.getFavorites();
        const favorites = Array.isArray(response.data) ? response.data : [];
        const isFavorite = favorites.some(fav => fav.id === restaurantId);
        console.log('Favorite status:', isFavorite);
        return isFavorite;
      } catch (error) {
        console.error('Failed to check favorite status:', error);
        throw error;
      }
    }
  }
});