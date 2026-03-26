# Database Bootstrap

This directory is for open-source reproducible DB setup.

## Files

- `schema.sql`: full bootstrap chain (core tables + business tables + agent tables)
- `seed.sql`: minimal anonymized demo data for quick verification

## Import Order

1. `schema.sql`
2. `seed.sql`

## Run (recommended)

```powershell
cd C:\deepIndus
mysql -u root -p < database/schema.sql
mysql -u root -p < database/seed.sql
```

## What `schema.sql` includes

- `V1__init.sql`: users/roles/permissions
- `V2__p0_business_tables.sql`: detection/process/production_line/camera/station
- `V3__products_and_relations.sql`: products + relations
- `INIT_USER_ROLE_DEPT_DATA.sql`: departments + user extensions + operation logs + base seed data
- `V6__fix_users_online_status_column_type.sql`: column alignment patch
- `agent_p0_manual.sql`: agent session/message/tool/audit tables

## Notes

- Keep only demo/anonymized data in `seed.sql`.
- Never commit production dumps or user private data.
