Nama Anggota Kelompok: 
6182401041 - 	Eugenius Steven Leif 		
6182401015 - 	Pearce Nathaniel Nicholas 
6182401051 - 	Kenneth Jonathan Harianto 

# WhutsAppBro 
> Sebuah Aplikasi Group Chat Berbasis JavaFX dengan Fitur Berbagi Media & Dokumen Menggunakan Arsitektur Sockets Dual-Port.

WhutsAppBro adalah aplikasi *chatting* multi-client yang dirancang menggunakan bahasa pemrograman Java dan JavaFX untuk antarmuka penggunanya. 

---

## Fitur Utama

* **Real-time Text Chatting:** Komunikasi teks instan antar client di dalam ruang obrolan (*chat room*) yang terisolasi.
* **Arsitektur Dual-Port Sockets:** * **Port 1234 (Control Plane):** Jalur khusus untuk penanganan perintah teks, autentikasi, manajemen lobi, dan protokol kontrol ruangan menggunakan pembatas `#`.
    * **Port 6767 (Data Plane):** Jalur khusus untuk mentransfer byte data mentah file gambar, video, dan dokumen secara cepat tanpa mengganggu kelancaran chat teks.
* **Any File Transfer Support (PDF, ZIP, DOCX, dll):** Mendukung pengiriman berkas jenis apa pun. Sistem akan membuat kartu lampiran dokumen yang menampilkan nama berkas serta ukurannya (KB).
* **OS Native Integration (Desktop API):** Cukup dengan mengklik kartu dokumen di dalam obrolan, aplikasi akan otomatis memerintahkan sistem operasi untuk membuka berkas tersebut menggunakan aplikasi default komputer (misal: PDF terbuka di Chrome).
* **Asynchronous Background Downloading:** Proses unduh file media dan dokumen berjalan di *background thread* dengan buffer 8KB/4KB untuk mencegah pembekuan (*freezing*) pada UI utama JavaFX.
* **Dynamic LAN Connection:** Pengguna dapat memasukkan Alamat IPv4 Wi-Fi Server secara dinamis pada layar login untuk pengujian lintas perangkat, lengkap dengan *fallback* otomatis ke `localhost` dan penanganan error koneksi yang aman.

---

## Struktur Arsitektur Jaringan

Aplikasi ini memisahkan aliran data teks kontrol dan file biner untuk menjaga efisiensi *bandwidth* dan stabilitas koneksi:

```text
[ Client A ] ---(Port 1234: Protokol Teks/Kontrol #)---> [ Chat Server ]
[ Client B ] <---(Port 1234: Broadcast Notifikasi #)---- [ Chat Server ]

[ Client B ] ---(Port 6767: Request DOWNLOAD)----------> [ File Transfer Server ]
[ Client B ] <--(Stream Byte Data Mentah/8KB Buffer)--- [ File Transfer Server ]

---

## Prasyarat Sistem
> Java Development Kit (JDK) 21
> Apache Maven atau Maven Daemon (mvnd)

---
## Quick Setup & Deployment Guide
1. Build Proyek Menjadi Berkas JAR
Buka terminal di direktori utama proyek (tempat file pom.xml berada) dan eksekusi perintah: "mvnd clean package"

2. Konfigurasi Jaringan & Menjalankan Server
Dapatkan IP Host: Buka Command Prompt pada komputer yang bertindak sebagai Server, ketik: "ipconfig"

Cari dan catat alamat IPv4 Address Wi-Fi aktif Anda (misalnya: 192.168.1.5).

Jalankan Backend Server: Eksekusi perintah berikut di terminal mesin Server: "java -cp target/WhutsAppBro.jar server.ChatServer"
Server otomatis aktif dan mendengarkan Port Teks 1234 serta Port Data 6767.

3. Menjalankan & Menghubungkan Client
Buka terminal di komputer mana saja yang berada dalam satu jaringan Wi-Fi yang sama, lalu jalankan:

java -jar target/WhutsAppBro_Launcher.jar
Skenario Simulasi Multi-Client (LAN Test):
Client 1 (Host/Server Laptop): Masukkan nama bebas, kosongkan kolom Server IP (fitur fallback otomatis akan langsung mengarah ke localhost), lalu buat ruang baru bernama Tubes.

Client 2 (Laptop Kedua/Remote): Masukkan nama berbeda, ketik Alamat IPv4 Server yang dicatat sebelumnya (misal: 192.168.1.5) ke kolom Server IP, klik masuk, lalu bergabunglah ke ruang Tubes.

4. Pengujian Fitur Transfer File Generik (Any File Transfer)
Pada salah satu Client, klik tombol ikon dokumen (📄) di baris bawah ruang obrolan.

Pilih berkas dokumen non-media apa saja (seperti file .pdf, .zip, atau .docx).

Setelah berhasil dikirim, sebuah kartu lampiran dokumen akan muncul secara real-time di kedua layar chat.

Klik kartu dokumen tersebut di sisi penerima. Sistem operasi akan otomatis memicu aplikasi bawaan komputer (seperti Adobe Reader atau Google Chrome) untuk membuka berkas tersebut secara instan dari folder cache lokal.