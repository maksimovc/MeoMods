package com.systmeo.permissions.commands;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.entity.player.EntityPlayer;
import com.systmeo.permissions.PermissionsManager;
import com.systmeo.permissions.managers.PermissionManager;

import javax.annotation.Nonnull;

public class CommandPermissions extends CommandBase {

    @Override
    public String getName() {
        return "permissions";
    }

    @Override
    public @Nonnull String getUsage(@Nonnull ICommandSender sender) {
        return "/permissions help";
    }

    private void showHelp(@Nonnull ICommandSender sender) {
        sender.sendMessage(new TextComponentString("§aДоступні команди Permissions:"));
        sender.sendMessage(new TextComponentString("§2=== Команди для груп ==="));
        sender.sendMessage(new TextComponentString("/permissions creategroup <name> - Створити нову групу"));
        sender.sendMessage(new TextComponentString("/permissions deletegroup <name> - Видалити групу"));
        sender.sendMessage(new TextComponentString("/permissions listgroups - Показати всі групи"));
        sender.sendMessage(new TextComponentString("§2=== Команди для користувачів ==="));
        sender.sendMessage(new TextComponentString("/permissions setgroup <player> <group> - Встановити основну групу"));
        sender.sendMessage(new TextComponentString("/permissions adduser <player> <group> - Додати гравця до групи"));
        sender.sendMessage(new TextComponentString("/permissions removeuser <player> <group> - Видалити гравця з групи"));
        sender.sendMessage(new TextComponentString("/permissions listusers <group> - Показати користувачів групи"));
        sender.sendMessage(new TextComponentString("§2=== Команди для прав ==="));
        sender.sendMessage(new TextComponentString("/permissions addperm <group> <perm> - Додати право до групи"));
        sender.sendMessage(new TextComponentString("/permissions removeperm <group> <perm> - Видалити право з групи"));
        sender.sendMessage(new TextComponentString("/permissions listperms <group> - Показати права групи"));
        sender.sendMessage(new TextComponentString("/permissions hasperm <perm> - Перевірити наявність права"));
        sender.sendMessage(new TextComponentString("§2=== Команди для треків ==="));
        sender.sendMessage(new TextComponentString("/permissions createtrack <name> - Створити трек підвищення"));
        sender.sendMessage(new TextComponentString("/permissions deletetrack <name> - Видалити трек"));
        sender.sendMessage(new TextComponentString("/permissions listtracks - Показати всі треки"));
        sender.sendMessage(new TextComponentString("/permissions addgrouptrack <track> <group> - Додати групу до треку"));
        sender.sendMessage(new TextComponentString("/permissions removegrouptrack <track> <group> - Видалити групу з треку"));
        sender.sendMessage(new TextComponentString("/permissions promote <player> <track> - Підвищити гравця в треку"));
        sender.sendMessage(new TextComponentString("/permissions gui - Відкрити графічний інтерфейс"));
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) {
        // Show help if no arguments or help command
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            showHelp(sender);
            return;
        }

        PermissionsManager.ensureLoaded();
        PermissionManager pm = PermissionsManager.getPermissionManager();

        // createtrack <name>
        if (args[0].equalsIgnoreCase("createtrack") && args.length == 2) {
            if (pm.getTrack(args[1]) != null) {
                sender.sendMessage(new TextComponentString("§cТрек вже існує: " + args[1]));
                return;
            }
            pm.createTrack(args[1]);
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aТрек створено: " + args[1]));
            return;
        }

        // deletetrack <name>
        if (args[0].equalsIgnoreCase("deletetrack") && args.length == 2) {
            if (pm.getTrack(args[1]) == null) {
                sender.sendMessage(new TextComponentString("§cТрек не знайдено: " + args[1]));
                return;
            }
            pm.deleteTrack(args[1]);
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aТрек видалено: " + args[1]));
            return;
        }

        // listtracks
        if (args[0].equalsIgnoreCase("listtracks")) {
            StringBuilder sb = new StringBuilder("§aСписок треків: ");
            for (String t : pm.getTracks().keySet()) {
                sb.append(t).append(", ");
            }
            String out = sb.toString();
            if (out.endsWith(", ")) out = out.substring(0, out.length() - 2);
            sender.sendMessage(new TextComponentString(out));
            return;
        }

        // addgrouptrack <track> <group>
        if (args[0].equalsIgnoreCase("addgrouptrack") && args.length == 3) {
            com.systmeo.permissions.data.Track track = pm.getTrack(args[1]);
            if (track == null) {
                sender.sendMessage(new TextComponentString("§cТрек не знайдено: " + args[1]));
                return;
            }
            if (track.getGroups().contains(args[2].toLowerCase())) {
                sender.sendMessage(new TextComponentString("§eГрупа вже є у треку: " + args[2]));
                return;
            }
            track.appendGroup(args[2]);
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aГрупу " + args[2] + " додано до треку " + args[1]));
            return;
        }

        // removegrouptrack <track> <group>
        if (args[0].equalsIgnoreCase("removegrouptrack") && args.length == 3) {
            com.systmeo.permissions.data.Track track = pm.getTrack(args[1]);
            if (track == null) {
                sender.sendMessage(new TextComponentString("§cТрек не знайдено: " + args[1]));
                return;
            }
            if (!track.getGroups().contains(args[2].toLowerCase())) {
                sender.sendMessage(new TextComponentString("§eГрупи немає у треку: " + args[2]));
                return;
            }
            track.removeGroup(args[2]);
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aГрупу " + args[2] + " видалено з треку " + args[1]));
            return;
        }

        // promote <player> <track>
        if (args[0].equalsIgnoreCase("promote") && args.length == 3) {
            EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
                sender.sendMessage(new TextComponentString("§cГравець не знайдений."));
                return;
            }
            String nextGroup = pm.promoteUser(target.getUniqueID(), args[2]);
            if (nextGroup != null) {
                pm.invalidateCache(target.getUniqueID());
                PermissionsManager.saveAll();
                sender.sendMessage(new TextComponentString("§aГравця " + target.getName() + " підвищено до групи " + nextGroup + " у треку " + args[2]));
            } else {
                sender.sendMessage(new TextComponentString("§eПідвищення неможливе (останній рівень або не знайдено трек)"));
            }
            return;
        }

        // creategroup <name>
        if (args[0].equalsIgnoreCase("creategroup") && args.length == 2) {
            String group = args[1];
            if (pm.getGroup(group) != null) {
                sender.sendMessage(new TextComponentString("§cГрупа вже існує: " + group));
                return;
            }
            pm.createGroup(group);
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aГрупу створено: " + group));
            return;
        }

        // deletegroup <name>
        if (args[0].equalsIgnoreCase("deletegroup") && args.length == 2) {
            String group = args[1];
            if (pm.getGroup(group) == null) {
                sender.sendMessage(new TextComponentString("§cГрупа не знайдена: " + group));
                return;
            }
            pm.deleteGroup(group);
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aГрупу видалено: " + group));
            return;
        }

        // listgroups
        if (args[0].equalsIgnoreCase("listgroups")) {
            StringBuilder sb = new StringBuilder("§aСписок груп: ");
            for (String g : pm.getGroups().keySet()) {
                sb.append(g).append(", ");
            }
            String out = sb.toString();
            if (out.endsWith(", ")) out = out.substring(0, out.length() - 2);
            sender.sendMessage(new TextComponentString(out));
            return;
        }

        // setgroup <player> <group>
        if (args[0].equalsIgnoreCase("setgroup") && args.length == 3) {
            EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
                sender.sendMessage(new TextComponentString("§cГравець не знайдений."));
                return;
            }
            if (pm.getGroup(args[2]) == null) {
                sender.sendMessage(new TextComponentString("§cГрупа не знайдена: " + args[2]));
                return;
            }
            PermissionsManager.setGroup(target, args[2]);
            sender.sendMessage(new TextComponentString("§aГрупу для " + target.getName() + " встановлено: " + args[2]));
            return;
        }

        // addperm <group> <perm>
        if (args[0].equalsIgnoreCase("addperm") && args.length == 3) {
            if (pm.getGroup(args[1]) == null) {
                sender.sendMessage(new TextComponentString("§cГрупа не знайдена: " + args[1]));
                return;
            }
            PermissionsManager.addPermission(args[1], args[2]);
            sender.sendMessage(new TextComponentString("§aДодано право '" + args[2] + "' до групи '" + args[1] + "'"));
            return;
        }

        // removeperm <group> <perm>
        if (args[0].equalsIgnoreCase("removeperm") && args.length == 3) {
            if (pm.getGroup(args[1]) == null) {
                sender.sendMessage(new TextComponentString("§cГрупа не знайдена: " + args[1]));
                return;
            }
            PermissionsManager.removePermission(args[1], args[2]);
            sender.sendMessage(new TextComponentString("§aВидалено право '" + args[2] + "' з групи '" + args[1] + "'"));
            return;
        }

        // hasperm <perm>
        if (args[0].equalsIgnoreCase("hasperm") && args.length == 2) {
            if (sender instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) sender;
                boolean has = PermissionsManager.hasPermission(player, args[1]);
                sender.sendMessage(new TextComponentString("§aУ вас " + (has ? "є" : "немає") + " право: " + args[1]));
            } else {
                sender.sendMessage(new TextComponentString("§cЦя команда доступна тільки для гравців"));
            }
            return;
        }

        // adduser <player> <group>
        if (args[0].equalsIgnoreCase("adduser") && args.length == 3) {
            EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
                sender.sendMessage(new TextComponentString("§cГравець не знайдений."));
                return;
            }
            if (pm.getGroup(args[2]) == null) {
                sender.sendMessage(new TextComponentString("§cГрупа не знайдена: " + args[2]));
                return;
            }
            com.systmeo.permissions.data.User user = pm.getOrCreateUser(target.getUniqueID());
            user.addGroup(args[2], 0);
            pm.invalidateCache(target.getUniqueID());
            PermissionsManager.saveAll();
            sender.sendMessage(new TextComponentString("§aГравця " + target.getName() + " додано до групи " + args[2]));
            return;
        }

        // removeuser <player> <group>
        if (args[0].equalsIgnoreCase("removeuser") && args.length == 3) {
            EntityPlayer target = server.getPlayerList().getPlayerByUsername(args[1]);
            if (target == null) {
                sender.sendMessage(new TextComponentString("§cГравець не знайдений."));
                return;
            }
            com.systmeo.permissions.data.User user = pm.getOrCreateUser(target.getUniqueID());
            boolean removed = user.removeGroup(args[2]);
            pm.invalidateCache(target.getUniqueID());
            PermissionsManager.saveAll();
            if (removed) {
                sender.sendMessage(new TextComponentString("§aГравця " + target.getName() + " видалено з групи " + args[2]));
            } else {
                sender.sendMessage(new TextComponentString("§eГравець не був у групі " + args[2]));
            }
            return;
        }

        // listusers <group>
        if (args[0].equalsIgnoreCase("listusers") && args.length == 2) {
            String group = args[1].toLowerCase();
            StringBuilder sb = new StringBuilder("§aКористувачі групи " + group + ": ");
            boolean found = false;
            for (com.systmeo.permissions.data.User user : pm.getUsers().values()) {
                for (com.systmeo.permissions.data.GroupNode gn : user.getGroups()) {
                    if (gn.getGroupName().equalsIgnoreCase(group)) {
                        sb.append(user.getUuid().toString()).append(", ");
                        found = true;
                    }
                }
            }
            String out = sb.toString();
            if (!found) out += "(немає)";
            else if (out.endsWith(", ")) out = out.substring(0, out.length() - 2);
            sender.sendMessage(new TextComponentString(out));
            return;
        }

        // listperms <group>
        if (args[0].equalsIgnoreCase("listperms") && args.length == 2) {
            com.systmeo.permissions.data.Group g = pm.getGroup(args[1]);
            if (g == null) {
                sender.sendMessage(new TextComponentString("§cГрупа не знайдена: " + args[1]));
                return;
            }
            StringBuilder sb = new StringBuilder("§aПрава групи " + args[1] + ": ");
            boolean found = false;
            for (com.systmeo.permissions.data.PermissionNode pn : g.getPermissions()) {
                sb.append(pn.getPermission()).append(", ");
                found = true;
            }
            String out = sb.toString();
            if (!found) out += "(немає)";
            else if (out.endsWith(", ")) out = out.substring(0, out.length() - 2);
            sender.sendMessage(new TextComponentString(out));
            return;
        }

        // gui
        if (args[0].equalsIgnoreCase("gui")) {
            if (sender instanceof EntityPlayer) {
                // TODO: Відкрити GUI на клієнті через packet
                sender.sendMessage(new TextComponentString("§eGUI ще не реалізований на сервері. Структура GUI створена в коді."));
                sender.sendMessage(new TextComponentString("§aДоступні команди: /permissions help"));
            } else {
                sender.sendMessage(new TextComponentString("§cЦю команду можна використовувати тільки гравцям"));
            }
            return;
        }

        sender.sendMessage(new TextComponentString("§cНевірна команда або аргументи. Використайте /permissions help"));
    }
}