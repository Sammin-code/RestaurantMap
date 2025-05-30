<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>個人資料</h2>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="error" class="error-container">
        <el-alert
          :title="error"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" @click="fetchUserData">重試</el-button>
      </div>
      
      <div v-else class="profile-content">
        <el-form
          ref="formRef"
          :model="userForm"
          :rules="rules"
          label-width="100px"
          disabled
        >
          <el-form-item label="用戶名" prop="username">
            <el-input v-model="userForm.username" />
          </el-form-item>
          
          <el-form-item label="電子郵件" prop="email">
            <el-input v-model="userForm.email" />
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <!-- 新增用戶創建的餐廳卡片 -->
    <el-card class="created-restaurants-card">
      <template #header>
        <div class="card-header">
          <h2>我發表的餐廳</h2>
          <el-button type="primary" @click="router.push('/restaurants/new')">新增餐廳</el-button>
        </div>
      </template>
      
      <div v-if="createdRestaurantsLoading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="createdRestaurantsError" class="error-container">
        <el-alert
          :title="createdRestaurantsError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" @click="fetchCreatedRestaurants">重試</el-button>
      </div>
      
      <div v-else-if="createdRestaurants.length === 0" class="empty-container">
        <el-empty description="您還沒有發表任何餐廳" />
        <el-button type="primary" @click="router.push('/restaurants/new')">立即新增</el-button>
      </div>
      
      <div v-else class="created-restaurants-list">
        <el-row :gutter="20">
          <el-col v-for="restaurant in createdRestaurants" :key="restaurant.id" :xs="24" :sm="12" :md="8" :lg="6">
            <RestaurantCard
              :restaurant="restaurant"
              :default-image="defaultRestaurantImage"
              :show-delete-button="true"
              :show-update-button="true"
              :show-favorite-button="false"
              @delete="deleteRestaurant"
              @update="updateRestaurant"
            />
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <el-card class="favorites-card">
      <template #header>
        <div class="card-header">
          <h2>我的收藏</h2>
        </div>
      </template>
      
      <div v-if="favoritesLoading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="favoritesError" class="error-container">
        <el-alert
          :title="favoritesError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" @click="fetchFavorites">重試</el-button>
      </div>
      
      <div v-else-if="favoriteRestaurants.length === 0" class="empty-container">
        <el-empty description="您還沒有收藏任何餐廳" />
      </div>
      
      <div v-else class="favorites-list">
        <el-row :gutter="20">
          <el-col v-for="restaurant in favoriteRestaurants" :key="restaurant.id" :xs="24" :sm="12" :md="8" :lg="6">
            <RestaurantCard
              :restaurant="restaurant"
              :default-image="defaultRestaurantImage"
              :show-delete-button="false"
              @delete="removeFavorite"
            />
          </el-col>
        </el-row>
      </div>
    </el-card>
    
    <!-- 新增評論列表卡片 -->
    <el-card class="reviews-card">
      <template #header>
        <div class="card-header">
          <h2>我的評論</h2>
        </div>
      </template>
      
      <div v-if="reviewsLoading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="reviewsError" class="error-container">
        <el-alert
          :title="reviewsError"
          type="error"
          show-icon
          :closable="false"
        />
        <el-button type="primary" @click="fetchUserReviews">重試</el-button>
      </div>
      
      <div v-else-if="userReviews.length === 0" class="empty-container">
        <el-empty description="您還沒有發表任何評論" />
      </div>
      
      <div v-else class="reviews-list">
        <el-timeline>
          <el-timeline-item
            v-for="review in userReviews"
            :key="review.id"
            :timestamp="formatDate(review.created_At)"
            placement="top"
          >
            <el-card class="review-card">
              <div class="review-header">
                <div class="restaurant-info">
                  <h3>{{ review.restaurantName || '未知餐廳' }}</h3>
                  <el-button 
                    v-if="review.restaurantId"
                    type="primary" 
                    link 
                    @click="viewRestaurant(review.restaurantId)"
                  >
                    查看餐廳
                  </el-button>
                </div>
                <div class="review-actions">
                  <el-button 
                    type="primary" 
                    link 
                    @click="editReview(review)"
                  >
                    <el-icon><Edit /></el-icon>
                    編輯
                  </el-button>
                  <el-button 
                    type="danger" 
                    link 
                    @click="deleteReview(review.id)"
                  >
                    <el-icon><Delete /></el-icon>
                    刪除
                  </el-button>
                </div>
              </div>
              
              <div class="review-content">
                <div class="rating">
                  <el-rate v-model="review.rating" disabled />
                  <span class="rating-value">{{ review.rating }} 分</span>
                </div>
                <p class="review-text">{{ review.content }}</p>
                <img 
                  v-if="review.imageUrl" 
                  :src="getRestaurantImageUrl({ imageUrl: review.imageUrl })" 
                  class="review-image" 
                  @error="(e) => handleImageError(e, review)"
                />
              </div>
              
              <div class="review-footer">
                <span v-if="review.isEdited" class="edited-badge">已編輯</span>
                <div class="like-section">
                  <el-button 
                    :type="review.isLiked ? 'primary' : 'default'"
                    :icon="review.isLiked ? 'StarFilled' : 'Star'"
                    circle
                    size="small"
                    @click="handleLikeClick(review)"
                  />
                  <span class="like-count">{{ review.likeCount || 0 }}</span>
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { ElMessage, ElMessageBox } from 'element-plus';
import { userApi, restaurantApi, reviewApi } from '@/services/api';
import { defaultRestaurantImage, getRestaurantImageUrl, handleRestaurantImageError } from '@/utils/imageHelper';
import { handleError } from '@/utils/errorHandler';
import { Edit, Delete } from '@element-plus/icons-vue';
import RestaurantCard from '@/components/restaurant/RestaurantCard.vue';

const router = useRouter();
const userStore = useUserStore();
const formRef = ref(null);

// 狀態
const loading = ref(false);
const error = ref(null);
const favoritesLoading = ref(false);
const favoritesError = ref(null);
const favoriteRestaurants = ref([]);
const reviewsLoading = ref(false);
const reviewsError = ref(null);
const userReviews = ref([]);
const createdRestaurantsLoading = ref(false);
const createdRestaurantsError = ref(null);
const createdRestaurants = ref([]);

// 表單數據
const userForm = reactive({
  username: '',
  email: ''
});

// 表單驗證規則
const rules = {
  username: [
    { required: true, message: '請輸入用戶名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '請輸入電子郵件', trigger: 'blur' },
    { type: 'email', message: '請輸入正確的電子郵件格式', trigger: 'blur' }
  ]
};

// 獲取用戶數據
const fetchUserData = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await userApi.getCurrentUser();
    userForm.username = response.data.username;
    userForm.email = response.data.email;
  } catch (err) {
    error.value = err.response?.data?.message || '獲取用戶數據失敗';
  } finally {
    loading.value = false;
  }
};

// 獲取收藏的餐廳
const fetchFavorites = async () => {
  if (!userStore.user?.id) {
    favoritesError.value = '請先登入';
    return;
  }
  try {
    favoritesLoading.value = true;
    favoritesError.value = null;
    const favoritesResponse = await userApi.getUserFavorites(userStore.user.id);
    console.log('收藏的餐廳數據:', favoritesResponse.data);
    favoriteRestaurants.value = favoritesResponse.data || [];
  } catch (err) {
    console.error('獲取收藏列表失敗:', err);
    favoritesError.value = err.response?.data?.message || '獲取收藏列表失敗';
  } finally {
    favoritesLoading.value = false;
  }
};

// 獲取用戶評論
const fetchUserReviews = async () => {
  if (!userStore.user?.id) {
    reviewsError.value = '請先登入';
    return;
  }
  try {
    reviewsLoading.value = true;
    reviewsError.value = null;
    const response = await userApi.getUserReviews(userStore.user.id);
    console.log('評論數據:', response.data);
    userReviews.value = response.data || [];
  } catch (err) {
    console.error('獲取評論列表失敗:', err);
    reviewsError.value = err.response?.data?.message || '獲取評論列表失敗';
  } finally {
    reviewsLoading.value = false;
  }
};

// 獲取用戶創建的餐廳
const fetchCreatedRestaurants = async () => {
  if (!userStore.user?.id) {
    createdRestaurantsError.value = '請先登入';
    return;
  }
  try {
    createdRestaurantsLoading.value = true;
    createdRestaurantsError.value = null;
    const response = await userApi.getCreatedRestaurants(userStore.user.id);
    console.log('創建的餐廳數據:', response.data);
    createdRestaurants.value = response.data || [];
  } catch (err) {
    createdRestaurantsError.value = err.response?.data?.message || '獲取創建的餐廳列表失敗';
  } finally {
    createdRestaurantsLoading.value = false;
  }
};

// 刪除餐廳
const deleteRestaurant = async (restaurantId) => {
  try {
    await ElMessageBox.confirm('確定要刪除這家餐廳嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await restaurantApi.deleteRestaurant(restaurantId);
    ElMessage.success('餐廳已刪除');
    await fetchCreatedRestaurants();
  } catch (err) {
    if (err !== 'cancel') {
      handleError(err, '刪除餐廳失敗');
    }
  }
};

// 更新餐廳
const updateRestaurant = (restaurantId) => {
  router.push(`/restaurants/${restaurantId}/edit`);
};

// 移除收藏
const removeFavorite = async (restaurantId) => {
  try {
    await restaurantApi.removeFavorite(restaurantId);
    ElMessage.success('已取消收藏');
    await fetchFavorites();
  } catch (err) {
    handleError(err, '取消收藏失敗');
  }
};

// 查看餐廳
const viewRestaurant = (restaurantId) => {
  router.push(`/restaurants/${restaurantId}`);
};

// 編輯評論
const editReview = (review) => {
  router.push(`/restaurants/${review.restaurant.id}?editReview=${review.id}`);
};

// 刪除評論
const deleteReview = async (reviewId) => {
  try {
    await ElMessageBox.confirm('確定要刪除這條評論嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await reviewApi.deleteReview(reviewId);
    ElMessage.success('評論已刪除');
    await fetchUserReviews();
  } catch (err) {
    if (err !== 'cancel') {
      handleError(err, '刪除評論失敗');
    }
  }
};

// 處理點讚
const handleLikeClick = async (review) => {
  try {
    if (review.isLiked) {
      await reviewApi.unlikeReview(review.id);
      review.likeCount = (review.likeCount || 0) - 1;
    } else {
      await reviewApi.likeReview(review.id);
      review.likeCount = (review.likeCount || 0) + 1;
    }
    review.isLiked = !review.isLiked;
  } catch (err) {
    handleError(err, '點讚操作失敗');
  }
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 處理圖片載入錯誤
const handleImageError = (e, review) => {
  handleRestaurantImageError(e, { imageUrl: review.imageUrl });
};

onMounted(async () => {
  try {
    // 先獲取用戶信息
    await fetchUserData();
    
    // 確保用戶已登入
    if (userStore.user?.id) {
      // 並行獲取其他數據
      await Promise.all([
        fetchFavorites(),
        fetchUserReviews(),
        fetchCreatedRestaurants()
      ]);
    }
  } catch (err) {
    handleError(err);
  }
});
</script>

<style scoped>
.profile-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.profile-card, .favorites-card, .reviews-card, .created-restaurants-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.loading-container, .error-container, .empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.error-container {
  gap: 10px;
}

.favorites-list {
  margin-top: 20px;
}

/* 評論卡片樣式 */
.reviews-list {
  padding: 0 10px;
}

.review-card {
  margin-bottom: 20px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 15px;
}

.restaurant-info {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.restaurant-info h3 {
  margin: 0;
  font-size: 1.1em;
  color: #333;
}

.review-actions {
  display: flex;
  gap: 10px;
}

.review-content {
  margin: 15px 0;
}

.rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.rating-value {
  color: #666;
  font-size: 0.9em;
}

.review-text {
  margin: 10px 0;
  color: #333;
  line-height: 1.5;
}

.review-image {
  width: 300px;
  height: 180px;
  margin-top: 10px;
  border-radius: 4px;
  object-fit: cover;
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  color: #666;
  font-size: 0.9em;
}

.edited-badge {
  background-color: #f0f0f0;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.8em;
}

.like-section {
  display: flex;
  align-items: center;
  gap: 5px;
}

.like-count {
  font-size: 0.9em;
  color: #666;
}
</style> 