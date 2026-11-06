# PFE Report — Gap Analysis & Completion Plan

Report: `docs/main.tex` (11 chapters, compiles to 78 pages with errors).
Project: Spring Boot 3.5.6 / Java 21 backend (`back/`), React 19 + MUI 7 frontend (`front/`),
Terraform on DigitalOcean (`infra/`), Ansible (`ansible/`), GitHub Actions (`.github/workflows/`).

---

## 0. Decisions needed from the authors before writing

**Answered (2026-11-05):** internship duration = **one month** (keep ch.3 wording); **no defense date**
on the title page; **one supervisor only: Mrs. Latifa Makhlouf** (no company supervisor);
report language = **English** (Résumé stays in French; ask before dropping it). Items 3–4 below are still open.

1. **Internship facts**: real start/end dates and duration (ch.3 says "one-month internship"; a PFE is usually 4–6 months), company supervisor name, academic supervisor name (title page has only "Mrs."), defense date (title page says 10/06/2024).
2. **Language**: ENSA reports are often in French. Confirm English, and whether a French *Résumé* is required next to the English *Abstract*.
3. **Real metrics vs. removal**: most numbers in the report are not backed by the repo (see §2). For each one, either measure it for real or delete it.
4. **Code fixes vs. honest limitations**: a few gaps in the code (§4) can be fixed quickly or described as limitations. Pick one approach for each.

---

## 1. LaTeX / build problems (the report does not compile cleanly)

| # | Problem | Where | Fix |
|---|---------|-------|-----|
| B1 | `\usepackage{ensa-a}` called inside the document body → "Can be used only in preamble" | `chapters/titlepage.tex` | Move `\usepackage{ensa-a}` to the preamble; resolve the clashes it causes (it redefines `geometry`, `Eblue`, `Ecyan`, `titlesec`, babel) |
| B2 | 19 missing images `placeholder-*.png` | ch.7 | Take real screenshots (see §3) into `docs/images/screens/` |
| B3 | No `\graphicspath` — logos only resolve by luck | preamble | Add `\graphicspath{{images/}{images/screens/}}` |
| B4 | `Dockerfile` listings language uses `#` → "Illegal parameter number" | preamble | Use `comment=[l]{\#}` |
| B5 | `min width` / `min height` TikZ keys don't exist (4×) | ch.7 pipeline figure (+ check figures/) | Use `minimum width` / `minimum height` |
| B6 | Unicode `≈`, `✓` errors; `60 %` breaks lines (in LaTeX `%` starts a comment!) | ch.5, ch.6, ch.8 | Use `$\approx$`, `\checkmark`, `60\,\%` |
| B7 | Preamble loads `listings`/`listingsutf8`/`inputenc` and the tikz libraries several times; two conflicting `\lstset` | `preamble.tex` | Rewrite the preamble once, cleanly |
| B8 | Captions include their number by hand ("Figure 7-1: …") → printed twice | ch.7 | Remove the hand numbering; add `\label`s |
| B9 | Hand-made "Figure Reference Index" table | end of ch.7 | Delete; use `\listoffigures` |
| B10 | 17 overfull hboxes (wide tables) | ch.2, 5, 7 | Use `tabularx`, smaller font, or `p{}` columns |
| B11 | Header says "DevSecOps Project Report" | preamble | Change to the actual report title |

## 2. Content that contradicts the code (accuracy — the most important part)

A jury that opens the repo will find these. Each must be corrected.

| Claim in report | Reality in repo | Chapters |
|---|---|---|
| React **18**, Material-UI | React **19.1**, MUI **7.3**, React Router 7 | 1, 3, 5 |
| Spring profiles dev/staging/prod | Only `application-local.yml`, `application-docker.yml` | 3 |
| RBAC with `ROLE_ADMIN` / `ROLE_USER`, role-aware navigation | One fixed user from env vars (`SimpleUserDetailsService`), one role `ROLE_ADMIN`; no `@PreAuthorize` | 3, 4, 5, 6 |
| JWT stored **in memory, not localStorage** (ch.6) vs. localStorage (ch.5) | `localStorage` (`AuthContext.js`); also a **demo fake-token login** path | 4, 5, 6 |
| JWT refresh tokens / automatic refresh | No refresh endpoint; 30-min expiry only | 6, 8 |
| MapStruct for DTO mapping | Hand-written `@Component` mappers; no MapStruct dependency | 8 |
| Dockerfile has `USER 1001` (non-root backend) | Backend Dockerfile runs as **root**; only the frontend image uses `USER nginx` | 5, 6 |
| Coverage 95/87/92/89 %, "over 95 % coverage", 60 %→95 % | ~30 tests in total, **Driver module only** (service, controller, repo, integration) | 5, 7, 8 |
| API 245 ms, startup 18 s, DB 67 ms, JWT 4 ms, bundle 1.3 MB, pipeline 8 m 19 s | Nothing in the repo measured these | 5, 7, 8 |
| Vulnerability before/after 12→0, 8→1, 15→2, 3→0 | No scan reports kept | 7, 8 |
| 80 % less manual entry, 99.5 % accuracy, 95 % satisfaction, "remediation effort −75 %" | No pilot study exists — **delete** | 7 |
| Deployment time 15 min → "under two" (ch.10) vs. 8 min (ch.8) | Inconsistent, unmeasured | 8, 10 |
| Parcel tracking-number search + result page (fig 7-19/20) | No such feature; app has Dashboard, Clients, Drivers, TransportLogs | 7 |
| "Rolling deployments, zero downtime, rollback" | `docker compose up -d` over SSH | 7 |
| `make docs` regenerates PlantUML diagrams | No `docs` target in `ansible/Makefile` | 8 |
| OPA = "future enhancement" (ch.5) vs. implemented (ch.4, 10) | Implemented: `infra/policy/*.rego` + Conftest in `terraform.yaml` | 5 |
| "Runtime monitoring" out of scope (ch.1) | `observability` role deploys node_exporter | 1 |
| Security gates block the pipeline | Trivy runs with `\|\| true` and npm audit with `\|\| true` → they report but **never fail** | 3, 5, 7 |
| Full CD with Ansible | `cd-front.yaml` is an `echo` placeholder; `cd-back.yaml` is SSH + `git pull` | 5, 6 |
| `GET /api/v1/clients/**` protected | `permitAll()` in `SecurityConfig` — client data is **public** | 3, 5, 7 |
| "Codex CLI analysis validates results" | A tool you used, not a project contribution; reads as AI-generated | 1, 2 |
| "Clean Architecture" | Classic layered architecture (controller/service/repo) | 4 |

## 3. Missing content (expected in an ENSA PFE report)

**Front matter (all missing):** dedication, acknowledgements, Abstract (+ Résumé, plus an Arabic abstract if the school requires it), list of acronyms, `\listoffigures`, `\listoftables`, page numbering in roman numerals for the front matter.

**General introduction / conclusion:** an unnumbered *General Introduction* before ch.1 and a *General Conclusion* (ch.10 partly does this).

**Project management (missing chapter or section):**
- Methodology actually followed (Scrum/Kanban? sprints?), with a **Gantt chart** (`pgfgantt`) built from the real timeline. Use git history: 127 commits, 2025-06-23 → 2025-09-26.
- Roles of the two students (who did what).
- Risks.

**Analysis & design (UML is required in ch.3–4):**
- Actors + **use-case diagram** (Admin/Operator).
- **Class diagram** of the real entities: `Client(id, name, identityId)`, `Driver(id, name, plateNumber)`, `TransportLog(…loadDate, loadLocation, unloadDate, unloadLocation, destinationName, deliveryNote, advance, fuelQuantity, fuelPricePerLiter, variableCharge, chargePrice, clientTariff, tripPrice, operator, commercial)` with `@ManyToOne` to Client and Driver. Check that fig 4-2 matches.
- **Sequence diagrams**: login (exists, fig 4-3), create transport log.
- Real REST endpoint table (paths come from `ApiPaths` and the controllers, e.g. `/api/v1/auth/login`, `/clients`, `/drivers`, `/transport-logs`; check the exact spelling).
- Explain the business logic of a TransportLog (trip price, fuel cost, advance), which is the actual business value for Youssra Trans-Négoce.

**Real evidence for ch.7 (replace the 19 placeholders):**
- App screenshots: login, dashboard, clients, drivers, transport-log form + validation error, Swagger UI.
- 401 on a protected endpoint (curl), `/actuator/health` → UP.
- GitHub Actions runs (CI back/front, security scans, terraform plan + tfsec/checkov, conftest), Semgrep/Trivy/Gitleaks/Dependency-Check SARIF in the GitHub Security tab.
- `mvn test` + JaCoCo report (real %), `terraform plan` output, Ansible `make bootstrap`/`make verify` recap, drift-detection issue.
- Real timings: `curl -w '%{time_total}'`, `docker compose up` startup time, Actions durations taken from the UI.

**Ch.6 implementation details worth adding (they exist in the repo and are currently thin):**
- Terraform: module `stack` (bastion/frontend/backend droplets, 3 firewalls, managed PG + db firewall), shared VPC, DNS records, remote backend (TFC org `3assasa`), `terraform.yaml` with plan → tfsec → checkov → conftest → apply + scheduled **drift detection** opening GitHub issues (`.github/drift-template.md`).
- Ansible: the 12 roles, the `ansible_user` migration playbook, vault per environment, inventory generated from Terraform outputs (`scripts/generate_inventory.py`), ProxyJump through the bastion.
- CI: reusable `workflow_call` security workflows, GHCR image push, OpenAPI docs published to gh-pages (`openapi-docs.yml`).
- Short code listings taken **from the real files** (current listings are paraphrased: e.g. the Dockerfile listing doesn't match `back/Dockerfile`).

**Bibliography:** only 3 entries, **zero `\cite`**. Add ~15–25 sources (OWASP Top 10 2021, OWASP ASVS, NIST SP 800-218 SSDF, NIST 800-204, RFC 7519 JWT, Spring Security docs, Terraform/Ansible/DO docs, Semgrep, Trivy, Gitleaks, Checkov, tfsec, OPA, DORA/Accelerate, a DevSecOps survey paper) and cite them in ch.2. Switch to BibTeX/biblatex with a `references.bib`.

**Appendices:** repo structure tree, full API table, workflow YAML excerpts, Ansible role list, environment variables (`.env.example`).

## 4. Structural / redundancy problems

- **Ch.5 "Implementation & Results" and ch.6 "Implementation" overlap** almost entirely (same Terraform/Ansible/Docker text, twice). Merge them into one implementation chapter. Ch.7 holds all results.
- The same metrics tables appear in ch.5, ch.7 and ch.8. Keep one copy (ch.7).
- "Future work" appears in ch.5, ch.7, ch.9 and ch.10. Keep it only in ch.10. Ch.9 keeps "Limitations".
- Ch.4 and ch.6 repeat the Terraform/Ansible design → ch.4 covers *design* (why), ch.6 covers *realization* (how + code).
- Ch.3 and ch.4 both contain "Scope Note" and architecture lists. Deduplicate.
- Ch.2 is a list of bullet lists with little prose and no citations. Rewrite it as a short literature review with sources.
- Chapter 1 company section: check the facts with the company (the partner list "La Bel Vie Group (ATACADAO / MARJANE / CARREFOUR)" is wrong: Marjane is not Label'Vie). Add an org chart if possible.
- Tone: drop marketing phrases ("not a buzzword", "central nervous system", "blueprint"), "Students learned…" (write as authors), and the "(Enhanced with Codex Integration)" comments in the headers.

Proposed final outline:
```
Front matter (dedication, acknowledgements, abstract/résumé, TOC, LoF, LoT, acronyms)
General Introduction
1  Host organization & project context (company, problem, objectives, scope, planning/Gantt)
2  State of the art: DevSecOps (cited literature, tool comparison tables)
3  Requirements analysis (actors, use cases, functional/non-functional reqs)
4  Design (architecture, class/ER, sequences, security design, infra design, pipeline design)
5  Implementation (backend, frontend, Docker, Terraform, Ansible, CI/CD — real code)
6  Results & evaluation (screenshots, real test/scan/pipeline numbers, security tests)
7  Challenges, limitations & discussion
General Conclusion & future work
Bibliography · Appendices
```

## 5. Code/repo issues to fix before submission (or list as limitations)

- **URGENT:** `ansible/.vault_pass` is untracked and **not gitignored** → one `git add .` publishes the vault password. Add it to `.gitignore`.
- `SecurityConfig`: `GET /api/v1/clients/**` is `permitAll`. Remove the line or justify it.
- `AuthContext.js` demo login with a fake token: remove it, or keep it behind a dev flag.
- Backend Dockerfile: add a non-root `USER` (then the report's claim becomes true).
- `opa-policy-check.yaml` uses `-chdir=terraform`, but the folder is `infra/` → that workflow cannot work.
- Trivy / npm audit `|| true`: make at least CRITICAL findings fail the job, or say "report-only" in the report.
- `README.md` still says **Angular**; `docker-compose.yaml` service is named `angular-frontend` on port 4200.
- Stray `app_mono/` and `untitled/` (empty IntelliJ Hello-World projects): delete them.
- Tests exist only for Driver. Add Client/TransportLog/Auth tests so the coverage is respectable, then report the real JaCoCo %.

---

## 6. Implementation plan with Sonnet subagents

Each package lists the files it owns, so packages in the same wave never edit the same file.
Shared rules for every agent: **write only facts you can verify in the repo; no invented numbers; leave `\todo{…}` (todonotes package) wherever a screenshot or measurement from a human is needed**, and compile with `latexmk -pdf` in a scratch output dir before finishing.

### Wave 1 (sequential, one agent): foundation
- **WP1 – Build & skeleton** (`main.tex`, `preamble.tex`, `chapters/titlepage.tex`, new `chapters/00-frontmatter.tex`, new `references.bib`)
  Fix B1, B3, B4, B6, B7, B11; add the front-matter placeholders, LoF/LoT, acronyms (`glossaries` or a simple table), `todonotes`, biblatex; restructure `main.tex` to the outline in §4 (rename/merge chapter files). Done = compiles with 0 errors except missing screenshots.

### Wave 2 (parallel, 5 agents)
- **WP2 – Ch.1 + General Intro**: correct the company facts with \todo markers, add the planning section + `pgfgantt` from the git-log dates, fix the scope (observability).
- **WP3 – Ch.2 State of the art**: rewrite as prose, add ~20 bib entries to a *separate* `refs-ch2.bib` (merged later), add `\cite`s, a tool-comparison table.
- **WP4 – Ch.3 Requirements + Ch.4 Design**: use-case diagram, class diagram from the real entities, sequence diagrams, real endpoint table taken from the controllers, accurate security design (single admin user, localStorage, 30-min JWT), fix figures 3-x/4-x (B5).
- **WP5 – Ch.5 Implementation (merged old 5+6)**: real code listings pulled from `back/`, `front/`, `infra/`, `ansible/`, `.github/workflows/`; describe the CI/CD as it actually is (including the CD limitations); fix figures 5-x/6-x.
- **WP6 – Code hygiene (repo, not LaTeX)**: the §5 fixes (gitignore, SecurityConfig, demo login, Dockerfile USER, OPA path, README/compose naming, delete stray dirs) + Client/TransportLog/Auth tests. Runs in its own worktree/branch.

### Wave 3 (after WP6 merged)
- **WP7 – Evidence collection** (partly human): agent runs `mvn verify` + JaCoCo, `npm test`, `npm run build` (bundle size), local `docker compose` startup timing, `curl` latency + 401 + health checks, and saves the outputs into `docs/evidence/`. **Humans** take app screenshots and GitHub Actions/Security-tab screenshots.
- **WP8 – Ch.6 Results**: rebuild from `docs/evidence/` only; fix B2, B8, B9, B10.

### Wave 4
- **WP9 – Ch.7 Challenges/Discussion + General Conclusion**: dedupe future work, remove unsupported numbers, honest limitations list.
- **WP10 – Final review** (one agent, read-only, then fixes): cross-chapter consistency (versions, numbers, names), every `\ref` resolves, every bib entry is cited, no "Codex", no leftover `\todo`, spell-check, final PDF.

---

## 7. Status after Wave 2 (2026-11-05)

Done: Wave 1 + Wave 2. Report builds with 0 errors (108 pages). Ch.5 = merged implementation
(`chapters/05-implementation.tex`; old 05/06 files are now empty stubs). Bib: `references.bib` + `refs-ch2.bib` (33 entries).
Code: clients now require auth (SecurityConfig + JwtAuthenticationFilter), backend container non-root, demo login removed,
OPA workflow path fixed, Trivy gates on CRITICAL, README/compose fixed. Backend: 79 tests pass, JaCoCo line coverage 74.9 % (superseded: 82 tests / 77.1 % after the fixes)
(measured; run Maven with JDK 21 — the shell's JAVA_HOME is JDK 8). Nothing committed.

### Code bugs found in Wave 2: all fixed (2026-11-05)
Fixed and now reflected in ch.4/5: client update/delete (404), 401/403 JSON entry points, API base URL and CORS origins, cd-back trigger name,
scheduled Terraform run (drift only), Ansible wiring of all roles and backend env template, Rego rules (rego.v1, verified with conftest on synthetic plans),
stop.sh path, useCallback instead of eslint-disable. Backend: 82 tests, JaCoCo line coverage 77.1 % (chapter 6 figures still to be rebuilt from this run).
Ansible was only syntax-checked (`ansible-playbook --syntax-check`), never run against real hosts.

### Still open (to fix or keep as stated limitations)
- Ansible vault variables to add in both environments: `vault_app_user_username`, `vault_app_user_password`; `vault_production_jwt_secret`;
  optional `vault_ghcr_token` (or `GHCR_TOKEN` env); the Slack/PagerDuty webhooks referenced in `group_vars` (`vault_staging_slack_webhook`,
  `vault_production_slack_webhook`, `vault_pagerduty_key`) must exist or be removed.
- Database port: `database.port` (5432) vs the DigitalOcean managed PostgreSQL default (25060): check against the real cluster.
- DNS must point at the frontend before `nginx_tls` runs (certbot standalone needs the domain to resolve to the host).
- node_exporter port (9100) is not opened in ufw: metrics are not reachable from outside the host.
- Known limitations kept in the report: single admin user and role, JWT in localStorage, no refresh token, frontend CD is a placeholder,
  npm audit is report-only, CI only builds `-staging` image tags (production needs an explicit tag), no DAST, Ansible not exercised on real hosts.
- Missing-character warning (comma in `nullfont`) in the build log around chapter 2 pages 8-9: not investigated (outside WP5 files).

### Open questions for the authors (from `\needsinput` in the PDF)
Company facts + partner list + staff figures + org chart (ch.1); previous manual process (ch.1); Gantt phase split, start/end dates,
working method, who did what (ch.1); business needs actually expressed by the company (ch.3); branch protection / prod approval on GitHub (ch.4);
dedication, acknowledgements, abstract, résumé (front matter). Check page ranges for royce1970, provos1999, myrbakken2017.

### Layout to polish (WP10)
Class diagram (fig4-2) TransportLog lines touch the box edge; Implementation chapter is ~25 pages (could drop some listings).

## 8. Status after Wave 3 (2026-11-06)

Done: real evidence in `docs/evidence/` (SUMMARY.md = single source of numbers), 16 screenshots in
`docs/images/screens/`, Results chapter (ch.6) rebuilt from evidence, chapters 7–9 rewritten without invented
claims, all chapters labelled (`chap:context … chap:conclusion`) and cross-referenced. Report: 0 errors, 120 pages.

Still needed from the authors: GitHub Actions + Security-tab screenshots, Ansible run output, Swagger UI retake
(`swagger-ui-ok.png`; current capture shows "Failed to load remote configuration"), and the 25 `\needsinput` boxes.
Next: WP10 final review (consistency, spelling, bib page numbers, layout polish: latency-chart labels overlap slightly,
class-diagram text touches box edge).
