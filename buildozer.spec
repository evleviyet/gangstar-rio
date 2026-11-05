[app]
title = Gangstar Rio
package.name = gangstar_rio
package.domain = org.gangstar.rio
source.dir = .
source.include_exts = py
version = 1.0.0
requirements = python3,flet
orientation = portrait
fullscreen = 0
icon.filename = %(source.dir)s/assets/icon.png
android.permissions = INTERNET
android.api = 34
android.minapi = 21
android.ndk = 25c
android.accept_sdk_license = True
android.archs = arm64-v8a,armeabi-v7a

[buildozer]
log_level = 2
warn_on_root = 1
