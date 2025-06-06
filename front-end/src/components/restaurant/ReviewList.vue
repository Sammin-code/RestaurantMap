<template>
  <el-card class="reviews-card">
    <div class="reviews-header">
      <h3>顧客評論 ({{ total }})</h3>
      <el-button 
        v-if="userStore.isLoggedIn && !isAdmin"
        type="primary" 
        class="add-review-btn"
        @click="$emit('show-review-dialog')"
      >
        <el-icon><Edit /></el-icon>
        發表評論
      </el-button>
    </div>
    <el-divider />
    
    <!-- 登入提示 -->
    <div class="login-prompt" v-if="!userStore.isLoggedIn">
      <p>登入後即可發表評論</p>
    </div>

    <!-- 評論列表 -->
    <div class="reviews-list">
      <div v-if="loading" class="loading">
        載入中...
      </div>
      <div v-else-if="error" class="error">
        {{ error }}
      </div>
      <div v-else-if="localReviews.length === 0" class="no-reviews">
        暫無評論
      </div>
      <div
        v-for="review in localReviews"
        :key="review.id"
        class="review-item"
      >
        <div class="review-header">
          <div class="user-info">
            <el-avatar 
              :size="40" 
              :src="review.user?.avatar || defaultAvatar"
            >
              {{ (review.user?.username || review.username || 'U')?.charAt(0)?.toUpperCase() }}
            </el-avatar>
            <div class="user-details">
              <span class="username">{{ review.user?.username || review.username || '未知用戶' }}</span>
            </div>
          </div>
          <div class="rating">
            <span v-for="n in 5" :key="n" class="star" :class="{ filled: n <= Number(review.rating) }">★</span>
          </div>
          <div class="review-actions" v-if="userStore.isLoggedIn">
            <el-button
              v-if="isOwner(review) && !isAdmin"
              type="primary"
              link
              @click="$emit('edit-review', review)"
            >
              <el-icon><Edit /></el-icon>
              編輯
            </el-button>
            <el-button 
              v-if="isOwner(review) || isAdmin"
              type="danger" 
              link
              @click="$emit('delete-review', review.id)"
            >
              <el-icon><Delete /></el-icon>
              刪除
            </el-button>
          </div>
        </div>
        
        <div class="review-content">
          <p class="review-text">{{ review.content }}</p>
          <div v-if="review.imageUrl" class="review-image">
            <el-image
              :src="getImageUrl(review.imageUrl)"
              :preview-src-list="[getImageUrl(review.imageUrl)]"
              fit="cover"
              @error="handleReviewImageError"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                  <span>圖片載入失敗</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>
        
        <div class="review-footer">
          <div class="review-meta">
            <span class="review-time">
              {{ review.updated_At ? formatDate(review.updated_At) : formatDate(review.created_At) }}
              <span v-if="review.updated_At && review.updated_At !== review.created_At" class="edited-badge">
                (已編輯)
              </span>
            </span>
          </div>
          <div class="like-section">
            <el-button 
              :type="review.isLiked ? 'primary' : 'default'"
              :plain="!review.isLiked"
              :class="{ liked: review.isLiked }"
              :disabled="!userStore.isLoggedIn"
              @click="handleLikeClick(review)"
              class="like-button"
            >
              <el-icon>
                <StarFilled v-if="review.isLiked" style="color: #FFD700;" />
                <Star v-else style="color: #bbb;" />
              </el-icon>
              <span class="like-count" :class="{ liked: review.isLiked }">
                {{ review.likeCount || 0 }}
              </span>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 評論分頁 -->
    <div class="pagination-container" v-if="localReviews.length > 0">
      <div class="custom-pagination">
        <div class="pagination-info">
          共 {{ total }} 筆評論，每頁
          <el-select 
            :value="pageSize"
            @update:model-value="$emit('size-change', $event)"
            style="width: 100px; margin: 0 5px;"
            size="small"
          >
            <el-option
              v-for="item in [5, 10, 20, 50]"
              :key="item"
              :value="item"
              :label="`${item} 筆/頁`"
            />
          </el-select>
        </div>
        
        <div class="pagination-controls">
          <el-button 
            type="primary" 
            text 
            :disabled="currentPage <= 1" 
            @click="$emit('current-change', currentPage - 1)"
          >
            上一頁
          </el-button>
          
          <!-- 頁碼按鈕 -->
          <div class="pagination-pages">
            <template v-if="totalPages <= 7">
              <el-button 
                v-for="page in totalPages" 
                :key="page"
                type="primary"
                :class="{ 'current-page': currentPage === page }"
                :text="currentPage !== page"
                @click="$emit('current-change', page)"
              >
                {{ page }}
              </el-button>
            </template>
            
            <template v-else>
              <!-- 顯示前兩頁 -->
              <template v-if="currentPage > 3">
                <el-button 
                  type="primary" 
                  text 
                  @click="$emit('current-change', 1)"
                >
                  1
                </el-button>
                <span v-if="currentPage > 4" class="ellipsis">...</span>
              </template>
              
              <!-- 顯示當前頁附近的頁碼 -->
              <template v-for="page in displayedPages" :key="page">
                <el-button 
                  type="primary"
                  :class="{ 'current-page': currentPage === page }"
                  :text="currentPage !== page"
                  @click="$emit('current-change', page)"
                >
                  {{ page }}
                </el-button>
              </template>
              
              <!-- 顯示末頁 -->
              <template v-if="currentPage < totalPages - 2">
                <span v-if="currentPage < totalPages - 3" class="ellipsis">...</span>
                <el-button 
                  type="primary" 
                  text 
                  @click="$emit('current-change', totalPages)"
                >
                  {{ totalPages }}
                </el-button>
              </template>
            </template>
          </div>
          
          <el-button 
            type="primary" 
            text 
            :disabled="currentPage >= totalPages" 
            @click="$emit('current-change', currentPage + 1)"
          >
            下一頁
          </el-button>
        </div>
        
        <div class="pagination-jumper">
          前往第
          <el-input
            v-model="jumpPage"
            size="small"
            style="width: 50px; margin: 0 5px;"
            @keyup.enter="handleJumpPage"
          />
          頁
          <el-button 
            type="primary" 
            size="small" 
            style="margin-left: 5px;" 
            @click="handleJumpPage"
          >
            前往
          </el-button>
        </div>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import { Edit, Delete, Star, StarFilled, Loading, Picture } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { useReviewStore } from '@/stores/review';
import { ElMessage } from 'element-plus';
import Rating from '@/components/common/Rating.vue';
import { useRouter } from 'vue-router';
import { handleError } from '@/utils/errorHandler';
import { reviewApi } from '@/services/api';
import { getImageUrl, handleReviewImageError } from '@/utils/imageHelper';

const props = defineProps({
  restaurantId: {
    type: [Number, String],
    required: true
  },
  total: {
    type: Number,
    required: true
  },
  currentPage: {
    type: Number,
    required: true
  },
  pageSize: {
    type: Number,
    required: true
  },
  reviews: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['update:current-page', 'update:page-size', 'edit-review', 'delete-review', 'show-review-dialog']);

const userStore = useUserStore();
const reviewStore = useReviewStore();
const router = useRouter();
const jumpPage = ref('');
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const totalPages = computed(() => Math.ceil(props.total / props.pageSize));

const displayedPages = computed(() => {
  const pages = [];
  const maxDisplayed = 5;
  let start = Math.max(1, props.currentPage - 2);
  let end = Math.min(totalPages.value, start + maxDisplayed - 1);
  
  if (end - start + 1 < maxDisplayed) {
    start = Math.max(1, end - maxDisplayed + 1);
  }
  
  for (let i = start; i <= end; i++) {
    pages.push(i);
  }
  
  return pages;
});

const loading = ref(false);
const error = ref(null);

// 本地響應式評論列表
const localReviews = ref(props.reviews.map(r => ({ ...r })));
watch(() => props.reviews, (newVal) => {
  localReviews.value = newVal.map(r => ({ ...r }));
});

const fetchReviews = async () => {
  if (!props.restaurantId) {
    console.log('沒有 restaurantId，跳過獲取評論');
    return;
  }

  try {
    loading.value = true;
    error.value = null;
    console.log('開始獲取評論列表, restaurantId:', props.restaurantId);
    await reviewStore.fetchReviews(props.restaurantId, props.currentPage, props.pageSize);
  } catch (error) {
    console.error('獲取評論列表失敗:', error);
    error.value = `獲取評論列表失敗: ${error.response?.data?.message || error.message}`;
  } finally {
    loading.value = false;
  }
};

// 監聽 restaurantId 的變化
watch(() => props.restaurantId, (newId) => {
  if (newId) {
    fetchReviews();
  }
}, { immediate: true });

const handleLikeClick = async (review) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('請先登入後再點讚');
    router.push('/login');
    return;
  }

  try {
    // 立即更新本地狀態
    review.isLiked = !review.isLiked;
    review.likeCount = review.isLiked 
      ? (review.likeCount || 0) + 1 
      : Math.max(0, (review.likeCount || 0) - 1);

    // 發送請求到後端
    if (review.isLiked) {
      await reviewApi.likeReview(review.id);
    } else {
      await reviewApi.unlikeReview(review.id);
    }
  } catch (error) {
    // 如果請求失敗，恢復原始狀態
    review.isLiked = !review.isLiked;
    review.likeCount = review.isLiked 
      ? (review.likeCount || 0) + 1 
      : Math.max(0, (review.likeCount || 0) - 1);
    handleError(error);
    ElMessage.error('點讚操作失敗');
  }
};

const handleJumpPage = () => {
  const page = parseInt(jumpPage.value);
  if (isNaN(page) || page < 1 || page > totalPages.value) {
    ElMessage.warning('請輸入有效的頁碼');
    return;
  }
  emit('current-change', page);
};

const formatDate = (dateString) => {
  try {
    console.log('Formatting date:', dateString);
    
    if (!dateString) {
      console.log('No date string provided');
      return '未知時間';
    }
    
    // 處理 YYYY-MM-DD HH:mm:ss 格式
    const [datePart, timePart] = dateString.split(' ');
    console.log('Date parts:', { datePart, timePart });
    
    const [year, month, day] = datePart.split('-');
    const [hour, minute] = timePart.split(':');
    console.log('Parsed values:', { year, month, day, hour, minute });
    
    const date = new Date(year, month - 1, day, hour, minute);
    console.log('Created date object:', date);
    
    if (isNaN(date.getTime())) {
      console.error('無效的日期格式:', dateString);
      return '日期格式錯誤';
    }

    const formattedDate = date.toLocaleDateString('zh-TW', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
    console.log('Formatted date:', formattedDate);
    
    return formattedDate;
  } catch (error) {
    console.error('日期格式化失敗:', error);
    return '日期格式錯誤';
  }
};

// 修改調試代碼
console.log('評論數據:', props.reviews);
console.log('日期格式:', props.reviews.map(review => ({
  id: review.id,
  createdAt: review.created_At,
  updatedAt: review.updated_At
})));

// 檢查是否為評論擁有者
const isOwner = (review) => {
  return userStore.user && review.userId === userStore.user.id;
};

// 檢查是否為管理員
const isAdmin = computed(() => {
  return userStore.hasRole('ROLE_ADMIN');
});

// 獲取用戶收藏的餐廳
const fetchFavoriteRestaurants = async () => {
  if (!userStore.isInitialized) {
    await userStore.initialize();
  }

  if (!userStore.isLoggedIn) {
    console.log('User not logged in, skipping favorites fetch')
    favoriteRestaurants.value = []
    return
  }

  try {
    const response = await restaurantApi.getFavorites()
    if (response.status === 204) {
      favoriteRestaurants.value = []
      return
    }
    favoriteRestaurants.value = response.data
  } catch (error) {
    console.error('Error fetching favorite restaurants:', error)
    if (error.response?.status === 401) {
      userStore.logout()
      ElMessage.warning('登入已過期，請重新登入')
    } else {
      ElMessage.error('獲取收藏列表失敗')
    }
    favoriteRestaurants.value = []
  }
}

// 檢查圖片 URL 是否有效
const isValidImageUrl = (imageUrl) => {
  if (!imageUrl) return false;
  
  // 不顯示 blob URL
  if (imageUrl.startsWith('blob:')) {
    console.log('跳過 blob URL:', imageUrl);
    return false;
  }
  
  // 顯示其他類型的 URL
  return true;
};
</script>

<style scoped>
.reviews-card {
  margin-bottom: 20px;
}

.reviews-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.reviews-header h3 {
  margin: 0;
  font-size: 1.2em;
  color: #333;
}

.reviews-header .add-review-btn {
  display: flex;
  align-items: center;
  gap: 5px;
}

.login-prompt {
  text-align: center;
  padding: 20px;
  color: #666;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.review-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  background-color: #fff;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: bold;
  color: #409EFF;
  margin-right: 10px;
}

.rating {
  color: #ffd700;
}

.star {
  color: #ccc;
}

.star.filled {
  color: #ffd700;
}

.review-actions {
  display: flex;
  gap: 10px;
}

.review-content {
  margin: 10px 0;
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

.image-loading,
.image-placeholder {
  width: 300px;
  height: 180px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  border-radius: 4px;
}

.image-loading .el-icon,
.image-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

.image-loading .el-icon {
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.review-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 10px;
}

.review-time {
  color: #909399;
}

.edited-badge {
  color: #909399;
  font-style: italic;
}

.like-section {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
}

.like-button {
  display: flex;
  align-items: center;
  gap: 5px;
  transition: all 0.3s ease;
  border-radius: 20px;
  padding: 8px 16px;
}

.like-button.liked {
  background-color: #409EFF !important;
  border-color: #409EFF !important;
  color: #fff !important;
}

.like-button.liked:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  transform: translateY(-1px);
}

.like-button:not(.liked) {
  background-color: #f5f7fa;
  border-color: #dcdfe6;
  color: #606266;
}

.like-button:not(.liked):hover {
  background-color: #ecf5ff;
  border-color: #409EFF;
  color: #409EFF;
}

.like-count {
  font-size: 0.9em;
  font-weight: 500;
  transition: all 0.3s ease;
}

.like-count.liked {
  color: #fff !important;
}

.like-button .el-icon {
  font-size: 16px;
  transition: all 0.3s ease;
}

.like-button.liked .el-icon {
  color: #FFD700 !important;
}

.pagination-container {
  margin-top: 20px;
  text-align: center;
}

.custom-pagination {
  display: inline-block;
}

.pagination-info {
  margin-bottom: 10px;
}

.pagination-controls {
  margin-bottom: 10px;
}

.pagination-pages {
  margin-bottom: 10px;
}

.pagination-jumper {
  margin-bottom: 10px;
}

/* 添加評分樣式 */
:deep(.el-rate) {
  margin-bottom: 10px;
}

.loading, .error, .no-reviews {
  text-align: center;
  padding: 20px;
  color: #666;
}

.error {
  color: #f56c6c;
}
</style>
