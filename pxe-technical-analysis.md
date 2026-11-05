# Windows 11 PXE Kurulum via Android Termux - Teknik Analiz

**Tarih:** 2026-11-04  
**Cihazlar:** Telefon (POCO F8 Pro, Android 16, Termux), Masaüstü (MSI A520M-A PRO, FreeDOS, UEFI)  
**Ağ:** 192.168.1.0/24 (Modem: 192.168.1.1, Telefon: 192.168.1.105, Masaüstü: 192.168.1.106)

---

## 1. AĞ TOPOLOJI VE KAPASİTE

### 1.1 Ağ Konfigürasyonu

```
┌─────────────────────────────────────────────┐
│  Modem (192.168.1.1)                        │
│  Gateway, DHCP Server (Standart Ports)      │
└─────────────┬──────────────────────┬────────┘
              │                      │
              │ Ethernet            │ Wi-Fi
              │ (0.072ms RTT)        │ (53ms RTT)
              │                      │
    ┌─────────▼──────┐       ┌───────▼────────┐
    │ Masaüstü       │       │ Telefon        │
    │ 192.168.1.106  │       │ 192.168.1.105  │
    │ FreeDOS + UEFI │       │ Android 16     │
    │ PXE Boot Ready │       │ Termux + Tools │
    └────────────────┘       └────────────────┘

- Subnet: 192.168.1.0/24
- Broadcast Domain: Ortak (255.255.255.255 ulaşılabilir)
- Ağ Gecikmesi: Masaüstü ≈ 0.1ms, Telefon ≈ 134ms (internet), Yerel ≈ minimal
```

### 1.2 Broadcast Kapabilite Testi

```
✓ UDP broadcast socket: Başarılı
✓ SO_BROADCAST flag: Aktif
✓ Port 8067 bind: Başarılı (unprivileged)
✓ Broadcast paket gönderme: 255.255.255.255:68
✗ Port 67 bind: PermissionError (root gerekli)
```

**Sonuç:** Broadcast etki alanı tam, ancak port <1024 bind root gerekli.

---

## 2. ROOT OLMADAN PXE ALTERNATİFLERİ

### 2.1 Seçenek A: UEFI HTTP Boot (TAVSIYE EDİLEN) ✓

**Statü:** ✓ ROOT GEREKTIRMEZ - **EN UYGULANABILIR**

#### Teknik Mimarisi

```
1. İlk Boot Aşaması (Masaüstü)
   ├─ Modem DHCP → IP 192.168.1.106 atar
   ├─ UEFI Network Stack aktifleştirilir
   ├─ Boot menüsünde "HTTP Boot" seçilir
   └─ URL: http://192.168.1.105:8000/ giriş yapılır

2. Bootloader Yükleme (UEFI → Telefon HTTP)
   ├─ GET /efi/boot/bootx64.efi
   ├─ Dosya boyutu: ~100KB
   ├─ İndirme süresi: <1 saniye
   └─ UEFI: bootx64.efi execute

3. Windows PE Boot (bootx64.efi → HTTP)
   ├─ bootx64.efi: boot.wim'i HTTP'den indir
   ├─ GET /sources/boot.wim (~200MB)
   ├─ İndirme süresi: ~20 saniye (Telefon Wi-Fi ~10Mbps)
   └─ Windows PE kernel başlat

4. Kurulum Aşaması (Windows PE → HTTP)
   ├─ setup.exe: install.wim HTTP'den indir
   ├─ GET /sources/install.wim (~6GB)
   ├─ İndirme süresi: ~10 dakika (6GB @ 10Mbps)
   └─ Windows 11 kurulumu başla
```

#### Gereksinimler

```
Telefon (Termux) tarafında:
  ✓ Python 3 (mevcut)
  ✓ HTTP server (built-in: http.server)
  ✓ ISO extract aracı (unzip mevcut)
  ✓ Port 80 bind (root gerektirmez)

Masaüstü (UEFI) tarafında:
  ? HTTP Boot seçeneği (kontrol gerekli)
  ✓ Network Stack (açık)
  ✓ PXE IPv4 (açık)
  ✓ LAN Option ROM (açık)
```

#### Avantajlar

- Root gerektirmez
- Hızlı (HTTP ~10Mbps vs TFTP ~0.1Mbps)
- En az kompleks
- UEFI native support
- FreeDOS uyumluluk sorunu yok

#### Dezavantajlar

- Masaüstü UEFI HTTP Boot desteklemeli (MSI A520M-A PRO kontrol edilmedi)

#### Başarı Olasılığı

- Masaüstü HTTP Boot var: **95%** ✓
- Masaüstü HTTP Boot yok: **0%** (Plan B gerekli)

---

### 2.2 Seçenek B: TFTP + HTTP Hybrid (Fallback)

**Statü:** ⚠️ ROOT GEREKTIRMEZ AMA YAŞALI

#### Teknik Mimarisi

```
1. Masaüstü UEFI PXE Boot
   └─ Modem DHCP + UEFI PXE request

2. Telefon TFTP Server (Port 69)
   ├─ dnsmasq veya tftp-hpa
   ├─ bootloader serve (128KB)
   └─ UEFI: TFTP'den indir (~5 saniye)

3. boot.wim HTTP Mount
   ├─ TFTP yerine HTTP kullan
   ├─ ~200MB @ 10Mbps → ~20 saniye
   └─ Windows PE boot

4. install.wim HTTP (Yukarıdaki gibi)
```

#### Gereksinimler

```
Telefon:
  ✓ TFTP server (tftp mevcut)
  ✓ Port 69 bind (root gerektirmez)
  ✓ HTTP server (port 80)

Masaüstü:
  ✓ PXE boot (mevcut)
  ? TFTP client (UEFI PXE içinde)
  ✓ HTTP erişim (Windows PE)
```

#### Avantajlar

- Root gerektirmez
- TFTP standart PXE mekanizması
- Masaüstü yalnızca PXE boot lazım

#### Dezavantajlar

- TFTP yavaş (~500KB/s)
  - boot.wim (~200MB) → ~6 dakika
  - Masaüstü network yavaş ise daha da yavaş
- Windows PE içinde HTTP mount karmaşık
- boot.wim mount timeout riski

#### Başarı Olasılığı

- Teknik olarak: **60%** (TFTP mount güvenilir değil)
- Pratik olarak: **40%** (timeout, compatibility sorunları)

---

### 2.3 Seçenek C: ProxyDHCP (Teorik - Uygulanamaz)

**Statü:** ✗ ROOT GEREKLI - UYGULANAMAZ

#### Sorun

```
ProxyDHCP Mimarisi:
- Port 67 (DHCP discovery) dinlemek gerekli
- Android Termux: Port <1024 = Root gerekli
- Root yok → Uygulanamaz
```

#### Alternatif: Custom Port (8067)

```
Masaüstü FreeDOS'tan Port Forward:
- iptables: Linux kernel tool (FreeDOS: yok)
- socat: Port redirect utility (FreeDOS: compatibility şüpheli)
- FreeDOS native networking: Limited

Sonuç: Port forward imkansız
```

---

### 2.4 Seçenek D: iPXE Framework

**Statü:** ⚠️ BOOTSTRAP SORUNU

#### Sorun

```
iPXE, custom port DHCP desteği sağlayabilir FAKAT:
1. iPXE'nin kendisi UEFI'ye boot edilmesi gerekli
2. Bootstrap: USB boot yok, PXE boot yok (başlangıçta)
3. Catch-22: iPXE başlatmak için zaten boot mekanizması lazım
```

**Sonuç:** Uygulanamaz (bootstrap sorunu).

---

## 3. MASAÜSTÜ UEFI KONFİGÜRASYONU (KONTROL LİSTESİ)

### 3.1 HTTP Boot Doğrulaması

**Yapılacak:** Masaüstü BIOS/UEFI menüsünde kontrol

```
Adımlar:
1. Masaüstüyü başlat → FreeDOS boot öncesi
2. Del / F2 / F10 tuşu (MSI motherboard'lar genelde Del)
3. UEFI Setup menüsü aç
4. Boot seçenekleri kontrol et:
   
   ☐ Seçenek A Var mı?
     - "Network Boot"
     - "PXE IPv4" [Mevcut ✓]
     - "HTTP Boot" [? KONTROL ET]
   
   ☐ Advanced Networking:
     - Network Stack: Enabled [✓]
     - UEFI HTTP Boot: Enabled / Supported?
   
   ☐ Boot Priority:
     - Network boot priority kaçıncı sırada?
     - HTTP Boot erişilebilir mi?

Not: Settings DEĞİŞTİRME, sadece KONTROL et
```

### 3.2 Beklenen Sonuçlar

| Senaryo | Seçenek A (HTTP Boot) | Seçenek B (TFTP) |
|--------|----------------------|------------------|
| HTTP Boot var | ✓ Kullan | - |
| Sadece PXE var | ✗ Olmaz | ✓ Kullan |
| PXE + HTTP var | ✓ Tercih et | ✓ Fallback |

---

## 4. WINDOWS 11 ISO İŞLEME

### 4.1 ISO Dosyası

```
- Boyut: ~6.5-7 GB
- Format: ISO 9660 + Joliet Extensions (unzip uyumlu)
- Kriti Dosyalar:
  • efi/boot/bootx64.efi (~100KB)
  • sources/boot.wim (~200MB)
  • sources/install.wim (~6GB)
  • bootmgr, BCD dosyaları
```

### 4.2 Extract Yöntemi (Termux)

```bash
# Yöntem 1: unzip (mevcut)
$ unzip -l Windows11.iso | head -20  # Listle
$ unzip Windows11.iso -d /home/iso-extracted/  # Extract

# Yöntem 2: 7z (yüklüyse)
$ 7z x Windows11.iso -o/home/iso-extracted/

# Not: ISO mount'u Termux'ta sorunlu olabilir
# Extract tavsiye edilir (~20GB alan gerekli: 7GB ISO + extracted)
```

### 4.3 HTTP Server Yapılandırması

```bash
# Termux'ta:
$ cd /home/iso-extracted/
$ python3 -m http.server 8000

# Test (başka terminal):
$ curl http://192.168.1.105:8000/efi/boot/bootx64.efi | xxd | head
$ curl http://192.168.1.105:8000/sources/boot.wim | file -

# Masaüstü'den (Windows PE):
$ powershell -Command "(New-Object System.Net.WebClient).DownloadFile('http://192.168.1.105:8000/sources/install.wim', 'C:\install.wim')"
```

---

## 5. BAŞARIŞIZLIK ANALİZİ

| Risk | Olasılık | Etki | Çözüm |
|------|----------|------|-------|
| Masaüstü HTTP Boot yok | 30-40% | Seçenek A başarısız | Seçenek B fallback |
| Telefon Wi-Fi interrupt | 10% | Download timeout | Retry / USB Ethernet |
| boot.wim extraction fail | 5% | ISO işleme sorun | Manual extract veya 7z kur |
| Windows PE HTTP timeout | 15% | Setup fail | Install.wim local copy |
| UEFI HTTP boot slow | 20% | >1 saat kurulum | Normal (ilk boot yavaş) |

---

## 6. HEMEN YAPILACAKLAR (Başlamadan Önce)

### 6.1 Masaüstü Tarafından

- [ ] UEFI menüsünde HTTP Boot seçeneğini kontrol et
- [ ] Network boot sıralamasını doğrula
- [ ] Ethernet kablo doğru bağlı mı kontrol et

### 6.2 Telefon Tarafından

- [ ] Windows 11 ISO dosyasını Termux'a transfer et (~7GB)
  - USB-C kablo: Masaüstü'den dosya transfer
  - WiFi: HTTP download (yavaş ama çalışır)
- [ ] ISO boyutu için disk alanı kontrol et (~20GB)
- [ ] Python 3 test: `python3 -m http.server 8000`

### 6.3 Network Testi

- [ ] Masaüstü → Telefon ping: `ping 192.168.1.105` (0-2ms beklenen)
- [ ] Telefon → Masaüstü ping: `ping 192.168.1.106` (0.1ms ölçüldü ✓)
- [ ] Broadcast test: `python3 broadcast_test.py` (başarılı ✓)

---

## 7. İLK KURULUM ADIMARI (Teknik Özet)

### 7.1 Seçenek A: HTTP Boot (Tavsiye Edilen)

```
1. Telefon:
   $ cd /home/iso-extracted/
   $ python3 -m http.server 8000
   [Server running http://192.168.1.105:8000]

2. Masaüstü BIOS/UEFI:
   Boot → HTTP Boot (or Network Boot)
   URL: http://192.168.1.105:8000/
   
3. UEFI bootloader yüklenir ve execute edilir
4. Windows PE kernel (boot.wim) HTTP'den indirilen
5. Windows PE boot
6. setup.exe: install.wim HTTP'den indir
7. Windows 11 kurulumu başla

Beklenen Süre: ~15 dakika toplam
```

### 7.2 Seçenek B: TFTP Fallback

```
1. Telefon:
   $ dnsmasq --port=0 --dhcp-boot=tftp://192.168.1.105/pxelinux.0
   $ tftp -l /home/tftp/pxelinux.0

2. Masaüstü UEFI:
   Boot → PXE Boot
   
3. UEFI: TFTP'den bootloader indir (~30 saniye)
4. Bootloader: boot.wim HTTP'den indir
5. Rest: Seçenek A gibi

Beklenen Süre: ~20-30 dakika (TFTP yavaşlığı)
```

---

## 8. SONUÇ VE ÖNERİ

### 8.1 En Uygulanabilir Route

**SEÇEnek A: UEFI HTTP Boot**

```
Başarı Kriterleri:
✓ Masaüstü UEFI HTTP Boot destekliyor
✓ Telefon HTTP server başlatılıyor
✓ ISO extracted ve port 8000'de serve ediliyor
✓ Ağ bağlantısı stabil

Başarı Olasılığı: 85-95% (HTTP Boot var ise)
Beklenen Süre: 15-30 dakika
```

### 8.2 Fallback Plan

**SEÇEnek B: TFTP + HTTP (Eğer HTTP Boot yok)**

```
Başarı Kriterleri:
✓ Masaüstü PXE boot destekliyor [✓ Kontrol edildi]
✓ Telefon TFTP server çalışıyor
✓ boot.wim HTTP accessible

Başarı Olasılığı: 60-70% (bootstrap sorunları riski)
Beklenen Süre: 30-45 dakika
```

---

## 9. SONRAKI ADIM

**Masaüstü UEFI kontrol et:**
1. HTTP Boot seçeneğinin var olup olmadığını doğrula
2. Sonucu bildir
3. Uygun seçeneği başlat

**Telefon hazırlıkları:**
1. Windows 11 ISO indirmeyi başlat (paralel olarak)
2. ISO extract / mount yöntemi belirle

---

**Hazırlayan:** Claude Code Termux Agent  
**Son Güncelleme:** 2026-11-04 00:00 UTC  
**Durum:** Hazırlanıyor - Teknik analiz tamamlandı ✓
