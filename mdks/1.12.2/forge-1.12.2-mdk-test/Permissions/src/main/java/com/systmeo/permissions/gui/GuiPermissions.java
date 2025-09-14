package com.systmeo.permissions.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextComponentString;
import com.systmeo.permissions.PermissionsManager;
import com.systmeo.permissions.managers.PermissionManager;

import javax.annotation.Nonnull;
import java.io.IOException;

public class GuiPermissions extends GuiScreen {

    private GuiTextField groupNameField;
    private GuiTextField permissionField;
    private GuiTextField playerNameField;
    private GuiButton createGroupButton;
    private GuiButton addPermissionButton;
    private GuiButton setGroupButton;
    private GuiButton listGroupsButton;
    private GuiButton listUsersButton;
    private GuiButton backButton;

    @Override
    public void initGui() {
        super.initGui();

        // Поля введення
        groupNameField = new GuiTextField(0, mc.fontRenderer, width / 2 - 100, height / 2 - 80, 200, 20);
        permissionField = new GuiTextField(1, mc.fontRenderer, width / 2 - 100, height / 2 - 50, 200, 20);
        playerNameField = new GuiTextField(2, mc.fontRenderer, width / 2 - 100, height / 2 - 20, 200, 20);

        // Кнопки
        createGroupButton = new GuiButton(3, width / 2 - 100, height / 2 + 10, 200, 20, "Створити групу");
        addPermissionButton = new GuiButton(4, width / 2 - 100, height / 2 + 35, 200, 20, "Додати право до групи");
        setGroupButton = new GuiButton(5, width / 2 - 100, height / 2 + 60, 200, 20, "Встановити групу гравцю");
        listGroupsButton = new GuiButton(6, width / 2 - 100, height / 2 + 85, 200, 20, "Показати всі групи");
        listUsersButton = new GuiButton(7, width / 2 - 100, height / 2 + 110, 200, 20, "Показати користувачів групи");
        backButton = new GuiButton(8, width / 2 - 100, height / 2 + 135, 200, 20, "Назад");

        buttonList.add(createGroupButton);
        buttonList.add(addPermissionButton);
        buttonList.add(setGroupButton);
        buttonList.add(listGroupsButton);
        buttonList.add(listUsersButton);
        buttonList.add(backButton);

        groupNameField.setFocused(true);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        // Заголовок
        String title = "§aУправління правами";
        drawCenteredString(mc.fontRenderer, title, width / 2, height / 2 - 110, 0xFFFFFF);

        // Підписи полів
        drawString(mc.fontRenderer, "Назва групи:", width / 2 - 100, height / 2 - 95, 0xFFFFFF);
        drawString(mc.fontRenderer, "Право:", width / 2 - 100, height / 2 - 65, 0xFFFFFF);
        drawString(mc.fontRenderer, "Гравець:", width / 2 - 100, height / 2 - 35, 0xFFFFFF);

        // Поля введення
        groupNameField.drawTextBox();
        permissionField.drawTextBox();
        playerNameField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        PermissionsManager.ensureLoaded();
        PermissionManager pm = PermissionsManager.getPermissionManager();

        if (button == createGroupButton) {
            String groupName = groupNameField.getText().trim();
            if (!groupName.isEmpty()) {
                if (pm.getGroup(groupName) != null) {
                    mc.player.sendMessage(new TextComponentString("§cГрупа вже існує: " + groupName));
                } else {
                    pm.createGroup(groupName);
                    PermissionsManager.saveAll();
                    mc.player.sendMessage(new TextComponentString("§aГрупу створено: " + groupName));
                    groupNameField.setText("");
                }
            }
        } else if (button == addPermissionButton) {
            String groupName = groupNameField.getText().trim();
            String permission = permissionField.getText().trim();
            if (!groupName.isEmpty() && !permission.isEmpty()) {
                if (pm.getGroup(groupName) == null) {
                    mc.player.sendMessage(new TextComponentString("§cГрупа не знайдена: " + groupName));
                } else {
                    PermissionsManager.addPermission(groupName, permission);
                    mc.player.sendMessage(new TextComponentString("§aДодано право '" + permission + "' до групи '" + groupName + "'"));
                    permissionField.setText("");
                }
            }
        } else if (button == setGroupButton) {
            String playerName = playerNameField.getText().trim();
            String groupName = groupNameField.getText().trim();
            if (!playerName.isEmpty() && !groupName.isEmpty()) {
                if (pm.getGroup(groupName) == null) {
                    mc.player.sendMessage(new TextComponentString("§cГрупа не знайдена: " + groupName));
                } else {
                    PermissionsManager.setGroup(mc.world.getPlayerEntityByName(playerName), groupName);
                    mc.player.sendMessage(new TextComponentString("§aГрупу для " + playerName + " встановлено: " + groupName));
                    playerNameField.setText("");
                }
            }
        } else if (button == listGroupsButton) {
            StringBuilder sb = new StringBuilder("§aГрупи: ");
            for (String g : pm.getGroups().keySet()) {
                sb.append(g).append(", ");
            }
            String out = sb.toString();
            if (out.endsWith(", ")) out = out.substring(0, out.length() - 2);
            mc.player.sendMessage(new TextComponentString(out));
        } else if (button == listUsersButton) {
            String groupName = groupNameField.getText().trim();
            if (!groupName.isEmpty()) {
                StringBuilder sb = new StringBuilder("§aКористувачі групи " + groupName + ": ");
                boolean found = false;
                for (com.systmeo.permissions.data.User user : pm.getUsers().values()) {
                    for (com.systmeo.permissions.data.GroupNode gn : user.getGroups()) {
                        if (gn.getGroupName().equalsIgnoreCase(groupName)) {
                            sb.append(user.getUuid().toString()).append(", ");
                            found = true;
                        }
                    }
                }
                String out = sb.toString();
                if (!found) out += "(немає)";
                else if (out.endsWith(", ")) out = out.substring(0, out.length() - 2);
                mc.player.sendMessage(new TextComponentString(out));
            }
        } else if (button == backButton) {
            mc.displayGuiScreen(null);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (groupNameField.isFocused()) {
            groupNameField.textboxKeyTyped(typedChar, keyCode);
        } else if (permissionField.isFocused()) {
            permissionField.textboxKeyTyped(typedChar, keyCode);
        } else if (playerNameField.isFocused()) {
            playerNameField.textboxKeyTyped(typedChar, keyCode);
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        groupNameField.mouseClicked(mouseX, mouseY, mouseButton);
        permissionField.mouseClicked(mouseX, mouseY, mouseButton);
        playerNameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}