FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci --legacy-peer-deps
COPY . .
RUN npm run build -- --configuration production

FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production

COPY --from=builder /app/dist/frontend ./dist/frontend
COPY package*.json ./
RUN npm ci --omit=dev --legacy-peer-deps

EXPOSE 4000
ENV PORT=4000
ENV NG_TRUST_PROXY_HEADERS=x-forwarded-for,x-forwarded-proto
ENTRYPOINT ["node", "dist/frontend/server/server.mjs"]
