# MeoRegion v1.0.0

**Created by SystMEO**

MeoRegion is an advanced, powerful, and user-friendly region management plugin for Spigot servers, designed for stability and performance. It allows players to claim, manage, and protect their territories using an intuitive GUI and a comprehensive set of commands. The plugin is built to be compatible with hybrid servers that use both plugins and mods.

---

## Features

- **Easy Land Claiming**: Players can claim a 3x3 chunk area with a single command.
- **Advanced Flag System**: Protect your region with a wide range of flags (PvP, mob spawning, item usage, etc.).
- **Full Mod Compatibility**: The flag system is designed to work with items and blocks from mods.
- **Economy Integration**: Sell or rent your regions to other players (requires Vault).
- **Intuitive GUI**: Manage all aspects of your region—members, flags, and economy—through an easy-to-use graphical interface.
- **Sub-chunks**: Fine-tune permissions for individual chunks within your region.
- **Full Localization**: Comes with complete English and Ukrainian translations. Easily add more languages.
- **Robust Data Storage**: Choose between JSON files (with automatic backups) or SQLite for data storage.
- **Performance-Oriented**: Optimized with features like asynchronous data saving to minimize server lag.

---

## Installation

1.  Ensure you have a Spigot (or compatible, like Paper/Purpur) server for Minecraft 1.12.2.
2.  If you want to use economy features, install [Vault](https://www.spigotmc.org/resources/vault.34315/) and an economy plugin (e.g., EssentialsX).
3.  Download the `MeoRegion-1.0.0.jar` file.
4.  Place the JAR file into your server's `plugins` folder.
5.  Restart your server.

---

## Commands

The main command is `/rg` (aliases: `/region`, `/regions`).

| Command                                       | Description                                      |
| --------------------------------------------- | ------------------------------------------------ |
| `/rg`                                         | Opens the main graphical menu.                   |
| `/rg help`                                    | Displays a list of all available commands.       |
| `/rg claim [name]`                            | Claims a 3x3 chunk area where you are standing.  |
| `/rg delete <region>`                         | Deletes one of your regions.                     |
| `/rg info [region]`                           | Shows detailed information about a region.       |
| `/rg list [player/all]`                       | Lists your regions or those of another player.   |
| `/rg gui <region>`                            | Opens the management menu for a specific region. |
| `/rg add <region> <player>`                   | Adds a player to your region as a MEMBER.        |
| `/rg remove <region> <player>`                | Removes a player from your region.               |
| `/rg setrole <region> <player> <role>`        | Sets a role for a player in your region.         |
| `/rg grid [region/off]`                       | Toggles a visual grid of the region's borders.   |
| `/rg find`                                    | Finds and lists nearby regions.                  |
| `/rg merge <region1> <region2>`               | Merges two of your regions into one.             |
| `/rg preview`                                 | Toggles a visual preview of the claim area.      |
| `/rg sch flag <flag> <value>`                 | Sets a flag for the sub-chunk you are in.        |

**Economy Commands:**

| Command                                       | Description                                      |
| --------------------------------------------- | ------------------------------------------------ |
| `/rg sell <region> <price>`                   | Puts a region up for sale.                       |
| `/rg unsell <region>`                         | Removes a region from sale.                      |
| `/rg buy <region>`                            | Buys a region that is for sale.                  |
| `/rg rent <region> <price> <time>`            | Puts a region up for rent (e.g., `1000 7d`).     |
| `/rg unrent <region>`                         | Removes a region from the rental market.         |

**Admin Commands:**

| Command                                       | Description                                      |
| --------------------------------------------- | ------------------------------------------------ |
| `/rg adminclaim <name> <radius>`              | Creates a server-owned admin region.             |
| `/rg flag <region> <flag> <value> [duration]` | Sets a flag for any region.                      |
| `/rg rename <old_name> <new_name>`            | Renames any region.                              |
| `/rg reload`                                  | Reloads the plugin's configuration.              |

---

## Permissions

| Permission                  | Description                                     | Default |
| --------------------------- | ----------------------------------------------- | ------- |
| `meoregion.admin`           | Grants full administrative access.              | `op`    |
| `meoregion.user`            | Grants basic access to claim and manage regions.| `true`  |
| `meoregion.user.find`       | Allows using the `/rg find` command.            | `true`  |
| `meoregion.admin.claim`     | Allows creating admin regions.                  | `op`    |
| `meoregion.admin.tempflag`  | Allows setting temporary flags.                 | `op`    |
| `meoregion.limit.unlimited` | Grants unlimited region claims.                 | `op`    |
| `meoregion.limit.X`         | Sets a player's region limit to `X` chunks.     | `op`    |

---

## Flags & Mod Compatibility

Flags are rules that define what can and cannot be done in a region. You can set them using the `/rg flag` command or via the region's GUI.

### How to Work with Modded Items

The most important feature for hybrid servers is the ability to manage modded items. The `banned-use-items` flag is designed for this.

To block a modded item, you need its **Item ID**. Here's how to find it:
1.  Hold the item in your hand in-game.
2.  Press **F3 + H** on your keyboard. This enables advanced tooltips.
3.  Hover over the item in your inventory. The tooltip will now show the Item ID, usually in the format `modid:item_name` (e.g., `ic2:wrench`).

**Example:**
To block players from using the Wrench from IndustrialCraft 2 in a region named `spawn`, use the following command:

```
/rg flag spawn banned-use-items add ic2:wrench
```

The plugin checks this list against the item a player is trying to use. This method is robust and fully compatible with modded items.

### List of Flags

| Flag                  | Type      | Default Value | Description                                                                 |
| --------------------- | --------- | ------------- | --------------------------------------------------------------------------- |
| `pvp`                 | Boolean   | `false`       | If `true`, allows Player vs. Player combat.                                 |
| `item-frame-destroy`  | Boolean   | `false`       | If `true`, allows non-members to destroy item frames.                       |
| `vehicle-destroy`     | Boolean   | `false`       | If `true`, allows non-members to destroy vehicles (minecarts, boats).       |
| `mob-damage`          | Boolean   | `true`        | If `false`, players in the region will not take damage from monsters.       |
| `animal-damage`       | Boolean   | `true`        | If `false`, players cannot damage passive animals.                          |
| `use`                 | Boolean   | `false`       | If `true`, allows non-members to use doors, buttons, levers.                |
| `chest-access`        | Boolean   | `false`       | If `true`, allows non-members to access chests and other containers.        |
| `tnt`                 | Boolean   | `false`       | If `true`, allows TNT to explode and cause damage.                          |
| `mob-spawning`        | Boolean   | `true`        | If `false`, prevents hostile mobs from spawning naturally.                  |
| `item-drop`           | Boolean   | `true`        | If `false`, prevents players from dropping items.                           |
| `item-pickup`         | Boolean   | `true`        | If `false`, prevents players from picking up items.                         |
| `banned-use-items`    | List      | `[]`          | A list of item IDs (e.g., `minecraft:tnt`) that are forbidden to use.   |
| `fire-spread`         | Boolean   | `false`       | If `true`, allows fire to spread.                                           |
| `leaf-decay`          | Boolean   | `true`        | If `false`, prevents leaves from decaying.                                  |
| `deny-enter`          | Boolean   | `false`       | If `true`, non-members cannot enter the region.                             |
| `greeting`            | String    | `none`        | A message shown to players when they enter the region.                      |
| `farewell`            | String    | `none`        | A message shown to players when they leave the region.                      |
| `keep-inventory`      | Boolean   | `false`       | If `true`, players will not lose their inventory upon death in the region.  |
| `god-mode`            | Boolean   | `false`       | If `true`, players in the region become invincible.                         |
| `flight`              | Boolean   | `false`       | If `true`, allows players to fly in the region (if they have the ability).  |

---

## Configuration

The `config.yml` file allows you to configure the core aspects of the plugin.

- `language`: Set the plugin's language (`en` or `uk`).
- `storage`: Choose the data storage method (`json` or `sqlite`).
- `economy.enabled`: Set to `true` to enable Vault integration.
- `defaults.default-region-limit`: The number of chunks a new player can claim.
- `performance.autosave-interval-minutes`: How often to save region data.
- `backups...`: Configure automatic backups for your region data.

---

**Thank you for using MeoRegion!**
