FROM golang:1.22-alpine AS builder

WORKDIR /app

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o /bin/commov-backend ./cmd

FROM alpine:3.20

RUN adduser -D -g '' appuser

WORKDIR /app

RUN mkdir -p /app/photos/image && chown -R appuser:appuser /app/photos

COPY --from=builder /bin/commov-backend /usr/local/bin/commov-backend

USER appuser

EXPOSE 8080

CMD ["commov-backend"]
