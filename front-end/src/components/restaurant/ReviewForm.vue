<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '編輯評論' : '發表評論'"
    width="50%"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-position="top"
    >
      <el-form-item label="評分" prop="rating">
        <Rating
          :model-value="form.rating"
          :disabled="false"
          :show-text="true"
          :show-distribution="false"
          @update:model-value="(value) => {
            console.log('Rating changed:', value);
            form.rating = Number(value);
          }"
        />
      </el-form-item>
      
      <el-form-item label="評論內容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          placeholder="請輸入您的評論"
        />
      </el-form-item>
      
      <el-form-item label="上傳圖片">
        <ImageUploader
          v-model="form.imageUrl"
          placeholder-text="點擊上傳評論圖片"
          tip="支援 jpg、png 格式，大小不超過 5MB"
          @change="handleImageChange"
        />
      </el-form-item>
    </el-form>
    
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '更新' : '發表' }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch, computed, onBeforeUnmount } from 'vue';
import { ElMessage } from 'element-plus';
import { useReviewStore } from '@/stores/review';
import { useUserStore } from '@/stores/user';
import Rating from '@/components/common/Rating.vue';
import ImageUploader from '@/components/common/ImageUploader.vue';
import { handleError } from '@/utils/errorHandler';
import { useRouter } from 'vue-router';
import { reviewApi } from '@/services/api';
import { getRestaurantImageUrl } from '@/utils/imageHelper';

const props = defineProps({
  visible: {
    type: Boolean,
    required: true
  },
  restaurantId: {
    type: [Number, String],
    required: true
  },
  review: {
    type: Object,
    default: null
  }
});

const emit = defineEmits(['update:visible', 'success']);

const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

const isEdit = computed(() => !!props.review);

const formRef = ref(null);
const submitting = ref(false);
const reviewStore = useReviewStore();
const userStore = useUserStore();
const router = useRouter();

const form = reactive({
  rating: 5,
  content: '',
  imageUrl: '',
  imageFile: null,
  imagePreview: null
});

const rules = {
  rating: [
    { required: true, message: '請選擇評分', trigger: 'change' }
  ],
  content: [
    { required: true, message: '請輸入評論內容', trigger: 'blur' }
  ]
};

watch(() => props.review, (newReview) => {
  if (newReview) {
    form.rating = newReview.rating;
    form.content = newReview.content;
    form.imageUrl = getRestaurantImageUrl(newReview);
    form.imageFile = null;
  } else {
    form.rating = 5;
    form.content = '';
    form.imageUrl = '';
    form.imageFile = null;
  }
}, { immediate: true });

const handleImageChange = (file) => {
  if (file) {
    // 檢查文件類型
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      ElMessage.error('只能上傳圖片文件');
      return;
    }

    // 檢查文件大小（限制為 5MB）
    const isLt5M = file.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      ElMessage.error('圖片大小不能超過 5MB');
      return;
    }

    form.imageFile = file;
    // 創建預覽 URL
    form.imagePreview = URL.createObjectURL(file);
  }
};

const handleClose = () => {
  dialogVisible.value = false;
  formRef.value?.resetFields();
  form.imageUrl = '';
  form.imageFile = null;
  form.imagePreview = null;
};

const handleSubmit = async () => {
  if (!form.rating) {
    ElMessage.warning('請選擇評分');
    return;
  }

  if (!form.content.trim()) {
    ElMessage.warning('請輸入評論內容');
    return;
  }

  try {
    const reviewData = {
      rating: Number(form.rating),
      content: form.content.trim()
    };

    console.log('Submitting review data:', reviewData);
    console.log('Has image file:', !!form.imageFile);

    const formData = new FormData();
    formData.append('review', JSON.stringify(reviewData));
    
    // 如果有新圖片，添加新圖片
    if (form.imageFile) {
      formData.append('image', form.imageFile);
      console.log('Image file details:', {
        name: form.imageFile.name,
        type: form.imageFile.type,
        size: form.imageFile.size
      });
    } 
    // 如果原本有圖片但現在沒有，添加移除標記
    else if (props.review?.imageUrl && !form.imageUrl) {
      formData.append('removeImage', 'true');
      console.log('Adding removeImage flag');
    }

    console.log('Form data review:', formData.get('review'));
    console.log('Form data has image:', formData.has('image'));
    console.log('Form data has removeImage:', formData.has('removeImage'));

    submitting.value = true;
    if (isEdit.value) {
      await reviewStore.updateReview(props.review.id, formData);
      ElMessage.success('評論已更新');
    } else {
      await reviewStore.createReview(props.restaurantId, formData);
      ElMessage.success('評論已發表');
    }
    
    dialogVisible.value = false;
    emit('success');
  } catch (error) {
    handleError(error);
  } finally {
    submitting.value = false;
  }
};

// 在組件卸載時清理預覽 URL
onBeforeUnmount(() => {
  if (form.imagePreview) {
    URL.revokeObjectURL(form.imagePreview);
  }
});
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.image-preview-container {
  position: relative;
}

.review-image {
  width: 100%;
  height: auto;
  border-radius: 4px;
}

.remove-image-button {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  background-color: rgba(255, 255, 255, 0.8);
  border: none;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}
</style> 