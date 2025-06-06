<template>
  <div class="rating-container">
    <!-- 評分顯示 -->
    <div class="rating-display" v-if="showRating">
      <el-rate
        v-model="ratingValue"
        :disabled="disabled"
        :show-score="false"
        :texts="texts"
        :show-text="showText"
        :colors="colors"
        :size="size"
        @change="handleRatingChange"
      />
      <span class="rating-score">{{ Number(ratingValue).toFixed(1) }}</span>
      <span v-if="showCount" class="rating-count">({{ count }} 則評論)</span>
    </div>

    <!-- 評分分布 -->
    <div class="rating-distribution" v-if="showDistribution">
      <div v-for="n in 5" :key="n" class="distribution-item">
        <span class="rating-label">{{ 6 - n }}星</span>
        <el-progress
          :percentage="getRatingPercentage(6 - n)"
          :color="getRatingColor(6 - n)"
          :show-text="false"
        />
        <span class="rating-count">{{ distribution?.[6 - n] || 0 }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';

const props = defineProps({
  modelValue: {
    type: Number,
    default: 0
  },
  reviews: {
    type: Array,
    default: () => []
  },
  disabled: {
    type: Boolean,
    default: false
  },
  showScore: {
    type: Boolean,
    default: true
  },
  showText: {
    type: Boolean,
    default: false
  },
  showCount: {
    type: Boolean,
    default: true
  },
  showRating: {
    type: Boolean,
    default: true
  },
  showDistribution: {
    type: Boolean,
    default: false
  },
  texts: {
    type: Array,
    default: () => ['很差', '差', '一般', '好', '很好']
  },
  colors: {
    type: Array,
    default: () => ['#F56C6C', '#F56C6C', '#E6A23C', '#95D475', '#67C23A']
  },
  size: {
    type: String,
    default: 'default'
  },
  total: {
    type: Number,
    default: null
  },
  distribution: {
    type: Object,
    default: () => ({})
  }
});

const emit = defineEmits(['update:modelValue', 'change']);

const ratingValue = ref(props.modelValue);
const count = computed(() => props.total !== null ? props.total : props.reviews.length);

// 計算評分分布百分比
const getRatingPercentage = (rating) => {
  const total = Object.values(props.distribution).reduce((sum, v) => sum + v, 0);
  return total > 0 ? Math.round((props.distribution[rating] || 0) / total * 100) : 0;
};

// 獲取評分數量
const getRatingCount = (rating) => {
  return props.reviews.filter(r => r?.rating === rating).length;
};

// 獲取評分顏色
const getRatingColor = (rating) => {
  const colors = {
    5: '#67C23A',
    4: '#95D475',
    3: '#E6A23C',
    2: '#F56C6C',
    1: '#F56C6C'
  };
  return colors[rating] || '#909399';
};

const handleRatingChange = (value) => {
  emit('update:modelValue', value);
  emit('change', value);
};
</script>

<style scoped>
.rating-container {
  width: 100%;
}

.rating-display {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.rating-count {
  color: #909399;
  font-size: 14px;
}

.rating-distribution {
  margin-top: 15px;
}

.distribution-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.rating-label {
  width: 40px;
  color: #606266;
  font-size: 14px;
}

:deep(.el-progress) {
  flex: 1;
}

:deep(.el-progress-bar__outer) {
  background-color: #f5f7fa;
}

:deep(.el-rate) {
  --el-rate-star-size: 20px;
  --el-rate-star-margin: 2px;
}

:deep(.el-rate__icon) {
  font-size: 20px;
}

.rating-count-small {
  color: #909399;
  font-size: 12px;
  min-width: 30px;
  text-align: right;
}
</style> 