# Folosim o imagine de Node.js (versiunea 20 este stabilă pentru Angular modern)
FROM node:20-alpine

# Directorul de lucru în container
WORKDIR /app

# Copiem fișierele care definesc dependențele
COPY package*.json ./

# Instalăm dependențele (npm install)
RUN npm install --legacy-peer-deps

# Copiem restul codului sursă din folderul frontend
COPY . .

# Expunem portul pe care rulează Angular în mod normal
EXPOSE 4200

# Pornim aplicația cu host 0.0.0.0 ca să poată fi accesată din afara containerului
# Adăugăm --poll ca să forțăm Hot Reload să meargă pe Windows/Docker
CMD ["npm", "run", "start", "--", "--host", "0.0.0.0", "--poll", "2000"]