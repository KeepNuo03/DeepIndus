# Architecture Overview

DeepIndus is organized as a full-stack monorepo:

- `induscore/`: Spring Boot backend + Android app
- `frontend/`: Vue web frontend
- `yolo-service/`: Python-based YOLO inference service
- `database/`: bootstrap SQL scripts

## Core Runtime Flow

1. Web/Android sends business request to backend.
2. Backend reads business data and/or calls yolo-service.
3. Backend returns structured response to clients.
4. Agent-related interactions are persisted in MySQL (`agent_*` tables).
