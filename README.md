
一個基於 Vue 3 和 Spring Boot 的餐廳地圖應用，讓用戶可以探索、分享和評論餐廳。

## 功能特點
-  搜尋功能：依名稱、類型、評分等條件搜尋餐廳
-  評分系統：用戶可以對餐廳進行評分和評論
-  圖片上傳：支援餐廳照片上傳（使用 Google Cloud Storage）
-  用戶系統：註冊、登入、個人資料管理

## 採用技術

### 前端
- Vue 3：使用 Composition API 和 `<script setup>`
- Element Plus：UI 元件庫
- Axios：HTTP  請求工具
- Vue Router：前端路由管理
- Pinia：狀態管理工具
- Vite：前端開發與建置工具

### 後端
- Spring Boot：後端框架
- Spring Security：安全與授權管理
- Spring Data JPA：資料庫操作
- MySQL：關聯式資料庫
- JWT：使用者身份驗證
- Google Cloud Storage：圖片儲存服務

## 專案結構

```plaintext
restaurant-map/
├── front-end/ # Vue 3 前端專案
<<<<<<< HEAD
│ ├── src/ # 前端原始碼
│ │ ├── components/# 可重複使用的components
│ │ ├── views/ # 頁面元件
│ │ ├── store/ # Pinia 狀態管理
│ │ ├── router/ # Vue Router 設定
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





