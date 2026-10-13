# Valorant MC Mod 🎯

Valorant mechanics, tactical gunplay, recoil systems, and agents implemented in Minecraft using **Fabric** (Minecraft 1.21.1).

---

## ✨ Features

- **Tactical Weapons**: Vandal, Phantom, Sheriff, Spectre, Operator, Odin, and more with custom stats, firerates, and ammo.
- **Realistic Recoil & Ballistics Engine**: Pitch/yaw kickback, spray patterns, recovery, and hitscan collision detection.
- **Recoil Follow & Custom Crosshair**: Dynamic crosshairs and optional recoil crosshair following.
- **Sound Effects & Visuals**: Custom gunfire audio, reload animations, bullet hole decals, and muzzle flash effects.
- **Agent Data**: Framework and presets for Valorant agents and abilities.

---

## 🛠️ Requirements

- **Java**: JDK 21+
- **Minecraft**: 1.21.1
- **Mod Loader**: Fabric Loader (>= 0.16.5)
- **Fabric API**: 0.104.0+1.21.1

---

## 🚀 Getting Started

### Clone the Repository
```bash
git clone https://github.com/HsHappy/valorant-mc-mod.git
cd valorant-mc-mod
```

### Launch Development Test Client
On Windows:
```cmd
.\gradlew.bat runClient
```
*(Or simply double click `run_game.bat` / `run_test.bat`)*

On Linux / macOS:
```bash
./gradlew runClient
```

### Build the Mod JAR
```bash
./gradlew build
```
The compiled mod JAR will be located in `build/libs/`.

---

## 🎮 Controls

- **Left Click**: Fire weapon
- **R**: Reload
- **H**: Toggle Recoil Follow Crosshair
