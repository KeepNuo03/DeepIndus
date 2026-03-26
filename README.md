# DeepIndus Monorepo

DeepIndus is a full-stack industrial quality inspection system, including:

- `induscore/`: backend (Spring Boot) + Android app
- `frontend/`: Web frontend (Vue)
- `yolo-service/`: Python YOLO inference service
- `database/`: DB bootstrap scripts (schema + seed)
- `docs/`: architecture and integration guides

## Quick Start (for learners)

1. Prepare MySQL 8+
2. Create database and import scripts in order:
   - `database/schema.sql`
   - `database/seed.sql`
3. Configure each service from its `.env.example` / config template.
4. Start services:
   - backend (`induscore/`)
   - yolo service (`yolo-service/`)
   - web frontend (`frontend/`)
5. (Optional) run Android app (`induscore/android-app/`)

## Repository Structure

```text
deepIndus/
  induscore/
  frontend/
  yolo-service/
  database/
  docs/
```

## Security Notes

- Do NOT commit real production data.
- Do NOT commit API keys / JWT / private certs.
- Keep only anonymized seed data in `database/seed.sql`.
