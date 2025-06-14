# Restaurant Map Backend

這是一個使用 Spring Boot 構建的餐廳地圖後端服務，提供餐廳資訊管理、用戶評論、收藏等功能。

## 技術架構

### 後端技術棧
- **核心框架**
  - Java 17
  - Spring Boot 3.2.0
  - Spring Security 6.0
  - Spring Data JPA

- **資料庫**
  - MySQL 8.0 (Google Cloud SQL)
  - Hibernate ORM
  - HikariCP 連接池

- **認證與安全**
  - JWT (JSON Web Token)
  - BCrypt 密碼加密
  - Spring Security 角色基礎訪問控制 (RBAC)

- **雲端服務 (Google Cloud Platform)**
  - Cloud Run (容器化部署)
  - Cloud SQL (資料庫服務)
  - Cloud Storage (圖片存儲)
  - Cloud Build (CI/CD)

- **開發工具**
  - Maven 3.9.5
  - Docker
  - Git

### 系統架構圖

[上面的圖會自動顯示在這裡]

### 功能特點

#### 1. 高效的資料存取
- 使用 Spring Data JPA 簡化數據訪問層
- HikariCP 連接池優化數據庫連接
- 實體關係映射優化（OneToMany, ManyToOne 等）

#### 2. 安全性設計
- 基於 JWT 的無狀態認證
- 密碼 BCrypt 加密存儲
- 角色基礎訪問控制（RBAC）
- API 端點權限精細控制

#### 3. 雲原生部署
- 容器化部署到 Cloud Run
- 自動擴展能力
- Cloud SQL 代理安全連接
- Cloud Storage 圖片存儲

#### 4. 效能優化
- 響應數據 JSON 序列化優化
- N+1 查詢問題解決
- 圖片處理優化
- 適當的緩存策略

#### 5. API 特性
- RESTful API 設計
- 統一的錯誤處理
- 請求參數驗證
- 完整的 API 文檔

## 環境要求

- JDK 17 或以上
- MySQL 8.0 或以上
- Maven 3.6 或以上
- Google Cloud SDK (用於部署)

## 環境變數配置

### 生產環境 (Cloud Run)

目前專案已部署在 Google Cloud Run，相關配置：

1. 資料庫 (Cloud SQL)
   - 實例名稱：fluid-unfolding-461212-p7:asia-east1:restaurant-map
   - 資料庫名稱：restaurant_map
   - 連接字串格式：
   ```
   jdbc:mysql:///restaurant_map?cloudSqlInstance=fluid-unfolding-461212-p7:asia-east1:restaurant-map&socketFactory=com.google.cloud.sql.mysql.SocketFactory&user=root&password=你的密碼
   ```

2. 圖片存儲 (Cloud Storage)
   - Project ID：fluid-unfolding-461212-p7
   - Bucket 名稱：restaurant-map-uploads

3. JWT 配置
   - 需要設置 JWT_SECRET 環境變數
   - 建議使用隨機生成的長字串

### 如何修改生產環境配置

1. 訪問 [Google Cloud Console](https://console.cloud.google.com/)
2. 選擇專案 `fluid-unfolding-461212-p7`
3. 前往 Cloud Run 服務
4. 選擇 `restaurantmap` 服務
5. 點擊 "編輯和部署新版本"
6. 在 "變數和密鑰" 部分可以修改環境變數

### 本地開發配置

1. 創建 `.env` 文件（已加入 .gitignore）
2. 複製以下內容到 `.env`，並填入相應的值：
```properties
# 資料庫配置 - 使用 Cloud SQL
SPRING_DATASOURCE_URL=jdbc:mysql:///restaurant_map?cloudSqlInstance=fluid-unfolding-461212-p7:asia-east1:restaurant-map&socketFactory=com.google.cloud.sql.mysql.SocketFactory&user=root&password=你的密碼

# JWT 配置
JWT_SECRET=your-jwt-secret-key

# Google Cloud 配置
GOOGLE_CLOUD_PROJECT=fluid-unfolding-461212-p7
BUCKET_NAME=restaurant-map-uploads
```

### 注意事項
- 不要將含有實際密碼的 .env 文件提交到 git
- 如果需要本地測試，請向專案管理員索取必要的憑證
- 生產環境的密碼和憑證請在 Google Cloud Console 中管理

## 本地開發

1. 克隆專案：
```bash
git clone https://github.com/<YOUR_GITHUB_USERNAME>/RestaurantMap.git
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
- PUT `/api/reviews/{reviewId}` - 更新評論 (需要認證)
- DELETE `/api/reviews/{reviewId}` - 刪除評論 (需要認證)

### 收藏相關

- GET `/api/restaurants/favorites` - 獲取收藏列表 (需要認證)
- POST `/api/restaurants/{id}/favorite` - 添加收藏 (需要認證)
- DELETE `/api/restaurants/{id}/favorite` - 取消收藏 (需要認證)

### 圖片相關

- POST `/api/images/upload` - 上傳圖片 (需要認證)
- DELETE `/api/images/{imageUrl}` - 刪除圖片 (需要認證)

## 權限設計

系統有兩種角色：

- REVIEWER：普通用戶，可以發布餐廳、評論和收藏
- ADMIN：管理員，具有刪除餐廳和評論的權限

## 部署

專案使用 Google Cloud Run 進行部署，配置文件位於 `cloudbuild.yaml`。

部署網址：
- 前端：https://restaurant-map-frontend-255668913932.asia-east1.run.app
- 後端：https://restaurantmap-255668913932.asia-east1.run.app

## 安全性

- 使用 JWT 進行身份驗證
- 所有密碼都經過 BCrypt 加密
- 圖片存儲使用 Google Cloud Storage，支援安全的檔案上傳和管理

## 開發團隊

- 開發者：<!-- TODO: 填寫你的名字 -->
- 聯絡方式：<!-- TODO: 填寫你的 Email -->

## License

MIT License 