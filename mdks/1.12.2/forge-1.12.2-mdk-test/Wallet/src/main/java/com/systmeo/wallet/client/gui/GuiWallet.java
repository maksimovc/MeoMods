package com.systmeo.wallet.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextFormatting;
import com.systmeo.wallet.WalletManager;
import net.minecraft.entity.player.EntityPlayer;
import javax.annotation.Nonnull;

import java.io.IOException;

public class GuiWallet extends GuiScreen {
    private int balance;

    public GuiWallet(EntityPlayer player) {
        this.balance = WalletManager.getBalance(player);
    }

    @Override
    public void initGui() {
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(0, this.width / 2 - 50, this.height / 2 + 10, 100, 20, "Close"));
    }

    @Override
    protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
        if (button.id == 0) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        String title = TextFormatting.GOLD + "Wallet";
        String bal = TextFormatting.WHITE + "Balance: " + TextFormatting.GREEN + balance;
        this.drawCenteredString(this.fontRenderer, title, this.width / 2, this.height / 2 - 30, 0xFFFFFF);
        this.drawCenteredString(this.fontRenderer, bal, this.width / 2, this.height / 2 - 10, 0xFFFFFF);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
