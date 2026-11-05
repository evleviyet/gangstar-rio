# PXE Boot Teşhis Planı
## Masaüstü PXE başlamadığında sorunu bulmak

**Tarih:** 2026-11-04  
**Durum:** Masaüstü PXE IPv4 seçildi ama kurulum başlamadı  
**Hedef:** DHCP/PXE/TFTP akışındaki sorunu tanımlamak

---

## TEŞHIS SONUÇLARI (Telefon/Termux)

### ✓ Doğrulanmış

```
1. Telefon IP: 192.168.1.105 (wlan0) ✓
2. Masaüstü reachability: 192.168.1.106 (ping OK) ✓
3. UDP 4011 bind: Root gerektirmez ✓
4. HTTP 8000 port: Bind başarılı ✓
5. Modem gateway: 192.168.1.1 (DHCP aktif) ✓
```

---

## TEŞHIS STRATEJİSİ

### SEVRE 1: MASAÜSTÜ (FreeDOS)

**Yapılacak testler:**

1. **IP Doğrulama**
   ```
   Komut: ipconfig.com
   Beklenen: IPv4 192.168.1.106, Gateway 192.168.1.1
   Sorular:
   - IP adresi doğru mu?
   - Subnet maskı 255.255.255.0 mu?
   ```

2. **Ağ Bağlantısı**
   ```
   Komut: ping 192.168.1.105
   Beklenen: 4 reply, 0% loss
   Sorular:
   - Telefon erişilebilir mi?
   - Timeout mu?
   ```

3. **HTTP Sunucuya Erişim**
   ```
   Komut: wget http://192.168.1.105:8000/
   Alternatif: curl http://192.168.1.105:8000/
   Beklenen: HTTP 200 OK veya 404 (dosya yok)
   Sorular:
   - Connection kuruldu mu?
   - HTTP yanıt alındı mı?
   ```

4. **BIOS Boot Log**
   ```
   BIOS gir (Del) → Boot Event Log
   Beklenen:
   - PXE: DHCP Discover sent ✓
   - PXE: DHCP Offer received ✓
   - PXE: TFTP RRQ sent
   
   Sorular:
   - DHCP başarılı mı?
   - TFTP başladı mı?
   - Hangi satırda timeout?
   ```

---

### SEVRE 2: TELEFON (Termux) - PACKET MONITORING

**Setup (3 terminal):**

```
Terminal 1: Packet Capture (DHCP/PXE/TFTP)
──────────────────────────────────────────

$ apt install tcpdump  # gerekli ise
$ tcpdump -i wlan0 -n 'udp port 67 or port 68 or port 69' -v

Beklenen paket listesi:
  [1] 192.168.1.106:68 → 255.255.255.255:67  DHCP Discovery
  [2] 192.168.1.1:67 → 255.255.255.255:68  DHCP Offer
  [3] 192.168.1.106:68 → 255.255.255.255:67  DHCP Request
  [4] 192.168.1.1:67 → 255.255.255.255:68  DHCP ACK
  [5] 192.168.1.106:68 → 255.255.255.255:67  PXE DHCPINFORM (?)
  [6] ??? → 192.168.1.106:68  PXE Reply (?)
  [7] 192.168.1.106:... → 192.168.1.105:69  TFTP RRQ (?)

Kritik: Paket [6] ProxyDHCP yanıt → Varsa PXE server çalışıyor


Terminal 2: HTTP Server
─────────────────────

$ cd /home
$ echo "PXE Boot Test" > index.html
$ python3 -m http.server 8000

Bu server masaüstü'nün HTTP request'lerini log'lamıyor
ancak çalışıyor olduğunu gösterir.


Terminal 3: Monitoring
─────────────────────

$ watch -n 1 'netstat -tun | grep 8000'
(veya)
$ tail -f /var/log/http  # (eğer log var)

HTTP 8000'e bağlantı geliyor mu görmek için.
```

---

## OLASI TEŞHIS SENARYOLARI

### Senaryo A: DHCP OK + PXE Timeout

```
Capture:
  [1-4] DHCP paketleri ✓ (başarılı)
  [5] PXE DHCPINFORM gönderildi
  [6] ??? Timeout (ProxyDHCP yanıt yok)

Tanı:
  → RFC 4578 ProxyDHCP sunucusu eksik
  → Modem boot options desteği yok
  → Realtek LAN: UDP 4011 desteği yok (UDP 67 de dinlemeyip broadcast)

Çözüm:
  → ProxyDHCP server başlat (Telefon'da, UDP 67 veya 4011)
  → Veya HTTP Boot alternatifine geç
```

### Senaryo B: DHCP Timeout

```
Capture:
  [1] DHCP Discovery gönderildi
  [2] Timeout (no DHCP Offer)

Tanı:
  → Broadcast paket yönlendirilmiyor
  → Modem DHCP down?
  → Network connectivity issue
  → Ethernet kablo problem

Test:
  $ arp -a  (ARP cache'de modem var mı?)
  $ route -n  (Default gateway 192.168.1.1 mi?)

Çözüm:
  → Ethernet kablo kontrol et
  → Modem yeniden başlat
  → Network settings kontrol et
```

### Senaryo C: DHCP OK + TFTP Timeout

```
Capture:
  [1-4] DHCP ✓
  [5] PXE DHCPINFORM ✓
  [6] PXE Reply alındı ✓ (siaddr: 192.168.1.105 veya 192.168.1.1)
  [7] TFTP RRQ gönderildi
  [8] Timeout (no TFTP reply)

Tanı:
  → TFTP server (port 69) çalışmıyor
  → TFTP port (69) blocked (firewall?)
  → siaddr yanlış (TFTP server adresi)

Çözüm:
  → TFTP server başlat (Telefon'da, port 69)
  → port 69 test et (root gerekli)
  → Firewall settings kontrol et
```

### Senaryo D: HTTP GET Başarılı

```
Terminal 2 (HTTP server):
  GET /  HTTP/1.1
  [Masaüstü bağlantısı geldi]

Tanı:
  → HTTP network connectivity OK
  → Telefon HTTP port 8000 cevap veriyor
  → Problem: PXE DHCP/TFTP

Çözüm:
  → PXE problemi daha spesifik
  → DHCP/TFTP sunucusu eksik (değil HTTP)
  → RFC 4578 ProxyDHCP gerekli
```

---

## ÇALIŞMA PROTOKOLÜ

### Sırada Yapılacak

1. **Telefon tarafında 3 terminal aç**
   - Terminal 1: tcpdump (capture)
   - Terminal 2: HTTP server 8000
   - Terminal 3: Monitoring

2. **Masaüstüde testleri çalıştır**
   - ipconfig.com
   - ping 192.168.1.105
   - wget http://192.168.1.105:8000/

3. **PXE boot seçimi yap**
   - Masaüstü UEFI: Network Boot → PXE IPv4
   - Observe: Boot attempt

4. **Capture analiz et**
   - Hangi paketler gözlemlendi?
   - Timeout neresi?
   - Hata mesajı?

5. **Bulgular raporla**
   - tcpdump output
   - BIOS boot log
   - Masaüstü test sonuçları

---

## SORULAR VE CEVAPLAR

### S: HTTP server çalışıyor mu?
**A:** Evet, Terminal 2 "Serving HTTP" gösteriyorsa çalışıyor.

### S: Masaüstü telefon'a ping yapabilir mi?
**A:** Yapabilmeli. Önemli: TTL ~64 means kernel respond.

### S: DHCP paketleri neden capture'da görünmez?
**A:** tcpdump filter yanlış olabilir. Test:
```
tcpdump -i wlan0 -n 'udp' -v | head -50
(tüm UDP paketleri görmek için)
```

### S: PXE reply nereden gelecek?
**A:** Şu anda telefon ProxyDHCP çalıştırmıyor → reply yok.
Bu yüzden timeout.

### S: TFTP port 69 test edilebilir mi?
**A:** Evet, ama root gerekli:
```
$ python3 -c "import socket; s = socket.socket(); s.bind(('', 69))"
# Result: PermissionError (expected, root yok)
```

---

## BEKLENEN SONUÇ (Teşhis sonrası)

```
Hangi aşamada fail oluyor?

☐ DHCP aşaması
  → Modem DHCP reply yok
  → Network connectivity problem
  
☐ PXE aşaması
  → ProxyDHCP yanıt yok
  → RFC 4578 sunucu eksik
  
☐ TFTP aşaması
  → TFTP server yok
  → Port 69 blocked
  → siaddr yanlış

Sonuç: Sonraki kurulum adımı belirlenecek
```

---

## TEKNIK NOTLAR

### DHCP Paket Yapısı

```
DHCP Discovery:
  Source: 0.0.0.0:68
  Dest: 255.255.255.255:67
  Option 60: "PXEClient:Arch:00007:UNDI:..."

DHCP Offer (Modem):
  Source: 192.168.1.1:67
  Dest: 255.255.255.255:68
  yiaddr: 192.168.1.106 (assigned IP)
  siaddr: 192.168.1.1 (server IP, TFTP server?)
  file: "" (empty - no boot filename)

PXE DHCPINFORM:
  Source: 192.168.1.106:68
  Dest: 255.255.255.255:67 (broadcast)
  Message Type: 8 (DHCPINFORM)
  Option 60: "PXEClient:..."
```

### RFC 4578 ProxyDHCP

```
RFC 4578 Clause 2.1.1.1:
"DHCPINFORM servers SHOULD listen on port 67,
 but MAY listen on port 4011"

Masaüstü (Realtek):
  Phase 1: DHCP Discovery → Modem (port 67/68)
  Phase 2: DHCP ACK → Modem reply
  Phase 3: PXE DHCPINFORM → ?
    Option 1: UDP 67 broadcast (standard)
    Option 2: UDP 4011 unicast (RFC 4578 modern)
    Option 3: TFTP from siaddr

Beklenen:
  → Telefon veya modem UDP 67/4011'de ProxyDHCP reply
  → Realtek: "next-server" field'inden TFTP server öğrenir
```

---

## İLETİŞİM PLAN

1. **Masaüstü output'ları gönder:**
   - ipconfig.com çıktısı
   - ping 192.168.1.105 sonuç
   - wget / curl çıktısı
   - BIOS boot log (photo veya write)

2. **Telefon tcpdump'ı gönder:**
   - Capture output (10-20 satır yeterli)
   - Görünen paket tipleri
   - Timeout noktası

3. **Gözlemler:**
   - Masaüstü ekranında neler oldu?
   - Kurulum başladı mı?
   - Hata mesajı var mı?

---

**Hazırlayan:** PXE Boot Diagnostics Plan  
**Durum:** Teşhis protokolü hazır, başlamaya açık  
**Sonraki:** Masaüstü + Telefon testlerini paralel çalıştır
