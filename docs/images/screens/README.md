# Screenshots for chapter 7

`\screenshot{<file>}{caption}{label}` in `chapters/07-results-evaluation.tex` picks the files up automatically; a missing file is printed as a grey "Screenshot needed" box.

## Present (produced 2026-11-05/06 on the local stack, commit f5a0bd2)

| File | Content | Used in ch. 7 |
|---|---|---|
| `login.png` | Login page | yes |
| `dashboard.png` | Dashboard (50 generated records of each kind) | yes |
| `clients.png`, `drivers.png`, `transport-logs.png` | Management pages | yes |
| `transport-form.png` | Creation dialog with validation errors | yes |
| `transport-form-open.png` | Same dialog, empty, no errors | no (available) |
| `actuator-health.png` | `/actuator/health` in a browser | yes |
| `jacoco-report.png` | JaCoCo HTML report | yes |
| `docker-logs.png` | Rendered text of `docker compose ps`, image sizes, `id`, backend log (not a terminal capture) | yes |
| `security-logs.png` | Rendered text of the backend "Invalid JWT token" log line | yes |
| `http-401.png`, `api-response.png`, `semgrep.png`, `trivy-scan.png` | Rendered text of curl / scanner outputs (not Postman, not a terminal capture; trivy-scan shows 3 CRITICAL, not "zero critical") | no (tables say the same) |
| `swagger-ui.png` | **Unusable**: shows "Failed to load remote configuration" | no |

## Still needed from the authors

| File | Should show |
|---|---|
| `swagger-ui-ok.png` | Swagger UI with the endpoint list loaded (retake; replaces the failed `swagger-ui.png`) |
| `ghactions.png` | GitHub Actions run summary of the CI workflows |
| `ghactions-security.png` | Logs of the security-scan workflow |
| `github-security-tab.png` | GitHub Security tab, code-scanning alerts |
| `ansible.png` | Output of a real Ansible playbook run against the servers |
| `devtools.png` (optional) | Browser DevTools page-load of the dashboard |

The parcel-tracking screenshots (`tracking-search.png`, `tracking-result.png`, `admin-dashboard.png`) were removed from the list: the feature does not exist in the application.
