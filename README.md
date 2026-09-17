# BedrockAuthBypass

Dua plugin yang saling melengkapi supaya **player Bedrock (lewat Geyser/Floodgate)
tidak perlu `/register` dan `/login` manual di AuthMe** — karena mereka sudah
terautentikasi lewat Xbox Live/akun Microsoft sebelum sampai ke server-mu.
Player Java tidak terpengaruh sama sekali, tetap pakai alur AuthMe normal.

```
geyser-authme-bypass/
├── pom.xml                      <- parent, build semua modul sekaligus
├── bedrockbypass-paper/         <- plugin PAPER, dipasang di server "login" (yang jalankan AuthMe)
└── bedrockbypass-velocity/      <- plugin VELOCITY, dipasang di proxy (opsional tapi direkomendasikan)
```

## Cara kerja

1. **bedrockbypass-velocity** (di proxy): setiap kali ada player yang mau
   pindah/connect ke server bernama `login`, plugin cek lewat Floodgate API
   apakah dia player Bedrock. Kalau ya, dia **dialihkan langsung** ke server
   lain (default: `lobby`) — jadi tidak pernah menyentuh AuthMe sama sekali.
2. **bedrockbypass-paper** (di server login): jaga-jaga kalau plugin Velocity
   di atas nonaktif, atau arsitektur jaringanmu memang mengharuskan Bedrock
   tetap transit dulu ke server login. Saat `PlayerJoinEvent`, plugin cek
   Floodgate API; kalau Bedrock, otomatis `forceRegister`/`forceLogin` ke
   AuthMe dengan password acak (dicoba beberapa kali dengan jeda, karena
   AuthMe memuat data player secara async).

Kamu bisa pakai salah satu saja atau keduanya sekaligus — keduanya independen.

## Prasyarat di server-mu (WAJIB, di luar plugin ini)

- **Geyser** terpasang di proxy Velocity, `auth-type: floodgate` di config Geyser.
- **Floodgate** terpasang di proxy Velocity **dan** di setiap backend Paper
  yang mau pakai Floodgate API (termasuk server login) — bukan cuma di proxy.
  Di config Floodgate proxy: `send-floodgate-data: true`, lalu salin file
  `key.pem` dari folder config Floodgate proxy ke folder config Floodgate
  di tiap backend server. **Jangan sebarkan `key.pem` ke siapa pun** — file
  ini yang membuat backend percaya "player ini beneran Bedrock terverifikasi".
- Di `velocity.toml`: `player-info-forwarding-mode` diset sesuai kebutuhan
  Floodgate (LEGACY atau MODERN, lihat dokumentasi Floodgate untuk versi
  Velocity-mu), dan di `spigot.yml` server backend: `settings.bungeecord: true`
  (atau setelan forwarding yang sesuai kalau pakai modern forwarding).
- **AuthMe 5.6.x** terpasang di server login.

Kalau salah satu di atas belum benar, kedua plugin ini akan log error yang
jelas di console (mis. "Floodgate tidak ditemukan") dan menonaktifkan diri
sendiri — supaya tidak diam-diam gagal.

## Build

Proyek ini butuh internet ke Maven Central / repo.papermc.io /
repo.opencollab.dev / repo.codemc.org untuk resolve dependency, jadi harus
di-build di luar sandbox pembuatan file ini. Ada 2 cara, pilih salah satu:

### Cara A - GitHub Actions (tanpa install apa-apa di komputer sendiri)

1. Buat repository baru di GitHub (boleh private), lalu upload/push seluruh
   isi folder `geyser-authme-bypass/` ke situ (termasuk folder `.github/`).
2. Buka tab **Actions** di repo tersebut - workflow "Build plugin jars"
   akan otomatis jalan setiap kamu push (atau klik **Run workflow** manual).
3. Setelah selesai (hijau/centang), buka run tersebut, scroll ke bagian
   **Artifacts** di bawah - ada `bedrockbypass-paper` dan
   `bedrockbypass-velocity`, masing-masing berisi file `.jar`-nya. Download,
   lalu extract dari zip yang GitHub buat.

### Cara B - Build manual di komputer/VPS sendiri

Butuh **JDK 25** (bukan cuma 17/21 — jar velocity-api 4.0.0-SNAPSHOT sendiri
di-compile pakai JDK 25, jadi javac versi lama tidak bisa membacanya sama
sekali, walau hasil jar plugin ini sendiri tetap jalan di JVM 17 ke atas)
dan Maven terpasang, lalu dari folder `geyser-authme-bypass/`:

```bash
mvn clean package
```

Hasil jar ada di:
- `bedrockbypass-paper/target/bedrockbypass-paper-1.0.0.jar`
- `bedrockbypass-velocity/target/bedrockbypass-velocity-1.0.0.jar`

### Soal versi (penting, baca ini sebelum build)

Minecraft pindah dari penomoran `1.21.x` ke penomoran berbasis tahun mulai
2026 (`26.1`, `26.1.1`, `26.2`, dst — rilis terakhir dengan awalan lama
adalah `1.21.11`). Konsekuensinya:

- Artifact **Paper API** memakai skema versi baru untuk rilis 26.x, mis.
  `26.2.build.87-stable`, bukan lagi `26.2-R0.1-SNAPSHOT`. Nomor build-nya
  berubah tiap beberapa hari.
- **Velocity** (resmi maupun fork populer) pindah ke `velocity-api
  4.0.0-SNAPSHOT` pertengahan 2026, dan proxy-nya sendiri butuh **Java 25**
  untuk DIJALANKAN (pastikan JVM yang menjalankan proxy-mu Java 25+). Untuk
  meng-*compile* plugin ini sendiri, JDK 17 ke atas sudah cukup — lihat
  catatan di bedrockbypass-velocity/pom.xml.
- Karena itu di setiap `pom.xml` modul, versi dependency sengaja ditaruh di
  `<properties>` paling atas dengan komentar link ke tempat cek versi
  terbaru — **cek dulu, sesuaikan kalau perlu, baru `mvn package`.**
- Kode plugin ini sendiri **hanya memakai API inti yang stabil** (event
  Bukkit dasar, scheduler, `@Plugin`/`@Subscribe` Velocity), jadi satu hasil
  build umumnya tetap kompatibel di rentang 1.21 → 26.x tanpa perlu
  di-compile ulang tiap update Minecraft — kecuali Paper/Velocity mengubah
  API tersebut (jarang terjadi untuk API sesederhana ini).
- Soal Java 17/21/25/26: kode di-compile dengan target `--release 17`
  (lihat `pom.xml` parent), jadi hasil `.jar`-nya bisa jalan di JVM 17
  sampai 26 — JVM baru selalu bisa menjalankan bytecode yang dikompilasi
  untuk versi lebih lama. Server dengan MC 1.21.x biasanya masih di
  Java 17/21, sedangkan server MC 26.x butuh Java 25 ke atas.

## Instalasi

1. Taruh `bedrockbypass-paper-1.0.0.jar` di folder `plugins/` **server
   login** (server Paper yang menjalankan AuthMe).
2. Taruh `bedrockbypass-velocity-1.0.0.jar` di folder `plugins/` **proxy
   Velocity**.
3. Start/restart proxy dan server login.
4. Edit `plugins/BedrockAuthBypass/config.properties` di proxy kalau nama
   server login/lobby-mu di `velocity.toml` bukan `login`/`lobby`.
5. Edit `plugins/BedrockAuthBypass/config.yml` di server login kalau mau
   ubah perilaku (mis. matikan auto-register, ubah jumlah percobaan, dll).

## Troubleshooting singkat

- **Log "Floodgate tidak ditemukan"** → Floodgate belum terpasang di server
  itu juga (ingat: harus di proxy *dan* di server login, bukan cuma proxy).
- **Player Bedrock tetap diminta login** → cek `key.pem` sudah disalin ke
  server login, dan `send-floodgate-data: true` di config Floodgate proxy.
- **Auto-login sering gagal** → perbesar/tambah angka di
  `retry-delays-ticks` pada `config.yml` (satuannya tick, 20 tick = 1 detik).
- **Server tujuan bypass tidak ditemukan** (di log proxy) → nama di
  `bypass-target-server` (config.properties) harus persis sama dengan nama
  server di `[servers]` pada `velocity.toml`.
