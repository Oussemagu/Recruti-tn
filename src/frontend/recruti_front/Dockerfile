FROM node:18-alpine AS build

WORKDIR /app

# Copier les fichiers de dépendances
COPY package*.json ./

# Installer les dépendances
RUN npm ci --legacy-peer-deps

# Copier tout le code source
COPY . .

# Build Angular - CORRECTION ICI : pas de flag --production
RUN npm run build

# Stage de production avec Nginx
FROM nginx:alpine

# Supprimer la config par défaut
RUN rm /etc/nginx/conf.d/default.conf

# Copier la config Nginx
COPY nginx.conf /etc/nginx/nginx.conf

# IMPORTANT : Avec Angular 17+, les fichiers sont dans browser/
COPY --from=build /app/dist/recruti_front/browser /usr/share/nginx/html/

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
