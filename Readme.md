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

- androidx-core-ktx: 1.12.0
- androidx-core-splashscreen: 1.0.1
- androidx-datastore-preferences: 1.1.3
- androidx-hilt-hilt-navigation-compose: 1.2.0
- androidx-hilt-work: 1.2.0
- androidx-material-icons-extended: 1.7.8
- androidx-material3-window-size: 1.1.0
- androidx-navigation-navigation-compose: 2.8.9
- androidx-palette: 1.0.0
- androidx-recyclerview: 1.3.1
- androidx-room-compiler: 2.6.1
- androidx-room-ktx: 2.6.1
- androidx-room-runtime: 2.6.1
- androidx-runtime-livedata: 1.7.8
- androidx-work-runtime-ktx: 2.8.1
- coil-compose: 2.7.0
- compiler: 4.16.0
- converter-gson: 2.11.0
- glide: 4.16.0
- hilt-android: 2.51.1
- hilt-android-compiler: 2.51.1
- junit: 4.13.2
- androidx-junit: 1.2.1
- androidx-espresso-core: 3.6.1
- androidx-lifecycle-runtime-ktx: 2.8.7
- androidx-activity-compose: 1.8.2
- androidx-compose-bom: 2025.03.00
- androidx-ui: (latest)
- androidx-ui-graphics: (latest)
- androidx-ui-tooling: (latest)
- androidx-ui-tooling-preview: (latest)
- androidx-ui-test-manifest: (latest)
- androidx-ui-test-junit4: (latest)
- androidx-material3: 1.3.1
- logging-interceptor: 4.12.0
- retrofit: 2.11.0
- tink-android: 1.17.0
- androidx-material3-window-size-class1-android: 1.3.2

- **Kotlin** – Bahasa pemrograman utama untuk Android.
- **Jetpack Compose** – Untuk UI modern dengan deklaratif.
- **Hilt** – Dependency injection.
- **Room** – Untuk manajemen database.
- **Retrofit** – Untuk komunikasi API.
- (Tambahkan library lainnya sesuai kebutuhan)

---

## Screenshot Aplikasi
**Login**  
![Login](Screenshots/Login.jpg)

**Home**  
![Home](Screenshots/Home.jpg)

**Lbrary**  
![Library](Screenshots/Library.jpg)

**Profile**  
![Profile](Screenshots/Profile.jpg)

**Add Song**  
![AddSong](Screenshots/AddSong.jpg)

**FullPlayer**  
![FullPlayer](Screenshots/FullPlayer.jpg)

**Edit Song**  
![Edit](Screenshots/Edit.jpg)

**LikedSong**  
![Liked](Screenshots/Liked.jpg)

**Queue**  
![Queue](Screenshots/Queue.jpg)

**No Network**  
![Offline](Screenshots/Offline.jpg)

**Online again**  
![Online](Screenshots/Online.jpg)

--- 

## Pembagian Kerja Anggota Kelompok


| No | Bagian/Fitur            | Anggota (PIC) | 
|----|-------------------------|---------------|
| 1  | Header dan Navbar       | 13522117      | 
| 2  | Login                   | 13522117      | 
| 3  | Home                    | 13522077      |  
| 4  | Library                 | 13522117      | 
| 5  | Pemutaran Lagu          | 13522019   | 
| 6  | Penambahan Lagu         | 13522117      |
| 7  | Profile                 | 13522019     |  
| 8  | Liked Songs             | 13522117      | 
| 9  | Background Service      | 13522117      | 
| 10 | Network Sensing         | 13522077     | 
| 11 | Queue                   | 13522077    | 
| 12 | Shuffle                 | 13522077     |
| 13 | Repeat                  | 13522077    | 
| 14 | OWASP                   |13522019     |
| 15 | Pencarian               | 13522117     | 
| 16 | Accessibility Testing   | 13522019    | 


---

## Jumlah Jam Persiapan dan Pengerjaan

Cantumkan jumlah jam yang dihabiskan untuk persiapan, pengembangan, dan pengerjaan secara keseluruhan untuk masing-masing anggota. Contoh:

| Nama           | Persiapan | Pengerjaan | Total Jam |
|----------------|-----------|------------|-----------|
| 13522019     | 40 Jam     | 32 Jam     | 72 Jam    |
| 13522077      |  40 Jam     | 32 Jam     | 72 Jam    |
| 13522117      | 40 Jam     | 48 Jam     | 88 Jam    |

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
  - Menggunakan validasi format (misalnya, regex untuk email) pada semua input pengguna dan memastikan data tidak kosong dan memenuhi standar yang telah ditetapkan (contoh: memeriksa apakah input berupa email atau password sudah terisi dengan benar).
  ![Validasi input](Assets/OWASP_4/OWASP4.2.png)
- **Penggunaan Parameterized Queries**  
  - Menerapkan parameter binding atau prepared statements pada query database untuk mencegah SQL injection. Implementasi di Client-Side mencakup implementasi saat melaksanakan Room Query seperti gambar dibawah
  ![Parameterized Queries](Assets/OWASP_4/OWASP4.1.png)

---

### M8: Security Misconfiguration

**Deskripsi:**  
M8 berhubungan dengan kesalahan konfigurasi sistem, baik pada server, aplikasi, maupun library yang digunakan. Konfigurasi yang tidak optimal atau masih menggunakan pengaturan default dapat membuat aplikasi rentan terhadap serangan.

**Langkah Penanganan:**  
- **Perbaikan Konfigurasi:**  
Konfigurasi buildTypes di dalam file build.gradle ini sesuai dengan M8 karena mengatur proses build aplikasi Android pada versi rilis (release), yang penting untuk memastikan aplikasi yang diproduksi siap untuk distribusi. Pengaturan isDebuggable = false mencegah proses debug pada aplikasi rilis, menjaga keamanan aplikasi. Sementara itu, isMinifyEnabled = true mengaktifkan minifikasi menggunakan ProGuard atau R8, yang mengurangi ukuran file aplikasi dan mengobfuskasi kode agar lebih sulit dibongkar.
  ![Handle Built](Assets/OWASP_8/OWASP_8.1.png)
---

### M9: Insecure Data Storage

**Deskripsi:**  
M9 menyangkut penyimpanan data yang tidak aman, di mana data sensitif dapat diakses atau dicuri oleh pihak tidak berwenang. Ini sangat penting pada aplikasi mobile, di mana penyimpanan lokal atau penggunaan cloud harus dilakukan dengan tingkat keamanan tinggi.

**Langkah Penanganan:**  
- **Enkripsi Data:**  
***Implementasi Kelas Enkripsi***

    - ***Implementasi Kelas Enkripsi***  
      
      ![Implementasi Kelas Enkripsi](Assets/OWASP_9/OWASP_9.1.png)
      
    - ***Implementasi Enkripsi pada penyimpanan token***  
      
      ![Implementasi Enkripsi pada penyimpanan token](Assets/OWASP_9/OWASP_9.2.png)
      
    - ***Implementasi Dekripsi saat mengambil refresh token***
      ![Implementasi Enkripsi pada penyimpanan token](Assets/OWASP_9/OWASP_9.3.png)


- **Penggunaan Penyimpanan Aman:**  
Aplikasi kami aman karena menggunakan <span style="color: red;font-weight: bold;">Google Tink</span> dan <span style="color: red;font-weight: bold;">Android KeyStore</span> untuk menyimpan data sensitif secara terenkripsi. Master key yang dilindungi hardware-backed memastikan kunci tidak dapat diakses, sementara keyset enkripsi disimpan di <span style="color: red;font-weight: bold;">SharedPreference</span> dalam bentuk terenkripsi, sehingga data seperti token tidak tersimpan sebagai plain text.

    - ***Implementasi Shared Preference dalam penyimpanan keyset enkripsi***
![Implementasi Dekripsi saat mengambil refresh token](Assets/OWASP_9/OWASP_9.4.png)
- **Pengaturan Izin Akses:**  
  - Membatasi akses ke data lokal dengan menetapkan izin yang tepat sehingga data tidak dapat diakses oleh aplikasi lain. Penggunaan penyimpanan internal seperti DataStore merupakan langkah yang sesuai dengan pendekatan OWASP M9, karena data hanya dapat diakses oleh aplikasi tersebut. 
      - ***Implementasi Penyimpanan token di Context***  
      
      ![Implementasi Kelas Enkripsi](Assets/OWASP_9/OWASP_9.5.png)

---

Dokumentasi ini mencerminkan upaya kami untuk mengatasi dan memitigasi risiko dari tiga kerentanan utama tersebut. Dengan menerapkan langkah-langkah di atas, kami berusaha mewujudkan aplikasi mobile yang aman, andal, dan sesuai dengan standar keamanan dari OWASP.


## Aksesibilitas Testing

Hasil testing dapat dilihat pada link berikut : https://drive.google.com/file/d/14YwVKkxjG2GD0K88KJaBYnUVgNcGpOG_/view?usp=sharing
 
Perubahan yang dilakukan:
- Memberikan content description yang berbeda untuk foto album serta judul lagu pada full player.
- Memberikan content description pada text dalam sebuah view dengan menggunakan mergeDescendants sehingga dianggap menjadi sebuah node
- Mengubah kontras warna pada tombol login serta tombol play
- Memberikan content description yang berbeda untuk tombol penambahan pada tiap komponen yang muncul dalam 1 halaman yang sama
- Meningkatkan aksesibilitas dengan memperbesar icon

Perubahan yang tidak / tidak dapat dilakukan:
- Adanya hidden text pada background login , dimana sistem melihat tulisan pada background sebagai text
- Content Description yang sama pada recycleview dimana keduanya muncul pada 1 halaman yang sama, namun dianggap minor karena jika dikonversi pada text, keduanya sebenarnya memiliki definisi yang sama serta fungsi yang sama , yakni untuk memutar lagu
- Kontras warna pada beberapa background album dengan background aplikasi. Hal ini dianggap minor karena tidak semua album , melainkan hanya album tertentu yang tidak memiliki kontras yang baik dengan background aplikasi.

NB : Karena merasa UI lebih penting, kami akhirnya memilih untuk memperkecil icon
---

