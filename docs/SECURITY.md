# Sicurezza API (Spring Security + Keycloak)

L'API è un **resource server OAuth2**: accetta solo `Authorization: Bearer <JWT>` emessi da Keycloak
(realm `guitarlab`), validando firma, `iss` e `aud` (`guitartortona-api`).

## Autorizzazioni

| Richiesta | Ruoli ammessi (realm role) |
|---|---|
| `GET /api/v1/**` | `guitarlab-viewer`, `app-user`, `app-admin` |
| `POST/PUT/PATCH /api/v1/**` | `app-user`, `app-admin` |
| `DELETE /api/v1/**` | `app-admin` |
| `actuator` su porta di management separata (8081, non esposta da Ingress/Service pubblico) | libero (probe, Prometheus) |
| `actuator` sulla porta API | solo `health`; il resto `app-admin` |
| qualsiasi altro percorso | negato |

I ruoli vengono letti dal claim `roles` (mapper `realm-roles` del client `guitartortona-web`) oppure da `realm_access.roles`.
Gruppi Keycloak → ruoli: `guitarlab-admins` → `app-admin`; `guitarlab-devs` → `app-user`; `guitarlab-viewers` → `guitarlab-viewer`.

Nota: `GET /api/v1/ledger/print` e `/product/print` restituiscono HTML (Thymeleaf) e richiedono anch'essi il Bearer.
Il frontend deve scaricarli con `fetch` + header `Authorization` e mostrarli (iframe/blob), non con un semplice link/`window.open`.

## Configurazione (variabili d'ambiente)

| Variabile | Default | Note |
|---|---|---|
| `KEYCLOAK_ISSUER_URI` | `https://keycloak.guitar.lab/realms/guitarlab` | deve coincidere con `iss` dei token |
| `KEYCLOAK_JWK_SET_URI` | `https://keycloak.guitar.lab/realms/guitarlab/protocol/openid-connect/certs` | in cluster usare `http://keycloak.keycloak.svc.cluster.local:8080/realms/guitarlab/protocol/openid-connect/certs` (evita di installare la CA interna nella JVM) |
| `KEYCLOAK_AUDIENCE` | `guitartortona-api` | audience richiesta |
| `CORS_ALLOWED_ORIGINS` | `https://vue.guitar.lab,https://vue-staging.guitar.lab` | elenco separato da virgole |
| `management.server.port` / `MANAGEMENT_SERVER_PORT` | (stessa porta) | se impostata, l'actuator su quella porta è libero |

Staging e produzione **non** devono impostare `app.security.enabled=false`.

## Sviluppo locale

Profilo `local` (default di `application.yml`): `app.security.enabled=false` (nessun login, log di WARN all'avvio)
e CORS su `http://localhost:5173` / `3000` / `8080`. Quindi frontend su `localhost:5173` → API su `localhost:8080` funziona senza token.

Per provare l'autenticazione vera in locale: avvia con `--app.security.enabled=true` e ottieni un token da Keycloak (serve il frontend o un client di test).

> Attenzione: `application.yml` attiva `local` se `SPRING_PROFILES_ACTIVE` non è impostata. Nei deployment imposta sempre `SPRING_PROFILES_ACTIVE` (es. `kubernetes,production`).

## Lato Keycloak (già nel realm del server `guitarlab`)

- client `guitartortona-api`: bearer-only
- client `guitartortona-web`: pubblico + PKCE, mapper `realm-roles` (claim `roles`) e `audience-api`
