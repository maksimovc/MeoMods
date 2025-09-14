package com.systmeo.wallet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import com.systmeo.wallet.client.gui.GuiWallet;

public class GuiHandler implements IGuiHandler {
    public static final int WALLET_GUI_ID = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null; // No container for simple balance view
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == WALLET_GUI_ID) {
            return new GuiWallet(player);
        }
        return null;
    }

    public static void openWalletGui(EntityPlayer player) {
        player.openGui(Wallet.instance, WALLET_GUI_ID, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
    }
}
