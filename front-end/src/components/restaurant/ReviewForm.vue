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
    form.imageUrl = newReview.imageUrl || '';  // 確保 imageUrl 不為 undefined
    form.imageFile = null;  // 重置 imageFile
    // 如果有現有圖片，設置預覽
    if (newReview.imageUrl) {
      form.imagePreview = newReview.imageUrl;
    } else {
      form.imagePreview = null;
    }
  } else {
    form.rating = 5;
    form.content = '';
    form.imageUrl = '';
    form.imageFile = null;
    form.imagePreview = null;
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
    
    if (form.imageFile) {
      formData.append('image', form.imageFile);
      console.log('Image file details:', {
        name: form.imageFile.name,
        type: form.imageFile.type,
        size: form.imageFile.size
      });
    }

    console.log('Form data review:', formData.get('review'));
    console.log('Form data has image:', formData.has('image'));

    if (props.review) {
      // 更新現有評論
      await reviewStore.updateReview(props.review.id, formData);
    } else {
      // 創建新評論
      await reviewStore.createReview(props.restaurantId, formData);
    }

    ElMessage.success(props.review ? '評論更新成功' : '評論發布成功');
    emit('success');
  } catch (error) {
    console.error('Review submission error:', error);
    ElMessage.error(props.review ? '更新評論失敗' : '發布評論失敗');
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
</style> 