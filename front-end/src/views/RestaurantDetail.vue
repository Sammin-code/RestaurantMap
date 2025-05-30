<template>
  <div class="restaurant-detail-container">
    <!-- 返回按鈕 -->
    <el-button
      class="back-button"
      :icon="ArrowLeft"
      @click="router.back()"
    >
      返回列表
    </el-button>

    <!-- 加載狀態 -->
    <div v-if="loading" class="loading">
      載入中...
    </div>
    <div v-else-if="error" class="error">
      {{ error }}
    </div>
    <div v-else-if="restaurant" class="restaurant-content">
      <!-- 餐廳基本信息 -->
      <RestaurantInfo
        :restaurant="restaurant"
        :review-data="reviewData"
        @toggle-favorite="handleFavoriteClick"
      />

      <!-- 餐廳詳細信息 -->
      <el-card v-if="restaurant" class="restaurant-detail-card">
        <div class="detail-content">
          <div class="description">
            <h3>餐廳簡介</h3>
            <el-divider />
            <p>{{ restaurant.description }}</p>
          </div>
        </div>
      </el-card>

      <!-- 評論區域 -->
      <ReviewList
        v-if="restaurant.id"
        :restaurant-id="restaurant.id"
        :reviews="reviewData.content"
        :total="reviewData.totalElements"
        :current-page="reviewData.currentPage"
        :page-size="reviewData.size"
        @edit-review="handleEditReview"
        @delete-review="handleDeleteReview"
        @show-review-dialog="showReviewDialog = true"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      />

      <!-- 評論表單對話框 -->
      <ReviewForm
        v-model:visible="showReviewDialog"
        :restaurant-id="restaurantId"
        :review="currentReview"
        @success="handleReviewSuccess"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ArrowLeft } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRestaurantStore } from '@/stores/restaurant';
import { useReviewStore } from '@/stores/review';
import { useUserStore } from '@/stores/user';
import RestaurantInfo from '@/components/restaurant/RestaurantInfo.vue';
import ReviewList from '@/components/restaurant/ReviewList.vue';
import ReviewForm from '@/components/restaurant/ReviewForm.vue';
import { defaultRestaurantImage } from '@/utils/imageHelper';
import { handleError } from '@/utils/errorHandler';
import { restaurantApi, reviewApi } from '@/services/api';

const route = useRoute();
const router = useRouter();
const restaurantStore = useRestaurantStore();
const reviewStore = useReviewStore();
const userStore = useUserStore();

const restaurantId = computed(() => route.params.id);
const restaurant = ref(null);
const reviewData = ref({
  content: [],
  totalElements: 0,
  currentPage: 0,
  size: 10,
  starDistribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
});
const loading = ref(false);
const error = ref(null);
const showReviewDialog = ref(false);
const currentReview = ref(null);

// 計算平均評分
const averageRating = computed(() => {
  if (!reviewData.value.content || reviewData.value.content.length === 0) {
    return 0;
  }
  const sum = reviewData.value.content.reduce((acc, review) => acc + review.rating, 0);
  return (sum / reviewData.value.content.length).toFixed(1);
});

// 計算評論數量 - 使用 totalElements
const reviewCount = computed(() => {
  return reviewData.value.totalElements || 0;
});

// 獲取餐廳詳情
const fetchRestaurantDetail = async () => {
  if (!restaurantId.value) return;
  
  try {
    loading.value = true;
    error.value = null;
    
    // 獲取餐廳詳情
    const { data: restaurantData } = await restaurantApi.getRestaurantById(restaurantId.value);
    
    // 獲取評論列表
    const { data: reviewsData } = await reviewApi.getRestaurantReviews(restaurantId.value, {
      page: reviewData.value.currentPage,
      size: reviewData.value.size
    });
    
    console.log('獲取到的評論數據:', reviewsData); // 添加日誌
    
    // 更新評論數據
    reviewData.value = {
      content: reviewsData.content || [],
      totalElements: reviewsData.totalElements || 0,
      currentPage: reviewsData.number || 0,
      size: reviewsData.size || 10,
      starDistribution: reviewsData.starDistribution || { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 }
    };
    
    // 合併數據
    restaurant.value = {
      ...restaurantData,
      averageRating: reviewsData.averageRating || 0
    };
    
    console.log('更新後的餐廳數據:', restaurant.value); // 添加日誌
    console.log('更新後的評論數據:', reviewData.value); // 添加日誌
    
  } catch (err) {
    console.error('獲取餐廳詳情失敗:', err);
    error.value = err.message || '獲取餐廳詳情失敗';
  } finally {
    loading.value = false;
  }
};

// 處理編輯評論
const handleEditReview = (review) => {
  console.log('Editing review:', review);  // 添加日誌
  currentReview.value = review;
  showReviewDialog.value = true;
};

const handleDeleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm('確定要刪除這條評論嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    await restaurantStore.deleteReview(reviewId);
    ElMessage.success('評論已刪除');
    await fetchRestaurantDetail();
  } catch (err) {
    if (err !== 'cancel') {
      handleError(err);
      ElMessage.error('刪除評論失敗');
    }
  }
};

const handleToggleLike = async (reviewId) => {
  try {
    await reviewStore.toggleLike(reviewId);
    await fetchRestaurantDetail();
  } catch (err) {
    handleError(err);
    ElMessage.error('操作失敗');
  }
};

const handlePageChange = async (page) => {
  reviewData.value.currentPage = page;
  await fetchRestaurantDetail();
};

const handleSizeChange = async (size) => {
  reviewData.value.size = size;
  reviewData.value.currentPage = 1;
  await fetchRestaurantDetail();
};

const handleReviewSuccess = async () => {
  try {
    await fetchRestaurantDetail();
    showReviewDialog.value = false;
    currentReview.value = null;  // 重置當前編輯的評論
  } catch (err) {
    console.error('更新失敗:', err);
    handleError(err);
    ElMessage.error('更新失敗');
  }
};

const handleFavoriteClick = async () => {
  if (!userStore.checkLogin()) return
  
  try {
    if (restaurant.value.isFavorite) {
      await restaurantApi.removeFavorite(restaurant.value.id)
      ElMessage.success('已取消收藏')
    } else {
      await restaurantApi.addFavorite(restaurant.value.id)
      ElMessage.success('已收藏')
    }
    // 更新餐廳的收藏狀態
    restaurant.value.isFavorite = !restaurant.value.isFavorite
  } catch (error) {
    handleError(error, '收藏操作失敗')
  }
}

onMounted(() => {
  console.log('[詳情] RestaurantDetail.vue mounted, restaurantId:', restaurantId.value);
  fetchRestaurantDetail();
});

watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchRestaurantDetail();
  }
});
</script>

<style scoped>
.restaurant-detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.restaurant-detail-card {
  margin-bottom: 20px;
}

.detail-content {
  padding: 20px;
}

.description h3 {
  margin-bottom: 15px;
  color: #333;
}

.description p {
  color: #666;
  line-height: 1.6;
}

.loading, .error {
  text-align: center;
  padding: 20px;
  color: #666;
}

.error {
  color: #f56c6c;
}


</style> 
