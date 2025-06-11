<template>
  <div class="restaurant-form-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <h2>{{ isEdit ? '編輯餐廳' : '新增餐廳' }}</h2>
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
        <el-button type="primary" @click="fetchRestaurantDetails">重試</el-button>
      </div>
      
      <div v-else class="form-content">
        <el-form
          ref="formRef"
          :model="restaurantForm"
          :rules="rules"
          label-width="120px"
        >
          <el-form-item label="餐廳名稱" prop="name">
            <el-input v-model="restaurantForm.name" placeholder="請輸入餐廳名稱" />
          </el-form-item>
          
          <el-form-item label="餐廳地址" prop="address">
            <el-input v-model="restaurantForm.address" placeholder="請輸入餐廳地址" />
          </el-form-item>
          
          <el-form-item label="聯絡電話" prop="phone">
            <el-input v-model="restaurantForm.phone" placeholder="請輸入聯絡電話" />
          </el-form-item>
          
          <el-form-item label="餐廳類別" prop="category">
            <el-select v-model="restaurantForm.category" placeholder="請選擇餐廳類別" style="width: 100%">
              <el-option label="中式料理" value="中式料理" />
              <el-option label="日式料理" value="日式料理" />
              <el-option label="韓式料理" value="韓式料理" />
              <el-option label="西式料理" value="西式料理" />
              <el-option label="義式料理" value="義式料理" />
              <el-option label="泰式料理" value="泰式料理" />
              <el-option label="越式料理" value="越式料理" />
              <el-option label="素食料理" value="素食料理" />
              <el-option label="快餐" value="快餐" />
              <el-option label="咖啡廳" value="咖啡廳" />
              <el-option label="甜點店" value="甜點店" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="餐廳描述" prop="description">
            <el-input
              v-model="restaurantForm.description"
              type="textarea"
              :rows="4"
              placeholder="請輸入餐廳描述"
            />
          </el-form-item>
          
          <el-form-item label="封面圖片">
            <el-upload
              class="restaurant-image-uploader"
              action="#"
              :auto-upload="false"
              :on-change="handleImageChange"
              :show-file-list="false"
            >
              <div v-if="imageUrl" class="image-preview-container">
                <img 
                  :src="imageUrl" 
                  class="restaurant-image" 
                  @error="handleImageError"
                />
              </div>
              <el-icon v-else class="restaurant-image-uploader-icon"><Plus /></el-icon>
              <div class="el-upload__tip">
                點擊上傳圖片，只能上傳 jpg/png 文件，且不超過 5MB
              </div>
            </el-upload>
          </el-form-item>
          
          <el-form-item>
            <div class="form-buttons">
              <el-button type="primary" @click="submitForm">保存</el-button>
              <el-button @click="goBack">取消</el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Plus, Delete } from '@element-plus/icons-vue';
import { restaurantApi } from '../services/api';
import { useUserStore } from '../stores/user';
import { defaultRestaurantImage, handleRestaurantImageError } from '../utils/imageHelper';
import { handleError } from '@/utils/errorHandler';

const router = useRouter();
const route = useRoute();
const userStore = useUserStore();
const formRef = ref(null);

// 狀態
const loading = ref(false);
const error = ref(null);
const imageUrl = ref('');
const imageFile = ref(null);

// 判斷是編輯還是新增
const isEdit = computed(() => route.meta.isEdit);
const restaurantId = computed(() => route.params.id);

// 表單數據
const restaurantForm = reactive({
  name: '',
  address: '',
  phone: '',
  category: '',
  description: '',
});

// 表單驗證規則
const rules = {
  name: [
    { required: true, message: '請輸入餐廳名稱', trigger: 'blur' },
    { min: 2, max: 50, message: '長度應為 2 到 50 個字符', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '請輸入餐廳地址', trigger: 'blur' },
    { min: 5, max: 200, message: '長度應為 5 到 200 個字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '請選擇餐廳類別', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '描述不能超過 500 個字符', trigger: 'blur' }
  ]
};

// 獲取餐廳數據（用於編輯）
const fetchRestaurantDetails = async () => {
  try {
    loading.value = true;
    const response = await restaurantApi.getRestaurantById(route.params.id);
    console.log('Restaurant details response:', response.data);
    
    // 處理返回的餐廳數據
    restaurantForm.name = response.data.name;
    restaurantForm.address = response.data.address;
    restaurantForm.phone = response.data.phone;
    restaurantForm.description = response.data.description;
    restaurantForm.category = response.data.category;
    
    // 處理餐廳圖片
    if (response.data.image) {
      imageUrl.value = response.data.image;
      console.log('Setting image URL:', response.data.image);
    } else {
      imageUrl.value = '';  // 不顯示預設圖片
      console.log('No image URL found');
    }
    
  } catch (error) {
    handleError(error);
  } finally {
    loading.value = false;
  }
};

// 處理移除圖片
const handleRemoveImage = () => {
  imageUrl.value = '';
  imageFile.value = null;
};

// 處理圖片變更
const handleImageChange = (file) => {
  const isJPG = file.raw.type === 'image/jpeg';
  const isPNG = file.raw.type === 'image/png';
  const isGIF = file.raw.type === 'image/gif';
  const isLt5M = file.raw.size / 1024 / 1024 < 5;

  if (!isJPG && !isPNG && !isGIF) {
    ElMessage.error('只能上傳 JPG、PNG 或 GIF 格式的圖片！');
    return false;
  }
  
  if (!isLt5M) {
    ElMessage.error('圖片大小不能超過 5MB！');
    return false;
  }
  
  // 預覽圖片
  imageUrl.value = URL.createObjectURL(file.raw);
  imageFile.value = file.raw;
};

// 處理圖片錯誤
const handleImageError = () => {
  imageUrl.value = '';
  imageFile.value = null;
};

// 處理圖片上傳
const handleImageUpload = (file) => {
  imageFile.value = file;
  imageUrl.value = URL.createObjectURL(file);
  return false;
};

// 處理圖片移除
const handleImageRemove = () => {
  imageFile.value = null;
  imageUrl.value = null;
};

// 提交表單
const submitForm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    
    const formData = new FormData();
    formData.append('restaurant', JSON.stringify({
      name: restaurantForm.name,
      description: restaurantForm.description,
      address: restaurantForm.address,
      phone: restaurantForm.phone,
      category: restaurantForm.category
    }));

    // 如果有新圖片，添加新圖片
    if (imageFile.value) {
      formData.append('image', imageFile.value);
      console.log('Adding new image:', {
        name: imageFile.value.name,
        type: imageFile.value.type,
        size: imageFile.value.size
      });
    }
    // 如果是更新且原本有圖片但現在沒有，添加移除標記
    else if (isEdit.value && !imageUrl.value) {
      formData.append('removeImage', 'true');
      console.log('Adding removeImage flag');
    }

    console.log('Form data restaurant:', formData.get('restaurant'));
    console.log('Form data has image:', formData.has('image'));
    console.log('Form data has removeImage:', formData.has('removeImage'));

    if (isEdit.value) {
      await restaurantApi.updateRestaurant(restaurantId.value, formData);
      ElMessage.success('餐廳更新成功');
    } else {
      await restaurantApi.create(formData);
      ElMessage.success('餐廳創建成功');
    }
    
    router.push('/restaurants');
  } catch (error) {
    handleError(error);
  }
};

// 返回上一頁
const goBack = () => {
  router.push('/profile');
};

// 修改 viewRestaurant 方法，當按鈕被點擊時實現餐廳詳情頁導航
const viewRestaurant = (id) => {
  router.push(`/restaurants/${id}`);
};

// 頁面加載時獲取餐廳數據（如果是編輯模式）
onMounted(async () => {
  if (!userStore.checkLogin()) return
  
  try {
    if (route.params.id) {
      await fetchRestaurantDetails()
    }
  } catch (error) {
    handleError(error, '獲取餐廳資料失敗')
  }
})
</script>

<style scoped>
.restaurant-form-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: #303133;
}

.loading-container,
.error-container {
  padding: 20px;
  text-align: center;
}

.error-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
  align-items: center;
}

.form-content {
  padding: 20px 0;
}

.restaurant-image-uploader {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.restaurant-image-uploader :deep(.el-upload) {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.restaurant-image-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.restaurant-image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100%;
  height: 200px;
  text-align: center;
  display: flex;
  justify-content: center;
  align-items: center;
}

.restaurant-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.image-preview-container {
  position: relative;
  width: 100%;
  height: 200px;
}

.image-actions {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  background-color: rgba(255, 255, 255, 0.8);
  border-radius: 50%;
  padding: 2px;
}

.el-upload__tip {
  text-align: center;
  color: #606266;
  font-size: 12px;
  margin-top: 7px;
}

.form-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-start;
}
</style> 