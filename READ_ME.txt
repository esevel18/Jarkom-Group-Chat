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