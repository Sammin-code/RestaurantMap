<template>
  <div class="image-uploader">
    <el-upload
      class="uploader"
      action="#"
      :auto-upload="false"
      :show-file-list="false"
      :on-change="handleImageChange"
      :accept="accept"
    >
      <div class="upload-container">
        <img v-if="imageUrl" :src="imageUrl" class="preview-image" />
        <div v-else-if="showDefaultImage" class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">{{ placeholderText }}</div>
        </div>
        <div v-else class="upload-placeholder">
          <el-icon class="upload-icon"><Plus /></el-icon>
          <div class="upload-text">{{ placeholderText }}</div>
        </div>
      </div>
    </el-upload>
    
    <div v-if="imageUrl" class="image-actions">
      <el-button type="danger" size="small" @click="removeImage">
        <el-icon><Delete /></el-icon>
        移除圖片
      </el-button>
    </div>
    
    <div class="upload-tip" v-if="tip">{{ tip }}</div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue';
import { Plus, Delete } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import { handleError } from '@/utils/errorHandler';

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholderText: {
    type: String,
    default: '點擊上傳圖片'
  },
  tip: {
    type: String,
    default: '支援 jpg、png 格式，大小不超過 2MB'
  },
  accept: {
    type: String,
    default: 'image/*'
  },
  maxSize: {
    type: Number,
    default: 2 // MB
  },
  showDefaultImage: {
    type: Boolean,
    default: false
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const imageUrl = ref(props.modelValue);
const imageFile = ref(null);
const defaultImage = 'https://example.com/default-image.jpg'; // 設置預設圖的 URL

watch(() => props.modelValue, (newValue) => {
  imageUrl.value = newValue;
});

const handleImageChange = (file) => {
  if (!file) {
    removeImage();
    return;
  }

  // 檢查文件類型
  const isImage = file.raw?.type?.startsWith('image/');
  if (!isImage) {
    ElMessage.error('請上傳圖片文件');
    return;
  }

  // 檢查文件大小
  const isLtMaxSize = file.raw?.size / 1024 / 1024 < props.maxSize;
  if (!isLtMaxSize) {
    ElMessage.error(`圖片大小不能超過 ${props.maxSize}MB`);
    return;
  }

  try {
    imageFile.value = file.raw;
    // 確保 file.raw 是一個有效的 File 對象
    if (file.raw instanceof File) {
      imageUrl.value = URL.createObjectURL(file.raw);
      emit('update:modelValue', imageUrl.value);
      emit('change', file.raw);
    } else {
      throw new Error('Invalid file object');
    }
  } catch (error) {
    console.error('Error creating blob URL:', error);
    ElMessage.error('圖片處理失敗，請重試');
    removeImage();
  }
};

const removeImage = () => {
  imageUrl.value = '';
  imageFile.value = null;
  emit('update:modelValue', '');
  emit('change', null);
};

const handleUpload = async (file) => {
  try {
    // 檢查文件大小
    if (file.size > 5 * 1024 * 1024) {
      ElMessage.error('圖片大小不能超過 5MB');
      return;
    }

    // 檢查文件類型
    if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.type)) {
      ElMessage.error('只支援 JPG、PNG 和 GIF 格式的圖片');
      return;
    }

    // 上傳圖片
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await fetch('/api/images/upload', {
      method: 'POST',
      body: formData,
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });

    if (!response.ok) {
      throw new Error('上傳失敗');
    }

    const data = await response.json();
    emit('update:modelValue', data);
    emit('change', file);
    ElMessage.success('圖片上傳成功');
  } catch (error) {
    console.error('圖片上傳失敗:', error);
    handleError(error, '圖片上傳失敗，請稍後再試');
  }
};

const handleImageError = (e) => {
  console.error('圖片載入失敗');
  e.target.src = defaultImage;
  ElMessage.warning('圖片載入失敗，已顯示預設圖片');
};
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

.uploader {
  width: 100%;
}

.upload-container {
  width: 100%;
  height: 200px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.upload-container:hover {
  border-color: var(--el-color-primary);
}

.preview-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.upload-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
}

.upload-icon {
  font-size: 28px;
  margin-bottom: 10px;
}

.upload-text {
  font-size: 14px;
}

.image-actions {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.upload-tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
  text-align: center;
}
</style> 