# FAQ

## 1) Can I publish this repo without DB data?
Yes. Publish schema and anonymized seed only. Do not publish production dump.

## 2) How do others run this repo quickly?
Provide:
- root `README.md`
- `database/schema.sql` + `database/seed.sql`
- `.env.example` templates

## 3) What should never be committed?
- API keys / JWT / private certs
- `.env` real values
- large model files (`*.pt`, `*.onnx`, `*.engine`, `*.tflite`)
- generated folders (`node_modules`, `build`, `target`, `venv`)

## 4) I switched to a new agent/chat. Will context be preserved?
Usually no full memory carry-over. Keep handover docs in `docs/` so next agent can continue immediately.
