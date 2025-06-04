/**
 * 圖片處理工具函數
 * 負責處理整個應用中餐廳和評論圖片的顯示邏輯
 */

import { DEFAULT_RESTAURANT_IMAGE } from '@/constants/images';

// 默認圖片 URL (當無法載入餐廳圖片時顯示)
export const defaultRestaurantImage = DEFAULT_RESTAURANT_IMAGE;

/**
 * 處理圖片URL，確保它是完整的URL
 * @param {string} url - 原始圖片URL或路徑
 * @returns {string} 處理後的完整URL
 */
export const processImageUrl = (url) => {
  if (!url || typeof url !== 'string') {
    return defaultRestaurantImage;
  }
  
  // 如果是完整的 URL，直接返回
  if (url.startsWith('http')) {
    return url;
  }
  
  // 如果是相對路徑，添加基礎 URL
  if (url.startsWith('/')) {
    return `http://localhost:8080${url}`;
  }
  
  // 如果是圖片 ID 或文件名，使用圖片 API
  return `http://localhost:8080/api/files/image/${url}`;
};

/**
 * 生成備用圖片URL
 * @param {object} item - 餐廳或評論對象
 * @param {boolean} isReview - 是否為評論圖片
 * @returns {string} - 備用圖片URL
 */
export const getBackupImageUrl = (item, isReview = false) => {
  if (isReview) {
    return defaultRestaurantImage;  // 使用相同的預設圖片
  }
  
  return defaultRestaurantImage;
};

/**
 * 統一的圖片錯誤處理邏輯
 * @param {object} item - 餐廳或評論對象
 * @param {string} baseUrl - API基礎URL
 * @param {boolean} isReview - 是否為評論圖片
 */
export const handleImageErrorForObject = (item, baseUrl = 'http://localhost:8080', isReview = false) => {
  const imageProperty = isReview ? 'imageUrl' : 'imageUrl';
  
  if (!item.triedPaths) {
    item.triedPaths = new Set();
  }
  item.triedPaths.add(item[imageProperty]);
  
  // 嘗試不同的 URL 格式
  const possibleUrls = [
    // 原始 URL
    item[imageProperty],
    // API URL
    `${baseUrl}/api/files/image/${item[imageProperty]?.split('/').pop()}`,
    // 公共 URL
    `${baseUrl}/uploads/${item[imageProperty]?.split('/').pop()}`,
    // 備用 URL
    getBackupImageUrl(item, isReview)
  ];

  // 嘗試每個可能的 URL
  for (const url of possibleUrls) {
    if (url && !item.triedPaths.has(url)) {
      item[imageProperty] = url;
      item.triedPaths.add(url);
      return;
    }
  }
  
  // 如果所有嘗試都失敗，使用預設圖片
  item[imageProperty] = isReview ? getBackupImageUrl(item, true) : defaultRestaurantImage;
  
  if (item.imageLoading !== undefined) {
    item.imageLoading = false;
  }
};

/**
 * 統一的圖片處理函數
 * @param {object} restaurant - 餐廳對象
 * @param {string} baseUrl - API基礎URL
 * @param {boolean} isReview - 是否為評論圖片
 * @returns {string} 處理後的圖片URL
 */
export function getRestaurantImageUrl(restaurant, baseUrl = 'https://restaurantmap-255668913932.asia-east1.run.app', isReview = false) {
  if (!restaurant) return defaultRestaurantImage;
  
  // 如果是評論圖片
  if (isReview && restaurant.imageUrl) {
    // 如果是 blob URL，直接返回
    if (restaurant.imageUrl.startsWith('blob:')) {
      return restaurant.imageUrl;
    }
    
    // 如果是完整的 URL（包括 Cloud Storage URL），使用 API 端點
    if (restaurant.imageUrl.startsWith('http')) {
      const fileName = restaurant.imageUrl.split('/').pop();
      return `${baseUrl}/api/images/${fileName}`;
    }
    
    // 如果是 Cloud Storage 路徑，使用 API 端點
    if (restaurant.imageUrl.startsWith('gs://')) {
      const fileName = restaurant.imageUrl.split('/').pop();
      return `${baseUrl}/api/images/${fileName}`;
    }
    
    // 其他情況，使用 API 路徑
    return `${baseUrl}/api/images/${restaurant.imageUrl.replace(/^\/+/, '')}`;
  }
  
  // 如果是餐廳圖片
  if (restaurant.imageUrl) {
    // 如果是完整的 URL（包括 Cloud Storage URL），使用 API 端點
    if (restaurant.imageUrl.startsWith('http')) {
      const fileName = restaurant.imageUrl.split('/').pop();
      return `${baseUrl}/api/images/${fileName}`;
    }
    
    // 如果是 Cloud Storage 路徑，使用 API 端點
    if (restaurant.imageUrl.startsWith('gs://')) {
      const fileName = restaurant.imageUrl.split('/').pop();
      return `${baseUrl}/api/images/${fileName}`;
    }
    
    // 其他情況，使用 API 路徑
    return `${baseUrl}/api/images/${restaurant.imageUrl.replace(/^\/+/, '')}`;
  }
  
  return defaultRestaurantImage;
}

/**
 * 統一的圖片錯誤處理函數
 * @param {Event} event - 圖片錯誤事件
 * @param {object} restaurant - 餐廳對象
 * @param {string} baseUrl - API基礎URL
 * @param {boolean} isReview - 是否為評論圖片
 */
export function handleRestaurantImageError(event, restaurant, baseUrl = 'http://localhost:8080', isReview = false) {
  try {
    if (!restaurant || !restaurant.imageUrl) {
      event.target.src = isReview ? getBackupImageUrl(restaurant, true) : defaultRestaurantImage;
      return;
    }

    // 如果是 blob URL，直接使用備用圖片
    if (restaurant.imageUrl.startsWith('blob:')) {
      event.target.src = isReview ? getBackupImageUrl(restaurant, true) : defaultRestaurantImage;
      return;
    }

    // 如果是完整的 URL（包括 Cloud Storage URL），直接使用
    if (restaurant.imageUrl.startsWith('http')) {
      event.target.src = restaurant.imageUrl;
      return;
    }
    
    // 如果是 Cloud Storage 路徑，直接使用
    if (restaurant.imageUrl.startsWith('gs://')) {
      event.target.src = restaurant.imageUrl;
      return;
    }
    
    // 其他情況，使用 API 路徑
    event.target.src = `${baseUrl}/api/images/${restaurant.imageUrl.replace(/^\/+/, '')}`;
  } catch (error) {
    console.error('Image error handling failed:', error);
    event.target.src = isReview ? getBackupImageUrl(restaurant, true) : defaultRestaurantImage;
  }
} 