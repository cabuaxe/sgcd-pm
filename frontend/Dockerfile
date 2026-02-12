FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npx ng build --configuration=production

FROM nginx:alpine
ARG NGINX_CONF=nginx.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY ${NGINX_CONF} /etc/nginx/conf.d/default.conf
EXPOSE 80
