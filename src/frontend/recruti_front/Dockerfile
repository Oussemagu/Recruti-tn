FROM node:18-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci
COPY . .

RUN npm run build -- --configuration production

FROM nginx:alpine

# ✅ Supprimer la config par défaut de nginx
RUN rm /etc/nginx/conf.d/default.conf

# ✅ Copier votre config
COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build /app/dist/recruti_front/ /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
