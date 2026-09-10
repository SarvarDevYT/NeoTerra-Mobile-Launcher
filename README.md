<div align="center">

# 🌍 NeoTerra Mobile Launcher

**Android qurilmalari uchun rasmiy Minecraft: Java Edition launcheri**

[![Platform](https://img.shields.io/badge/Platform-Android%207.0%2B-38BDF8?style=for-the-badge&logo=android&logoColor=white)](https://neoterra.uz)
[![Server](https://img.shields.io/badge/Server-play.neoterra.uz-22C55E?style=for-the-badge&logo=minecraft&logoColor=white)](https://neoterra.uz)
[![Performance](https://img.shields.io/badge/Performance-60%2B%20FPS-EAB308?style=for-the-badge&logo=speedtest&logoColor=white)](https://neoterra.uz)
[![Language](https://img.shields.io/badge/Language-O%27zbekcha-A855F7?style=for-the-badge)](https://neoterra.uz)
[![License](https://img.shields.io/badge/License-GPLv3-EF4444?style=for-the-badge)](LICENSE)

<br/>

**NeoTerra Mobile Launcher** — o'zbek Minecraft hamjamiyati uchun maxsus ishlab chiqilgan, kuchsiz va o'rta toifadagi smartfonlarda ham yuqori FPS va silliq o'yin jarayonini ta'minlaydigan zamonaviy mobil launcher.

</div>

---

## ✨ Asosiy Imkoniyatlar

- 🚀 **Maksimal Unumdorlik (60+ FPS):** Holy GL4ES va Zink Vulkan grafik drayverlari, 720p aqlli masshtablash (*Resolution Scaler*), G1GC xotira tozalash optimizatsiyasi orqali qizib ketish va qotishlarsiz barqaror kadrlar tezligi.
- 🌐 **NeoTerra Serveriga 1-Click Kirish:** Bosh ekrandagi maxsus yashil tugma orqali to'g'ridan-to'g'ri `play.neoterra.uz:25565` serveriga avtomatik ulanish.
- 🇺🇿 **100% O'zbekcha Lokalizatsiya:** Barcha menyular, sozlamalar, boshqaruv tugmalari va tizim xabarlari to'liq ona tilimizda.
- 🎨 **Ely.by Skin & Plash Tizimi:** Ely.by yoki o'yinchi niki kiritilishi bilanoq uning skini va 3D bosh qismi avtomatik yuklanadi hamda serverdagi barcha o'yinchilarga ko'rinadi.
- 📱 **Zamonaviy Sensorli Boshqaruv (Touch Controls):** Qulay shaffof tugmalar (*squircles*), o'zbekcha yorliqlar (`URISH`, `BLOK`, `SAKRASH`, `SNEAK`, `INV`, `KURSOR`, `KLAV`, `F3`, `F5`, `CHAT`, `TAB`) va haptic titrash orqali qulay boshqaruv.
- 🧩 **Modlar Qo'llab-quvvatlovi:** Forge, Fabric, Quilt, OptiFine, Iris, Sodium modlari hamda Minecraft 1.7.10 dan tortib 1.21+ gacha bo'lgan barcha versiyalar bilan to'liq moslik.
- ☕ **Avtomatik Java boshqaruvi:** Java 8, Java 17 va Java 21 versiyalarini avtomatik moslashtirib ishga tushirish.

---

## 📥 O'rnatish va Foydalanish

1. [Releases](https://github.com/SarvarDevYT/NeoTerra-Mobile-Launcher/releases) bo'limidan eng so'nggi **NeoTerra-Launcher-Mobile.apk** faylini yuklab oling.
2. APK faylni smartfoningizga o'rnating va kerakli xotira ruxsatlarini bering.
3. Launcherni oching va nikingizni kiriting (agar Ely.by da hisobingiz bo'lsa, o'sha nikni kiritsangiz skiningiz avtomatik ulanadi).
4. **"NEOTERRA SERVERIGA KIRISH"** tugmasini bosing va o'yindan zavqlaning!

---

## 🛠 Manba Kodidan Yig'ish (Building from Source)

Loyihani o'zingiz yig'ish uchun:

### Talablar:
- **JDK 17** (masalan, Eclipse Adoptium JDK 17)
- **Android SDK** (API 34+, Build-Tools 34.0.0+)
- **Android NDK** (r27b yoki 27.1.12297006)

### Yig'ish buyrug'i:

```bash
# Repozitoriyani klonlash
git clone https://github.com/SarvarDevYT/NeoTerra-Mobile-Launcher.git
cd NeoTerra-Mobile-Launcher

# Windows uchun:
.\gradlew.bat assembleDebug

# Linux / macOS uchun:
chmod +x gradlew
./gradlew assembleDebug
```

Tayyor APK fayl quyidagi manzilda hosil bo'ladi:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🌐 Server Ma'lumotlari

| Parametr | Qiymat |
| :--- | :--- |
| **Server Nomi** | NeoTerra Minecraft Server |
| **Server IP** | `play.neoterra.uz` |
| **Port** | `25565` |
| **Rasmiy Veb-sayt** | [neoterra.uz](https://neoterra.uz) |
| **Kompyuter Launcheri** | [NeoTerra Launcher PC](https://github.com/SarvarDevYT/NEOTERRA-Launcher) |

---

## 📄 Litsenziya

NeoTerra Mobile Launcher [GNU General Public License v3.0](LICENSE) litsenziyasi asosida taqdim etiladi.
Open-source hamjamiyatiga va barcha Minecraft modderlariga o'z minnatdorchiligimizni bildiramiz.
