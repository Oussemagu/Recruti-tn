FROM node:20-bookworm-slim AS build

WORKDIR /app

# Reduce npm install flakiness with native binaries (e.g. esbuild) in Docker builds.
ENV npm_config_audit=false \
	npm_config_fund=false \
	npm_config_jobs=1

COPY package*.json ./
RUN npm ci
COPY . .

RUN npm run build -- --configuration production

FROM nginx:alpine

# ✅ Supprimer la config par défaut de nginx
RUN rm /etc/nginx/conf.d/default.conf

# ✅ Copier votre config
COPY nginx.conf /etc/nginx/nginx.conf

COPY --from=build /app/dist/recruti_front/browser/. /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
