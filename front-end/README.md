# Restaurant Map Frontend

餐廳地圖應用的前端部分，使用 Vue 3 開發。

## 技術棧
- Vue 3：使用 Composition API 和 `<script setup>`
- Element Plus：UI 元件庫
- Axios：HTTP 請求
- Vue Router：路由管理
- Pinia：狀態管理
- Vite：建構工具

## 安裝步驟
```bash
# 進入前端目錄
cd front-end

# 安裝依賴
npm install

# 啟動開發服務器
npm run dev

# 構建生產版本
npm run build
```

## 環境要求
- Node.js 16+
- npm 8+

## 目錄結構
front-end/
├── src/ # 前端源碼
│ ├── components/ # 可重用元件
│ ├── views/ # 頁面元件
│ ├── store/ # Pinia 狀態管理
│ ├── router/ # Vue Router 配置
│ ├── assets/ # 靜態資源
│ ├── App.vue # 根元件
│ └── main.js # 入口文件
├── public/ # 公共資源
├── .env # 環境變數
├── package.json # 項目依賴
└── vite.config.js # Vite 配置