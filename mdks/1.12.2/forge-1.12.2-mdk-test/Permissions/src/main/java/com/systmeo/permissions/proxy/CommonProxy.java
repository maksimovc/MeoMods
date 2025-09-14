package com.systmeo.permissions.proxy;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import com.systmeo.permissions.commands.CommandPermissions;

public class CommonProxy {
    public void preInit(FMLPreInitializationEvent event) {}
    public void init(FMLInitializationEvent event) {}
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandPermissions());
    }
    public void serverStopping(FMLServerStoppingEvent event) {}
}
