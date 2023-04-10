package com.lumaa.act;

import com.lumaa.act.packet.NPCPackets;
import com.lumaa.act.util.NPCEntity;
import com.mojang.authlib.GameProfile;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class ActClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(NPCPackets.APPEAR, (client, handler, buf, responseSender) -> {
            int id = buf.readVarInt();
            UUID uuid = buf.readUuid();
            EntityType<?> entityTypeId = Registries.ENTITY_TYPE.get(buf.readVarInt());
            String name = buf.readString();
            double x = buf.readDouble();
            double y = buf.readDouble();
            double z = buf.readDouble();
            float yaw = (float)(buf.readByte() * 360) / 256.0F;
            float pitch = (float)(buf.readByte() * 360) / 256.0F;
            float headYaw = (float)(buf.readByte() * 360) / 256.0F;
            GameProfile profile = readProfile(buf);
            client.execute(() -> spawnPlayer(id, uuid, entityTypeId, name, x, y, z, yaw, pitch, headYaw, profile));
        });
        ClientPlayNetworking.registerGlobalReceiver(NPCPackets.SET_GAMEPROFILE, (client, handler, buf, responseSender) -> {
            int entityId = buf.readVarInt();
            GameProfile profile = readProfile(buf);
            client.execute(() -> {
                        Entity entity = Objects.requireNonNull(client.world).getEntityById(entityId);
                        if (entity instanceof NPCEntity) {
                            ((NPCEntity) entity).setGameProfile(profile);
                        }
                    }
            );
        });
    }

    @Nullable
    private static GameProfile readProfile(PacketByteBuf buf) {
        boolean hasProfile = buf.readBoolean();
        return hasProfile ? new GameProfile(buf.readUuid(), buf.readString()) : null;
    }

    private <P extends PlayerEntity> void spawnPlayer(int id, UUID uuid, EntityType<?> entityTypeId, String name, double x, double y, double z, float yaw, float pitch, float headYaw, GameProfile profile) {
        ClientWorld world = MinecraftClient.getInstance().world;
        assert world != null;
        @SuppressWarnings("unchecked") EntityType<P> playerType = (EntityType<P>) entityTypeId;
        NPCEntity other = new NPCEntity(world.getServer(), world.getServer().getOverworld(), profile);
        other.setId(id);
        other.setPosition(x, y, z);
        other.updateTrackedPosition(x, y, z);
        other.bodyYaw = headYaw;
        other.prevBodyYaw = headYaw;
        other.headYaw = headYaw;
        other.prevHeadYaw = headYaw;
        other.updatePositionAndAngles(x, y, z, yaw, pitch);
        other.setGameProfile(profile);
        world.addEntity(id, other);
    }

}
