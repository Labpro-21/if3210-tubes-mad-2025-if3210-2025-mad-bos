<center>
  <h1 style="color: #008080;font-size: 40px;font-weight: bold;">Aplikasi Purrytify</h1>
</center>


Aplikasi Purrytify adalah platform streaming musik yang memudahkan pengguna untuk menambah dan menikmati lagu secara interaktif. Dengan fitur unggulan seperti penambahan lagu ke dalam koleksi pribadi, streaming berkualitas tinggi, serta kontrol pemutaran seperti mode "next" dan daftar putar yang dapat dikustomisasi, aplikasi ini didesain untuk memberikan pengalaman mendengarkan musik yang seru dan praktis. Antarmuka yang intuitif serta desain modern mendukung kemudahan navigasi dan penggunaan setiap harinya.

## Table of Contents
- [Library yang Digunakan](#library-yang-digunakan)
- [Screenshot Aplikasi](#screenshot-aplikasi)
- [Pembagian Kerja Anggota Kelompok](#pembagian-kerja-anggota-kelompok)
- [Jumlah Jam Persiapan dan Pengerjaan](#jumlah-jam-persiapan-dan-pengerjaan)
- [Penanganan dan Analisis OWASP](#penanganan-dan-analisis-owasp)
- [Aksesibilitas Testing](#aksesibilitas-testing)

---

## Library yang Digunakan

Masukin lagi nanti diakhir:

- **Kotlin** – Bahasa pemrograman utama untuk Android.
- **Jetpack Compose** – Untuk UI modern dengan deklaratif.
- **Hilt** – Dependency injection.
- **Room** – Untuk manajemen database.
- **Retrofit** – Untuk komunikasi API.
- (Tambahkan library lainnya sesuai kebutuhan)

---

## Screenshot Aplikasi

Simpan screenshot aplikasi di folder `screenshot` dan tampilkan di bagian ini. Contoh:

![Screenshot 1](screenshot/screenshot1.png)
![Screenshot 2](screenshot/screenshot2.png)

---

## Pembagian Kerja Anggota Kelompok


| No | Bagian/Fitur            | Anggota (PIC) | 
|----|-------------------------|---------------|
| 1  | Header dan Navbar       | Anggota 1     | 
| 2  | Login                   | Anggota 2     | 
| 3  | Home                    | Anggota 3     |  
| 4  | Library                 | Anggota 1     | 
| 5  | Pemutaran Lagu          | 13522019   | 
| 6  | Penambahan Lagu         | Anggota 2     |
| 7  | Profile                 | 13522019     |  
| 8  | Liked Songs             | Anggota 1     | 
| 9  | Background Service      | Anggota 4     | 
| 10 | Network Sensing         | Anggota 2     | 
| 11 | Queue                   | Anggota 3     | 
| 12 | Shuffle                 | Anggota 4     |
| 13 | Repeat                  | Anggota 1     | 
| 14 | OWASP                   |13522019     |
| 15 | Pencarian               | Anggota 3     | 
| 16 | Accessibility Testing   | Anggota 4     | 


---

## Jumlah Jam Persiapan dan Pengerjaan

Cantumkan jumlah jam yang dihabiskan untuk persiapan, pengembangan, dan pengerjaan secara keseluruhan untuk masing-masing anggota. Contoh:

| Nama           | Persiapan | Pengerjaan | Total Jam |
|----------------|-----------|------------|-----------|
| 13522019     | 40 Jam     | 32 Jam     | 72 Jam    |
| 13522077      |  40 Jam     | XX Jam     | XX Jam    |
| 13522117      | 40 Jam     | XX Jam     | XX Jam    |

---

## Penanganan dan Analisis OWASP

**OWASP Top 10 Mobile** adalah daftar sepuluh kerentanan keamanan yang paling umum ditemukan pada aplikasi mobile. Daftar ini diterbitkan oleh Open Web Application Security Project (OWASP), organisasi nirlaba yang berfokus pada peningkatan keamanan perangkat lunak. Tujuan utama dari OWASP Top 10 Mobile adalah untuk meningkatkan kesadaran pengembang akan ancaman keamanan dan mendorong penerapan langkah-langkah mitigasi guna mengurangi risiko pada aplikasi mobile.

Dalam eksperimen pengujian ini, kami melakukan analisis keamanan terhadap aplikasi berdasarkan tiga kerentanan utama dari daftar 2024, yaitu:

- **M4: Insufficient Input/Output Validation**
- **M8: Security Misconfiguration**
- **M9: Insecure Data Storage**

---

### M4: Insufficient Input/Output Validation

**Deskripsi:**  
M4 terjadi ketika aplikasi tidak melakukan validasi dan sanitasi input pengguna maupun output data dengan benar. Hal ini dapat membuka celah bagi serangan seperti SQL injection, Cross-Site Scripting (XSS), dan manipulasi data lainnya. Secara umum, validasi dilakukan pada bagian server, namun dalam implementasi kali ini, kami sebiasa mungkin mengimplementasikan pada sisi client.

**Langkah Penanganan:**  
- **Validasi Input:**  
  - Menggunakan validasi format (misalnya, regex untuk email) pada semua input pengguna.
  - Memastikan data tidak kosong dan memenuhi standar yang telah ditetapkan (contoh: memeriksa apakah input berupa email atau password sudah terisi dengan benar).
- **Sanitasi Output:**  
  - Meng-encode data sebelum ditampilkan ke pengguna, terutama ketika data diambil dari sumber eksternal.
- **Penggunaan Parameterized Queries**  
  - Menerapkan parameter binding atau prepared statements pada query database untuk mencegah SQL injection. Implementasi di Client-Side mencakup implementasi saat melaksanakan Room Query seperti gambar dibawah
  ![Parameterized Queries](Assets/OWASP4.1.png)

---

### M8: Security Misconfiguration

**Deskripsi:**  
M8 berhubungan dengan kesalahan konfigurasi sistem, baik pada server, aplikasi, maupun library yang digunakan. Konfigurasi yang tidak optimal atau masih menggunakan pengaturan default dapat membuat aplikasi rentan terhadap serangan.

**Langkah Penanganan:**  
- **Perbaikan Konfigurasi:**  
  - Mengubah konfigurasi default pada server dan aplikasi guna menghindari setting yang rentan.
  - Mengamankan file konfigurasi dan mengatur hak akses yang tepat untuk mencegah akses tidak sah.
- **Konektivitas Aman:**  
  - Memastikan semua komunikasi menggunakan protokol terenkripsi (misalnya, HTTPS).
- **Audit dan Pemantauan:**  
  - Melakukan audit konfigurasi secara berkala untuk mendeteksi perubahan yang tidak diinginkan.
  - Mengimplementasikan alat pemantauan untuk memeriksa potensi kesalahan konfigurasi secara real-time.

---

### M9: Insecure Data Storage

**Deskripsi:**  
M9 menyangkut penyimpanan data yang tidak aman, di mana data sensitif dapat diakses atau dicuri oleh pihak tidak berwenang. Ini sangat penting pada aplikasi mobile, di mana penyimpanan lokal atau penggunaan cloud harus dilakukan dengan tingkat keamanan tinggi.

**Langkah Penanganan:**  
- **Enkripsi Data:**  
***Implementasi Kelas Enkripsi***

![Implementasi Kelas Enkripsi](Assets/OWASP_9/OWASP_9.1.png)

***Implementasi Enkripsi pada penyimpanan token***

![Implementasi Enkripsi pada penyimpanan token](Assets/OWASP_9/OWASP_9.2.png)

***Implementasi Dekripsi saat mengambil refresh token***

![Implementasi Dekripsi saat mengambil refresh token](Assets/OWASP_9/OWASP_9.3.png)

- **Penggunaan Penyimpanan Aman:**  
Aplikasi kami aman karena menggunakan <span style="color: red;font-weight: bold;">Google Tink</span> dan <span style="color: red;font-weight: bold;">Android KeyStore</span> untuk menyimpan data sensitif secara terenkripsi. Master key yang dilindungi hardware-backed memastikan kunci tidak dapat diakses, sementara keyset enkripsi disimpan di <span style="color: red;font-weight: bold;">SharedPreference</span> dalam bentuk terenkripsi, sehingga data seperti token tidak tersimpan sebagai plain text.

***Implementasi Shared Preference dalam penyimpanan keyset enkripsi***

![Implementasi Dekripsi saat mengambil refresh token](Assets/OWASP_9/OWASP_9.4.png)
- **Pengaturan Izin Akses:**  
  - Membatasi akses ke data lokal dengan menetapkan izin yang tepat sehingga data tidak dapat diakses oleh aplikasi lain.
- **Pemeriksaan Keamanan:**  
  - Melakukan pengujian penetrasi secara berkala untuk memastikan bahwa data tidak mudah disusupi dan disimpan dengan aman.

---

Dokumentasi ini mencerminkan upaya kami untuk mengatasi dan memitigasi risiko dari tiga kerentanan utama tersebut. Dengan menerapkan langkah-langkah di atas, kami berusaha mewujudkan aplikasi mobile yang aman, andal, dan sesuai dengan standar keamanan dari OWASP.


## Aksesibilitas Testing

P

---

