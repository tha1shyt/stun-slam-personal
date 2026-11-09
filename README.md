# ⚡ Stun Slam Mod (Fabric 1.21.x)
### *Fully Optimal, 100% Accurate Shield-Break & Mace-Slam Combo with Native ToxMaceSwap Compatibility*

---

## 📖 Overview
In modern Minecraft Mace PvP (1.21+), a **Stun Slam** is an elite combat technique:
When an opponent guards with a shield, a direct mace attack deals zero damage and triggers full attack cooldown. To bypass this, you must hit the opponent's shield with an **Axe** (which disables their shield for 5 seconds), and instantly follow up with a **Mace Smash Attack** before they can react or recover.

### Why Standard Mods Fail or Break with ToxMaceSwap:
1. **Slot Overwrite Conflict:** Mods like **ToxMaceSwap** (Smart Mace Swap) constantly monitor your falling state to force-select Density or Breach maces. If you try to swap to an Axe to break a shield, ToxMaceSwap immediately overrides the slot back to a Mace, ruining the shield stun!
2. **Anti-Cheat Flags & Desync:** Trying to send packet-spam attacks without proper slot coordination causes ghost items, rejected hits, or anti-cheat rubberbanding.

### How Stun Slam Solves This:
- **Atomic Hotbar Lock:** Stun Slam engages a configurable sub-tick execution lock during the combo. Other mods (including ToxMaceSwap) cannot interrupt or change your hotbar slot until the Axe breaks the shield and the Mace slam lands.
- **100% Accuracy on Shield Click:** The moment you left-click on an opponent who is blocking with a shield, Stun Slam intercepts the input, verifies the shield angle, selects your Axe, stuns the shield, swaps to your Mace, delivers the smash, and smoothly returns to your weapon.
- **Full In-Game Customizability:** Integrated with **Cloth Config** and **Mod Menu**, plus in-game `/stunslam` commands.

---

## 🛠️ Features

| Feature | Description |
| :--- | :--- |
| **100% Accurate Shield Interception** | Automatically detects when your crosshair or attack clicks a shield-blocking enemy and executes the combo. |
| **ToxMaceSwap Native Lock** | Pauses external hotbar switching during the Axe-to-Mace sequence so ToxMaceSwap never desyncs. |
| **Smart Weapon Prioritization** | Automatically picks the highest-tier Axe (Netherite > Diamond > Iron) and smart-selects Density/Breach Maces. |
| **Configurable Tick Delay** | Supports **Instant (0-Tick burst)** for maximum speed or **1-Tick Delay** for anti-cheat safety (GrimAC/Vulcan). |
| **Shield Angle Verification** | Only attempts shield-break if the enemy's shield is facing you (within the 180° blocking arc). If facing away, your Mace hits unhindered. |
| **Post-Combo Recovery** | Choose whether to return to your previous slot, stay on Mace, or swap directly to Sword. |
| **In-Game GUI & Commands** | Full Cloth Config screen accessible via Mod Menu, toggle keybind (`V` by default), and `/stunslam` commands. |

---

## ⚙️ Configuration Guide

Access settings in-game via **Pause Menu -> Mods -> Stun Slam -> Configure**, or use chat commands (`/stunslam` / `/ss`).

### 1. General Settings
- **Enable Stun Slam:** Toggle on/off (Default: `ON`, Keybind: `V`).
- **Trigger Mode:**
  - `ON_SHIELD_CLICK` *(Recommended)*: Triggers whenever you left-click an opponent blocking with a shield.
  - `TRIGGER_BOT`: Automatically swings the moment you aim at a shielding opponent.
  - `KEYBIND_ONLY`: Only triggers while holding a designated key.

### 2. Combat & Timing
- **Swap Delay Ticks:**
  - `0 Ticks (Instant)`: Performs Axe hit and Mace hit in the same client tick. Best for vanilla servers and practice maps.
  - `1 Tick Delay (Anti-Cheat Safe)`: Fires Axe hit on Tick 0, Mace hit on Tick 1 (50ms). Recommended for strict servers (GrimAC, Vulcan, Polar).
- **Reach Distance:** Default `3.8` blocks (configurable up to `5.0`).
- **Require Facing Shield:** Ensures the shield is facing you so you don't waste Axe durability on back hits.
- **Require Falling:** Toggle whether Stun Slam only triggers when you have falling momentum.

### 3. ToxMaceSwap Compatibility
- **ToxMaceSwap Lock:** When enabled, drops external slot change packets during the combo window.
- **Lock Duration:** Default `2 ticks`.
- **Post-Combo Action:**
  - `Return to Previous Slot` (switches back to whatever you held before)
  - `Keep Mace Equipped` (stays on Mace)
  - `Switch to Sword` (swaps to Sword to finish the target)
  - `Hand Off to ToxMaceSwap` (instantly releases control to ToxMaceSwap)

---

## 📦 How to Build the Mod

To compile the mod into a `.jar` ready for your `.minecraft/mods` folder:

### Prerequisites:
- Java JDK 21 installed on your computer.

### Build Steps:
1. Open a terminal / command prompt in this directory (`StunSlamMod`).
2. Run:
   ```bash
   ./gradlew build
   ```
   *(On Windows Command Prompt: `gradlew.bat build`)*
3. Once finished, find your compiled JAR in:
   ```
   build/libs/StunSlam-1.0.0.jar
   ```
4. Copy `StunSlam-1.0.0.jar` into your Minecraft mods folder:
   ```
   %appdata%/.minecraft/mods/
   ```
5. Ensure you have **Fabric API**, **Mod Menu**, and optionally **Cloth Config** in your mods folder.

---

## 🎮 Recommended Hotbar Setup
For flawless combat flow:
- **Slot 1:** Sword
- **Slot 2:** Mace (Density / Breach)
- **Slot 3:** Axe (Netherite or Diamond)
- **Slot 4:** Wind Charges / Ender Pearls
- **Slot 5–9:** Utilities, Golden Apples, Fireworks, Totems

Whenever you click on an opponent who raises their shield, Stun Slam will handle the rest with 100% precision!
