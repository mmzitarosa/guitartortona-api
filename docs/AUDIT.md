# Registro di audit

Ogni creazione, modifica ed eliminazione di un'entità viene scritta nella tabella `audit_log` (migration V5), nella stessa transazione della modifica. I record non vengono mai cancellati.

## Funzioni coperte

Tutte le entità JPA: prodotti (`Product`), vendite, fatture e acquisti, brogliaccio, fornitori, marche, categorie.

| Operazione | Cosa viene registrato |
|---|---|
| `CREATE` | una riga con snapshot JSON dell'entità in `new_value` |
| `UPDATE` | una riga per ogni campo modificato (`field`, `old_value`, `new_value`) |
| `DELETE` | una riga con snapshot JSON in `old_value` |

Utente: claim `preferred_username` del token (`sub` in fallback); `local` con sicurezza disattivata.

## Escluso

- Campi tecnici: `id`, `createdDate`, `updatedDate`.
- `stock` e `stockPending`: gestiti dai trigger DB (V3), non dall'applicazione.
- Il codice interno assegnato subito dopo la creazione del prodotto.
- Update JPQL in blocco e modifiche dirette sul DB: non passano dagli eventi Hibernate.

## Consultazione

- `GET /api/v1/audit` (solo `app-admin`): filtri `username`, `entityType`, `entityId`, `operation`, `from`, `to`; paginato, dal più recente.
- `GET /api/v1/product/{id}/timeline`: storia del prodotto già costruita dal server (`ProductTimelineService`): acquisti, modifiche alle righe fattura, vendite, modifiche e creazione del prodotto, dalla voce più recente; più i totali acquistato/venduto.
  - Ogni riga di audit di prodotto, righe fattura e vendite porta `product_id`, così la storia si legge senza join.
  - La voce di acquisto riporta i valori originali (quelli precedenti alla prima modifica); `superseded` elenca i campi poi sovrascritti.
  - Le modifiche dello stesso giorno sulla stessa entità diventano una voce: primo valore precedente → ultimo nuovo.
  - Costi, fatture e prezzi di vendita solo per chi vede i costi; gli utenti solo per l'admin.
