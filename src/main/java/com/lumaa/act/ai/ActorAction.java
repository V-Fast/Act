package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ActorAction {
    public ActorAI actorAI;
    private Actions action = Actions.NONE;
    private PlayerEntity playerFollow;

    public ActorAction(ActorAI actorAI) {
        this.actorAI = actorAI;
    }

    public void tick() {
        this.tickFollow();
        this.changePose();
    }

    public void tickFollow() {
        if (this.getAction().equals(Actions.FOLLOW)) {
            boolean run = this.runToFollow();
            this.actorAI.moveToEntity(this.playerFollow, run ? ActorMovement.MovementState.RUN : ActorMovement.MovementState.WALK);
        }
    }

    /**
     * Forces the actor to run if the following player is too far (3.5 blocks)
     */
    public boolean runToFollow() {
        return !this.actorAI.actor.getPos().isInRange(this.playerFollow.getPos(), 3.5d);
    }

    /**
     * Changes the player model's pose
     */
    public void changePose() {
        if (this.actorAI.movement.isSneaking())  {
            this.actorAI.actor.setPose(EntityPose.CROUCHING);
        } else if (this.actorAI.movement.isCrawling()) {
            this.actorAI.actor.setPose(EntityPose.SWIMMING);
        } else {
            this.actorAI.actor.setPose(EntityPose.STANDING);
        }
    }

    /**
     * Define a player follower
     * @param player Player to follow
     */
    public void follow(PlayerEntity player) {
        this.playerFollow = player;
        this.setAction(Actions.FOLLOW);
    }

    public ActionResult placeBlock(BlockPos position) {
        ActorEntity actorEntity = this.actorAI.actor;
        actorEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position.toCenterPos());
        BlockHitResult raycast = new BlockHitResult(position.toCenterPos(), Direction.DOWN, position, false);
        actorEntity.swingHand(Hand.MAIN_HAND);
        return actorEntity.interactionManager.interactBlock(actorEntity, actorEntity.getWorld(), actorEntity.getInventory().getMainHandStack(), Hand.MAIN_HAND, raycast);
    }

    /**
     * Breaks <b>ALL</b> blocks instantly
     * @param position The position of the block to break
     */
    public void instantBreakBlock(BlockPos position) {
        ActorEntity actorEntity = this.actorAI.actor;
        actorEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position.toCenterPos());
        actorEntity.swingHand(Hand.MAIN_HAND);
        actorEntity.interactionManager.tryBreakBlock(position);
    }

    public void progressBreakBlock(BlockPos position) {
        ActorEntity actorEntity = this.actorAI.actor;
        actorEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, position.toCenterPos());
        actorEntity.swingHand(Hand.MAIN_HAND);
        actorEntity.interactionManager.processBlockBreakingAction(position, PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, Direction.DOWN, actorEntity.getWorld().getHeight(), 15);
        //TODO: Break block once processBlockBreakingAction is finished
    }

    public void setAction(Actions actorAction) {
        this.action = actorAction;
    }

    public Actions getAction() {
        return action;
    }

    public PlayerEntity getPlayerFollow() {
        return playerFollow;
    }

    /**
     * Different actions an actor can perform
     */
    public enum Actions {
        NONE,
        MOVE,
        FOLLOW,
        PLACING,
        BREAKING,
    }
}
