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
          v-model="form.rating"
          :disabled="false"
          :show-text="true"
          :show-distribution="false"
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

const form = reactive({
  rating: 5,
  content: '',
  imageUrl: '',
  imageFile: null
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
    form.imageUrl = newReview.imageUrl || '';  // 確保 imageUrl 不為 undefined
    form.imageFile = null;  // 重置 imageFile
  } else {
    form.rating = 5;
    form.content = '';
    form.imageUrl = '';
    form.imageFile = null;
  }
}, { immediate: true });

const handleImageChange = (file) => {
  if (!file) {
    form.imageFile = null;
    form.imageUrl = '';
    return;
  }

  try {
    // 檢查文件類型
    const isImage = file.raw?.type?.startsWith('image/');
    if (!isImage) {
      ElMessage.error('請上傳圖片文件');
      return;
    }

    // 檢查文件大小
    const isLt5M = file.raw?.size / 1024 / 1024 < 5;
    if (!isLt5M) {
      ElMessage.error('圖片大小不能超過 5MB');
      return;
    }

    form.imageFile = file.raw;
    // 創建一個新的 blob URL
    const blobUrl = URL.createObjectURL(file.raw);
    form.imageUrl = blobUrl;
    
    // 添加日誌
    console.log('Image changed:', {
      hasFile: !!form.imageFile,
      imageUrl: form.imageUrl,
      blobUrl: blobUrl
    });
  } catch (error) {
    console.error('Error creating blob URL:', error);
    ElMessage.error('圖片處理失敗，請重試');
    form.imageFile = null;
    form.imageUrl = '';
  }
};

// 在組件卸載時清理 blob URL
onBeforeUnmount(() => {
  if (form.imageUrl && form.imageUrl.startsWith('blob:')) {
    URL.revokeObjectURL(form.imageUrl);
  }
});

const handleClose = () => {
  dialogVisible.value = false;
  formRef.value?.resetFields();
  form.imageUrl = '';
  form.imageFile = null;
};

const handleSubmit = async () => {
  if (!userStore.checkLogin()) return;
  
  try {
    submitting.value = true;
    
    // 驗證表單
    await formRef.value.validate();
    
    // 檢查評論內容是否為空或只包含空格
    if (!form.content.trim()) {
      ElMessage.warning('評論內容不能為空');
      return;
    }
    
    // 檢查評論內容長度
    if (form.content.length > 1000) {
      ElMessage.warning('評論內容不能超過1000字');
      return;
    }
    
    // 添加日誌
    console.log('Form Data:', {
      rating: form.rating,
      content: form.content.trim(),
      imageFile: form.imageFile ? 'Has file' : 'No file',
      imageUrl: form.imageUrl
    });
    
    const formData = new FormData();
    formData.append('rating', form.rating);
    formData.append('content', form.content);
    
    if (form.imageFile) {
      formData.append('file', form.imageFile);
    }
    
    if (isEdit.value) {
      await reviewStore.updateReview(props.restaurantId, props.review.id, formData);
      ElMessage.success('評論更新成功');
    } else {
      await reviewStore.createReview(props.restaurantId, formData);
      ElMessage.success('評論發布成功');
    }
    
    dialogVisible.value = false;
    emit('success');
  } catch (error) {
    handleError(error, isEdit.value ? '評論更新失敗' : '評論發布失敗');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 