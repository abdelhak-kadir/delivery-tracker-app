# Evidence summary (Wave 3)

Collected 2026-11-05/06 on macOS 26.7.2, Intel i5-1038NG7, 16 GB RAM, Docker Desktop.
Code at commit `f5a0bd2` (+ uncommitted evidence only). **Performance values are local measurements on a
developer laptop (load generator on the same machine), not production.** CI values come from GitHub Actions
runs of the *remote* branches (2025-09-24 → 2025-12-15), which do not yet include the 2026-11-05 fixes.
Each raw file starts with the exact command, date and machine context.

## Tests and coverage

| Metric | Value | File | Caveat |
|---|---|---|---|
| Backend tests | 82 run, 0 failures, 0 errors, 0 skipped (13 classes) | backend-tests.txt | `./mvnw clean verify`, 54.6 s wall time |
| JaCoCo line coverage | 77.1 % (306/397) | jacoco.csv | branch 62.5 % (20/32), instruction 85.4 %, method 90.2 % |
| Coverage by package (lines) | controller 100 %, service 97.1 %, config 89.8 %, mapper 8.8 % | jacoco.csv | mappers are hand-written but only exercised indirectly |
| Frontend tests | 2 pass | frontend-build.txt | both are placeholder tests — no real component tests |
| Frontend production build | 38.4 s; main.js 676,488 B raw / 203,125 B gzip; CSS 337 B / 282 B gzip | frontend-build.txt | `CI=true npm run build` |

## Security scans (local, current code)

| Scan | Result | File | Caveat |
|---|---|---|---|
| Gitleaks (143 commits) | 1 finding: generic-api-key in `.env.example` line 7 (commit 88a18bb) | gitleaks.json | example JWT secret committed in 2025; replace with a placeholder |
| Semgrep `--config auto` (back/src, front/src) | 0 findings (351 rules, 67 files) | semgrep.json | |
| npm audit (front) | 70 vulns: 3 critical, 36 high, 13 moderate, 18 low (1,384 deps) | npm-audit.json | mostly transitive via react-scripts (build tooling) |
| Trivy fs 0.56.2 | 3 CRITICAL + 106 HIGH vuln entries; 16 CRITICAL Terraform misconfigs | trivy-fs.json, trivy-summary.txt | entries ≠ unique CVEs; CRITICAL = front transitive deps (proxy-addr, shell-quote, websocket-driver); HIGH includes direct front deps axios (12 entries) and react-router (8) → upgrade; backend HIGH: postgresql 42.7.7, actuator 3.5.6; misconfigs = DO firewalls with 0.0.0.0/0 (public 80/443 + egress, partly intended) |
| Trivy image (OS packages) | backend 2 HIGH / 0 CRITICAL; frontend 0 | trivy-image-*.json | Java library DB (949 MB) not downloaded |
| OWASP Dependency-Check | not run | — | NVD download too slow on this link |
| GitHub code scanning (remote) | 4 Trivy alerts, high: 2 fixed, 2 open (CVE-2021-3803, CVE-2025-58754) | code-scanning-alerts.json | |

## Containers and runtime

| Metric | Value | File | Caveat |
|---|---|---|---|
| Image size | backend 353 MB, frontend 70.1 MB | containers.txt | |
| Non-root | backend uid 999 (`app`), frontend uid 101 (`nginx`) | containers.txt | |
| Cold build, both images | 28 min 13 s | containers.txt | dominated by slow network (base images + Maven); not representative |
| Startup after `compose up -d` | frontend HTTP 200 at 14.6 s; backend health UP at 27.1 s | startup-time.txt | includes Postgres start; Spring's own "Started in …" time was not captured in a raw file |
| Idle memory | backend 375.8 MiB, Postgres 56.1 MiB, nginx 9.4 MiB | containers.txt | single `docker stats` snapshot |
| Health | UP (db, diskSpace, ping, ssl) | health.json | `show-details: always` exposes component details publicly |

## API behaviour

| Check | Result | File |
|---|---|---|
| Unauthenticated GET /api/v1/clients | 401 JSON ErrorResponse | http-401.txt |
| Wrong password | 401 JSON | http-401.txt |
| Invalid bearer token | 401; backend logs "Invalid JWT token" | security-logs.png (log capture) | the api-crud.txt "bad token" request actually had no Authorization header (= no-token case); also covered by SecurityIntegrationTest (malformed/expired token → 401) |
| Valid login | 200 + JWT (redacted) | api-crud.txt |
| CRUD clients / drivers / trans_logs | POST 201, GET/PUT/DELETE 200, GET after delete 404 | api-crud.txt |
| Validation error | 400 with per-field messages | api-crud.txt |
| CORS preflight | allowed for http://localhost:3000; rejected ("Invalid CORS request") for http://evil.example.com | api-crud.txt |

## Performance (local, indicative)

50 sequential `curl` requests, authenticated (latency-curl.txt), in ms:

| Endpoint | min | median | p95 |
|---|---|---|---|
| GET /api/v1/clients?size=20 | 8.8 | 10.5 | 14.7 |
| GET /api/v1/drivers | — | 10.0 | 13.1 |
| GET /api/v1/trans_logs | — | 27.0 | 46.0 |
| POST /api/v1/auth/login | 91.3 | 93.6 | 172.9 |
| unauthenticated → 401 | — | 4.0 | — |

Login latency includes BCrypt verification; JWT generation alone was not isolated.

k6, 10 VUs, 30 s, GET clients + drivers (k6-summary.json, k6-output.txt, k6-script.js):
20,983 requests, 696.5 req/s, 0.00 % failed; latency avg 14.15 ms, median 11.13, p90 20.84, p95 26.73, max 577 ms.

## CI (GitHub Actions, remote, 99 runs) — ci-runs.json, ci-jobs.json, ci-summary.txt

| Workflow | Success | Notes |
|---|---|---|
| CI Backend | 14/18 (77.8 %) | latest success 18445487440: build 57 s, test 48 s, docker 140 s, security-gate 132 s (383 s total) |
| CI Frontend | 8/25 (32 %) | latest success 18445560020: build-and-test 69 s, docker 108 s, security-gate 64 s (246 s total) |
| Backend Security Scans | 16/16 | 2–4 s runs (reusable-workflow wrapper entries) |
| CD Backend / CD Frontend | 16/16, 4/4 | 2–4 s placeholder-style jobs |
| Terraform Infrastructure / terraform.yaml | 0/11, 0/8 | never succeeded on the remote — no job timings |
| Generate OpenAPI Docs | 0/1 | |

Durations come from job start/end times (`updatedAt − createdAt` was unreliable).

## Screenshots (docs/images/screens/)

Produced (headless Chrome 1440×900 on the running local stack): login.png, dashboard.png, clients.png, drivers.png,
transport-logs.png, transport-form-open.png, transport-form.png (validation errors), swagger-ui.png (**unusable**: shows "Failed to load remote configuration" → retake as swagger-ui-ok.png),
actuator-health.png, jacoco-report.png.
Rendered from real captured text (HTML render, not a terminal capture): http-401.png, docker-logs.png,
security-logs.png, semgrep.png, trivy-scan.png, api-response.png (curl latency table, not Postman).

### Screenshots the authors must take
- GitHub Actions: pipeline run summary (ghactions.png) and security-scan workflow logs (ghactions-security.png).
- GitHub Security tab (code scanning alerts).
- Ansible playbook output (ansible.png) — needs a real run against the servers.
- Optional: browser DevTools page-load (devtools.png).
- Not applicable (feature doesn't exist): tracking-search.png, tracking-result.png, admin-dashboard.png → drop from the report.
