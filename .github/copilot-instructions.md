# Istruzioni Copilot – guitartortona-api

Rispondi e commenta in italiano; codice e nomi in inglese.

## Stack
Java 21, Spring Boot 3.5, Spring Data JPA, MariaDB (driver mariadb-java-client), MapStruct, Lombok, Thymeleaf (stampe), Actuator/Prometheus, Maven wrapper (`./mvnw`).
Package root: `it.mmzitarosa.guitartortona` (controller, service, repository, entity, dto, mapper, utils).

## Architettura
- Controller sottili: validano input (`@Valid`), delegano ai service, non contengono logica di business.
- Mai esporre entity JPA: usare sempre DTO + MapStruct mapper.
- Transazioni (`@Transactional`) solo nel layer service.
- Importi monetari: `BigDecimal`, mai `double`/`float`.
- Schema DB: `ddl-auto: none`; ogni modifica allo schema richiede una migrazione versionata (Flyway), mai modifiche manuali.

## Sicurezza (priorità alta, dati contabili)
- Autenticazione/autorizzazione tramite Keycloak (OAuth2 Resource Server, JWT). Ogni endpoint deve avere una regola esplicita di ruolo; default deny.
- Nessun segreto nel codice o nei file committati: usare variabili d'ambiente (`DB_URL`, `DB_USER`, `DB_PASSWORD`) gestite da Vault/External Secrets.
- Solo query parametrizzate (Spring Data / JPQL con parametri); mai concatenare stringhe SQL.
- Validazione su tutti i DTO in ingresso (Bean Validation); limiti di dimensione/paginazione.
- CORS ristretto agli origin noti; CSRF/headers di sicurezza coerenti con API stateless.
- Non loggare dati personali, token o importi sensibili. Errori verso il client senza stack trace.
- Actuator: esporre pubblicamente solo `health`; il resto protetto.
- Verificare sempre IDOR/controllo di proprietà dei dati su accessi per id.

## Qualità
- Test con JUnit 5 + Mockito; test di integrazione con Testcontainers (MariaDB).
- Conventional commits (vedi COMMIT.md). Branch: `develop` -> staging, `main` -> produzione.
- Prima di consegnare: `./mvnw -q verify`.
