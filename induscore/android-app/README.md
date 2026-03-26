# induscore-mobile (Android)

Minimal Android scaffold for Induscore mobile inspection app.

## Stack
- Kotlin + Jetpack Compose
- Retrofit + OkHttp
- Room (SQLite)
- WorkManager (offline upload retry)

## Current scope
- Login screen (`/v1/auth/login`)
- System home screen (module button entry)
- Workbench screen (`/v1/mobile/workbench`)
- Review task list (`/v1/mobile/tasks/review`)
- Offline upload queue worker scaffold

## Navigation flow
`Login` -> `System Home` -> module button -> target feature screen

## Run
1. Open `android-app` in Android Studio.
2. Let Gradle sync finish.
3. Start backend at `http://localhost:8000`.
4. Run Android emulator and start app.

> Default API base URL is `http://10.0.2.2:8000/` in `AppConfig.kt`.
> If using a real device, change base URL to your LAN IP (e.g. `http://192.168.1.100:8000/`).
