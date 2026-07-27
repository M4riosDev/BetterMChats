# BetterMChats (Fabric)

FiveM-style HUD + chat messages for Minecraft 1.21.1 - **Fabric** edition

## Requirements

- Minecraft 1.21.1
- [Fabric Loader](https://fabricmc.net/use/) 0.16+
- [Fabric API](https://modrinth.com/mod/fabric-api) built for 1.21.1 (**required**, the mod will not load without it)
- Java 21

## Building

```
./gradlew build
```

The built jar appears in `build/libs/`. This was written and tested for correctness by hand in a sandbox with no internet access to Maven/Fabric/Mojang servers, so **the first build is the real first compile** - see "Things worth double-checking" below.

## Commands

Same as the Forge version: `/staff`, `/police`, `/gov`, `/twt`, `/system`, `/announce`, `/ooc`, `/robbery`, `/anon`, `/event`, `/ems`, `/ad`, `/me <action>`, `/hud <raw>`, `/clear chats all|group <group>|user <username>`. Client-side: `/hudmute <channel>`, `/hudunmute <channel>`, `/hudmuted`.