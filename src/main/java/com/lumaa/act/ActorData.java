package com.lumaa.act;

import com.lumaa.act.entity.ActorEntity;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.util.telemetry.TelemetrySender;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.World;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
public class ActorData implements ModInitializer {
    private static final String ACTORS_FILE = "ActorData.nbt";
    private static final String WORLD_DATA_FILE="data/"; //Todo
    private  String worldName;
    /*
    Reading and writing the data and spawning
     */
    @Override
    public void onInitialize() {
        // Register the player join callback
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Load the actors from the file
            NbtCompound nbt = readNbtFromFile(Objects.requireNonNull(server.getWorld(World.OVERWORLD)));
            if (nbt != null) {
                for (ServerWorld world : server.getWorlds()) {
                    worldName = String.valueOf((world.getServer().getSavePath(WorldSavePath.ROOT).toFile().getName()));
                    List<ActorEntity> actors = loadActorEntities(nbt, server, worldName);
                    System.out.println("Loaded actors for world " + worldName + ": " + actors);

                    // Add the actors to the world and send S2C packets
                    for (ActorEntity actor : actors) {
                        ServerWorld actorWorld = server.getWorld(actor.world.getRegistryKey());
                        assert actorWorld != null;
                        actor.networkHandler = new ServerPlayNetworkHandler(server, new ClientConnection(NetworkSide.CLIENTBOUND), actor);
                        actor.sendProfileUpdatePacket();
                        actorWorld.spawnEntity(actor);
                        handler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, actor));
                        handler.sendPacket(actor.createSpawnPacket());
                    }
                }
            }
        });

        // Register the server stop callback
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // Save all actors present in all worlds of your server
            NbtCompound nbt = new NbtCompound();
            saveAllActors(server,nbt);
            // Save the NBT data to a file
            for (ServerWorld world:server.getWorlds()) {
                writeNbtToFile(nbt, world);
            }
            File worldDirectory = Objects.requireNonNull(server.getWorld(World.OVERWORLD)).getServer().getSavePath(WorldSavePath.ROOT).toFile();
            File dataDirectory = new File(worldDirectory, "data");
            File file = new File(dataDirectory, ACTORS_FILE);
            System.out.println("Writing nbt to: "+ file );
        });

     ActMod.print("Actor Data Saving and Loading Initialised");
    }

    /*
       Saving all the actors in the world using the getEntitiesByType method.
       Also calling saveActorEntities() in this method and passing List<ActorEntities> actorsInWorld as first parameter
     */
    private void saveAllActors(MinecraftServer server,NbtCompound nbt) {
        for (ServerWorld world : server.getWorlds()) {
            worldName= String.valueOf(world.getServer().getSavePath(WorldSavePath.ROOT).toFile().getName());
            List<ActorEntity> actorsInWorld = world.getEntitiesByType(EntityType.PLAYER, (entity) -> entity instanceof ActorEntity).stream().map(entity -> (ActorEntity) entity).collect(Collectors.toList());
            saveActorEntities(actorsInWorld, nbt,worldName);

            // Save the NBT data to a file
            writeNbtToFile(nbt,Objects.requireNonNull(server.getWorld(World.OVERWORLD)));
        }
    }

    /*
      Save all the actors, passed on by the saveAllActors method, in the nbt file
     */
    private void saveActorEntities(List<ActorEntity> actors, NbtCompound nbt,String worldName) {
        String key = "Actors_" + worldName;
        NbtList actorList;
        if (nbt.contains(key)) {
            actorList = nbt.getList(key, 10);
        } else {
            actorList = new NbtList();
        }
        for (ActorEntity actor : actors) {
            NbtCompound actorNbt = new NbtCompound();
            actorNbt.putDouble("X", actor.getX());
            actorNbt.putDouble("Y", actor.getY());
            actorNbt.putDouble("Z", actor.getZ());
            actorNbt.putFloat("Pitch", actor.getPitch());
            actorNbt.putFloat("Yaw", actor.getYaw());
            actorNbt.putFloat("Health", actor.getHealth());
            actorNbt.putString("World", actor.getWorld().toString());
            actorNbt.putString("Dimension",actor.world.getRegistryKey().getValue().toString());
            actorNbt.putString("Name", actor.gameProfile.getName());
            actorNbt.putUuid("UUID", actor.getUuid());
            actorNbt.putBoolean("OnFire",actor.isOnFire());

            // Save the inventory
            NbtList inventoryList = new NbtList();
            for (int i = 0; i < actor.getInventory().size(); i++) {
                ItemStack stack = actor.getInventory().getStack(i);
                if (!stack.isEmpty()) {
                    NbtCompound stackNbt = new NbtCompound();
                    stackNbt.putInt("Slot", i);
                    stack.writeNbt(stackNbt);
                    inventoryList.add(stackNbt);
                }
            }
            actorNbt.put("Inventory", inventoryList);
            actorList.add(actorNbt);
        }

        nbt.put(key, actorList);
    }

    /*
    Load All the actors and their info in the nbt file
    Sets position,yaw and pitch of the actor
    Sets Health of the actor
    Checks if the actor world and Dimension saved in the nbt file and the world and Dimension the Actor is being currently loaded is same
     */
    private List<ActorEntity> loadActorEntities(NbtCompound nbt, MinecraftServer server,String WorldName) {
        List<ActorEntity> actors = new ArrayList<>();
        String key="Actors_"+WorldName;
        if (nbt.contains(key)) {
            NbtList actorList = nbt.getList(key, 10);

            for (int i = 0; i < actorList.size(); i++) {
                NbtCompound actorNtb = actorList.getCompound(i);
                double x = actorNtb.getDouble("X");
                double y = actorNtb.getDouble("Y");
                double z = actorNtb.getDouble("Z");
                float pitch = actorNtb.getFloat("Pitch");
                float yaw = actorNtb.getFloat("Yaw");
                float health = actorNtb.getFloat("Health");
                String ActorWorld = actorNtb.getString("World");
                String ActorDimension = actorNtb.getString("Dimension");
                String name = actorNtb.getString("Name");
                UUID uuid = actorNtb.getUuid("UUID");
                boolean onfire=actorNtb.getBoolean("OnFire");

                GameProfile profile = new GameProfile(uuid, name);
                ServerWorld world = switch (ActorDimension) {
                    case "minecraft:the_nether" -> server.getWorld(World.NETHER);
                    case "minecraft:the_end" -> server.getWorld(World.END);
                    default -> server.getWorld(World.OVERWORLD);
                };
                ActorEntity actor = new ActorEntity(server, world, profile);
                actor.refreshPositionAndAngles(x, y, z, yaw, pitch);
                actor.setHealth(health);
                actor.setOnFire(onfire);

                System.out.println("ActorDimension: " + ActorDimension);
                System.out.println("Actor.world.getRegistryKey().getValue().toString(): " +  actor.world.getRegistryKey().getValue().toString());

                //Check for world and Dimension
                if (!Objects.equals(ActorWorld, actor.getWorld().toString())) continue;
                if (!Objects.equals(ActorDimension, actor.world.getRegistryKey().getValue().toString())) continue;

                // Load the inventory
                NbtList inventoryList = actorNtb.getList("Inventory", 10);
                for (int j = 0; j < inventoryList.size(); j++) {
                    NbtCompound stackNtb = inventoryList.getCompound(j);
                    int slot = stackNtb.getInt("Slot");
                    ItemStack stack = ItemStack.fromNbt(stackNtb);
                    if (slot >= 0 && slot < actor.getInventory().size()) {
                        actor.getInventory().setStack(slot, stack);
                    }
                }
                assert world != null;
                if (world.getEntity(uuid) == null) {
                    actors.add(actor);
                }
            }
        }
        System.out.println("Actors in LoadActors: "+actors);
        return actors;
    }

    //Writes in NBT file
    private void writeNbtToFile(NbtCompound nbt, ServerWorld world) {
        File worldDirectory = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
        File dataDirectory = new File(worldDirectory, "data");
        File file = new File(dataDirectory, ACTORS_FILE);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            NbtIo.writeCompressed(nbt, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Reads NBT file
    private NbtCompound readNbtFromFile(ServerWorld world) {
        File worldDirectory = world.getServer().getSavePath(WorldSavePath.ROOT).toFile();
        File dataDirectory = new File(worldDirectory, "data");
        File file = new File(dataDirectory, ACTORS_FILE);
        try (FileInputStream fis = new FileInputStream(file)) {
            return NbtIo.readCompressed(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

