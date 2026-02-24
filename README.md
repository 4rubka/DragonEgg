# 🐉 DragonEgg - Ultimate Dragon Power Plugin

[![Modrinth](https://img.shields.io/modrinth/dt/dragonegg?style=for-the-badge&logo=modrinth&color=00af5c)](https://modrinth.com/plugin/dragonegg)
[![Spigot](https://img.shields.io/badge/dynamic/json?style=for-the-badge&logo=spigot&color=ff9900&label=downloads&query=downloads&url=https://api.spigotmc.org/simple/0.2.0/index.php?action=getResource&id=0)](https://www.spigotmc.org/resources/dragonegg)
[![Version](https://img.shields.io/badge/version-1.0.0-blue?style=for-the-badge)](https://github.com/DragonEgg/Plugin/releases)
[![License](https://img.shields.io/badge/license-MIT-green?style=for-the-badge)](LICENSE)

**DragonEgg** is a powerful, modern Minecraft plugin that transforms the iconic Dragon Egg into an artifact of immense power. Grant permanent abilities, extra hearts, and unlock legendary mechanics for players who possess this mythical item!

---

## ✨ Features

### 🔥 Core Mechanics

| Feature | Description |
|---------|-------------|
| **Permanent Effects** | Grant configurable potion effects (Strength, Speed, Regeneration, etc.) |
| **Extra Hearts** | Add permanent bonus HP with configurable caps |
| **Dragon Bond** | Egg binds to first user, preventing theft |
| **Evolution System** | Level up the egg through kills and playtime |

### ⚔️ Combat Abilities

| Ability | Description |
|---------|-------------|
| **Dragon Shield** | Chance to negate incoming damage |
| **Dragon Fury** | Power boost when health is critical |
| **Dragon Revive** | Once-per-cooldown death prevention |
| **Dragon Aura** | Buff nearby teammates |

### 🎨 Visual Effects

- Custom particle effects
- Glowing player effect
- Custom item name & lore
- Sound effects for all abilities

---

## 📋 Requirements

- **Server Software:** Paper, Spigot, or compatible forks
- **Minecraft Version:** 1.20.4+ (1.19-1.20.x compatible)
- **Java Version:** Java 17+

---

## 🚀 Installation

1. Download the latest `.jar` file from [Modrinth](https://modrinth.com/plugin/dragonegg) or [Releases](https://github.com/DragonEgg/Plugin/releases)
2. Place the file in your server's `plugins/` folder
3. Restart your server
4. Configure the plugin in `plugins/DragonEgg/config.yml`

---

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/dragonegg` | `dragonegg.use` | Main command |
| `/dragonegg give <player>` | `dragonegg.commands.give` | Give Dragon Egg |
| `/dragonegg reload` | `dragonegg.commands.reload` | Reload configuration |
| `/dragonegg info [player]` | `dragonegg.commands.info` | Show Dragon Egg info |
| `/dragonegg sethearts <amount>` | `dragonegg.commands.sethearts` | Set extra hearts |
| `/dragonegg menu` | `dragonegg.commands.menu` | Open configuration menu |
| `/dragonegg addEffect <effect> <level>` | `dragonegg.commands.addeffect` | Add effect |
| `/dragonegg removeEffect <effect>` | `dragonegg.commands.removeeffect` | Remove effect |
| `/dragonegg setEffectLevel <effect> <level>` | `dragonegg.commands.seteffect` | Set effect level |
| `/dragonegg clearEffects` | `dragonegg.commands.cleareffects` | Clear all effects |

---

## 🔐 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `dragonegg.use` | true | Use Dragon Egg features |
| `dragonegg.admin` | op | Full admin access |
| `dragonegg.commands.give` | op | Give Dragon Egg |
| `dragonegg.commands.reload` | op | Reload config |
| `dragonegg.commands.info` | true | View info |
| `dragonegg.commands.sethearts` | op | Set hearts |
| `dragonegg.commands.menu` | true | Open menu |
| `dragonegg.bypass.limit` | op | Bypass limits |
| `dragonegg.bypass.cooldown` | op | Bypass cooldowns |
| `dragonegg.hearts.vip` | false | +2 extra hearts |
| `dragonegg.hearts.mvp` | false | +4 extra hearts |
| `dragonegg.hearts.admin` | false | +10 extra hearts |

---

## ⚙️ Configuration

### Effects Configuration

```yaml
effects:
  STRENGTH:
    enabled: true
    level: 1          # Amplifier
    permanent: true   # Permanent or refresh-based
```

### Hearts Configuration

```yaml
hearts:
  extra: 4            # Base extra hearts
  max-cap: 40         # Maximum hearts cap
  permission-scaling:
    dragonegg.hearts.vip: 2
```

### Mechanics Configuration

```yaml
mechanics:
  aura:
    enabled: true
    radius: 10.0
  fury:
    enabled: true
    hp-threshold: 20.0
    duration: 10
    cooldown: 60
  shield:
    enabled: true
    negation-chance: 10.0
  revive:
    enabled: true
    cooldown-hours: 24
```

---

## 🌍 Multi-Language Support

DragonEgg supports multiple languages out of the box:

- 🇬🇧 **English** (en)
- 🇺🇦 **Ukrainian** (ua)
- 🇵🇱 **Polish** (pl)
- 🇩🇪 **German** (de)
- 🇫🇷 **French** (fr)
- 🇪🇸 **Spanish** (es)

Change the language in `config.yml`:
```yaml
language: en
```

---

## 📊 Evolution System

The Dragon Egg evolves through 5 levels:

| Level | Kills Required | Playtime | Unlocks |
|-------|---------------|----------|---------|
| 1 | 0 | 0 min | Base effects |
| 2 | 10 | 60 min | Strength II |
| 3 | 25 | 180 min | Speed II, Regeneration I |
| 4 | 50 | 360 min | Resistance I |
| 5 | 100 | 720 min | Absorption II, Fire Resistance |

---

## 🎯 GUI Menu

Access the configuration menu with `/dragonegg menu`:

- **Effects** - Manage potion effects
- **Extra Hearts** - Adjust bonus HP
- **Mechanics** - Toggle abilities
- **Visual Effects** - Customize particles & sounds

---

## 🔧 Developer API

DragonEgg provides a simple API for integration:

```java
DragonEggMain plugin = DragonEggMain.getInstance();

// Get player's extra hearts
double hearts = plugin.getHeartManager().getExtraHearts(player);

// Check if player has Dragon Egg
boolean hasEgg = plugin.getDragonBond().isBonded(player);

// Get evolution level
int level = plugin.getEggEvolution().getLevel(player);
```

---

## 📝 To-Do / Roadmap

- [ ] Add faction/clan support for Dragon Aura
- [ ] Custom enchantments for Dragon Egg
- [ ] Dragon mount mechanic
- [ ] Leaderboards for evolution progress
- [ ] Web dashboard for server admins
- [ ] PlaceholderAPI expansion

---

## 🐛 Bug Reports & Feature Requests

- **Issues:** [GitHub Issues](https://github.com/DragonEgg/Plugin/issues)
- **Discussions:** [GitHub Discussions](https://github.com/DragonEgg/Plugin/discussions)
- **Discord:** [Join our Discord](https://discord.gg/dragonegg)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Credits

- **Developer:** DragonEggTeam
- **Contributors:** See [Contributors](https://github.com/DragonEgg/Plugin/graphs/contributors)
- **Icons:** [Game Icons](https://game-icons.net/)

---

## 🙏 Support the Project

- ⭐ Star this repository on GitHub
- 💖 Download on [Modrinth](https://modrinth.com/plugin/dragonegg)
- 📊 Rate on [SpigotMC](https://www.spigotmc.org/resources/dragonegg)

---

**DragonEgg** - Unleash the power of the End Dragon! 🐉✨
