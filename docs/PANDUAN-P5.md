# Panduan Praktikum 5 — Cashflow (Spring Boot + Vaadin + JDBC PostgreSQL)

Proyek: `ifs25016-p5-pbo2026-springboot-cashflow` · Package: `org.delcom.ifs25016springbootcashflow`

> **Jujur soal status:** logika inti (domain, use case, adapter, JDBC) sudah **dikompilasi dan dites** (23 tes lulus,
> termasuk repository JDBC dengan H2). Bagian **Spring/Vaadin** (tampilan, konfigurasi) belum bisa dijalankan di
> lingkungan saya, jadi langkah 4 di bawah adalah uji pertamanya. Kalau ada error, kirim teksnya ke saya.
>
> Modul meminta mahasiswa mengerjakan sendiri. Gunakan proyek ini sebagai **acuan belajar**: baca tiap layer
> (urutan di bagian 8) sampai kamu bisa menjelaskannya sendiri.

---

## 1. Prasyarat (cek dulu)
| Cek | Perintah | Hasil yang benar |
|---|---|---|
| Java | `java -version` | versi 25 atau lebih |
| PostgreSQL berjalan | buka **pgAdmin** dan sambungkan server | tersambung tanpa error |
| Password postgres | (yang kamu buat saat instalasi) | ingat, dipakai di `.env` |

Maven tidak perlu dipasang: proyek sudah punya `mvnw` / `mvnw.cmd`.

## 2. Siapkan proyek
1. Unduh zip, ekstrak. Hasilnya folder `ifs25016-p5-pbo2026-springboot-cashflow`.
2. Pindahkan folder itu ke tempat kerjamu (mis. `D:\YULI SEMESTER 3\PBO\`).
3. Buka folder di **VS Code** (File → Open Folder) lalu buka terminal (**Ctrl + `**). Pastikan terminal berada di folder yang berisi `pom.xml`.

## 3. Buat database (wajib sebelum run)
Aplikasi langsung membuka koneksi saat start; tanpa database ia gagal dengan `database "pbo2026_cashflow" does not exist`.

**Cara A — pgAdmin:** klik kanan *Databases* → *Create* → *Database…* → nama `pbo2026_cashflow` → Save.

**Cara B — PowerShell** (ganti `PASSWORD_ANDA`; ganti `17` sesuai versi PostgreSQL-mu):
```powershell
$env:PGPASSWORD="PASSWORD_ANDA"; & "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -h localhost -c "CREATE DATABASE pbo2026_cashflow;"
```
Hasil benar: `CREATE DATABASE`. (Kalau bilang *already exists*, berarti sudah ada, aman.)

## 4. Atur `.env` lalu jalankan
1. Buka file `.env`. Ubah **hanya** `DB_PASSWORD` agar sama dengan password postgres-mu. Contoh isi:
   ```
   DB_HOST=localhost
   DB_PORT=5432
   DB_DATABASE=pbo2026_cashflow
   DB_USERNAME=postgres
   DB_PASSWORD=password_anda
   ROOT_USERNAME=admin
   ROOT_PASSWORD=admin
   ```
2. Jalankan:
   ```
   .\mvnw.cmd spring-boot:run
   ```
   Build pertama bisa 5–10 menit (mengunduh dependency dan frontend Vaadin). Tunggu `Started App in ... seconds`.
3. Buka http://localhost:8080 → otomatis dialihkan ke **/login**. Masuk dengan `admin` / `admin`.

## 5. Uji mandiri (centang satu per satu)
| # | Uji | Hasil yang diharapkan |
|---|---|---|
| 1 | Klik *Daftar sekarang*, buat user `budi` | langsung masuk ke dashboard |
| 2 | Tekan **F5** | tetap login |
| 3 | Menu avatar → **Keluar** | kembali ke /login; tombol Back tidak masuk lagi |
| 4 | Buka `/` atau `/profile` tanpa login | dialihkan ke /login |
| 5 | Saat login, buka `/login` atau `/register` | dialihkan ke `/` |
| 6 | Login sebagai `budi`, ketik `/users` di address bar | dialihkan ke `/` (ditolak) |
| 7 | Login `admin`, buka `/users` | daftar pengguna tampil; menu *Pengguna* ada |
| 8 | Tambah pemasukan `5000000`, pengeluaran `1253000` | saldo kartu `Rp 3,747,000.00` |
| 9 | Isi keterangan kosong / jumlah `abc` / `0` / `-5` | notifikasi merah, data tidak berubah |
| 10 | Tambah transaksi + pilih gambar bukti | thumbnail muncul; klik → modal zoom; klik gambar = zoom 1.0x/1.8x; tombol Perkecil/Perbesar/Tutup di kanan footer |
| 11 | Cari (huruf besar/kecil), urutkan (4 opsi + klik header) | hasil benar, hanya data sendiri |
| 12 | Ubah dan Hapus (ada dialog konfirmasi) | saldo ikut berubah |
| 13 | Admin → *Pengguna* → *Lihat Transaksi* budi | tampil read-only, tanpa tombol ubah/hapus |
| 14 | Profil: ubah nama, password, foto | avatar di sidebar ikut berubah |
| 15 | **Matikan** aplikasi (Ctrl+C), jalankan lagi | data masih ada (PostgreSQL) |
| 16 | Buka `http://localhost:8080/uploads/<nama-file>` | gambar tampil |
| 17 | Arahkan mouse ke tombol/menu/header grid | kursor tangan (pointer) |
| 18 | Buka dialog | header biru `#2563EB`, footer abu-abu `#F3F4F6`, tombol di kanan |

## 6. Jalankan tes dan coverage
```
.\mvnw.cmd clean verify
```
Laporan: `target\site\jacoco\index.html`. Tes memakai **H2** (profil `test`), jadi PostgreSQL tidak dibutuhkan.
`jacoco.haltOnFailure=false` di `pom.xml`: bila coverage < 100% build **tidak** gagal (hanya peringatan).
Ubah ke `true` hanya jika kamu sudah memastikan 100%.

## 7. Build production (wajib untuk deploy)
```
.\mvnw.cmd clean package -Pproduction
java -jar target\ifs25016-p5-pbo2026-springboot-cashflow-0.0.1-SNAPSHOT.jar
```
Tanpa `-Pproduction`, `java -jar` gagal dengan `vaadin-dev-server not found`.

## 8. Peta kode ↔ tahapan modul (untuk belajar dan presentasi)
| Tahap modul | File utama |
|---|---|
| 3 domain/entity | `Transaction` (amount `long`), `TransactionType`, `User`, `Role`, `SortOption` (4 opsi + Comparator) |
| 4 domain | `PasswordHasher` (SHA-256), `DomainException` → `EntityNotFoundException`, `ValidationException` |
| 5 repository (port) | `IRepository<T, ID>`, `ITransactionRepository`, `IUserRepository` |
| 6 usecase | `AuthUseCase`, `FinanceUseCase`, `UserUseCase` (tanpa Vaadin / `System.out`) |
| 7 adapter/repository | `SchemaInitializer`, `JdbcTransactionRepository`, `JdbcUserRepository`, `InMemoryRepository` + `TransactionRepository`/`UserRepository` |
| 8 adapter/storage | `FileStorage` (UUID + ekstensi, batas 2 MB, tolak non-gambar) |
| 9 presenter | `FinancePresenter` → `Rp 1,000,000.00` |
| 10 config | `AppConfig` (semua `@Bean`, seed admin, `/uploads/**`) |
| 11 auth | `AuthSession` (VaadinSession + cookie `cashflow_uid` + `LOGGED_OUT_KEY`) |
| 12 view | `LoginView`, `RegisterView`, `MainLayout`, `FinanceView`, `ProfileView`, `UsersView`, `UserTransactionsView`, `ImagePreviewDialog`, `LoadingIndicatorInitListener`, `styles.css` |
| 13–15 uji | `AppTest` + tes unit + `JdbcRepositoryTest` (H2) |

**Keputusan desain yang bisa kamu jadikan argumentasi:**
- *Kepemilikan dicek di use case*, bukan di view: `findOwned` menolak hapus/ubah transaksi orang lain.
- *Cookie sesi ditandatangani HMAC* (`SESSION_SECRET`), bukan sekadar ID, agar tidak bisa dipalsukan.
- *File dihapus bila validasi gagal* dan saat transaksi dihapus (tidak ada file yatim).
- *SQL memakai parameter terikat* (`?`), aman dari SQL injection.
- *SHA-256 tanpa salt* mengikuti ketentuan modul; di produksi nyata pakai bcrypt/argon2.
- `RepositoryException` membungkus `SQLException` agar domain tidak bergantung pada JDBC.

## 9. Git, GitHub, dan CI/CD (ringkas, rinci di PANDUAN-CICD.md)
1. `git init -b main` → `git add .` → `git status` (pastikan **tidak** ada `.env`, `uploads/`, `target/`, `generated`) → `git commit -m "feat: cashflow p5"`.
2. Buat repo GitHub `ifs25016-p5-pbo2026-springboot-cashflow` (nama repo = nama job Jenkins), lalu `git remote add origin <URL>` dan `git push -u origin main`.
3. Letakkan **`Jenkinsfile` dari konten kursus** di root proyek (tidak saya buat), commit dan push.
4. Di **Delcom → Website**: teknologi `java-springboot`, **Database: PostgreSQL**. Salin nilai koneksi dari halaman Detail ke tab **.env** website (`DB_HOST`, `DB_PORT`, `DB_DATABASE`, `DB_USERNAME`, `DB_PASSWORD`, plus `ROOT_*`, `SESSION_SECRET`, `APP_PORT`).
5. Jenkins: item bernama sama dengan repo, webhook GitHub, lalu cek semua stage hijau.
6. Delcom Grading: karena `/` butuh login, isi **path publik**: `/login` dan `/register`.

## 10. Troubleshooting
| Gejala | Penyebab / solusi |
|---|---|
| `database "pbo2026_cashflow" does not exist` | belum dibuat → ulangi bagian 3 |
| `password authentication failed for user "postgres"` | `DB_PASSWORD` di `.env` salah |
| `Connection refused` | PostgreSQL belum jalan (buka Services → postgresql) atau `DB_PORT` salah |
| `Port 8080 was already in use` | tutup aplikasi lama, atau ubah `APP_PORT` di `.env` |
| `mvn` tidak dikenal | pakai `.\mvnw.cmd`, bukan `mvn` |
| Error `cannot find symbol` / import Vaadin | di IDE tekan **Alt+Enter** pada baris merah → *Import class*; kirim teks error ke saya |
| Upload ditolak | hanya png/jpg/jpeg/gif/webp, maksimal 2 MB |
| Setelah login balik ke /login terus | pastikan cookie browser aktif; coba jendela Incognito |
| Jenkins gagal `mvnw: No such file` | file `mvnw` dan folder `.mvn` belum ter-push |
| Jenkins gagal di stage Build & Test | jalankan `.\mvnw.cmd clean verify` lokal dulu |
