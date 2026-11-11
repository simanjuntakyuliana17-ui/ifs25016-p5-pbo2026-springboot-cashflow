# Cashflow (Praktikum 5 PBO 2026)

Aplikasi web pencatat keuangan pribadi: **Spring Boot 4.1.2 + Vaadin 25.3.4 + JDBC PostgreSQL**, Clean Architecture.

Fitur: registrasi/login/logout (sesi bertahan setelah refresh), role ADMIN/USER, tambah pemasukan/pengeluaran
dengan bukti gambar (zoom), ubah, hapus (konfirmasi), cari, urut (4 `SortOption`), kartu saldo, profil + foto,
halaman admin (daftar pengguna + transaksi pengguna lain read-only).

## Menjalankan
1. Buat database: `CREATE DATABASE pbo2026_cashflow;`
2. Salin `.env.example` menjadi `.env` lalu sesuaikan `DB_PASSWORD`.
3. `.\mvnw.cmd spring-boot:run` lalu buka http://localhost:8080 (admin: `admin` / `admin`).

Build production: `.\mvnw.cmd clean package -Pproduction`. Tes: `.\mvnw.cmd clean verify`.

Panduan lengkap: [docs/PANDUAN-P5.md](docs/PANDUAN-P5.md).
