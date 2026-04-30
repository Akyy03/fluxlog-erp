# Folosim o imagine oficială de Java 17
FROM eclipse-temurin:21-jdk-jammy

# Setăm folderul de lucru în interiorul containerului
WORKDIR /app

# Copiem wrapper-ul de Maven și fișierul pom.xml pentru a instala dependențele
# Asta ajută la cache-uirea librăriilor (să nu le downloadeze la fiecare modificare de cod)
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Rulăm comanda care descarcă dependențele (va dura puțin prima dată)
RUN ./mvnw dependency:go-offline

# Copiem restul codului sursă (folderul src)
COPY src ./src

# Comanda care pornește aplicația în mod "watch" (DevTools activ)
# Folosim ./mvnw spring-boot:run pentru a permite Hot Reload
CMD ["./mvnw", "spring-boot:run"]