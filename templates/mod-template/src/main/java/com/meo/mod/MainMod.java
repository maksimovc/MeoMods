package com.meo.mod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = "modid", name = "ModName", version = "1.0")
public class MainMod {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        System.out.println("Mod initialized");
    }
}
