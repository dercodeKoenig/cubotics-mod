package com.example.robots;

import net.minecraft.init.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Robots.MODID, name = Robots.NAME, version = Robots.VERSION)
public class Robots
{
    public static final String MODID = "robots";
    public static final String NAME = "Robots mod";
    public static final String VERSION = "1.0";

    @SidedProxy(clientSide = "com.example.robots.ClientProxy", serverSide = "com.example.robots.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {

    }
}
