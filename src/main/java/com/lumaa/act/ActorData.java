package com.lumaa.act;

import com.lumaa.act.entity.ActorEntity;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ActorData {
    private static final String ACTOR_DATA_FILE = "ActorData.dat";
    private static final String WORLD_DATA_DIR = "data/";

    public static void saveActorData(List<ActorEntity> actors) {
        if (actors.isEmpty()) return;

        NbtList nbtList = new NbtList();
        for (ActorEntity actor : actors) {
            NbtCompound nbt = new NbtCompound();
            nbt.putUuid("UUID", actor.getUuid());
            nbt.putString("Name", actor.getName().getString());
            nbt.putDouble("X", actor.getX());
            nbt.putDouble("Y", actor.getY());
            nbt.putDouble("Z", actor.getZ());
            nbt.putFloat("Yaw", actor.getYaw());
            nbt.putFloat("Pitch", actor.getPitch());
            nbt.putString("World", actor.getWorld().toString());
            nbt.putFloat("Health",actor.getHealth());
            NbtList inventoryNbt = new NbtList();
            for (int i = 0; i < actor.getInventory().size(); i++) {
                ItemStack stack = actor.getInventory().getStack(i);
                NbtCompound itemNbt = new NbtCompound();
                stack.writeNbt(itemNbt);
                inventoryNbt.add(itemNbt);
            }
            nbt.put("Inventory", inventoryNbt);
            nbtList.add(nbt);
        }

        NbtCompound root = new NbtCompound();
        root.put("Actors", nbtList);
        try {

            File file = new File(actors.get(0).getServer().getRunDirectory(), WORLD_DATA_DIR + ACTOR_DATA_FILE);
            NbtIo.writeCompressed(root, new FileOutputStream(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadActorData(MinecraftServer server, PacketSender packetSender, ServerWorld world) {
        List<ActorEntity> actors = new ArrayList<>();
        try {
            File file = new File(server.getRunDirectory(), WORLD_DATA_DIR + ACTOR_DATA_FILE);
            if (!file.exists()) return;
            NbtCompound root = NbtIo.readCompressed(new FileInputStream(file));
            NbtList nbtList = root.getList("Actors", 10);
            for (int i = 0; i < nbtList.size(); i++) {
                NbtCompound nbt = nbtList.getCompound(i);
                UUID uuid = nbt.getUuid("UUID");
                String name = nbt.getString("Name");
                double x = nbt.getDouble("X");
                double y = nbt.getDouble("Y");
                double z = nbt.getDouble("Z");
                float yaw = nbt.getFloat("Yaw");
                float pitch = nbt.getFloat("Pitch");
                float health =nbt.getFloat("Health");
                String actorWorld = nbt.getString("World");

                GameProfile gameProfile = new GameProfile(uuid, name);
                ActorEntity actorEntity = new ActorEntity(server, world, gameProfile);
                actorEntity.refreshPositionAndAngles(x, y, z, yaw, pitch);
                actors.add(actorEntity);
                System.out.println("Actors: " + actors);

                if (!Objects.equals(actorEntity.getWorld().toString(), actorWorld)) continue;

                packetSender.sendPacket(actorEntity.createSpawnPacket());
                packetSender.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, actorEntity));

                for (int j = 0; j < actors.size(); j++) {
                    if (actorEntity.getHealth()<=0) continue;
                    actorEntity.setHealth(health);
                    world.spawnEntity(actorEntity);
                    if (nbt.contains("Inventory")) {
                        NbtList inventoryNbt = nbt.getList("Inventory", 10);
                        for (int k = 0; k < inventoryNbt.size(); k++) {
                            NbtCompound itemNbt = inventoryNbt.getCompound(i);
                            ItemStack stack = ItemStack.fromNbt(itemNbt);
                            actorEntity.getInventory().setStack(i, stack);
                        }
                    }
                    System.out.println("Spawned: " + name);

                    actorEntity.teleport(world, x, y + 0.1d, z, yaw, pitch);

                    System.out.println("Teleported: " + world + ", " + y);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

