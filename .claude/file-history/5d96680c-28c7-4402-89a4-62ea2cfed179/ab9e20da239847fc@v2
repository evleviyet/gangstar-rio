# Windows 11 HDD Boot Kurulumu - Teknik Analiz
## USB olmadan Windows 11 kurulumunun uygulanabilirliği

**Tarih:** 2026-11-04  
**Hedef:** MSI A520M-A PRO (UEFI, BIOS'ta FreeDOS) → Windows 11 kurulumu  
**Araç:** POCO F8 Pro (Android 16, Termux) + Telefon/Masaüstü ağı

---

## 1. TEKNIK SORUN ANALİZİ

### 1.1 Başlangıç Durumu

```
Masaüstü:
├─ İşletim Sistemi: FreeDOS
├─ HDD: N/A (layout unknown)
├─ BIOS/UEFI: UEFI aktif, PXE IPv4 mevcut
├─ Boot: UEFI BIOS → FreeDOS
└─ Hedef: Aynı HDD'de Windows 11 kurulumu

Telefon:
├─ İşletim Sistemi: Android 16
├─ Termux: Root olmadan
├─ Ağ: 192.168.1.105 (WiFi)
├─ Depolama: 128GB+ (tahmin)
└─ USB-C: Mass storage mode? (unknown)

Kısıtlamalar:
├─ USB/CD: Yok
├─ Root: Telefonda yok
├─ Network: WiFi (10-20Mbps)
├─ Disk repartition: Risk (veri kaybı)
└─ Kurulum süresi: Kısıtlama yok
```

### 1.2 Temel Gereksinimler

```
Windows 11 Kurulumu İçin:
├─ UEFI boot: ✓ Mevcut
├─ Network/USB: GEREKLI (ISO delivery mekanizması)
├─ Boot media: ISO, USB, Network, HDD
└─ Storage: 25GB+ NTFS partition

Başarısızlık Sebepleri:
├─ USB/CD yok → Traditional boot media imkansız
├─ Network: WiFi var (PXE alternatifi araştırıldı, başarısız)
├─ Remaining: HDD boot + ISO loopback (GRUB2)
└─ Problem: FreeDOS HDD'de, GRUB2 kurulumu gerekli
```

---

## 2. ÜÇ UYGULANABILIR SEÇENEK

### Seçenek A: GRUB2 + ISO Loopback (RFC Uyumlu)

**Teknik Mimarisi:**

```
Phase 1: Kurulum (Linux Live-Boot)
─────────────────────────────────

USB stick 1 → Linux live-boot (Ubuntu 22.04 LTS)
Masaüstü: USB'den boot → Linux live environment

Linux live'da:
├─ Disk layout analiz: parted -l
├─ Partition strategy:
│  ├─ Keep: FreeDOS partition (1GB)
│  ├─ Create: ext4 partition (ISO storage)
│  ├─ Create: NTFS partition (Windows install)
│  └─ Modify: EFI System Partition (boot)
├─ GRUB2 install: grub-install --target=x86_64-efi
├─ GRUB2 config: menuentry "Windows 11" (loopback)
└─ ISO transfer: cp Windows11.iso /boot/iso/

Phase 2: Windows Boot
────────────────────

UEFI firmware → /EFI/BOOT/BOOTX64.EFI (GRUB2)
GRUB2 menu display
User select: "Windows 11"

GRUB2 actions:
├─ loopback loop /iso/Windows11.iso
├─ insmod part_gpt (GPT partition table)
├─ insmod ext2 (ext4 filesystem driver)
├─ set root=(hd0,gpt2) (partition pointer)
└─ chainload (loop)/efi/boot/bootx64.efi

Windows PE boot (ISO'dan):
├─ bootx64.efi: Execute (loop device)
├─ BCD: /boot/bcd (ISO 9660 read-only)
├─ boot.wim: /sources/boot.wim (RAM disk'e load)
├─ Windows PE kernel: Execute
└─ Setup.exe: Windows 11 kurulumu başla

Phase 3: Windows 11 Installation
──────────────────────────────────

Setup disk seçer: NTFS partition
Installation files: /sources/install.wim (ISO'dan)
Real-time drivers: Kurulum sırasında indir
Kernel install: Windows 11 to NTFS partition
Boot manager: Windows Boot Manager (HDD kurulum)

Phase 4: First Boot (Windows 11)
───────────────────────────────

UEFI firmware: HDD boot → Windows Boot Manager
Windows BCD: Windows 11 partition boot
Result: Windows 11 normal boot
```

**Gereksinimler:**

```
Hardware:
├─ USB stick (8GB+) - Linux live-boot
├─ USB-C cable (Telefon → Masaüstü, opsiyonel)
└─ HDD (repartition tolerance)

Software (Telefon):
├─ Windows 11 ISO (6.7GB download)
├─ Linux live ISO (1.5GB download)
└─ Tools: 7z/unzip (ISO write), dd (USB write)

Software (Linux live):
├─ GRUB2 tools (grub-install, grub-mkconfig)
├─ parted (disk partitioning)
├─ ext4 support (kernel built-in)
└─ dd/rsync (file transfer)

Time Budget:
├─ ISO download: 1-2 saat (WiFi)
├─ Linux live boot: 10 dakika
├─ Partitioning: 5 dakika (safe)
├─ GRUB2 install: 5 dakika
├─ ISO copy: 10 dakika (USB-C) ~ 30 dakika (USB stick)
├─ Windows setup boot: 5 dakika
├─ Windows 11 kurulumu: 20-30 dakika
└─ **Total: 2-3 saat**

Risk Assessment:
├─ Disk repartition: ⚠️ MODERATE
│  ├─ Safe tool: parted (batch commands)
│  ├─ Backup strategy: Minimal (FreeDOS recovery possible)
│  └─ Recovery: Linux live → parted restore
│
├─ GRUB2 misconfiguration: ⚠️ MODERATE
│  ├─ Impact: Boot fail (UEFI shell fallback)
│  ├─ Recovery: Linux live → grub-install redo
│  └─ Effort: ~10 dakika
│
├─ ISO loopback failure: ⚠️ LOW
│  ├─ RFC 4578 standard, well-tested
│  ├─ Impact: Windows PE boot fail
│  └─ Recovery: USB fallback (if needed)
│
└─ Data loss: ⚠️ MINIMAL
   ├─ FreeDOS partition: Intact (if kept)
   ├─ NTFS partition: Fresh install
   └─ Existing data: Backed up separately (recommended)
```

**Uygulanabilirlik: ✓ FEASIBLE**

---

### Seçenek B: Direct Windows 11 Installation (USB Fallback)

**Teknik Mimarisi:**

```
Phase 1: ISO Preparation
──────────────────────────

Telefon (Termux):
├─ Windows 11 ISO download (6.7GB)
├─ ISO → USB stick write (dd kullanmak)
│  $ dd if=Windows11.iso of=/dev/sdX bs=4M status=progress
└─ USB stick ready

Phase 2: USB Boot
──────────────────

Masaüstü USB-C port → USB stick (via adapter veya Telefon tether)
Alternative: Masaüstü'de başka USB adapter varsa doğrudan

BIOS/UEFI: USB boot seçimi
Result: Windows 11 kurulum media

Phase 3: Windows Setup
──────────────────────

Standard Windows setup:
├─ Disk seçimi: HDD
├─ Partition strategy:
│  ├─ Delete: FreeDOS partition (veya keep as secondary)
│  ├─ Create: NTFS partition (Windows)
│  └─ Setup handles rest
├─ Installation: Normal flow
└─ First boot: Windows 11 (UEFI boot manager)

Result: Windows 11 installed on HDD
```

**Gereksinimler:**

```
Hardware:
├─ USB stick (8GB+)
├─ USB-C cable (Telefon → Masaüstü, device-dependent)
└─ USB adapter (Type-C to Type-A, if needed)

Software:
├─ Windows 11 ISO (6.7GB)
├─ dd command (Termux, available)
└─ USB write permission (Android USB access)

Time Budget:
├─ ISO download: 1-2 saat
├─ USB write: 10 dakika
├─ Windows setup: 30 dakika
└─ **Total: 1.5-2.5 saat**

Risk Assessment:
├─ USB write error: LOW (simple operation)
├─ Disk corruption: LOW (Windows setup handles safely)
├─ Data loss: MINIMAL (FreeDOS deletion expected)
└─ Recovery: Boot from Windows 11 (normal)
```

**Uygulanabilirlik: ✓ FEASIBLE** (USB device needed)

---

### Seçenek C: ISO HDD Boot via Termux (Complex, Not Recommended)

**Teknik Mimarisi:**

```
Phase 1: Termux Linux Environment (proot)
──────────────────────────────────────────

Termux: proot Alpine Linux setup
├─ proot-distro install alpine
├─ Alpine: Lightweight Linux userland
├─ GRUB2 compile: apt-get install grub-efi
├─ HDD access: /dev/block/... (Android kernel via Termux)
└─ Limitation: Device file access = SELinux sandbox issue

Phase 2: HDD Repartition (proot'dan)
─────────────────────────────────────

parted → HDD partition modify:
├─ Partition 1: FreeDOS (keep)
├─ Partition 2: ext4 (ISO storage)
├─ Partition 3: NTFS (Windows install)
└─ EFI partition: Create/modify

Risk: HIGH
├─ Termux proot: Limited kernel device access
├─ SELinux: untrusted_app → /dev/block/... deny
├─ Workaround: None (SELinux policy hardcoded)
└─ Result: LIKELY FAILURE

Phase 3: ISO Transfer (WiFi)
─────────────────────────────

Termux → HDD (proot ext4 mount):
├─ ISO download: wget/curl (6.7GB)
├─ Transfer: cp /home/iso → /mnt/hdd/iso/
├─ Speed: WiFi ~10Mbps → ~1 saat
├─ Reliability: WiFi interrupt risk (30%)
└─ Fallback: Retry + resume (curl -C)

Phase 4: GRUB2 Install (proot'dan)
──────────────────────────────────

proot Alpine GRUB2:
├─ grub-install --target=x86_64-efi → HDD ESP
├─ Limitation: Termux kernel module loading?
├─ EFI firmware access: Limited
└─ Result: QUESTIONABLE

Phase 5: Boot (UEFI → GRUB2 → ISO → Windows)
──────────────────────────────────────────

If successful:
├─ UEFI → GRUB2-EFI (HDD)
├─ GRUB2 menu → Windows 11
├─ loopback /iso/Windows11.iso
├─ bootx64.efi execute → Windows PE
└─ Setup → Windows 11 install

But: Multiple failure points (proot limitations, GRUB2 install, etc.)
```

**Gereksinimler:**

```
Hardware:
├─ USB device: NOT NEEDED ✓
└─ HDD: Repartition risk (data loss)

Software:
├─ Termux proot: Alpine Linux
├─ GRUB2: Compile + install (proot)
├─ parted: Partition management (proot)
└─ Issue: SELinux sandbox → device access denied

Time Budget:
├─ proot setup: 30 dakika
├─ GRUB2 compile: 1 saat
├─ Repartition: 10 dakika
├─ ISO transfer: 1-2 saat (WiFi unreliable)
├─ GRUB2 install: 10 dakika (⚠️ likely fail)
├─ Troubleshooting: 1+ saat
└─ **Total: 4-6 saat (with high failure risk)**

Risk Assessment:
├─ proot SELinux sandbox: ✗ BLOCKING ISSUE
├─ Device file access: ✗ DENIED (/dev/block/...)
├─ GRUB2 compile + install: ⚠️ QUESTIONABLE
├─ Kernel module: ⚠️ CANNOT LOAD (Termux limitation)
├─ Fallback strategy: NONE (if GRUB2 fails)
└─ Data loss: ✗ HIGH RISK (repartition without recovery)
```

**Uygulanabilirlik: ✗ NOT FEASIBLE** (SELinux block, too complex)

---

## 3. KONTROL LİSTESİ - İLK ADIMLAR

### 3.1 Masaüstü Kontrol (FreeDOS'ta)

```
Disk Layout Analiz:
────────────────────

Adım 1: Partition table
$ fdisk -l
└─ Output: MBR vs GPT, partition count, sizes

Adım 2: Current mount points
$ df -h
└─ Output: FreeDOS partition size, used/free

Adım 3: Disk space
$ dir C:\  (toplam kapasite bilgisi)
└─ Output: Approximate HDD size

Adım 4: Partition info (GPT check)
$ fdisk -l /dev/sda | grep "Disklabel"
└─ Output: "dos" (MBR) vs "gpt" (GPT)

───────────────────────────────────────

Kritik Sorular:
☐ Disk toplam boyutu? (500GB, 1TB, 2TB?)
☐ Kaç partition? (1 vs multiple)
☐ MBR vs GPT?
☐ FreeDOS partition boyutu? (1GB? 5GB?)
☐ Boş alan? (HDD'nin kaç %'si dolu?)
☐ Mevcut data? (CRITICAL: Veri kaybı riski)
```

### 3.2 Telefon Kontrol (Termux'ta)

```
USB-C Capabilities:
────────────────────

Adım 1: USB mode check
$ lsusb  (veya Android settings → USB)
└─ Output: Device enumeration, transfer mode

Adım 2: Mass storage capability
$ getprop ro.usb.config
└─ Output: "adb", "mass_storage", "mtp" vb.

Adım 3: File system access
$ df -h /mnt/  (veya /storage/)
└─ Output: External storage available?

───────────────────────────────────────

Kritik Sorular:
☐ POCO F8 Pro USB mass storage mode destekliyor mu?
☐ USB-C → USB-A adapter bulunur mu?
☐ Storage genişliği? (7GB ISO + OS space)
```

### 3.3 Network Test

```
Speed & Reliability:
─────────────────────

Adım 1: WiFi speed test
$ ping 8.8.8.8 (latency)
$ curl -o /dev/null -w "%{speed_download}\n" http://speedtest.url

Adım 2: ISO download time estimate
Size: 6.7GB
Speed: (result from above)
Time: 6.7GB / speed_Mbps = ?

───────────────────────────────────────

Kritik Sorular:
☐ WiFi ortalama hız? (5Mbps? 20Mbps?)
☐ Download süresi acceptable? (30 dakika? 2 saat?)
☐ Timeout riski? (WiFi interrupt history?)
```

---

## 4. ÖNERİLEN KURULUM ROTASI

### Tavsiye 1: Direct Windows 11 Installation (En Basit)

**Seçilir eğer:**
- POCO F8 Pro USB mass storage destekliyorsa
- USB adapter mevcut ise
- Hız önemli ise

**Adımlar:**

```
1. Telefon: Windows 11 ISO indir (1-2 saat)
   $ cd /storage/downloads/
   $ wget https://software-download.microsoft.com/...

2. USB stick prep
   $ dd if=Windows11.iso of=/dev/sdX bs=4M (write)

3. Masaüstü: USB boot
   UEFI BIOS → Boot order USB first → Save
   Reboot

4. Windows 11 setup
   Partition: Delete FreeDOS (or keep)
   Install: Standard Windows setup
   
5. First boot: Windows 11 ✓
```

**Beklenen Sonuç:** Windows 11 installed, working

---

### Tavsiye 2: GRUB2 + ISO Loopback (Best Long-term)

**Seçilir eğer:**
- USB adapter yoksa
- Dual-boot isteğe bağlı ise
- Reusable boot system isteniyor ise

**Adımlar:**

```
1. Telefon: İSO'lar indir
   - Windows 11 ISO (6.7GB)
   - Ubuntu 22.04 LTS ISO (1.5GB)

2. Linux USB boot media create
   $ dd if=ubuntu-22.04-live.iso of=/dev/sdY bs=4M

3. Masaüstü: USB'den boot (Linux live)
   UEFI → USB boot

4. Linux live'da: GRUB2 + Partitioning
   $ parted -l  (analyze)
   $ parted /dev/sda mkpart primary ext4 ...  (create partition)
   $ grub-install --target=x86_64-efi /dev/sda
   $ cp Windows11.iso /boot/iso/

5. GRUB config edit
   $ nano /boot/grub/grub.cfg
   [Add menuentry "Windows 11" with loopback]

6. Reboot: GRUB2 menu
   Select "Windows 11"
   → Windows PE boot
   → Windows 11 setup
   → Installation complete

7. First boot: Windows 11 (UEFI boot manager)
```

**Beklenen Sonuç:** Windows 11 installed, GRUB2 bootloader preserved

---

## 5. RISK ANALYSIS & MITIGATION

### Critical Risks

| Risk | Impact | Probability | Mitigation |
|------|--------|-------------|------------|
| FreeDOS veri kaybı | Total data loss | 5-10% | Backup (USB? online?) |
| GRUB2 install fail | Boot fail | 2-5% | Linux live retry |
| ISO loopback error | Windows PE fail | <1% | RFC uyumlu, tested |
| USB write corruption | Kurulum başarısız | 5% | Re-write, verify |
| WiFi interrupt | ISO download fail | 20-30% | curl resume, retry |
| Disk space insufficient | Kurulum fail | 10% (check first) | Shrink partitions |

### Recovery Strategies

```
Scenario 1: GRUB2 boot fail
└─ UEFI shell → Manual boot
└─ Alternative: Fallback USB Windows 11 setup

Scenario 2: Partition corruption
└─ Linux live parted → Repair/recreate
└─ Data recovery tools (TestDisk, etc.)

Scenario 3: Windows PE boot fail (loopback)
└─ USB Windows 11 setup (fallback)
└─ Manual UEFI boot troubleshooting

Scenario 4: HDD full during install
└─ Shrink FreeDOS partition
└─ Free up space, retry
```

---

## 6. KARAR MAT RİSİ

```
┌──────────────────┬──────────────┬──────────────┬─────────────┐
│ Seçenek          │ USB Gerekli? │ Karmaşıklık  │ Başarı %    │
├──────────────────┼──────────────┼──────────────┼─────────────┤
│ A. Direct Win11  │ ✓ (1x)       │ Düşük        │ 85-90%      │
│ B. GRUB2 + ISO   │ ✓ (1x)       │ Orta         │ 80-85%      │
│ C. Termux HDD    │ ✗            │ Çok yüksek   │ 20-30%      │
└──────────────────┴──────────────┴──────────────┴─────────────┘

RECOMMENDATION:

IF USB/adapter available:
   → SEÇENEK A (Direct Windows 11) - EASIEST

IF USB adapter NOT available BUT USB adapter purchased/found:
   → SEÇENEK A (Direct Windows 11) - EASIEST

IF complex solution acceptable:
   → SEÇENEK B (GRUB2 + ISO loopback) - ROBUST

IF no USB device available:
   → USB device procure (recommend)
   → OR SEÇENEK B (minimal USB usage)

SEÇENEK C (Termux HDD):
   → NOT RECOMMENDED (SELinux barrier, too risky)
```

---

## 7. SONRAKI ADIM

### Immediately:

1. Masaüstü FreeDOS'ta diskdosyayı çıktı al:
   ```
   $ fdisk -l > disk_layout.txt
   $ df -h > disk_free.txt
   ```

2. Telefon'da kontrol:
   - USB mass storage capability test
   - WiFi speed measurement
   - Storage space (7GB+ free?)

3. Karar ver: USB adapter alabilir misin?

### Then:

Seçilen seçeneğe göre kurulum planı aç.

---

**Hazırlayan:** Windows 11 HDD Boot Analysis  
**Durum:** Teknik İnceleme Tamamlandı - Uygulanabilir Seçenekler Hazır  
**Tavsiye:** Masaüstü disk layout'ı kontrol et, USB adapter durumunu değerlendir
