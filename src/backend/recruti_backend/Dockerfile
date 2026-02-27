# =============================================================
# STAGE 1 : BUILD
# On utilise une image Maven avec Java 17 pour compiler le projet
# "AS builder" donne un nom à ce stage pour le référencer plus tard
# =============================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

# On définit le répertoire de travail à l'intérieur du conteneur
# Tous les fichiers copiés et les commandes s'exécuteront ici
WORKDIR /app

# -------------------------------------------------------------
# On copie UNIQUEMENT le pom.xml en premier (avant le code source)
# Astuce : si le code source change mais pas le pom.xml,
# Docker réutilise le cache de cette étape → build plus rapide
# -------------------------------------------------------------
COPY pom.xml .

# Maven télécharge toutes les dépendances déclarées dans pom.xml
# "go-offline" = télécharger tout ce dont on a besoin maintenant
# pour ne pas avoir besoin d'internet lors du build suivant
RUN mvn dependency:go-offline

# -------------------------------------------------------------
# Maintenant on copie tout le code source (dossier src/)
# On le fait APRÈS le téléchargement des dépendances pour
# profiter du cache Docker (voir explication ci-dessus)
# -------------------------------------------------------------
COPY src ./src

# On compile le projet et on génère le fichier JAR
# -DskipTests : on ne lance pas les tests pendant le build Docker
# Le JAR sera généré dans /app/target/
RUN mvn clean package -DskipTests

# =============================================================
# STAGE 2 : RUN
# Image beaucoup plus légère : juste le JRE (pas besoin de Maven)
# "alpine" = version ultra-légère de Linux (~5MB)
# On n'embarque PAS Maven dans l'image finale → image plus petite
# =============================================================
FROM eclipse-temurin:17-jre-alpine

# Répertoire de travail dans le conteneur final
WORKDIR /app

# -------------------------------------------------------------
# On copie UNIQUEMENT le JAR généré depuis le stage "builder"
# L'image finale ne contient pas Maven, ni le code source,
# ni les dépendances séparées → image propre et légère
# "*.jar" permet de ne pas avoir à connaître le nom exact du JAR
# -------------------------------------------------------------
COPY --from=builder /app/target/*.jar app.jar

# -------------------------------------------------------------
# On informe Docker que l'application écoute sur le port 8080
# C'est documentaire : cela ne publie pas le port automatiquement
# Le port est publié dans docker-compose.yml (ports: "8080:8080")
# -------------------------------------------------------------
EXPOSE 8080

# -------------------------------------------------------------
# Commande de démarrage de l'application
# ENTRYPOINT : s'exécute toujours au démarrage du conteneur
# Equivalent de taper "java -jar app.jar" dans un terminal
# On utilise la forme tableau ["cmd"] plutôt que la forme string
# pour éviter les problèmes de gestion des signaux (CTRL+C etc.)
# -------------------------------------------------------------
ENTRYPOINT ["java", "-jar", "app.jar"]
#pour le hot reloading avec spring devtools
#ENTRYPOINT ["mvn", "spring-boot:run"]