package com.lumaa.act;

import com.lumaa.act.entity.ActorEntity;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
public class ActorData implements ModInitializer {
    private static final String ACTORS_FILE = "ActorData.nbt";
    private static final String WORLD_DATA_FILE="data/"; //Todo

    /*
    Reading and writing the data and spawning
     */
    @Override
    public void onInitialize() {
        // Register the player join callback
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Load the actors from the file
            NbtCompound nbt = readNbtFromFile(ACTORS_FILE);
            if (nbt != null) {
                List<ActorEntity> actors = loadActorEntities(nbt, server);
                System.out.println("Actors: "+actors);
                // Add the actors to the world and send S2C packets
                for (ActorEntity actor : actors) {
                    ServerWorld world = server.getWorld(actor.world.getRegistryKey());
                    assert world != null;
                    actor.networkHandler = new ServerPlayNetworkHandler(server, new ClientConnection(NetworkSide.CLIENTBOUND), actor);
                    actor.sendProfileUpdatePacket();
                    handler.sendPacket(actor.createSpawnPacket());
                    handler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, actor));
                    world.spawnEntity(actor);
                }

            }
        });

        // Register the server stop callback
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // Save all actors present in all worlds of your server
            NbtCompound nbt = new NbtCompound();
            saveAllActors(server,nbt);
            // Save the NBT data to a file
            writeNbtToFile(ACTORS_FILE, nbt);
            System.out.println("Writing nbt to: "+ACTORS_FILE);
        });

     ActMod.print("Actor Data Saving and Loading Initialised");
    }

    /*
       Saving all the actors in the world using the getEntitiesByType method.
       Also calling saveActorEntities() in this method and passing List<ActorEntities> actorsInWorld as first parameter
     */
    private void saveAllActors(MinecraftServer server,NbtCompound nbt) {
        List<ActorEntity> actorsInWorlds = new ArrayList<>();
        for (ServerWorld world : server.getWorlds()) {
            List<ActorEntity> actorsInWorld = world.getEntitiesByType(EntityType.PLAYER, (entity) -> entity instanceof ActorEntity).stream().map(entity -> (ActorEntity) entity).collect(Collectors.toList());
            actorsInWorlds.addAll(actorsInWorld);
        }
        System.out.println("ActorsInWorld: "+actorsInWorlds);
        saveActorEntities(actorsInWorlds, nbt);
    }

    /*
      Save all the actors, passed on by the saveAllActors method, in the nbt file
     */
    private void saveActorEntities(List<ActorEntity> actors, NbtCompound nbt) {
        NbtList actorList = new NbtList();

        for (ActorEntity actor : actors) {
            NbtCompound actorNbt = new NbtCompound();
            actorNbt.putDouble("X", actor.getX());
            actorNbt.putDouble("Y", actor.getY());
            actorNbt.putDouble("Z", actor.getZ());
            actorNbt.putFloat("Pitch", actor.getPitch());
            actorNbt.putFloat("Yaw", actor.getYaw());
            actorNbt.putFloat("Health", actor.getHealth());
            actorNbt.putString("World", actor.getWorld().toString());
            actorNbt.putString("Name", actor.gameProfile.getName());
            actorNbt.putUuid("UUID", actor.getUuid());

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

        nbt.put("Actors", actorList);
    }

    /*
    Load All the actors and their info in the nbt file
    Sets position,yaw and pitch of the actor
    Sets Health of the actor
    Checks if the actor world saved in the nbt file and the world the Actor is being currently loaded is same
     */
    private List<ActorEntity> loadActorEntities(NbtCompound nbt, MinecraftServer server) {
        List<ActorEntity> actors = new ArrayList<>();
        NbtList actorList = nbt.getList("Actors", 10);

        for (int i = 0; i < actorList.size(); i++) {
            NbtCompound actorNtb = actorList.getCompound(i);
            double x = actorNtb.getDouble("X");
            double y = actorNtb.getDouble("Y");
            double z = actorNtb.getDouble("Z");
            float pitch = actorNtb.getFloat("Pitch");
            float yaw = actorNtb.getFloat("Yaw");
            float health = actorNtb.getFloat("Health");
            String Actorworld=actorNtb.getString("World");
            String name = actorNtb.getString("Name");
            UUID uuid = actorNtb.getUuid("UUID");

            GameProfile profile = new GameProfile(uuid, name);
            ActorEntity actor = new ActorEntity(server, server.getOverworld(), profile);
            actor.refreshPositionAndAngles(x, y, z, yaw, pitch);
            actor.setHealth(health);

            if (!(Objects.equals(Actorworld, actor.getWorld().toString()))) continue;

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

            actors.add(actor);
        }
        System.out.println("Actors in LoadActors: "+actors);
        return actors;
    }

    //Writes in NBT file
    private void writeNbtToFile(String fileName, NbtCompound nbt) {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            NbtIo.writeCompressed(nbt, fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Reads NBT file
    private NbtCompound readNbtFromFile(String fileName) {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            return NbtIo.readCompressed(fis);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

