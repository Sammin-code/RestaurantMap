
一個基於 Vue 3 和 Spring Boot 的餐廳地圖應用，讓用戶可以探索、分享和評論餐廳。

## 功能特點
-  互動式地圖：使用 Google Maps API 顯示餐廳位置
-  搜尋功能：依名稱、類型、評分等條件搜尋餐廳
-  評分系統：用戶可以對餐廳進行評分和評論
-  圖片上傳：支援餐廳照片上傳（使用 Google Cloud Storage）
-  用戶系統：註冊、登入、個人資料管理
-  響應式設計：支援各種裝置尺寸

## 技術棧
### 前端
- Vue 3：使用 Composition API 和 `<script setup>`
- Element Plus：UI 元件庫
- Axios：HTTP 請求
- Vue Router：路由管理
- Pinia：狀態管理
- Vite：建構工具
- Google Maps API：地圖功能

### 後端
- Spring Boot：後端框架
- Spring Security：安全認證
- Spring Data JPA：資料庫操作
- MySQL：資料庫
- JWT：用戶認證
- Google Cloud Storage：圖片存儲

## 專案結構

```plaintext
restaurant-map/
├── front-end/ # Vue 3 前端專案
│ ├── src/ # 源碼
│ │ ├── components/ # 可重用元件
│ │ ├── views/ # 頁面元件
│ │ ├── store/ # Pinia 狀態管理
│ │ ├── router/ # 路由配置
│ │ └── assets/ # 靜態資源
│ └── public/ # 公共資源
├── back-end/ # Spring Boot 後端專案
│ ├── src/ # 源碼
│ │ ├── main/ # 主要程式碼
│ │ └── test/ # 測試程式碼
│ └── pom.xml # Maven 配置
```


## 快速開始

### 前端
```bash
cd front-end
npm install
npm run dev
```

### 後端
```bash
cd back-end
mvn install
mvn spring-boot:run
```

## 部署
- 前端：部署在 Google Cloud Run
- 後端：部署在 Google Cloud Run
- 資料庫：使用 Google Cloud SQL
- 圖片存儲：使用 Google Cloud Storage

## 詳細文檔
- [前端文檔](./front-end/README.md)
- [後端文檔](./back-end/README.md)





