<template>
  <div class="restaurant-list-container">
    <!-- 搜索和篩選區域 -->
    <el-card class="filter-card">
      <div class="filter-header">
        <el-form :inline="true" :model="filterForm" class="filter-form">
          <el-form-item label="搜索">
            <el-input
              v-model="filterForm.keyword"
              placeholder="餐廳名稱或地址"
              clearable
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </el-form-item>
          
          <el-form-item label="分類">
            <el-select v-model="filterForm.category" placeholder="選擇分類" clearable style="width: 250px;">
              <el-option
                v-for="category in categories"
                :key="category.value"
                :label="category.label"
                :value="category.value"
              />
              <template #suffix>
                <span v-if="filterForm.category" class="selected-value">
                  已選擇: {{ filterForm.category }}
                </span>
              </template>
            </el-select>
          </el-form-item>
          
          <el-form-item label="評分">
            <el-select v-model="filterForm.minRating" placeholder="最低評分" clearable style="width: 250px;">
              <el-option
                v-for="rating in ratingOptions"
                :key="rating.value"
                :label="rating.label"
                :value="rating.value"
              />
              <template #suffix>
                <span v-if="filterForm.minRating" class="selected-value">
                  已選擇: {{ getRatingLabel(filterForm.minRating) }}
                </span>
              </template>
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="resetFilter">重置</el-button>
          </el-form-item>
        </el-form>
        <el-button 
          type="success" 
          @click="handleAddClick"
        >
          <el-icon><Plus /></el-icon>
          新增餐廳
        </el-button>
      </div>
      
      <!-- 顯示已選過濾條件 -->
      <div v-if="hasActiveFilters" class="active-filters">
        <span class="filter-label">已選過濾條件:</span>
        <el-tag 
          v-if="filterForm.category" 
          closable 
          @close="filterForm.category = ''; handleSearch()"
          type="success"
          effect="light"
          class="filter-tag"
        >
          分類: {{ filterForm.category }}
        </el-tag>
        <el-tag 
          v-if="filterForm.minRating" 
          closable 
          @close="filterForm.minRating = ''; handleSearch()"
          type="warning"
          effect="light"
          class="filter-tag"
        >
          最低評分: {{ getRatingLabel(filterForm.minRating) }}
        </el-tag>
        <el-tag 
          v-if="filterForm.keyword" 
          closable 
          @close="filterForm.keyword = ''; handleSearch()"
          type="info"
          effect="light"
          class="filter-tag"
        >
          關鍵字: {{ filterForm.keyword }}
        </el-tag>
      </div>
    </el-card>

    <!-- 新增餐廳對話框 -->
    <el-dialog
      v-model="addRestaurantDialogVisible"
      title="新增餐廳"
      width="50%"
    >
      <el-form
        ref="addRestaurantFormRef"
        :model="addRestaurantForm"
        :rules="addRestaurantRules"
        label-position="top"
      >
        <el-form-item label="餐廳名稱" prop="name">
          <el-input v-model="addRestaurantForm.name" placeholder="請輸入餐廳名稱" />
        </el-form-item>
        
        <el-form-item label="地址" prop="address">
          <el-input v-model="addRestaurantForm.address" placeholder="請輸入餐廳地址" />
        </el-form-item>
        
        <el-form-item label="電話" prop="phone">
          <el-input v-model="addRestaurantForm.phone" placeholder="請輸入餐廳電話" />
        </el-form-item>
        
        <el-form-item label="分類" prop="category">
          <el-select v-model="addRestaurantForm.category" placeholder="請選擇分類">
            <el-option
              v-for="category in categories"
              :key="category.value"
              :label="category.label"
              :value="category.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="addRestaurantForm.description"
            type="textarea"
            :rows="3"
            placeholder="請輸入餐廳描述"
          />
        </el-form-item>
        
        <el-form-item label="餐廳圖片">
          <el-upload
            class="restaurant-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
          >
            <img v-if="addRestaurantForm.coverImage && addRestaurantForm.coverImage.url" :src="addRestaurantForm.coverImage.url" class="uploaded-image" />
            <div v-else class="upload-placeholder">
              <el-icon class="uploader-icon"><Plus /></el-icon>
              <div>點擊上傳圖片</div>
            </div>
          </el-upload>
          <div class="upload-tip">點擊上傳餐廳圖片，建議尺寸 800x600 像素</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addRestaurantDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleAddRestaurant" :loading="addingRestaurant">
            確認新增
          </el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 餐廳列表 -->
    <div class="restaurant-grid">
      <el-row :gutter="20">
        <el-col
          v-for="restaurant in processedRestaurants"
          :key="restaurant.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <RestaurantCard
            :restaurant="restaurant"
            :default-image="defaultRestaurantImage"
            :show-delete-button="true"
            @toggle-favorite="handleToggleFavorite"
            @delete="handleDelete"
          >
            <el-image
              :src="getRestaurantImageUrl(restaurant)"
              :preview-src-list="[getRestaurantImageUrl(restaurant)]"
              fit="cover"
              @error="(e) => handleImageError(e, restaurant)"
            >
              <!-- ... image slots ... -->
            </el-image>
          </RestaurantCard>
        </el-col>
      </el-row>
    </div>

    <!-- 分頁器 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[8, 16, 24, 32]"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        layout="total, sizes, prev, pager, next, jumper"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, computed, onUnmounted, onBeforeUnmount } from 'vue';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/stores/user';
import { useRestaurantStore } from '@/stores/restaurant';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search, Picture, Plus, Delete, Edit, Star, StarFilled, Loading } from '@element-plus/icons-vue';
import { restaurantApi } from '@/services/api';
import { defaultRestaurantImage, handleRestaurantImageError, getRestaurantImageUrl } from '../utils/imageHelper';
import { handleError } from '@/utils/errorHandler';
import RestaurantCard from '@/components/restaurant/RestaurantCard.vue';

const router = useRouter();
const userStore = useUserStore();
const restaurantStore = useRestaurantStore();
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(8);
const loading = ref(false);
const sortBy = ref('id');
const sortDirection = ref('DESC');
const reviews = ref([]);
const totalElements = ref(0);
const restaurants = ref([]);

// 篩選表單
const filterForm = reactive({
  keyword: '',
  category: '',
  minRating: ''
});

// 分類選項
const categories = [
  { value: '中式', label: '中式' },
  { value: '西式', label: '西式' },
  { value: '韓式', label: '韓式' },
  { value: '日式', label: '日式' },
  { value: '泰式', label: '泰式' },
  { value: '速食', label: '速食' },
  { value: '甜點', label: '甜點' },
  { value: '飲料', label: '飲料' }
];

// 評分選項
const ratingOptions = [
  { value: 4, label: '4星以上' },
  { value: 3, label: '3星以上' },
  { value: 2, label: '2星以上' },
  { value: 1, label: '1星以上' }
];

// 添加收藏相關的狀態和方法
const favoriteRestaurants = ref([]);

// 解析 JWT 令牌的函數
const parseJwt = (token) => {
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    console.error('Error parsing JWT token:', e);
    return {};
  }
};

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

// 切換收藏狀態
const handleToggleFavorite = async (restaurant) => {
  try {
    await restaurantStore.toggleFavorite(restaurant.id);
  } catch (error) {
    console.error('收藏操作失敗:', error);
    handleError(error, '收藏操作失敗');
  }
};

// 添加一個計算屬性來處理餐廳數據
const processedRestaurants = computed(() => {
  return restaurants.value.map(restaurant => ({
    ...restaurant,
    coverImage: restaurant.coverImage || defaultRestaurantImage
  }));
});

// 獲取餐廳列表
const fetchRestaurants = async () => {
  try {
    loading.value = true;
    
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value,
      sort: `${sortBy.value},${sortDirection.value.toLowerCase()}`
    };
    
    // 添加過濾條件
    if (filterForm.category) {
      params.category = filterForm.category;
    }
    
    if (filterForm.minRating) {
      params.minRating = filterForm.minRating;
    }
    
    if (filterForm.keyword) {
      params.keyword = filterForm.keyword;
    }
    
    const response = await restaurantApi.getRestaurants(params);
    restaurants.value = response.data.content;
    total.value = response.data.totalElements;
    
  } catch (error) {
    console.error('獲取餐廳時發生意外錯誤:', error);
    ElMessage.error('獲取餐廳列表失敗，請稍後再試');
  } finally {
    loading.value = false;
  }
};

// 搜索
const handleSearch = () => {
  currentPage.value = 1;
  fetchRestaurants();
};

// 重置篩選
const resetFilter = () => {
  filterForm.keyword = '';
  filterForm.category = '';
  filterForm.minRating = '';
  currentPage.value = 1;
  fetchRestaurants();
};

// 處理每頁顯示數量變化
const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;  // 重置為第一頁
  fetchRestaurants();
};

// 處理頁碼變化
const handlePageChange = (page) => {
  currentPage.value = page;
  fetchRestaurants();
};

// 新增餐廳相關
const addRestaurantDialogVisible = ref(false);
const addRestaurantFormRef = ref(null);
const addingRestaurant = ref(false);

const addRestaurantForm = reactive({
  name: '',
  address: '',
  phone: '',
  category: '',
  description: '',
  id: null,
  coverImage: null  // 改為初始值為null
});

const addRestaurantRules = {
  name: [
    { required: true, message: '請輸入餐廳名稱', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '請輸入餐廳地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '請輸入餐廳電話', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '請選擇餐廳分類', trigger: 'change' }
  ],
  description: [
    { required: true, message: '請輸入餐廳描述', trigger: 'blur' }
  ]
};

const showAddRestaurantDialog = () => {
  if (!userStore.token) {
    ElMessage.warning('請先登入');
    router.push('/login');
    return;
  }
  addRestaurantForm.name = '';
  addRestaurantForm.address = '';
  addRestaurantForm.phone = '';
  addRestaurantForm.category = '';
  addRestaurantForm.description = '';
  addRestaurantForm.id = null;
  addRestaurantForm.coverImage = null; // 重置為null
  addRestaurantDialogVisible.value = true;
};

const handleAddRestaurant = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('請先登入');
    router.push('/login');
    return;
  }

  // 檢查用戶角色
  if (userStore.user.role !== 'ROLE_REVIEWER') {
    ElMessage.warning('只有評論者可以新增餐廳');
    return;
  }
  
  if (!addRestaurantFormRef.value) return;
  
  try {
    await addRestaurantFormRef.value.validate();
    addingRestaurant.value = true;
    
    const formData = new FormData();
    
    const restaurantData = {
      name: addRestaurantForm.name,
      description: addRestaurantForm.description,
      address: addRestaurantForm.address,
      phone: addRestaurantForm.phone,
      category: addRestaurantForm.category,
      createdByUsername: userStore.user.username
    };
    
    console.log('Restaurant data:', restaurantData);
    console.log('User store token:', userStore.token);
    
    // 將餐廳數據作為字符串添加到 FormData
    formData.append('restaurant', new Blob([JSON.stringify(restaurantData)], {
      type: 'application/json'
    }), 'restaurant.json');
    
    // 添加圖片
    if (addRestaurantForm.coverImage && addRestaurantForm.coverImage.file) {
      console.log('Adding image to FormData:', addRestaurantForm.coverImage.file);
      formData.append('image', addRestaurantForm.coverImage.file, addRestaurantForm.coverImage.file.name);
    }
    
    // 印出 formData 內容
    for (let pair of formData.entries()) {
      console.log('FormData:', pair[0], pair[1]);
    }
    
    let response;
    if (addRestaurantForm.id) {
      // 更新餐廳
      response = await restaurantApi.updateRestaurant(addRestaurantForm.id, formData);
      ElMessage.success('更新餐廳成功');
    } else {
      // 新增餐廳
      console.log('Creating new restaurant with data:', restaurantData);
      console.log('FormData entries:', Array.from(formData.entries()));
      try {
        response = await restaurantApi.create(formData);
        
        // 驗證響應數據
        if (!response.data || !response.data.id) {
          console.error('Invalid response data:', response.data);
          throw new Error('新增餐廳失敗：返回數據不完整');
        }
        
        console.log('Create response:', response);
        ElMessage.success('新增餐廳成功');
        
        // 關閉對話框並重置表單
        addRestaurantDialogVisible.value = false;
        resetForm();
        
        // 延遲刷新列表，確保後端數據已經完全處理
        setTimeout(async () => {
          await fetchRestaurants();
          // 確保新創建的餐廳不會被標記為已收藏
          const newRestaurant = restaurants.value.find(r => r.id === response.data.id);
          if (newRestaurant) {
            newRestaurant.isFavorite = false;
          }
        }, 1000);
      } catch (error) {
        console.error('新增餐廳失敗:', error);
        if (error.response?.status === 403) {
          ElMessage.error('您沒有權限新增餐廳');
        } else if (error.response?.status === 500) {
          ElMessage.error('伺服器錯誤，請稍後再試');
        } else if (error.response?.data?.message) {
          ElMessage.error(error.response.data.message);
        } else {
          ElMessage.error(error.message || '新增餐廳失敗，請稍後再試');
        }
        // 不要關閉對話框，讓用戶可以修改後重試
      } finally {
        addingRestaurant.value = false;
      }
    }
  } catch (error) {
    console.error('Operation error:', error);
    if (error.response?.status === 401) {
      ElMessage.error('登入已過期，請重新登入');
      userStore.logout();
      router.push('/login');
    } else {
      handleError(error);
    }
  }
};

// 重置表單的輔助函數
const resetForm = () => {
  addRestaurantForm.name = '';
  addRestaurantForm.description = '';
  addRestaurantForm.address = '';
  addRestaurantForm.phone = '';
  addRestaurantForm.category = '';
  addRestaurantForm.coverImage = null;
  if (addRestaurantFormRef.value) {
    addRestaurantFormRef.value.resetFields();
  }
};

// 查看詳情
const viewDetails = (restaurant) => {
  router.push(`/restaurants/${restaurant.id}`);
};

// 檢查是否為管理員或創建者
const isAdminOrCreator = (restaurant) => {
  const currentUser = userStore.user;
  console.log('Current user:', currentUser);
  console.log('Restaurant:', restaurant);
  console.log('User role:', currentUser?.role);
  console.log('Restaurant creator:', restaurant.createdBy);
  console.log('Username:', currentUser?.username);
  
  if (!currentUser) {
    console.log('No current user');
    return false;
  }
  
  const isAdmin = currentUser.role === 'ROLE_ADMIN';
  // 如果 createdBy 為 undefined，則認為當前用戶是創建者
  const isCreator = !restaurant.createdBy || currentUser.username === restaurant.createdBy;
  
  console.log('Is admin:', isAdmin);
  console.log('Is creator:', isCreator);
  
  return isAdmin || isCreator;
};

// 編輯餐廳
const handleEdit = (restaurant) => {
  console.log('Editing restaurant:', restaurant);
  addRestaurantForm.name = restaurant.name;
  addRestaurantForm.address = restaurant.address;
  addRestaurantForm.phone = restaurant.phone;
  addRestaurantForm.category = restaurant.category;
  addRestaurantForm.description = restaurant.description;
  addRestaurantForm.id = restaurant.id;
  addRestaurantForm.coverImage = restaurant.coverImage;
  addRestaurantDialogVisible.value = true;
};

// 刪除餐廳
const handleDelete = async (restaurantId) => {
  try {
    await ElMessageBox.confirm(
      '確定要刪除這家餐廳嗎？此操作不可恢復。',
      '警告',
      {
        confirmButtonText: '確定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );
    await restaurantApi.deleteRestaurant(restaurantId);
    ElMessage.success('餐廳已成功刪除');
    fetchRestaurants();
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Delete error:', error);
      ElMessage.error('刪除餐廳失敗');
    }
  }
};

// 修改 handleImageChange 函數，添加檔案驗證
const handleImageChange = async (file) => {
  try {
    // 檢查檔案類型
    if (!file.raw.type.startsWith('image/')) {
      ElMessage.error('請上傳圖片檔案');
      return;
    }

    // 檢查檔案大小（5MB）
    if (file.raw.size > 5 * 1024 * 1024) {
      ElMessage.error('圖片大小不能超過 5MB');
      return;
    }

    // 壓縮圖片
    const compressedFile = await compressImage(file.raw);
    
    // 更新表單
    addRestaurantForm.coverImage = {
      file: compressedFile,
      url: URL.createObjectURL(compressedFile)
    };
  } catch (error) {
    console.error('圖片處理錯誤:', error);
    ElMessage.error('圖片處理失敗，請重試');
  }
};

// 圖片壓縮函數
const compressImage = (file) => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target.result;
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let width = img.width;
        let height = img.height;
        
        // 如果圖片太大，等比例縮小
        const MAX_WIDTH = 800;
        const MAX_HEIGHT = 600;
        
        if (width > height) {
          if (width > MAX_WIDTH) {
            height = Math.round((height * MAX_WIDTH) / width);
            width = MAX_WIDTH;
          }
        } else {
          if (height > MAX_HEIGHT) {
            width = Math.round((width * MAX_HEIGHT) / height);
            height = MAX_HEIGHT;
          }
        }
        
        canvas.width = width;
        canvas.height = height;
        
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);
        
        // 轉換為 Blob
        canvas.toBlob((blob) => {
          // 創建新的 File 物件
          const compressedFile = new File([blob], file.name, {
            type: 'image/jpeg',
            lastModified: Date.now()
          });
          
          console.log('圖片壓縮結果:', {
            原始大小: (file.size / 1024 / 1024).toFixed(2) + 'MB',
            壓縮後大小: (compressedFile.size / 1024 / 1024).toFixed(2) + 'MB',
            原始尺寸: `${img.width}x${img.height}`,
            壓縮後尺寸: `${width}x${height}`
          });
          
          resolve(compressedFile);
        }, 'image/jpeg', 0.8); // 使用 JPEG 格式，品質 0.8
      };
      img.onerror = reject;
    };
    reader.onerror = reject;
  });
};

// 添加一個計算屬性，檢查是否有任何活躍的過濾條件
const hasActiveFilters = computed(() => {
  return filterForm.keyword || filterForm.category || filterForm.minRating;
});

// 獲取評分選項標籤文字的函數
const getRatingLabel = (rating) => {
  const option = ratingOptions.find(opt => opt.value === rating);
  return option ? option.label : '';
};

// 處理圖片載入錯誤
const handleImageError = (e, restaurant) => {
  // 使用統一的圖片錯誤處理邏輯
  handleRestaurantImageError(e, restaurant, restaurantApi.defaults.baseURL || 'http://localhost:8080');
};

// 預處理餐廳數據以準備顯示
const prepareRestaurants = (restaurants) => {
  return restaurants.map(restaurant => {
    // 處理圖片路徑
    if (restaurant.coverImage) {
      restaurant.originalImagePath = restaurant.coverImage;
      restaurant.coverImage = getRestaurantImageUrl(
        restaurant.coverImage, 
        restaurantApi.defaults.baseURL || 'http://localhost:8080'
      );
    } else {
      restaurant.coverImage = defaultRestaurantImage;
    }

    // 確保評分和評論數有值
    restaurant.averageRating = restaurant.averageRating || 0;
    restaurant.reviewCount = restaurant.reviewCount || 0;

    // 格式化評分到小數點後一位
    if (typeof restaurant.averageRating === 'number') {
      restaurant.averageRating = Number(restaurant.averageRating.toFixed(1));
    }

    return restaurant;
  });
};

onMounted(() => {
  fetchRestaurants();
});

// 添加 onBeforeUnmount
onBeforeUnmount(() => {
  console.log('=== RestaurantList 組件卸載 ===');
  // 清空收藏列表，避免在組件卸載後還嘗試更新狀態
  favoriteRestaurants.value = [];
});

// 新增餐廳按鈕
const handleAddClick = () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('請先登入後再新增餐廳');
    return;
  }

  // 檢查用戶角色
  if (userStore.user.role !== 'ROLE_REVIEWER') {
    ElMessage.warning('只有評論者可以新增餐廳');
    return;
  }

  showAddRestaurantDialog();
};
</script>

<style scoped>
/* 保留必要的樣式，刪除未使用的樣式 */
.restaurant-list-container {
  padding: 20px;
}

/* 餐廳卡片樣式 */
.restaurant-card {
  margin-bottom: 20px;
  transition: all 0.3s;
  border-radius: 8px;
  overflow: hidden;
}

.restaurant-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.restaurant-image {
  height: 200px;
  overflow: hidden;
}

.restaurant-image .el-image {
  width: 100%;
  height: 100%;
}

.image-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}

.image-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.image-loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #409eff;
}

.image-loading .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
  animation: rotate 1.5s linear infinite;
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.restaurant-info {
  padding: 15px;
}

.restaurant-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.info-row {
  display: flex;
  align-items: center;
  margin-bottom: 6px;
  font-size: 12px;
  flex: 1;
  margin-right: 8px;
}

.info-label {
  color: #909399;
  margin-right: 4px;
  flex-shrink: 0;
  min-width: 42px;
  text-align: right;
}

.info-value {
  color: #606266;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.info-value.category {
  color: #409EFF;  /* 保持分類的藍色 */
}

.rating-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 13px;
}

.rating-row {
  display: flex;
  align-items: center;
  color: #f59e42;
}

.star {
  font-size: 14px;
  margin-right: 2px;
}

.rating {
  font-weight: 500;
}

.review-count {
  color: #909399;
  font-size: 12px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}

.actions .el-button {
  padding: 5px 12px;
  font-size: 12px;
}

/* 篩選區域樣式 */
.filter-card {
  margin-bottom: 20px;
  border-radius: 8px;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

/* 分頁樣式 */
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

/* 餐廳網格樣式 */
.restaurant-grid {
  margin-top: 20px;
}

/* 圖片上傳區域樣式 */
.restaurant-uploader {
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 300px;
  height: 200px;
  margin: 0 auto;
  background-color: #fafafa;
  transition: border-color 0.3s;
}

.restaurant-uploader:hover {
  border-color: #409eff;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  color: #8c939d;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 30px;
  height: 30px;
  margin-bottom: 8px;
}

.uploaded-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  max-width: 300px;
  max-height: 200px;
}

.upload-tip {
  font-size: 12px;
  color: #606266;
  margin-top: 7px;
  text-align: center;
}

/* 對話框樣式優化 */
:deep(.el-dialog__body) {
  padding: 20px 25px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-dialog__title) {
  font-weight: 600;
  font-size: 18px;
}

:deep(.el-dialog__footer) {
  border-top: 1px solid #ebeef5;
  padding: 15px 25px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  width: 100%;
}

/* 已選過濾條件樣式 */
.active-filters {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.filter-label {
  margin-right: 10px;
  font-weight: bold;
  color: #606266;
}

.filter-tag {
  margin-right: 8px;
  margin-bottom: 5px;
}

/* 加入選擇值的顯示樣式 */
.selected-value {
  margin-left: 10px;
  color: #409eff;
  font-weight: bold;
}
</style>