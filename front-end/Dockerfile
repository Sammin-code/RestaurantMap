# 使用 Node.js 作為構建環境
FROM node:18-alpine as build

# 設置工作目錄
WORKDIR /app

# 複製 package.json 和 package-lock.json
COPY package*.json ./

# 安裝依賴
RUN npm install

# 複製所有源代碼
COPY . .

# 設置環境變數
ENV VITE_API_URL=https://restaurantmap-255668913932.asia-east1.run.app/api

# 構建應用
RUN npm run build

# 使用 nginx 作為生產環境
FROM nginx:alpine

# 複製構建好的文件到 nginx 目錄
COPY --from=build /app/dist /usr/share/nginx/html

# 複製 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 8080

# 啟動 nginx
CMD ["nginx", "-g", "daemon off;"] 