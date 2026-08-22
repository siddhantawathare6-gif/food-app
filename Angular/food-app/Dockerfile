# Stage 1: Build Angular application
FROM node:22.23.2 as build

WORKDIR /app

# Install dependencies
COPY package*.json ./
RUN npm install

# Copy source code
COPY . .

# Build Angular application
RUN npm run build

# Stage 2: Serve Angular application using Nginx
FROM nginx:alpine

# Copy Angular build output
COPY --from=build /app/dist/food-app /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]