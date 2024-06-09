package com.example.robots;

import com.example.robots.EntityRobot;
import com.example.robots.Robots;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;

import java.awt.*;

@Mod.EventBusSubscriber(modid = Robots.MODID)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onRegisterEntitiesEvent(RegistryEvent.Register<EntityEntry> event) {
        event.getRegistry().registerAll(
                EntityEntryBuilder.create()
                        .entity(EntityRobot.class)
                        .id(new ResourceLocation(Robots.MODID, "entity_robot"), 0)
                        .name("entity_robot")
                        .tracker(64, 3, false)
                        .build()
        );
    }
}
