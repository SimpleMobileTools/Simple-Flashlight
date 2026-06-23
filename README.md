# Peitsman Flashlight

<img alt="Logo" src="graphics/icon.png" width="120" />

A deliberately simple flashlight app in the Peitsman house style. No menus, no clutter — just one big button to toggle your device's LED, and a slider to set the brightness.

## Features

- **One-tap on/off** — a single large button turns the flashlight on and off.
- **Brightness control** — a slider adjusts the LED brightness on devices that support it (Android 13+).
- **Nothing else** — no stroboscope, no SOS, no bright display, no widget, no settings, no quick-settings tile. The app does one thing well.

## Privacy & permissions

- Requires only the `FLASHLIGHT` permission and a device with a camera flash.
- No internet access, no ads, no tracking, no account.

## Branding

The app follows the **Peitsman Brand Identity Guidelines (v1.5)**. The launcher icon places the flashlight glyph on the Peitsman brand red (`#FF3C00`, PMS 2028 C). Supporting brand colors: smoky black `#141312` and off-white `#F6F3F1`.

## Building

The project is a standard Gradle Android build. You need a **JDK 17 that includes the `jmods` directory** (the slimmed-down "nomod" JBR that ships with Android Studio will fail the `jlink`/`JdkImageTransform` step).

```bash
# Point at a full JDK 17 (with jmods)
export JAVA_HOME=/path/to/jdk-17

# Debug APK (no signing required)
./gradlew assembleCoreDebug

# Release APK (requires keystore.properties, see keystore.properties_sample)
./gradlew assembleCoreRelease
```

The debug APK is written to `app/build/outputs/apk/core/debug/`.

Product flavors: `core`, `fdroid`, `prepaid`. Build types: `debug` and `release`.

## Credits

Based on the open-source [Simple Flashlight](https://github.com/SimpleMobileTools/Simple-Flashlight) by Simple Mobile Tools, simplified and rebranded for Peitsman. Released under the GPL-3.0 license — see [LICENSE](LICENSE).
