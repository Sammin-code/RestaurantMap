<template>
  <div class="container mx-auto px-4 py-8">
    <!-- 歡迎橫幅 -->
    <div class="welcome-banner">
      <h1>歡迎來到餐廳地圖</h1>
      <p>發現並分享您最愛的餐廳</p>
      <el-button type="primary" size="large" @click="router.push('/restaurants')">
        瀏覽餐廳
      </el-button>
    </div>
  </div>

  <!-- 熱門餐廳區塊（移到外面，這樣就會貼齊左邊，橫向排列） -->
  <div class="mb-8 w-full text-left">
    <h2 class="text-2xl font-bold mb-4 text-left">熱門餐廳</h2>
    <div class="restaurant-grid">
      <el-row :gutter="20">
        <el-col
          v-for="restaurant in restaurantStore.popularRestaurants"
          :key="restaurant.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <RestaurantCard
            :restaurant="restaurant"
            :default-image="defaultCoverImage"
            @toggle-favorite="handleToggleFavorite"
          />
        </el-col>
      </el-row>
    </div>
  </div>

  <!-- 最新餐廳（和熱門餐廳一模一樣橫向排列） -->
  <div class="mb-8 w-full text-left">
    <h2 class="text-2xl font-bold mb-4 text-left">最新餐廳</h2>
    <div class="restaurant-grid">
      <el-row :gutter="20">
        <el-col
          v-for="restaurant in restaurantStore.latestRestaurants"
          :key="restaurant.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <RestaurantCard
            :restaurant="restaurant"
            :default-image="defaultCoverImage"
            @toggle-favorite="handleToggleFavorite"
          />
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useRestaurantStore } from '@/stores/restaurant';
import RestaurantCard from '@/components/restaurant/RestaurantCard.vue';
import { handleError } from '@/utils/errorHandler';
import { useUserStore } from '@/stores/user';

const router = useRouter();
const restaurantStore = useRestaurantStore();
const userStore = useUserStore();
const loading = ref(false);

// 預設圖片
const defaultCoverImage = 'https://images.pexels.com/photos/67468/pexels-photo-67468.jpeg?auto=compress&cs=tinysrgb&w=800&h=600&dpr=1';

// 獲取熱門餐廳
const fetchPopularRestaurants = async () => {
  try {
    await restaurantStore.fetchPopularRestaurants();
  } catch (error) {
    handleError(error);
  }
};

// 獲取最新餐廳
const fetchLatestRestaurants = async () => {
  try {
    await restaurantStore.fetchLatestRestaurants();
  } catch (error) {
    handleError(error);
  }
};

// 處理收藏切換
const handleToggleFavorite = async (restaurantId) => {
  try {
    // 檢查登入狀態和角色
    if (!userStore.isLoggedIn) {
      ElMessage.warning('請先登入');
      router.push('/login');
      return;
    }

    if (userStore.user?.role !== 'ROLE_REVIEWER') {
      ElMessage.error('只有評論者可以收藏餐廳');
      return;
    }

    await restaurantStore.toggleFavorite(restaurantId);
  } catch (error) {
    handleError(error);
  }
};

onMounted(async () => {
  try {
    // 1. 等待路由完全準備好
    await router.isReady();
    
    // 2. 確保用戶狀態已初始化
    if (!userStore.isInitialized) {
      await userStore.initialize();
    }

    // 3. 等待一小段時間確保所有狀態都已更新
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 4. 按順序獲取數據
    await fetchPopularRestaurants();
    
    // 5. 等待一小段時間確保熱門餐廳數據已處理完成
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 6. 再獲取最新餐廳
    await fetchLatestRestaurants();
    
  } catch (error) {
    handleError(error);
  }
});
</script>

<style scoped>
.home-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-banner {
  text-align: center;
  padding: 60px 20px;
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  color: white;
  border-radius: 8px;
  margin-bottom: 40px;
}

.welcome-banner h1 {
  font-size: 2.5em;
  margin-bottom: 10px;
}

.welcome-banner p {
  font-size: 1.2em;
  margin-bottom: 20px;
  opacity: 0.9;
}

.restaurant-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  color: #303133;
}

.restaurant-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
</style> 