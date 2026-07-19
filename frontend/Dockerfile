# ====================================
# AgentFlow Base - 前端 Dockerfile
# Multi-stage: Vite Build + Nginx
# GCP Cloud Run 部署用
# ====================================

# Stage 1: Build React (Vite) App
FROM node:24-alpine AS build

WORKDIR /app

# Cypress 僅供本機 E2E，容器建置時跳過其 binary 下載
ENV CYPRESS_INSTALL_BINARY=0

COPY package*.json ./
RUN npm ci --no-audit --no-fund

COPY . .

# 後端 API URL 於建置期注入（src/app/api.ts 讀取 import.meta.env.VITE_API_URL）
ARG VITE_API_URL=http://localhost:8080/api
ENV VITE_API_URL=${VITE_API_URL}

RUN echo "前端建置使用後端 API URL: ${VITE_API_URL}" && npm run build

# Stage 2: Serve with Nginx
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/templates/default.conf.template

COPY --from=build /app/dist /usr/share/nginx/html

# Cloud Run 預設 PORT=8080
ENV PORT=8080
EXPOSE 8080

# 使用 envsubst 將 nginx template 的 ${PORT} 替換後啟動
CMD ["/bin/sh", "-c", "envsubst '${PORT}' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]
