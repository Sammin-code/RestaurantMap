import { defineStore } from 'pinia';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from './user';
import { useRouter } from 'vue-router';
import { restaurantApi } from '../services/api';

export const useRestaurantStore = defineStore('restaurant', () => {
  const router = useRouter();
  const userStore = useUserStore();
  
  const restaurants = ref([]);
  const popularRestaurants = ref([]);
  const latestRestaurants = ref([]);
  const currentRestaurant = ref(null);
  const loading = ref(false);
  const error = ref(null);
  const total = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);
  const filters = ref({
    search: '',
    tags: [],
    minRating: 0,
    sortBy: 'rating',
    sortOrder: 'desc'
  });
  const isLoadingPopular = ref(false);
  const isLoadingLatest = ref(false);
  const errorPopular = ref(null);
  const errorLatest = ref(null);
  const favorites = ref([]);

  const fetchRestaurants = async (params = {}) => {
    loading.value = true;
    error.value = null;
    
    try {
      const { data } = await restaurantApi.getAll({
        page: params.page || currentPage.value - 1,
        size: params.size || pageSize.value,
        ...params
      });
      restaurants.value = data.content;
      total.value = data.totalElements;
      return data;
    } catch (err) {
      error.value = err.message;
      ElMessage.error('獲取餐廳列表失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const fetchPopularRestaurants = async () => {
    isLoadingPopular.value = true;
    errorPopular.value = null;
    
    try {
      const { data } = await restaurantApi.getPopular();
      
      // 處理圖片 URL
      popularRestaurants.value = data.map(restaurant => ({
        ...restaurant,
        imageUrl: restaurant.imageUrl || restaurant.image_url || ''  // 處理不同的字段名稱
      }));
      
      return data;
    } catch (err) {
      errorPopular.value = err.message;
      ElMessage.error('獲取熱門餐廳失敗');
      throw err;
    } finally {
      isLoadingPopular.value = false;
    }
  };

  const fetchLatestRestaurants = async () => {
    isLoadingLatest.value = true;
    errorLatest.value = null;
    
    try {
      const { data } = await restaurantApi.getLatest();
      
      // 處理圖片 URL
      latestRestaurants.value = data.map(restaurant => ({
        ...restaurant,
        imageUrl: restaurant.imageUrl || restaurant.image_url || ''  // 處理不同的字段名稱
      }));
      
      return data;
    } catch (err) {
      errorLatest.value = err.message;
      ElMessage.error('獲取最新餐廳失敗');
      throw err;
    } finally {
      isLoadingLatest.value = false;
    }
  };

  const fetchRestaurantById = async (id) => {
    loading.value = true;
    error.value = null;
    
    try {
      // 獲取餐廳基本信息
      const { data: restaurantData } = await restaurantApi.getById(id);
      
      // 獲取評論列表
      const { data: reviewsData } = await restaurantApi.getReviews(id, 0, 10);
      
      // 計算平均評分
      const totalRating = reviewsData.content.reduce((sum, review) => sum + review.rating, 0);
      const averageRating = reviewsData.content.length > 0 
        ? (totalRating / reviewsData.content.length).toFixed(1) 
        : 0;
      
      // 合併數據
      const updatedRestaurant = {
        ...restaurantData,
        averageRating: Number(averageRating),  // 轉換為數字
        reviewCount: reviewsData.totalElements || 0,
        reviews: reviewsData.content || []
      };
      
      currentRestaurant.value = updatedRestaurant;
      return updatedRestaurant;
    } catch (err) {
      error.value = err.message;
      ElMessage.error('獲取餐廳詳情失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const createRestaurant = async (restaurantData) => {
    if (!userStore.isAuthenticated) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      const { data } = await restaurantApi.create(restaurantData);
      restaurants.value.unshift(data);
      ElMessage.success('餐廳創建成功');
      return data;
    } catch (err) {
      error.value = err.message;
      ElMessage.error('創建餐廳失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const updateRestaurant = async (id, restaurantData) => {
    if (!userStore.isAuthenticated) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      const { data } = await restaurantApi.update(id, restaurantData);
      const index = restaurants.value.findIndex(r => r.id === id);
      if (index !== -1) {
        restaurants.value[index] = data;
      }
      if (currentRestaurant.value?.id === id) {
        currentRestaurant.value = data;
      }
      ElMessage.success('餐廳更新成功');
      return data;
    } catch (err) {
      error.value = err.message;
      ElMessage.error('更新餐廳失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const deleteRestaurant = async (id) => {
    if (!userStore.isAuthenticated) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      await restaurantApi.delete(id);
      restaurants.value = restaurants.value.filter(r => r.id !== id);
      if (currentRestaurant.value?.id === id) {
        currentRestaurant.value = null;
      }
      ElMessage.success('餐廳刪除成功');
    } catch (err) {
      error.value = err.message;
      ElMessage.error('刪除餐廳失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const toggleFavorite = async (restaurantId) => {
    if (!userStore.checkLogin()) {
      ElMessage.warning('請先登入後再收藏餐廳');
      return;
    }
    
    try {
      const restaurant = restaurants.value.find(r => r.id === restaurantId);
      if (!restaurant) {
        throw new Error('找不到餐廳');
      }

      if (restaurant.isFavorite) {
        await restaurantApi.removeFavorite(restaurantId);
        restaurant.isFavorite = false;
      } else {
        await restaurantApi.addFavorite(restaurantId);
        restaurant.isFavorite = true;
      }

      // 更新當前餐廳的收藏狀態
      if (currentRestaurant.value?.id === restaurantId) {
        currentRestaurant.value.isFavorite = !currentRestaurant.value.isFavorite;
      }

      return { isFavorite: restaurant.isFavorite };
    } catch (err) {
      error.value = err.message;
      ElMessage.error('操作失敗');
      throw err;
    }
  };

  const updateFilters = (newFilters) => {
    filters.value = { ...filters.value, ...newFilters };
    currentPage.value = 1;
    fetchRestaurants();
  };

  const updatePagination = (page, size) => {
    currentPage.value = page;
    pageSize.value = size;
    fetchRestaurants();
  };

  const getReviews = async (restaurantId) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await restaurantApi.getReviews(restaurantId);
      return response;
    } catch (err) {
      error.value = '獲取評論失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const createReview = async (restaurantId, reviewData) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await restaurantApi.createReview(restaurantId, reviewData);
      return response;
    } catch (err) {
      error.value = '創建評論失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const deleteReview = async (restaurantId, reviewId) => {
    loading.value = true;
    error.value = null;
    try {
      const response = await restaurantApi.deleteReview(restaurantId, reviewId);
      return response;
    } catch (err) {
      error.value = '刪除評論失敗';
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const addFavorite = async (restaurantId) => {
    try {
      const response = await restaurantApi.addFavorite(restaurantId);
      // 更新餐廳列表中的收藏狀態
      const restaurant = restaurants.value.find(r => r.id === restaurantId);
      if (restaurant) {
        restaurant.isFavorite = true;
      }
      // 更新當前餐廳的收藏狀態
      if (currentRestaurant.value?.id === restaurantId) {
        currentRestaurant.value.isFavorite = true;
      }
      return response;
    } catch (error) {
      console.error('添加收藏失敗:', error);
      throw error;
    }
  };

  const removeFavorite = async (restaurantId) => {
    try {
      await restaurantApi.removeFavorite(restaurantId);
      // 更新餐廳列表
      await fetchRestaurants();
    } catch (error) {
      console.error('移除收藏失敗:', error);
      throw error;
    }
  };

  const fetchFavorites = async () => {
    try {
      const { data } = await restaurantApi.getFavorites();
      favorites.value = data;
      return data;
    } catch (error) {
      console.error('獲取收藏列表失敗:', error);
      throw error;
    }
  };

  return {
    restaurants,
    popularRestaurants,
    latestRestaurants,
    currentRestaurant,
    loading,
    error,
    total,
    currentPage,
    pageSize,
    filters,
    isLoadingPopular,
    isLoadingLatest,
    errorPopular,
    errorLatest,
    favorites,
    fetchRestaurants,
    fetchPopularRestaurants,
    fetchLatestRestaurants,
    fetchRestaurantById,
    createRestaurant,
    updateRestaurant,
    deleteRestaurant,
    toggleFavorite,
    updateFilters,
    updatePagination,
    getReviews,
    createReview,
    deleteReview,
    addFavorite,
    removeFavorite,
    fetchFavorites
  };
});