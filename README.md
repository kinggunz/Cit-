# gunzdev

APK Android (tema astronot) + backend WhatsApp (pairing code).

## 1. Backend (wajib, jalankan di VPS/Railway/Render yang selalu hidup)
```
cd backend && npm install && npm start
```
Harus punya HTTPS publik. Lalu edit `gradle.properties` -> `BACKEND_URL=https://domain-kamu`.

## 2. Google Login
Google Cloud Console -> APIs & Services -> Credentials -> Create OAuth client ID -> **Android**
- Package name: `com.gunzdev.app`
- SHA-1: ``
(Juga isi OAuth consent screen, tambahkan email kamu sebagai Test user.)

## 3. Build APK
Upload folder ini ke GitHub (branch main) -> tab **Actions** -> workflow "Build APK gunzdev" -> download artifact **gunzdev-apk**.
