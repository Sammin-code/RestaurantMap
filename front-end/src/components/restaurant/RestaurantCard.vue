<template>
  <el-card class="restaurant-card" :body-style="{ padding: '0px' }">
    <div
      class="favorite-icon-wrapper"
      v-if="showFavoriteButton && userStore.isLoggedIn && userStore.user?.role === 'ROLE_REVIEWER'"
    >
      <el-button
        class="favorite-icon-btn"
        :icon="isFavorite ? StarFilled : Star"
        :type="isFavorite ? 'warning' : 'info'"
        circle
        size="large"
        @click.stop="handleFavoriteClick"
      />
    </div>
    <div class="restaurant-image">
      <el-image
        :src="getRestaurantImageUrl(restaurant)"
        :preview-src-list="[getRestaurantImageUrl(restaurant)]"
        fit="cover"
        class="restaurant-img"
        @error="(e) => handleRestaurantImageError(e, restaurant)"
      >
        <template #placeholder>
          <div class="image-loading">
            <el-icon><Loading /></el-icon>
            <div>載入中...</div>
          </div>
        </template>
        <template #error>
          <div class="image-placeholder">
            <el-icon><Picture /></el-icon>
            <div>圖片載入失敗</div>
          </div>
        </template>
      </el-image>
    </div>
    <div class="restaurant-info">
      <h3 class="restaurant-name">{{ restaurant.name }}</h3>
      <div class="restaurant-details">
        <div class="detail-item">
          <el-icon><Location /></el-icon>
          <span>{{ restaurant.address }}</span>
        </div>
        <div class="detail-item">
          <el-icon><ChatDotRound /></el-icon>
          <span>{{ restaurant.category }}</span>
        </div>
      </div>
      <div class="rating-info">
        <div class="rating-row">
          <span class="star">★</span>
          <span class="rating">{{ formattedRating }}</span>
        </div>
        <span class="review-count">{{ reviewCount }} 則評論</span>
      </div>
      <div class="restaurant-actions">
        <el-button type="primary" size="small" @click="viewDetails">查看詳情</el-button>
        <el-button 
          v-if="isOwner && showUpdateButton"
          type="warning" 
          size="small" 
          @click="handleUpdate"
        >
          更新
        </el-button>
        <el-button 
          v-if="(isOwner || isAdmin) && showDeleteButton"
          type="danger" 
          size="small" 
          @click="handleDelete"
        >
          刪除
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue';
import { Location, ChatDotRound, Picture, Loading, Star, StarFilled } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import { ElMessage } from 'element-plus';
import { getRestaurantImageUrl, handleRestaurantImageError } from '@/utils/imageHelper';
import { useRouter } from 'vue-router';
import { restaurantApi } from '@/services/api';

const props = defineProps({
  restaurant: {
    type: Object,
    required: true
  },
  defaultImage: {
    type: String,
    default: 'https://example.com/default-image.jpg'
  },
  showDeleteButton: {
    type: Boolean,
    default: false
  },
  showUpdateButton: {
    type: Boolean,
    default: false
  },
  showFavoriteButton: {
    type: Boolean,
    default: true
  }
});

const emit = defineEmits(['delete', 'update']);

const router = useRouter();
const userStore = useUserStore();

// 使用 computed 屬性來獲取收藏狀態
const isFavorite = computed(() => {
  return userStore.isFavorite(props.restaurant.id);
});

// 評論數
const reviewCount = computed(() => {
  return props.restaurant.reviewCount || 0;
});

// 格式化評分
const formattedRating = computed(() => {
  const rating = props.restaurant.averageRating || 0;
  return rating ? rating.toFixed(1) : '尚無評分';
});

// 檢查是否為管理員
const isAdmin = computed(() => {
  if (!userStore.isInitialized) {
    return false;
  }
  return userStore.hasRole('ROLE_ADMIN');
});

// 檢查是否為餐廳創建者
const isOwner = computed(() => {
  if (!userStore.isInitialized) {
    return false;
  }
  return userStore.user?.username === props.restaurant.createdByUsername;
});

onMounted(async () => {
  try {
    await router.isReady();
    
    if (!userStore.isInitialized) {
      await userStore.initialize();
    }
    
    if (!userStore.isLoggedIn) {
      return;
    }

    await userStore.fetchFavorites();
    
    // 移動到這裡，確保在初始化後輸出
    console.log('[RestaurantCard] userStore.user:', userStore.user);
    console.log('[RestaurantCard] isAdmin:', isAdmin.value);
  } catch (err) {
    console.error('獲取收藏狀態失敗:', err);
  }
});

// 處理收藏點擊
const handleFavoriteClick = async () => {
  try {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }

    if (isFavorite.value) {
      await userStore.removeFavorite(props.restaurant.id);
      ElMessage.success('已取消收藏');
    } else {
      await userStore.addFavorite(props.restaurant.id);
      ElMessage.success('收藏成功');
    }
  } catch (err) {
    console.error('收藏操作失敗:', err);
    if (err.response?.status === 401) {
      ElMessage.error('請重新登入');
      userStore.clearUser();
      router.push('/login');
    } else if (err.response?.status === 403) {
      ElMessage.error('權限不足');
    } else if (err.response?.status === 400 && err.response.data.message === '該餐廳已經在收藏列表中') {
      await userStore.fetchFavorites();
      ElMessage.info('該餐廳已在收藏列表中');
    } else {
      ElMessage.error('操作失敗，請稍後再試');
    }
  }
};

const viewDetails = () => {
  router.push(`/restaurants/${props.restaurant.id}`);
};

// 處理刪除
const handleDelete = () => {
  emit('delete', props.restaurant.id);
};

// 處理更新
const handleUpdate = () => {
  emit('update', props.restaurant.id);
};
</script>

<style scoped>
.restaurant-card {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.3s ease;
  background: white;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
}

.favorite-icon-wrapper {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 2;
}

.favorite-icon-btn {
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.restaurant-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.restaurant-image {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.restaurant-img {
  width: 100%;
  height: 100%;
}

.restaurant-img :deep(.el-image__inner) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.restaurant-img :deep(.el-image__preview) {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.restaurant-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.restaurant-name {
  margin: 0 0 10px;
  font-size: 1.2em;
  font-weight: 600;
  color: #333;
}

.restaurant-details {
  margin-bottom: 10px;
}

.detail-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  color: #666;
  font-size: 0.9em;
}

.detail-item .el-icon {
  margin-right: 5px;
  font-size: 1.1em;
}

.rating-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.rating-row {
  display: flex;
  align-items: center;
}

.star {
  color: #f59e42;
  margin-right: 4px;
}

.rating {
  font-weight: 500;
  color: #333;
}

.review-count {
  color: #666;
  font-size: 0.9em;
}

.restaurant-actions {
  margin-top: auto;
  display: flex;
  gap: 8px;
}

.image-loading,
.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
}

.image-loading .el-icon,
.image-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}

/* 響應式設計 */
@media (max-width: 768px) {
  .restaurant-image {
    height: 180px;
  }
}

@media (max-width: 480px) {
  .restaurant-image {
    height: 160px;
  }
}
</style>