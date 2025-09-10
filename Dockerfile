# Dockerfile para o Policy Request Service
# Utiliza multi-stage build para otimizar o tamanho da imagem final

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .

# Download das dependências (aproveitando cache do Docker)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY src ./src

# Compilar aplicação (pulando testes para build mais rápido)
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Metadados da imagem
LABEL maintainer="Sistema ACME <dev@acme.com>"
LABEL description="Microsserviço para gerenciamento de solicitações de apólices de seguro"
LABEL version="1.0.0"

# Criar usuário não-root para segurança
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Instalar dependências necessárias
RUN apk add --no-cache \
    curl \
    tzdata \
    && rm -rf /var/cache/apk/*

# Definir timezone
ENV TZ=America/Sao_Paulo
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da aplicação do estágio de build
COPY --from=build /app/target/policy-request-service-*.jar app.jar

# Alterar propriedade dos arquivos para o usuário da aplicação
RUN chown -R appuser:appgroup /app

# Mudar para usuário não-root
USER appuser

# Definir variáveis de ambiente padrão
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=80.0 -XX:+UseG1GC -XX:+UseStringDeduplication"
ENV SERVER_PORT=8080
ENV SPRING_PROFILES_ACTIVE=docker

# Expor porta da aplicação
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para iniciar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]