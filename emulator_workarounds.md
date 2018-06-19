Some possible workarounds for bugs in android studio/dev tools
 
## 100% cpu workload
add to ~/.local/android/avd/<name>/config.ini
```
hw.audioInput=no
hw.audioOutput=no
```

## emulator doesn't start in Kubuntu 17
add `export ANDROID_EMULATOR_USE_SYSTEM_LIBS=1` to android-studio `studio.sh` to force system lib usage in AVD
