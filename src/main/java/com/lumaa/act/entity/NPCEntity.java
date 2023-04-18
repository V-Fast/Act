package com.lumaa.act.entity;

import com.lumaa.act.packet.NPCPackets;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class NPCEntity extends ServerPlayerEntity {
    public GameProfile gameProfile;

    public NPCEntity(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(server, world, profile);
        this.gameProfile = profile;
        this.networkHandler =  new ServerPlayNetworkHandler(world.getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), this);
        this.writeToSpawnPacket();
        this.sendProfileUpdatePacket();
        this.setHealth(1f);
        setAllPartsVisible(true);
    }

    @Override
    public void tick() {
        this.closeHandledScreen();
        super.tick();
        this.tickFallStartPos();
        this.playerTick();
    }

    public void setAllPartsVisible(boolean visible) {
        setVisiblePart(PlayerModelPart.CAPE, visible);
        setVisiblePart(PlayerModelPart.HAT, visible);
        setVisiblePart(PlayerModelPart.JACKET, visible);
        setVisiblePart(PlayerModelPart.LEFT_PANTS_LEG, visible);
        setVisiblePart(PlayerModelPart.LEFT_SLEEVE, visible);
        setVisiblePart(PlayerModelPart.RIGHT_PANTS_LEG, visible);
        setVisiblePart(PlayerModelPart.RIGHT_SLEEVE, visible);
    }

    public void setVisiblePart(PlayerModelPart modelPart, boolean visible) {
        int byt = this.getDataTracker().get(PLAYER_MODEL_PARTS);

        byte newByt;
        if (visible) {
            newByt = (byte) (byt + modelPart.getBitFlag());
        } else {
            newByt = (byte) (modelPart.getBitFlag() - byt);
        }

        this.dataTracker.set(PLAYER_MODEL_PARTS, newByt);
    }

    public void setInHand(ItemStack itemStack) {
        PlayerInventory inv = this.getInventory();
        inv.setStack(inv.selectedSlot, itemStack);
    }

    public void setHotbar(int hotbar) {
        this.getInventory().selectedSlot = MathHelper.clamp(hotbar, 0, 9);
    }

    public void copyInventoryFrom(PlayerInventory playerInventory) {
        PlayerInventory npcInventory = this.getInventory();

        for (int i = 0; i < playerInventory.size(); i++) {
            npcInventory.setStack(i, playerInventory.getStack(i));
        }
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    protected void writeToSpawnPacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.getId());
        buf.writeUuid(this.gameProfile.getId());
        buf.writeVarInt(Registries.ENTITY_TYPE.getRawId(this.getType()));
        buf.writeString(this.getGameProfile().getName());
        buf.writeDouble(this.getX());
        buf.writeDouble(this.getY());
        buf.writeDouble(this.getZ());
        buf.writeByte((byte)((int)(this.getYaw() * 256.0F / 360.0F)));
        buf.writeByte((byte)((int)(this.getPitch() * 256.0F / 360.0F)));
        buf.writeByte((byte)((int)(this.headYaw * 256.0F / 360.0F)));
        writeProfile(buf, this.getGameProfile());
    }

    public void sendProfileUpdatePacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(this.getId());
        writeProfile(buf, this.getGameProfile());

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(NPCPackets.SET_GAMEPROFILE, buf);

        for (ServerPlayerEntity e : PlayerLookup.tracking(this)) {
            e.networkHandler.sendPacket(packet);
        }
    }

    public static void writeProfile(PacketByteBuf buf, @Nullable GameProfile profile) {
        buf.writeBoolean(profile != null);

        if (profile != null) {
            buf.writeUuid(profile.getId());
            buf.writeString(profile.getName());
        }
    }

    @Override
    public Text getDisplayName() {
        return Text.literal(this.gameProfile.getName());
    }

    @Override
    public boolean canResetTimeBySleeping() {
        return true;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public GameProfile getGameProfile() {
        return gameProfile;
    }
}