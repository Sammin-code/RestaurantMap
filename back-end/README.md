# Restaurant Map Backend

這是一個使用 Spring Boot 構建的餐廳地圖後端服務，提供餐廳資訊管理、用戶評論、收藏等功能。

## 技術架構

- **核心框架**
  - Java 17
  - Spring Boot 3.2.0
  - Spring Security 6.0
  - Spring Data JPA

- **資料庫**
  - MySQL 8.0 (Google Cloud SQL)
  - Hibernate ORM

- **雲端服務 (Google Cloud Platform)**
  - Cloud Run (容器化部署)
  - Cloud SQL (資料庫服務)
  - Cloud Storage (圖片存儲)

## 後端架構

```
┌─────────────────────────────────────────────────────────┐
│                    Controller 層                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Restaurant  │  │   Review    │  │    User     │     │
│  │ Controller  │  │ Controller  │  │ Controller  │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Service 層                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Restaurant  │  │   Review    │  │    User     │     │
│  │  Service    │  │   Service   │  │   Service   │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Repository 層                         │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │
│  │ Restaurant  │  │   Review    │  │    User     │     │
│  │ Repository  │  │ Repository  │  │ Repository  │     │
│  └─────────────┘  └─────────────┘  └─────────────┘     │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    Database 層                           │
│                    ┌─────────────┐                      │
│                    │   MySQL     │                      │
│                    └─────────────┘                      │
└─────────────────────────────────────────────────────────┘
```

### 架構說明
1. **Controller 層**
   - 處理 HTTP 請求
   - 請求參數驗證
   - 返回 HTTP 響應
   - 實現 RESTful API

2. **Service 層**
   - 實現業務邏輯
   - 事務管理
   - 數據處理和轉換
   - 調用 Repository 層

3. **Repository 層**
   - 數據訪問層
   - 使用 Spring Data JPA
   - 實現數據持久化
   - 處理數據庫操作

4. **Database 層**
   - MySQL 數據庫
   - 存儲業務數據
   - 通過 Cloud SQL 代理連接

## 快速開始

### 環境要求
- JDK 17 或以上
- MySQL 8.0 或以上
- Maven 3.6 或以上

### 本地開發
1. Clone專案：
```bash
git clone https://github.com/Sammin-code/RestaurantMap.git
cd restaurant-map-backend
```

2. 安裝依賴：
```bash
mvn clean install
```

3. 運行應用：
```bash
mvn spring-boot:run
```

應用將在 http://localhost:8080 啟動

## API 文檔

### 認證相關
- POST `/api/users/register` - 用戶註冊
- POST `/api/users/login` - 用戶登入

### 餐廳相關
- GET `/api/restaurants` - 獲取餐廳列表
- GET `/api/restaurants/{id}` - 獲取餐廳詳情
- POST `/api/restaurants` - 新增餐廳 (需要認證)
- PUT `/api/restaurants/{id}` - 更新餐廳 (需要認證)
- DELETE `/api/restaurants/{id}` - 刪除餐廳 (需要認證)

### 評論相關
- GET `/api/reviews/restaurant/{restaurantId}` - 獲取餐廳評論
- POST `/api/reviews/restaurant/{restaurantId}` - 新增評論 (需要認證)

### 收藏相關
- GET `/api/restaurants/favorites` - 獲取收藏列表 (需要認證)
- POST `/api/restaurants/{id}/favorite` - 添加收藏 (需要認證)
- DELETE `/api/restaurants/{id}/favorite` - 取消收藏 (需要認證)

## 部署

專案使用 Google Cloud Run 進行部署。

部署網址：
- 前端：https://restaurant-map-frontend-255668913932.asia-east1.run.app
- 後端：https://restaurantmap-255668913932.asia-east1.run.app

## 安全性

- 使用 JWT 進行身份驗證
- 所有密碼都經過 BCrypt 加密
- 圖片存儲使用 Google Cloud Storage
