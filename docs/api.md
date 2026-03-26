# API Notes

This repository contains multiple services and APIs:

- Backend REST APIs (Spring Boot): see `induscore/src/main/java/.../controller`
- Agent APIs:
  - `POST /v1/agent/chat`
  - `POST /v1/agent/chat/stream`
  - `GET /v1/agent/sessions`
  - `GET /v1/agent/sessions/{sessionId}`
  - `DELETE /v1/agent/sessions/{sessionId}`
- Mobile APIs:
  - `GET /v1/mobile/workbench`
  - `GET /v1/mobile/tasks/review`
  - `GET /v1/mobile/uploads/{idempotencyKey}`
- YOLO service APIs: see `yolo-service/README.md` for endpoint details.

For detailed interface contracts, also refer to:
- `induscore/android_docs/18_Agent接口契约与联调规范.md`
