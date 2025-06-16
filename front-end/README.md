# Restaurant Map Frontend

餐廳地圖應用的前端部分，使用 Vue 3 開發。

## 使用技術
- Vue 3：使用 Composition API 和 `<script setup>`
- Element Plus：UI 元件庫
- Axios：HTTP 請求
- Vue Router：路由管理
- Pinia：狀態管理
- Vite：建構工具

## 安裝步驟
```bash
# Navigate to the frontend directory
cd front-end

# Install dependencies
npm install

# Start the development server
npm run dev

# Build for production
npm run build
```

## 環境要求
- Node.js 16+
- npm 8+

## 目錄結構
```plaintext
front-end/
├── src/                 # 前端原始碼
│   ├── components/      # 可重複使用的components
│   ├── views/           # 各個頁面元件
│   ├── store/           # Pinia 狀態管理
│   ├── router/          # Vue Router 設定
│   ├── assets/          # 靜態資源
│   ├── App.vue          # 根components
│   └── main.js          # 程式進入點
├── public/              # 公用資源目錄
├── .env                 # 環境變數設定檔
├── package.json         # 專案相依套件設定
└── vite.config.js       # Vite 設定檔

