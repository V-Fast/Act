package com.lumaa.act.entity;

import com.lumaa.act.ai.ActorAI;
import com.lumaa.act.packet.ActorPackets;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.entity.PlayerModelPart;
import net.minecraft.entity.player.PlayerEntity;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ActorEntity extends ServerPlayerEntity {
    public GameProfile gameProfile;
    private PlayerEntity player;
    private ActorAI ai;
    public boolean isFollowingPlayer = false;
    public boolean isFollowingBlock = false;
    public boolean isStuck = false;
    public volatile boolean keepMovingToPlayer = true;
    public volatile boolean keepMovingToBlock = true;
    public Thread playermovementThread = null;
    public Thread positionmovementThread = null;
    private final double runSpeed = 0.2;
    private final double walkSpeed = 0.14;
    private final double sneakSpeed = 0.04;
    public final double swimSpeed = 0.30;

    public ActorEntity(MinecraftServer server, ServerWorld world, GameProfile profile) {
        super(server, world, profile);
        this.gameProfile = profile;
        this.networkHandler =  new ServerPlayNetworkHandler(world.getServer(), new ClientConnection(NetworkSide.CLIENTBOUND), this);
        this.writeToSpawnPacket();
        this.sendProfileUpdatePacket();
        this.setHealth(20f);
        this.ai = new ActorAI(this);
        setAllPartsVisible(true);
    }

    @Override
    public void tick() {
        this.closeHandledScreen();
        super.tick();
        this.tickFallStartPos();
        this.playerTick();
        this.ai.tick();
    }

    /**
     * Shows/hides all the toggleable layers
     */
    public void setAllPartsVisible(boolean visible) {
        for (int i = 0; i < PlayerModelPart.values().length; i++) {
            PlayerModelPart part = PlayerModelPart.values()[i];
            setVisiblePart(part, visible);
        }
    }
    public void checkAndTeleport(PlayerEntity player) {
        // Check if the actor is in the Nether or the End
        if (this.getWorld().getRegistryKey() == World.OVERWORLD||this.getWorld().getRegistryKey() == World.NETHER || this.getWorld().getRegistryKey() == World.END) {
            // Get the actor's position
            BlockPos actorPos = this.getBlockPos();
            // Search for Nether and End portal blocks in a 2x2x2 area around the actor
            BlockPos portalPos = null;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        BlockPos pos = actorPos.add(x, y, z);
                        BlockState state = getWorld().getBlockState(pos);
                        if (state.isOf(Blocks.NETHER_PORTAL) || state.isOf(Blocks.END_PORTAL)) {
                            // Found a Nether or End portal block
                            portalPos = pos;
                            break;
                        }
                    }
                }
            }
            if (portalPos != null && player.getWorld().getRegistryKey() != this.getWorld().getRegistryKey()) {
                // There is a portal nearby and the player is in a different dimension, teleport the actor to the player's position
                ServerWorld targetWorld = this.getServer().getWorld(player.getWorld().getRegistryKey());
                this.teleport(targetWorld, player.getBlockPos());
            }
        }
    }

    public double getMovementSpeed(Movement.EMovementState movementState) {
        if (movementState == Movement.EMovementState.WALK) {
            return walkSpeed;
        } else if (movementState == Movement.EMovementState.RUN) {
            return runSpeed;
        } else if (movementState == Movement.EMovementState.SNEAK) {
            return sneakSpeed;
        } else {
            return 0.0d;
        }
    }


    /**
     * Shows/hides a toggleable layer
     * @param modelPart A layer to change
     * @param visible The visibility
     */
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

    /**
     * Replaces the current held item with another one
     * @param itemStack The item to change
     */
    public void setInHand(ItemStack itemStack) {
        PlayerInventory inv = this.getInventory();
        inv.setStack(inv.selectedSlot, itemStack);
    }

    /**
     * Change the selected slot
     */
    public void setHotbar(int hotbar) {
        this.getInventory().selectedSlot = MathHelper.clamp(hotbar, 0, 9);
    }

    /**
     * Copy an existing inventory to the actor's inventory
     * @param playerInventory The inventory to copy
     */
    public void copyInventoryFrom(PlayerInventory playerInventory) {
        PlayerInventory npcInventory = this.getInventory();

        for (int i = 0; i < playerInventory.size(); i++) {
            npcInventory.setStack(i, playerInventory.getStack(i));
        }
    }

    public void teleport(ServerWorld world, BlockPos pos) {
        teleport(world, pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d, 0, 0);
    }

    public void teleport(BlockPos pos) {
        teleport(this.getServerWorld(), pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d, 0, 0);
    }

    public void teleport(Vec3d movement) {
        teleport(new BlockPos((int) movement.x, (int) movement.y, (int) movement.z));
    }

    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }

    public ActorAI getAi() {
        return ai;
    }

    public void writeToSpawnPacket() {
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

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(ActorPackets.SET_GAMEPROFILE, buf);

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
