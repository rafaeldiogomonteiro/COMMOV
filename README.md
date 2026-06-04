# COMMOV

Aplicação de gestão de projetos com backend em Go e cliente Android.

## Estrutura

- `backend/` — API HTTP (Chi), PostgreSQL (GORM + Goose), autenticação por token
- `frontend/` — app Android (Kotlin, Jetpack Compose)

## Arranque rápido (backend)

```bash
cd backend
docker compose up -d --build
```

Por defeito cria o utilizador admin (`DEFAULT_USER` / `DEFAULT_USER_PASS` em `.env`).

- Health: `GET http://localhost:8080/health`
- Login: `POST http://localhost:8080/login` com `{"email","password"}`

Migrações manuais (com Postgres acessível em `localhost:5432`):

```bash
cd backend
make goose-up
```

## App Android

Configure `API_BASE_URL` em `frontend/.env` (emulador: `http://10.0.2.2:8080`).

Abra `frontend/` no Android Studio e execute a app.

## API (Bruno)

Coleção em `backend/api/commov/`. Defina a variável `token` após login.

## Papéis

| Papel | Capacidades |
|-------|-------------|
| `admin` | Utilizadores + projetos + tarefas |
| `project_manager` | Projetos + tarefas |
| `user` | Ver projetos atribuídos; concluir/registar tempo nas suas tarefas |

## Testes backend

```bash
cd backend
go test ./...
```
