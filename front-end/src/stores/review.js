import { defineStore } from 'pinia';
import { ref } from 'vue';
import { ElMessage } from 'element-plus';
import { useUserStore } from './user';
import { useRouter } from 'vue-router';
import { reviewApi } from '@/services/api';
import { getImageUrl } from '@/utils/imageHelper';

const useReviewStore = defineStore('review', () => {
  const router = useRouter();
  const userStore = useUserStore();
  
  const reviews = ref([]);
  const loading = ref(false);
  const error = ref(null);
  const totalElements = ref(0);
  const currentPage = ref(1);
  const pageSize = ref(10);

  const fetchReviews = async (restaurantId, page = 1, size = 10) => {
    loading.value = true;
    error.value = null;
    
    try {
      const response = await reviewApi.getRestaurantReviews(restaurantId, { 
        currentPage: page - 1,  // 轉換為 0-based 索引
        size 
      });
      reviews.value = response.data.content;
      totalElements.value = response.data.totalElements;
      currentPage.value = response.data.currentPage + 1;  // 轉換為 1-based 索引
      pageSize.value = response.data.size;
      return response.data;
    } catch (err) {
      error.value = err.message;
      ElMessage.error('獲取評論失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const createReview = async (restaurantId, formData) => {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      // 添加除錯日誌
      console.log('Creating review:', {
        restaurantId,
        hasReview: formData.has('review'),
        hasImage: formData.has('image')
      });
      
      const response = await reviewApi.createReview(restaurantId, formData);
      console.log('Review created successfully:', response.data);
      
      // 處理圖片 URL
      if (response.data.imageUrl) {
        response.data.imageUrl = getImageUrl(response.data.imageUrl);
      }
      
      // 更新本地評論列表
      reviews.value.unshift(response.data);
      
      ElMessage.success('評論發布成功');
      return response.data;
    } catch (err) {
      error.value = err.message;
      console.error('Create Review Error:', err);
      if (err.response) {
        console.error('Error Response:', err.response.data);
      }
      ElMessage.error('發布評論失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const updateReview = async (reviewId, formData) => {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      // 添加除錯日誌
      console.log('Updating review:', {
        reviewId,
        hasReview: formData.has('review'),
        hasImage: formData.has('image')
      });
      
      const response = await reviewApi.updateReview(reviewId, formData);
      console.log('Review updated successfully:', response.data);
      
      // 處理圖片 URL
      if (response.data.imageUrl) {
        response.data.imageUrl = getImageUrl(response.data.imageUrl);
      }
      
      // 更新本地評論列表
      const index = reviews.value.findIndex(r => r.id === reviewId);
      if (index !== -1) {
        reviews.value[index] = response.data;
      }
      
      ElMessage.success('評論更新成功');
      return response.data;
    } catch (err) {
      error.value = err.message;
      console.error('Update Review Error:', err);
      if (err.response) {
        console.error('Error Response:', err.response.data);
      }
      ElMessage.error('更新評論失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const deleteReview = async (reviewId) => {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      await reviewApi.deleteReview(reviewId);
      reviews.value = reviews.value.filter(r => r.id !== reviewId);
      ElMessage.success('評論刪除成功');
    } catch (err) {
      error.value = err.message;
      ElMessage.error('刪除評論失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  const toggleLike = async (reviewId) => {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }
    
    loading.value = true;
    error.value = null;
    
    try {
      const numericReviewId = Number(reviewId);
      const review = reviews.value.find(r => r.id === numericReviewId);
      
      if (!review) {
        throw new Error('找不到評論');
      }

      // 先更新本地狀態
      const isLiked = !review.isLiked;
      review.isLiked = isLiked;
      review.likeCount = isLiked 
        ? (review.likeCount || 0) + 1 
        : Math.max(0, (review.likeCount || 0) - 1);

      // 然後發送請求到後端
      if (isLiked) {
        await reviewApi.likeReview(numericReviewId);
      } else {
        await reviewApi.unlikeReview(numericReviewId);
      }
      
      return review;
    } catch (err) {
      error.value = err.message;
      // 如果請求失敗，恢復原始狀態
      const review = reviews.value.find(r => r.id === Number(reviewId));
      if (review) {
        review.isLiked = !review.isLiked;
        review.likeCount = review.isLiked 
          ? (review.likeCount || 0) + 1 
          : Math.max(0, (review.likeCount || 0) - 1);
      }
      ElMessage.error('操作失敗');
      throw err;
    } finally {
      loading.value = false;
    }
  };

  return {
    reviews,
    loading,
    error,
    totalElements,
    currentPage,
    pageSize,
    fetchReviews,
    createReview,
    updateReview,
    deleteReview,
    toggleLike
  };
});

export { useReviewStore }; 