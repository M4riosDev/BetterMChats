# M4R1OS HUD & Chat Forge Mod

![Minecraft](https://img.shields.io/badge/Minecraft-1.16.5-brightgreen?logo=minecraft)
![Forge](https://img.shields.io/badge/Forge-36%2B-orange?logo=curseforge)
![Java](https://img.shields.io/badge/Java-8%2B-red?logo=openjdk)
![Version](https://img.shields.io/badge/Version-1.1.3-blue)

> **FiveM-style chat channels and HUD overlay for Minecraft Forge 1.16.5**

A server-side mod (with a required client component) that replaces Minecraft's default chat with a sleek, GTA/FiveM-inspired HUD. Messages appear as styled, icon-tagged boxes that slide in and fade out — perfect for roleplay servers.

---

## Features

- **Custom chat overlay** — replaces the vanilla chat with a styled HUD overlay (colored boxes, icons, labels)
- **10 chat channels** — each with its own icon, color, and label
- **Staff-only channels** — `staff` and `system` are restricted to operators (permission level 2)
- **Smooth animations** — messages slide in and fade out after a configurable duration
- **Scrollable chat history** — open chat and scroll through up to 500 past messages
- **Server-configurable HUD** — the server can push HUD layout settings directly to clients via packets
- **System message interception** — server system messages are automatically styled and shown through the HUD

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.18.2 |
| Minecraft Forge | 40+ |
| Java | 17+ |

> ⚠️ Both the **server** and **client** must have this mod installed.

---

## Installation

1. Download the `.jar` file from the releases page.
2. Place it in the `mods/` folder of both your **server** and **client**.
3. Start the server — the mod registers its config and network channel automatically.

---

## Chat Channels

These commands are available to send styled messages through the HUD. Most are open to all players; `staff` and `system` require permission level 2.

| Command | Label | Color | Icon | Permission |
|---|---|---|---|---|
| `/staff <message>` | STAFF | Purple `#7D3CFF` | 🚀 staff | OP (lvl 2) |
| `/system <message>` | SYSTEM | Yellow `#FFF200` | 💾 system | OP (lvl 2) |
| `/police <message>` | POLICE | Blue `#2E6BFF` | 🚔 police | All |
| `/gov <message>` | GOV | Green `#2ECC71` | 🏛 gov | All |
| `/twt <message>` | TWITTER | Twitter Blue `#1DA1F2` | 🐦 twitter | All |
| `/announce <message>` | ANNOUNCE | Cyan `#34E8EB` | 📢 announce | All |
| `/ooc <message>` | OOC | Dark gray `#2B2B2B` | 📰 ooc | All |
| `/robbery <message>` | ROBBERY | Orange `#FFA600` | 💰 robbery | All |
| `/anon <message>` | ANONONYMOUS | Red `#FF1100` | 🪓 anon | All |
| `/event <message>` | EVENT | Purple `#9B59B6` | 🏆 event | All |

> **Note:** For `/twt` and `/ooc`, the sender's name is automatically prepended to the message and displayed as `CHANNEL | Username | message`.

---

## Channel Muting (v1.1.3+)

You can now mute specific channels to hide them from your HUD overlay. Muted channels are stored locally on your client and persist across client restarts.

### Mute Commands

| Command | Description |
|---|---|
| `/hudmute <channel>` | Mute a channel (e.g., `/hudmute ooc`, `/hudmute police`) |
| `/hudunmute <channel>` | Unmute a channel |
| `/hudmuted` | List all currently muted channels |

### Examples

```
/hudmute ooc          # Hide OOC messages
/hudmute twitter      # Hide Twitter messages
/hudmute staff        # Hide staff channel
/hudunmute police     # Show police messages again
/hudmuted             # See list: "Muted channels: OOC, STAFF"
```

---

## Raw HUD Command

```
/hud <raw markup string>
```

Sends a raw markup string directly to all players. Requires permission level 0 (any player), but is mainly intended for admin/plugin use. See the [Markup Format](#markup-format) section below.

---

## Markup Format

The HUD uses a simple tag-based markup parsed from a raw string. Tags appear at the start of the message in square brackets.

```
[emoji=<key>][label=<TEXT>][box][color=#RRGGBB] Your message here
```

### Tags

| Tag | Description |
|---|---|
| `[emoji=<key>]` | Icon to display. Keys: `staff`, `police`, `gov`, `twitter`, `system`, `announce`, `ooc`, `robbery`, `anon` |
| `[label=<TEXT>]` | Channel label shown before the message (e.g. `POLICE`) |
| `[box]` | Draws a colored background box behind the message |
| `[color=#RRGGBB]` | Background box color. Accepts hex (`#2E6BFF`) or named colors |
| `[bg=#RRGGBB]` | Alias for `[color=...]` |

### Named Colors

`blue`, `red`, `green`, `yellow`, `purple`, `gray`, `black`, `white`, `twitter`

### Example

```
[emoji=police][label=POLICE][box][color=#2E6BFF] Officer down at central bank!
```

---

## HUD Layout & Configuration

The server can push HUD display settings to clients at any time using the `HudConfigPacket` network packet. The following values are configurable:

| Field | Default | Description |
|---|---|---|
| `offsetX` | `8` | Horizontal offset from the anchor edge (px) |
| `offsetY` | `8` | Vertical offset from the anchor edge (px) |
| `width` | `240` | Width of the HUD panel (120–260 px) |
| `lineHeight` | `18` | Height of each text line (12–40 px) |
| `gap` | `4` | Gap between messages (0–30 px) |
| `maxEntries` | `8` | Max messages visible at once (1–20) |
| `lifeMs` | `9000` | How long a message stays fully visible (ms) |
| `fadeMs` | `1200` | Fade-out duration after `lifeMs` expires (ms) |
| `showIcon` | `true` | Whether to show channel icons |
| `iconSize` | `14` | Icon size in pixels (8–32) |

The HUD is anchored to the **top-left** by default. Messages grow downward when chat is closed, and are displayed above the chat input bar when chat is open. A slide-in animation plays for each new message.

---

## Chat History

When the chat screen is open, you can scroll through the last **500 messages**:

| Key | Action |
|---|---|
| Scroll wheel | Scroll ±2 entries |
| Page Up | Scroll +10 entries |
| Page Down | Scroll −10 entries |
| Home | Jump to oldest |
| End | Jump to newest |

---

## Project Structure

```
src/main/java/com/m4r1os/fivemhud/
├── FiveMHudMod.java          # Mod entry point, command registration
├── ServerHudSync.java        # Server-side HUD config sync
├── client/
│   ├── HudOverlay.java       # Main HUD rendering, message history
│   ├── FiveMChatScreen.java  # Custom chat screen with scrollable history
│   ├── Markup.java           # Tag parser for raw HUD strings
│   ├── ClientHudState.java   # Client-side HUD state (layout config)
│   └── EmojiRegistry.java    # Maps icon keys to textures
├── commands/
│   └── ModCommands.java      # All channel commands (/police, /staff, etc.)
└── network/
    ├── ModNetwork.java        # Forge network channel setup
    ├── ChannelMsgPacket.java  # Packet: send a raw HUD message to a client
    └── HudConfigPacket.java   # Packet: push HUD layout config to clients
```

---

## Building from Source

```bash
./gradlew build
```

The compiled `.jar` will be in `build/libs/`.

---

## Authors

- **m4r1os** — original concept & development
- **MNV** — development
- **SaralisDev** — development & design

---

## License

All Rights Reserved. Do not redistribute without permission.
