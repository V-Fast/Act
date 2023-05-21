package com.lumaa.act;

import com.lumaa.act.command.ActorCommand;
import com.lumaa.act.entity.ActorEntity;
import com.lumaa.act.item.ActItems;
import com.lumaa.act.pathfinding.Path;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ActMod implements ModInitializer {
    public static final String MODID = "act";
    public static final Logger logger = LoggerFactory.getLogger(MODID);
    ActorCommand npc = new ActorCommand();
    @Override
    public void onInitialize() {
        npc.register();
        ActItems.registerModItems();
        ServerPlayConnectionEvents.DISCONNECT.register(( sender, server)->Path.stopMoving());

        // print mod mini-motto
        print("Actors are on stage");
    }

    public static void print(String message) {
        logger.info("[Act] " + message);
    }
}