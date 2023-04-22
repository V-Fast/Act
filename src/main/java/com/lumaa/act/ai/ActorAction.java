package com.lumaa.act.ai;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;

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

    public boolean runToFollow() {
        return !this.actorAI.actor.getPos().isInRange(this.playerFollow.getPos(), 3.5d);
    }

    public void changePose() {
        if (this.actorAI.movement.isSneaking())  {
            this.actorAI.actor.setPose(EntityPose.CROUCHING);
        } else if (this.actorAI.movement.isCrawling()) {
            this.actorAI.actor.setPose(EntityPose.SWIMMING);
        } else {
            this.actorAI.actor.setPose(EntityPose.STANDING);
        }
    }

    public void follow(PlayerEntity player) {
        this.playerFollow = player;
        this.setAction(Actions.FOLLOW);
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

    public enum Actions {
        NONE,
        MOVE,
        FOLLOW
    }
}
