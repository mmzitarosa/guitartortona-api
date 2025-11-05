## Commit Messages

### Sviluppo Quotidiano (Conventional Commits)
```bash
feat: <descrizione>     # Nuova feature
fix: <descrizione>      # Bug fix
docs: <descrizione>     # Documentazione
refactor: <descrizione> # Refactoring
test: <descrizione>     # Test
chore: <descrizione>    # Task varie
```

### Deploy (Keywords Speciali)
```bash
RELEASE: <descrizione>  # Deploy in staging
MAJOR: <descrizione>    # Breaking changes + nuova major version
```

### Esempi
```bash
# Sviluppo
git commit -m "feat: add user login"
git commit -m "fix: resolve login crash"

# Deploy
git commit -m "RELEASE: Deploy login feature"
```