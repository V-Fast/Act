package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ActorAI {
    protected ActorEntity actor;
    public ActorMovement movement;
    public ActorAction action;

    public ActorAI(ActorEntity actor) {
        this.actor = actor;
        this.movement = new ActorMovement(actor);
        this.action = new ActorAction(this);
    }

    public void tick() {
        this.movement.execute();
        this.action.tick();
    }

    public void moveTo(ActorMovement.MovementState state, Vec3d pos) {
        this.movement.pathfinder = new Pathfinder(this);
        this.movement.pathfinder.setFollows(true);
        this.movement.pathfinder.setFinishedFollowing(false);
        this.movement.pathfinder.setPositions(this.actor.getBlockPos(), new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()));
        this.movement.movementState = state;
        this.movement.goal = pos;
    }

    public void walkTo(BlockPos pos) {
        moveTo(ActorMovement.MovementState.WALK, pos.toCenterPos());
    }

    public void runTo(BlockPos pos) {
        moveTo(ActorMovement.MovementState.RUN, pos.toCenterPos());
    }

    public void sneakTo(BlockPos pos) {
        moveTo(ActorMovement.MovementState.SNEAK, pos.toCenterPos());
    }

    public void crawlTo(BlockPos pos) {
        moveTo(ActorMovement.MovementState.CRAWL, pos.toCenterPos());
    }

    public void moveToEntity(LivingEntity entity, ActorMovement.MovementState state) {
        moveTo(state, entity.getPos());
    }

    public void followEntity(LivingEntity entity) {
        this.action.follow((PlayerEntity) entity);
    }
}
