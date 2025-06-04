<template>
  <el-card class="restaurant-info-card">
    <div class="restaurant-header">
      <el-image
        :src="getRestaurantImageUrl(restaurant)"
        fit="cover"
        class="restaurant-image"
        @error="handleImageError"
      >
        <template #error>
          <div class="image-placeholder">
            <el-icon><Picture /></el-icon>
          </div>
        </template>
      </el-image>
      <div style="color: red; font-size: 12px; word-break: break-all;">
        圖片網址: {{ getRestaurantImageUrl(restaurant) }}
      </div>
      <div class="restaurant-info">
        <h1>{{ restaurant.name }}</h1>
        
        <div class="rating-section" v-if="reviewData && reviewData.starDistribution && restaurant.reviewCount !== null">
          <Rating
            v-model="restaurant.averageRating"
            :reviews="restaurant.reviews"
            :total="restaurant.reviewCount"
            :distribution="reviewData.starDistribution"
            :disabled="true"
            :show-distribution="true"
          />
          <span v-if="restaurant.reviewCount" style="margin-left: 8px;">
            （{{ restaurant.reviewCount }} 則評論）
          </span>
        </div>
        
        <p class="address">
          <el-icon><Location /></el-icon>
          {{ restaurant.address }}
        </p>
        <p class="phone" v-if="restaurant.phone">
          <el-icon><Phone /></el-icon>
          {{ restaurant.phone }}
        </p>
        <div class="restaurant-tags">
          <el-tag
            v-for="tag in restaurant.tags"
            :key="tag"
            size="small"
            class="tag"
          >
            {{ tag }}
          </el-tag>
        </div>
        <el-button
          type="primary"
          :icon="Star"
          :class="{ 'is-favorite': isFavorite }"
          @click="toggleFavorite"
          v-if="userStore.user?.role !== 'ROLE_ADMIN'"
        >
          {{ isFavorite ? '取消收藏' : '收藏餐廳' }}
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { Picture, Location, Phone, Star, ChatDotRound } from '@element-plus/icons-vue';
import { useUserStore } from '@/stores/user';
import Rating from '@/components/common/Rating.vue';
import { DEFAULT_RESTAURANT_IMAGE } from '@/constants/images';
import { handleError } from '@/utils/errorHandler';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { getRestaurantImageUrl } from '@/utils/imageHelper';

const props = defineProps({
  restaurant: {
    type: Object,
    required: true
  },
  reviewData: {
    type: Object,
    default: () => ({ starDistribution: { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 } })
  }
});

const emit = defineEmits(['toggle-favorite']);

const userStore = useUserStore();
const isFavorite = ref(false);
const router = useRouter();

// 預設圖片
const defaultCoverImage = DEFAULT_RESTAURANT_IMAGE;

const handleImageError = (e) => {
  // 避免 fallback 圖片也載入失敗時無限觸發
  if (e.target.src.includes('default-restaurant') || e.target.src === defaultCoverImage) return;
  console.error('餐廳圖片載入失敗:', props.restaurant.name);
  e.target.src = defaultCoverImage;
};

const toggleFavorite = async () => {
  if (!checkPermission()) return;
  try {
    // 先檢查實際收藏狀態
    const isCurrentlyFavorite = await userStore.checkFavoriteStatus(props.restaurant.id);
    
    if (isCurrentlyFavorite) {
      // 如果已經收藏，就取消收藏
      await userStore.removeFavorite(props.restaurant.id);
      isFavorite.value = false;
      ElMessage.success('已取消收藏');
    } else {
      // 如果還沒收藏，就添加收藏
      await userStore.addFavorite(props.restaurant.id);
      isFavorite.value = true;
      ElMessage.success('收藏成功');
    }
  } catch (error) {
    ElMessage.error('操作失敗，請稍後再試');
  }
};

const safeDistribution = computed(() => {
  return props.reviewData && props.reviewData.starDistribution
    ? props.reviewData.starDistribution
    : { 5: 0, 4: 0, 3: 0, 2: 0, 1: 0 };
});

const displayAverageRating = computed(() => {
  // 先轉成數字再 toFixed(1)
  return Number(props.restaurant.averageRating || 0).toFixed(1);
});

const checkPermission = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('請先登入');
    return false;
  }
  if (!['ROLE_REVIEWER', 'ROLE_ADMIN'].includes(userStore.user?.role)) {
    ElMessage.error('您沒有權限進行此操作');
    return false;
  }
  return true;
};

onMounted(async () => {
  // 等待用戶初始化完成
  if (!userStore.isInitialized) {
    await userStore.initialize();
  }

  // 確保用戶已登入且有正確權限
  if (!userStore.isLoggedIn || !userStore.user?.role) {
    console.log('User not logged in or no role found');
    return;
  }

  // 檢查用戶角色
  if (!['ROLE_REVIEWER', 'ROLE_ADMIN'].includes(userStore.user.role)) {
    console.log('User does not have required role');
    return;
  }

  try {
    const response = await userStore.checkFavoriteStatus(props.restaurant.id);
    isFavorite.value = response;
  } catch (error) {
    console.error('檢查收藏狀態失敗:', error);
    if (error.response?.status === 500) {
      console.log('Server error - skipping favorite status check');
      return;
    }
    if (error.message === '未登入') {
      ElMessage.warning('請先登入');
      router.push('/login');
    } else if (error.message === '您沒有權限查看收藏列表') {
      ElMessage.error('您沒有權限查看收藏列表');
    } else {
      ElMessage.error(error.message || '檢查收藏狀態失敗');
    }
  }
  console.log('props.reviewData:', props.reviewData);
  console.log('props.reviewData.starDistribution:', props.reviewData.starDistribution);
  console.log('props.restaurant.reviewCount:', props.restaurant.reviewCount);
});
</script>

<style scoped>
.restaurant-info-card {
  margin-bottom: 20px;
}

.restaurant-header {
  display: flex;
  gap: 20px;
}

.restaurant-image {
  width: 400px;
  height: 300px;
  border-radius: 8px;
}

.image-placeholder {
  width: 400px;
  height: 300px;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 30px;
  border-radius: 8px;
}

.restaurant-info {
  flex: 1;
}

.restaurant-info h1 {
  margin: 0 0 10px 0;
  font-size: 24px;
  color: #303133;
}

.address,
.phone {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  margin-bottom: 10px;
}

.restaurant-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 15px;
}

.tag {
  margin-right: 5px;
}

.is-favorite {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.review-count {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #606266;
  font-size: 14px;
}

.review-count .el-icon {
  font-size: 16px;
}
</style> 