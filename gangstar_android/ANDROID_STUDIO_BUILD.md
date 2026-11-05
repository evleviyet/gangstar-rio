# 🎨 GANGSTAR RIO - ANDROID STUDIO BUILD GUIDE

## 📋 PRE-REQUISITES

1. **Download & Install Android Studio**
   - Visit: https://developer.android.com/studio
   - Download latest version
   - Install on your computer

2. **Install Android SDK**
   - Open Android Studio
   - Tools → SDK Manager
   - Install:
     - SDK Platform 34 (Android 14)
     - SDK Tools
     - Android Emulator (optional)

---

## 🚀 STEP 1: Get Project Files

### Option A: Download from Termux (USB Cable)
```bash
# On Termux:
cd ~/gangstar_android
tar -czf gangstar_rio_project.tar.gz .

# Transfer to computer
adb pull /data/data/com.termux/files/home/gangstar_rio_project.tar.gz

# Extract
tar -xzf gangstar_rio_project.tar.gz
```

### Option B: GitHub Push
```bash
# On Termux:
cd ~/gangstar_android
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/YOUR_USERNAME/gangstar-rio.git
git push -u origin main

# On Computer:
git clone https://github.com/YOUR_USERNAME/gangstar-rio.git
```

### Option C: Manual Copy
- Copy all files from ~/gangstar_android to your computer

---

## 🎯 STEP 2: Open in Android Studio

1. **Launch Android Studio**

2. **Open Project**
   - File → Open
   - Select the gangstar_android folder
   - Click Open

3. **Wait for Indexing**
   - Android Studio will index files
   - Download dependencies (may take 5-10 min)
   - Gradle sync completes

---

## 🔧 STEP 3: Configure Project

1. **Check Java Version**
   - File → Project Structure
   - Set Java version to 11
   - SDK should be API 34

2. **Check Kotlin Plugin**
   - Plugins should auto-install
   - If not: Plugins → Marketplace → Search "Kotlin"

3. **Sync Gradle**
   - File → Sync Now
   - Wait for completion

---

## 🛠️ STEP 4: Build APK

### Method A: Debug APK (for testing)
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait ~2-5 minutes
3. Notification shows: "APK(s) generated successfully"
4. Click "Locate" to find APK

### Method B: Release APK (production)
1. Build → Generate Signed Bundle / APK
2. Choose "APK"
3. Click "Create new..."
4. Fill in keystore details:
   - Key store path: (create new)
   - Password: (set password)
   - Key alias: gangstar
   - Key password: (set password)
5. Next → finish

**Output:** `app/build/outputs/apk/release/app-release.apk`

---

## 📱 STEP 5: Test APK

### On Android Device
```bash
# Connect device via USB
adb devices

# Install APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Run
adb shell am start -n com.gangstar.rio.android/.MainActivity

# View logs
adb logcat | grep gangstar
```

### On Android Emulator
1. Android Studio → Tools → Device Manager
2. Create/start emulator
3. Build → Run 'app'
4. Select emulator
5. App launches automatically

---

## 🐛 TROUBLESHOOTING

### Error: "Manifest merger failed"
**Fix:**
```gradle
android {
    compileSdk = 34
    minSdk = 21
    targetSdk = 34
}
```

### Error: "Cannot resolve symbol"
**Fix:**
- File → Invalidate Caches
- Restart Android Studio

### Error: "Gradle sync failed"
**Fix:**
- Check internet connection
- Delete `.gradle` folder
- Sync again

### Error: "Build APK takes too long"
**Normal** - first build takes 5-15 minutes
- Subsequent builds are faster

---

## ✅ SUCCESS CHECKLIST

- [ ] Project opens in Android Studio
- [ ] Gradle sync completes
- [ ] No red errors in code
- [ ] APK builds successfully
- [ ] APK size ~5-10 MB
- [ ] App installs on device
- [ ] App starts without crash
- [ ] Game renders (blue/yellow/red shapes)
- [ ] Touch input works
- [ ] FPS shows in corner

---

## 📦 FINAL APK LOCATION

**Debug APK:**
```
app/build/outputs/apk/debug/app-debug.apk
```

**Release APK:**
```
app/build/outputs/apk/release/app-release.apk
```

---

## 🎮 RUNNING THE GAME

1. **Install APK** (see Step 5)
2. **Launch game** on device
3. **Controls:**
   - Touch: Analog stick (move character)
   - DPAD: 8-directional movement
   - Space: Fire weapon
   - Mission auto-assigns

4. **Gameplay:**
   - Cash: $0 (earn from missions)
   - Wanted: 0 (increases if crimes)
   - Missions: Accept and complete
   - Vehicles: Enter/exit with proximity
   - Police: Avoid or engage
   - Territory: Conquer for reputation

---

## 🚀 NEXT STEPS

1. ✅ Build & test APK
2. Test all gameplay features
3. Optimize performance
4. Add original Gangstar assets
5. Publish to Play Store (optional)

---

## 📞 SUPPORT

If issues occur:
1. Check Build → Clean Project
2. Invalidate caches (File menu)
3. Delete `build` and `.gradle` folders
4. Restart Android Studio
5. Sync Gradle again

---

**Status: Ready for Android Studio Build! 🚀**
