[app]
title = Gangstar Rio
package.name = gangstar_rio
package.domain = org.gangstar
source.dir = .
source.include_exts = py,png,jpg,kv,atlas
version = 1.0.0
requirements = python3,kivy,flet
orientation = portrait
fullscreen = 0
android.permissions = INTERNET,WRITE_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE
android.api = 31
android.minapi = 21
android.ndk = 25c
android.accept_sdk_license = True

[buildozer]
log_level = 2
warn_on_root = 1
