# 🔍 HERMES AGENT APK - COMPLETE ANALYSIS REPORT

## 📦 Basic Information
- **App Name**: Hermes Agent - AI v3.8
- **Package**: Unknown (obfuscated)
- **Type**: Remote Execution & Device Control Platform
- **Backend**: 126111.xyz domain

## 🏗️ Architecture

### Container & Runtime
```
├── Linux Container Runtime
│   ├── /root/workspace - Live shared filesystem with phone
│   ├── /root/.hermes - Config directory
│   ├── Python venv + Node.js runtime
│   └── Shell: bash, sh, zsh, fish
├── Terminal Emulation (xterm)
├── File System Browser & Editor
└── API Server (HTTP)
```

### Native Components (Split APK)
- **libqroot.so** - Root privilege framework
- **libxermes_proxy.so** - HTTP/API Proxy Gateway
- **libtermux.so** - Termux integration
- **libbit7z_jni.so** - 7-zip compression/extraction
- **libsentry*.so** - Error tracking
- **libc++_shared.so** - C++ runtime

## 🌐 Backend Infrastructure

### API Endpoints
```
https://newapi.126111.xyz/v1       - Main API server
https://share.126111.xyz/          - File sharing service  
https://tshare.126111.xyz/         - Temporary file sharing
https://gh-proxy.com/              - GitHub proxy mirror
https://raw.gitmirror.com/         - Git repository mirror
```

### Local Server
```
http://127.0.0.1:PORT              - Authenticated loopback
Authorization: Bearer {ACCESS_TOKEN}
Key Management: XERMES_LOCAL_PROXY_KEY
```

## 🔗 WebView & Native Bridge

### JavaScript Interfaces
- `window.Android` / `window.Xermes` - JS↔Native bridge
- `window.HermesAndroid` - Full API bridge
- `bridgeToken` - Session authentication
- Methods: open URL, error reporting, TTS, clipboard, device info

### Web Routes
```
/config          - Configuration
/env             - Environment variables
/files           - File browser/editor
/sessions        - Session management
/logs            - Application logs
/analytics       - Usage analytics
/skills          - Installed skills
/plugins         - Plugin manager
/mcp             - MCP server registry
/pairing         - Device pairing
/channels        - Communication channels
/webhooks        - Webhook management
/system          - System info & control
/docs            - Documentation
/cron            - Scheduled tasks
/profiles        - User profiles
/profile-builder - Profile creation
```

## ⚠️ CRITICAL CAPABILITIES

### 1. Remote Code Execution
```bash
/bin/bash                 # Full shell access
/root/workspace          # Shared execution environment
Python + Node.js         # Multi-language execution
```

### 2. Privilege Escalation Paths
```
/system/bin/su           # Standard su binary
/system/xbin/su          # Alternative su location
/system/app/Superuser.apk # Superuser app
qroot framework          # Custom root framework
```

### 3. File Operations
- ✅ Upload files: `/upload?path=`
- ✅ Download files: `/download`
- ✅ File picker integration
- ✅ ZIP backup/extraction
- ✅ LAN file sharing
- ✅ Workspace ↔ Phone sync

### 4. Device Integration
```
android_device.py Skills:
├── notify          - System notifications
├── alarm           - Alarm/reminders
├── toast           - Toast messages
├── clipboard-set   - Clipboard write
├── file-list       - File browsing
├── file-pick       - File picker
├── file-capture    - Camera integration
└── file-save       - Save to Downloads
```

### 5. Package Management
- ✅ APK installation checking
- ✅ Package information access
- ✅ Multi-package support (.apk, .aab, .xapk)

## 🔐 Authentication & Security

### Token System
- `ACCESS_TOKEN` - API authentication (Bearer token)
- `bridgeToken` - JS bridge session token
- `XERMES_LOCAL_PROXY_KEY` - Local proxy key (env var)
- `x-api-key` - API key header support

### Security Implementation
- BouncyCastle cryptography (v1.79)
- Signature verification on APKs
- Policy validation framework
- SecurityError exceptions
- Network security policies

## 📊 Dependencies & Libraries

### Key Libraries
```
Firebase encoders (17.0.0)
Google Play Services (23.6.0) - Ads, Location, Tasks
AndroidX (full stack 1.15.0+)
Sentry (8.51.0) - Error tracking
Kotlin Coroutines (1.7.1)
Room Database (2.5.0)
BouncyCastle (1.79)
WorkManager - Background tasks
```

## 🔍 Tracking & Telemetry

### Sentry Integration
```
Sentry Token: MNMM3TDLWFC5DOCIOFYQJO7JWI
Features:
├── Exception/Error tracking
├── Session replay
├── NDK (native) crash reporting
├── SQLite operation tracking
└── Fragment lifecycle tracking
```

### Firebase Analytics
- Measurement SDK v20.1.2
- Data transport (3.1.8)
- User identification

## 🚀 Execution Flow

1. **Startup**: Android native app loads WebView
2. **Bridge Setup**: JS/Native communication initialized
3. **Container Init**: qroot/Termux container started
4. **Connection**: Local proxy established (http://127.0.0.1:PORT)
5. **Backend Sync**: Connects to 126111.xyz API
6. **Ready**: Full remote control interface available

## ⚠️ RISK ASSESSMENT

### Severity: **CRITICAL**

**Why**: 
- Remote code execution on Android device
- Privilege escalation capabilities
- Full filesystem access
- Device function control
- No obvious user consent/permissions dialog
- Persistent background execution
- Encrypted command channel
- Telemetry & tracking

### Potential Attack Surface
- Compromised backend (126111.xyz)
- Man-in-the-middle on local network
- Intercepted token/credentials
- Malicious skill/plugin installation
- Container escape vulnerabilities

## 📝 Conclusion

**Hermes Agent** is a sophisticated remote administration tool disguised as an AI assistant. It provides:
- Complete shell access to device
- Root privilege escalation
- File upload/download/sync
- Package installation
- Device hardware control
- Persistent monitoring (Sentry)

This would require EXPLICIT user authorization to be used legitimately.

## 🔄 PERSISTENCE & AUTO-START

### Foreground Service (AgentService)
```
- Runs as continuous background process
- Foreground service flag enabled
- Google Play Services integration
- Auto-restart capability
```

### Daemon Processes
```
/system/xbin/daemonsu   - Daemon-mode privilege escalation
Persistent background execution
```

## 📡 Communication Protocol

### Local IPC
```
HTTP/REST API over localhost
Port: HERMES_ANDROID_CAPABILITY_PORT (dynamic)
Auth: Bearer {HERMES_ANDROID_CAPABILITY_TOKEN}
```

### Remote Backend
```
API Server: newapi.126111.xyz/v1
File Sharing: share.126111.xyz
Temp Files: tshare.126111.xyz
Protocol: HTTPS
Authentication: x-api-key / Bearer token
```

## 🚨 COMPLETE THREAT PROFILE

### Data Exfiltration Capability
- ✅ Full filesystem read access
- ✅ Workspace automatic sync
- ✅ File sharing endpoints
- ✅ ZIP export functionality
- ✅ No encryption/obfuscation visible

### Code Execution Paths
1. **Python scripts** - Full python execution
2. **Node.js** - JavaScript/TypeScript execution  
3. **Shell commands** - bash/sh execution
4. **APK installation** - Package manager access
5. **System calls** - Fork, exec, pipe available

### Device Control
- 🎤 Camera integration
- 📋 Clipboard access
- 🔔 Notification system
- ⏰ Alarm/timer control
- 📱 Device info access
- 🌐 URL/intent handling

### Lateral Movement
- ✅ Local network file sharing
- ✅ LAN upload capability
- ✅ GitHub integration (for code)
- ✅ Cloud file sharing

## 🎯 ATTACK SCENARIOS

### Scenario 1: Legitimate Development Tool
- Developer tool for Android app testing
- Required explicit user installation
- For personal/authorized testing only

### Scenario 2: Malicious Remote Access
- Backend compromise → Full device control
- Silent background execution
- Persistent data exfiltration
- Zero user visibility

### Scenario 3: Supply Chain Attack
- Installed via enterprise MDM
- Masqueraded as admin tool
- Used for mass device compromise

## 🛡️ MITIGATION RECOMMENDATIONS

1. **Immediate**
   - ✅ Uninstall the app
   - ✅ Check for suspicious backend activity
   - ✅ Review file access logs
   - ✅ Monitor network traffic to 126111.xyz

2. **Investigation**
   - ✅ Check installation date/source
   - ✅ Look for other suspicious APKs
   - ✅ Review /root/workspace contents
   - ✅ Check Sentry token usage

3. **Device Security**
   - ✅ Factory reset if concerned
   - ✅ Check other system apps for similar patterns
   - ✅ Enable APK signature verification
   - ✅ Use network monitoring tools

## 📊 IOCS (Indicators of Compromise)

### Network
- Domain: 126111.xyz (all subdomains)
- Sentry Token: MNMM3TDLWFC5DOCIOFYQJO7JWI
- API Pattern: /v1 endpoints

### File Patterns
- Directory: /root/workspace
- Directory: /root/.hermes
- File: .hermes_file_events.jsonl
- APKs: *3.8.xapk files

### Process Names
- AgentService
- Xermes processes
- qroot container

---
**Report Generated**: 2026-11-05
**Analysis Method**: Reverse Engineering (APK deconstruction)
**Tools Used**: strings, unzip, bash
**Confidence Level**: HIGH (direct bytecode analysis)
