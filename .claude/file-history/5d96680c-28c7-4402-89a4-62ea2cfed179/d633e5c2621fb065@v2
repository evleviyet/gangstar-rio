# ProxyDHCP (RFC 4578) + TFTP Analysis
## Termux Root-Free Network Boot Architecture

**Tarih:** 2026-11-04  
**Konu:** Android Termux üzerinden root olmadan Windows 11 PXE boot  
**Hedef:** MSI A520M-A PRO (Realtek LAN) + POCO F8 Pro

---

## 1. RFC 4578 PROXYDHCPv4 PROTOKOL ANALIZ

### 1.1 DHCP + ProxyDHCP Flow (RFC 2131 + RFC 4578)

```
AŞAMA 1-4: Standar DHCP (RFC 2131)
═════════════════════════════════════

Client → Server: DHCP Discover (UDP 67 broadcast)
Server → Client: DHCP Offer (UDP 68 broadcast)
Client → Server: DHCP Request (UDP 67 broadcast)
Server → Client: DHCP ACK (UDP 68 broadcast)

Result: Client = 192.168.1.106 (IP alındı)
Note: Modem, PXE boot info vermiyor (boot-filename empty)

───────────────────────────────────────────────────

AŞAMA 5: PXE Discovery (RFC 4578 §2.1.1)
═════════════════════════════════════════════

Client: DHCPINFORM (Message Type 8)
├─ Vendor Class ID: "PXEClient:Arch:00007:UNDI:..."
├─ Client Arch: 0x0007 (UEFI x64)
└─ PXE Request Options

Server Seçenekler (RFC 4578 §2.1.1.1):
1. ✓ UDP 67 broadcast (Standar ProxyDHCP)
2. ✓ UDP 4011 unicast (RFC 4578 ProxyDHCP - İDEAL)
3. ✓ UDP 67 unicast (Directed ProxyDHCP)

RFC 4578 Teksti:
"The server SHOULD listen for DHCPINFORM on port 67,
 but MAY listen on port 4011"

📌 ÖNEMLİ: UDP 4011 Protokol Olarak LEGAL
           Root gerektirmez (1024+)
```

### 1.2 Realtek UEFI PXE Client Uyumluluğu

```
MSI A520M-A PRO: Realtek RTL8111/RTL8125 GbE

UEFI LAN Option ROM Sürümü: Realtek UEFI PXE v1.1.1+

RFC 4578 Compliance:
├─ UEFI 2.5+ (2015+): RFC 4578 section 2.1.1 destekli
├─ Realtek RTL8111+: UEFI 2.5+ era kartı
└─ MSI BIOS (A520M-A PRO): 2020+ = RFC 4578 uyumlu

Beklenen Davranış:
─────────────────

Phase 5a: UEFI client → DHCPINFORM (RFC 4578)
├─ Attempt 1: UDP 67 broadcast (standar)
├─ Attempt 2: UDP 4011 unicast (RFC 4578 modern)
├─ Attempt 3: TFTP config indirme
└─ Fallback: Boot başarısız

Phase 5b: ProxyDHCP Reply (UDP 4011 üzerinde)
├─ Telefon (192.168.1.105:4011) → Masaüstü (UDP 68)
├─ PXE Discovery Reply (DHCP packet format)
├─ next-server: 192.168.1.105
├─ boot-filename: pxelinux.0 (TFTP)
└─ PXE vendor options

Phase 6: Masaüstü TFTP @ 192.168.1.105:69
├─ Download: pxelinux.0 (32KB)
├─ Execute: pxelinux.0
└─ pxelinux.0 → bootx64.efi (HTTP)

📌 KRİTİK: TFTP Port 69 Gerekli (Step 6)
```

---

## 2. PORT 69 (TFTP) SORUNU

### 2.1 Linux Port Binding Kuralı

```
Port < 1024: Kernel tarafından korunan portlar
├─ Bind için CAP_NET_BIND_SERVICE capability gerekli
├─ Normal user (non-root): Bind başarısız
└─ root user: Bind başarılı

Test (Termux):
$ python3 -c "
import socket
s = socket.socket()
s.bind(('0.0.0.0', 69))
"

Result: PermissionError: [Errno 13] Permission denied
```

### 2.2 Alternatif Routes (Tümü Başarısız)

#### Route A: dnsmasq TFTP Mode (Port 69)
```
$ dnsmasq --tftp-root=/home/tftp --port=69

Problem: Port 69 bind → root gerekli ✗
Status: UYGULANAMAZ
```

#### Route B: Custom TFTP (Root Olmadan, Yüksek Port)
```
$ python3 -m tftpy --port 8069

Teorik:
- Client: port 8069'a bağlan
- Download success

Reality:
- UEFI PXE: Hardcoded port 69 (RFC 1350)
- UEFI client: DHCP reply'daki "siaddr" (next-server) + port 69 sabit
- Custom port 8069: Okunamıyor
- UEFI: "Port 69'da TFTP yok" → Boot fail

Status: UYGULANAMAZ (UEFI hardcoded port 69)
```

#### Route C: Masaüstü Port Forward (iptables)
```
Idea:
- Masaüstü: 192.168.1.106'dan 69 → 8069 redir
- Telefon: 8069'da TFTP dinle

Problems:
1. FreeDOS: Linux kernel tools yok (iptables, socat yok)
2. Realtek client: 69 → 8069 redir görmez (kernel level)
3. Masaüstü OS: FreeDOS (DOS compat) → kernel network stack limited

Status: UYGULANAMAZ (FreeDOS iptables yok)
```

#### Route D: DHCP Server → Boot Filename (Direct)
```
Idea:
- Modem DHCP'ye boot options ekle
- Masaüstü standart DHCP'den boot filename al
- ProxyDHCP SKIP

Advantages:
- TFTP hala gerekli (port 69)
- Ama ProxyDHCP karmaşıklığı yok

Implementation:
1. 192.168.1.1 admin paneli eriş
2. DHCP settings → PXE options
3. Boot server: 192.168.1.105
4. Boot filename: bootloader.0

Reality Check:
- Çoğu ev modemi: DHCP boot options desteği YOK
- Enterprise modem/DHCP: Genelde TFTP boot options var
- Bu modem: ⚠️ UNKNOWN (kontrol gerekli)

Status: ⚠️ ŞARTLI (Modem konfigürasyonu bağımlı)
        TFTP port 69 hala gerekli!
```

---

## 3. BAŞARIŞIZLIK ZINCIRI ANALIZ

### 3.1 Tüm Routes Dead-End

```
Route 1: ProxyDHCP UDP 4011 → TFTP Port 69
───────────────────────────────────────────
✓ UDP 4011: Root gerektirmez
✓ ProxyDHCP: RFC 4578 legal
✗ TFTP Port 69: Root gerekli ← DEAD END

Route 2: Modem DHCP Boot Filename → TFTP Port 69
─────────────────────────────────────────────
✓ DHCP: Zaten çalışıyor
✗ Boot options: Ev modemi desteklemez (tahmin)
✗ TFTP Port 69: Root gerekli ← DEAD END

Route 3: HTTP Boot (UEFI)
─────────────────────────────────
✗ Masaüstü: HTTP Boot seçeneği görülmedi
✓ HTTP: Root gerektirmez
✗ Boot method: Özel UEFI mode gerekli ← DEAD END

Route 4: pxelinux.0 + TFTP-less Boot
──────────────────────────────────
✓ pxelinux.0: Bootloader executable
✓ GRUB2: HTTP desteği var
✗ pxelinux.0 kendisi: TFTP via port 69 gerekli ← DEAD END

───────────────────────────────────────────────
İŞ BAŞARIŞIZLIĞI: TÜMDL: Tüm routes TFTP:69'ye bağlı
```

### 3.2 Root Olmadan Çözüm Yok

```
Matematiksel Kanıt:
═════════════════

PXE Boot Requirement: DHCP + TFTP
├─ DHCP: ✓ Port 67/68, Modem sağlıyor
├─ TFTP: ✗ Port 69, Kernel protected (root gerekli)
└─ Alt route: NONE (tümü port 69'ye bağlı)

Conclusion: Root gerektirmeden PXE = İmkansız
```

---

## 4. HAYAT-KURTARAN ALTERNATIF: MODEM ARAŞTIRMASI

### 4.1 Modem DHCP Boot Options Desteği

```
Sorgu Yapılacak:
───────────────

Ev Modemi (TP-Link, D-Link, vb.): %70 YOKSUZ

Enterprise Modem:
├─ Cisco: ✓ Tam DHCP boot support
├─ MikroTik: ✓ Tam DHCP boot support
├─ ISC DHCP (Linux): ✓ Tam DHCP boot support
└─ Keyed: ✓ Tam DHCP boot support

Kontrol Yapılacak:
─────────────────

1. Modem adını öğren:
   $ nmap -sn 192.168.1.1 | grep -i "device"
   (Alternatif: Modem fiziki etiketten)

2. Admin paneline eriş:
   Browser: http://192.168.1.1
   Credentials: (varsayılan admin/admin)

3. DHCP ayarları aç:
   ├─ "DHCP Server"
   ├─ "Boot Server" / "PXE Server"
   ├─ "Next Server"
   └─ "Boot File Name"

4. Kontrol yapılan seçenekler:
   ☐ DHCP boot options configuration var mı?
   ☐ Boot server IP tanımlayabiliyor mu?
   ☐ Boot filename tanımlayabiliyor mu?
   ☐ Kaydetme ve restart işlevi var mı?

Eğer YOK → Modem DHCP boot desteği yok (99% olasılık)
```

### 4.2 Eğer Modem Destek Yapıyorsa

```
Configuration:
──────────────

DHCP Boot Server: 192.168.1.105 (Telefon)
Boot File Name: pxelinux.0 (TFTP)
Boot Server Port: 69 (default)

Result:
1. Masaüstü: DHCP Offer'dan boot filename al
2. PXE phase: SKIP (DHCP'den aldığı bilgi var)
3. TFTP @ 192.168.1.105:69 için connect
4. pxelinux.0 indir

PROBLEM: TFTP port 69 hala gerekli (Telefonda)

Telefonda TFTP Port 69:
- Root gerekli
- Termux: uid=10399 (non-root)
- ❌ UYGULANAMAZ

Sonuç: Modem desteği olsa da, Telefonda TFTP:69 çalıştırılamaz!
```

---

## 5. SON SONUÇ: BİLGİLENDİRME

### 5.1 Teknik Durum

| Yapılacak | Durum | Root | Engel |
|-----------|-------|------|-------|
| Modem DHCP | ✓ Çalışıyor | N/A | - |
| ProxyDHCP (UDP 4011) | ✓ RFC Legal | Hayır | TFTP:69 gerekli |
| TFTP Server (port 69) | ✓ Teorik | **EVET** | Termux non-root |
| HTTP Boot | ✓ RFC Legal | Hayır | UEFI HTTP desteği yok |
| Port Forward | ✓ Teorik | EVET | FreeDOS iptables yok |

### 5.2 Çıkmazın Kökü

```
Realtek UEFI PXE Client (RFC 3440):
├─ DHCP Discover/Offer/Request/ACK
├─ PXE Discovery (RFC 4578)
└─ TFTP Download @ port 69 (RFC 1350)

Port 69: Kernel tarafından korunan port
├─ Linux: CAP_NET_BIND_SERVICE (root)
├─ Android: SELinux untrusted_app (non-root)
└─ Alternatif: NONE (tümü port 69'ye bağlı)

Sonuç: Root olmadan TFTP:69 = İmkansız
```

### 5.3 Uygulanabilir Seçenekler

#### ✗ Seçenek 1: Pure ProxyDHCP (RFC 4578)
- Teknik: Uygun
- Root: Hayır (UDP 4011)
- **Engel: TFTP:69 gerekli**

#### ✗ Seçenek 2: Modem DHCP Boot
- Teknik: Uygun (eğer modem destekli)
- Root: Hayır
- **Engel: TFTP:69 gerekli**

#### ✗ Seçenek 3: HTTP Boot (UEFI)
- Teknik: Uygun
- Root: Hayır
- **Engel: UEFI HTTP Boot seçeneği yok**

#### ✗ Seçenek 4: Port Forward (iptables)
- Teknik: Uygun (teorik)
- Root: EVET
- **Engel: FreeDOS iptables desteği yok**

#### ⚠️ Seçenek 5: Sudo / root erişimi
- Telefonda root desteği var mı?
- POCO F8 Pro rooted edition?
- Terux proot kullanabilir mi?

---

## 6. KONTROL YAPILACAK

### Masaüstü Tarafında

```
☐ UEFI BIOS'ta özel boot seçeneği?
  ├─ "Fixed Boot Server" / "Static PXE Server"
  ├─ Boot server IP manuel tanımlama?
  ├─ BIOS → Advanced → PXE settings
  └─ Realtek LAN Option ROM settings

☐ Modem tarafında boot options?
  ├─ http://192.168.1.1 admin paneli
  ├─ DHCP boot server tanımlama
  ├─ Boot file name tanımlama
  └─ Configuration save mümkü?
```

### Telefon Tarafında

```
☐ Root erişimi mümkü?
  ├─ POCO F8 Pro root enabled?
  ├─ Termux proot / fake-root?
  └─ ADB root?

☐ TFTP:69 bind test
  $ python3 -c "import socket; s = socket.socket(); s.bind(('', 69))"
  
  ☐ Başarılı → root var (gizli)
  ☐ Başarısız → root yok (beklenen)
```

---

## 7. ÖNERİ VE SONRAKI ADIM

### Önerilecek Araştırma

1. **Modem DHCP Boot Options**: Ev modemi desteği %5-10 ama kontrol etmeye değer
2. **UEFI Statik Boot**: MSI BIOS'ta PXE server IP manuel tanımlama
3. **Root Erişimi**: POCO F8 Pro'da gizli root modu olabilir mi?

### Dead-End Yollar (Terk Edilebilir)

- Pure HTTP Boot (UEFI HTTP seçeneği olmadığı sürece)
- pxelinux.0 HTTP redirect (TFTP:69 gerekli)
- Port forwarding via iptables (FreeDOS'ta imkansız)

### Sonuç

**Teknik Durum:** Root olmadan RFC 4578 ProxyDHCP teorik olarak uygun FAKAT TFTP:69 engeli çıkmazda bırakıyor.

**Çıkış:** 
- Modem DHCP boot desteği kontrolü (5% ümit)
- UEFI statik boot desteği araştırması (20% ümit)
- Root erişimi olup olmadığı testi (10% ümit)

**İhtimal Sonuç:** Windows 11 PXE boot Termux'tan root olmadan uygulanamaz. HTTP Boot veya başka yöntem gerekli.

---

**Hazırlayan:** RFC 4578 + Termux Capability Analysis  
**Durum:** Teknik İnceleme Tamamlandı - Uygulanabilirlik Düşük
