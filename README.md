# DevSecOps Web App Project ![Status](https://img.shields.io/badge/status-in%20progress-yellow)

A delivery-tracking web application used as a practical implementation of **DevSecOps practices**: a full-stack app deployed through a secured CI/CD pipeline onto infrastructure managed as code.

## Stack
- **Backend:** Spring Boot 3.5, Java 21, Spring Security (JWT)
- **Frontend:** React 19 with Material UI, served by nginx
- **Database:** PostgreSQL 16
- **Containerization:** Docker, Docker Compose
- **Infrastructure:** Terraform on DigitalOcean (`infra/`), OPA policies in `infra/policy/`
- **Configuration management:** Ansible (`ansible/`)
- **CI/CD:** GitHub Actions (`.github/workflows/`)
- **Security scanning:** Gitleaks (secrets), Semgrep (SAST), OWASP Dependency-Check and npm audit (SCA), Trivy (filesystem and image), tfsec, Checkov and OPA (infrastructure as code)

## Repository layout
- `back/` Spring Boot API
- `front/` React app
- `docker/` Docker Compose files
- `infra/` Terraform
- `ansible/` Ansible playbooks and roles

## Run locally
Requires Docker with Compose.

1. Copy `.env.example` to `.env` and fill in the values (PostgreSQL credentials, `JWT_SECRET`, `APP_USER_USERNAME`, `APP_USER_PASSWORD`).
2. Start the stack:
   ```bash
   ./start.sh
   ```
   This runs `docker compose -f docker/docker-compose.local.yaml --env-file .env up --build` (the script uses `sudo`).
3. Open the services:
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080/api/v1
   - Swagger UI: http://localhost:8080/swagger-ui/index.html
   - PostgreSQL: localhost:5432

Stop with `./stop.sh`.

### Frontend without Docker (CRA dev server)
The frontend calls the API at `process.env.REACT_APP_API_URL`, falling back to the relative path `/api/v1`.
- In the container, nginx (`front/nginx.conf`) proxies `/api/` to the backend, so no configuration is needed.
- With `npm start` in `front/` (port 3000), the `"proxy": "http://localhost:8080"` field in `front/package.json` forwards `/api/*` to a backend running on port 8080, so the relative default also works. To target another backend, set `REACT_APP_API_URL` (e.g. `REACT_APP_API_URL=http://localhost:8080/api/v1 npm start`); the backend must then allow your origin via `APP_CORS_ALLOWED_ORIGINS` (default `http://localhost:3000,http://127.0.0.1:3000`).

## Status
This project is in progress.
