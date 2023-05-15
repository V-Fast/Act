package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

public class ActorMovement implements IMovement {
    protected ActorEntity actor;
    private static final double runSpeed = 0.15;
    private static final double walkSpeed = 0.1;
    private static final double sneakSpeed = 0.035;
    private static final double crawlSpeed = 0.035;
    public Pathfinder pathfinder;
    public MovementState movementState;
    public Vec3d goal;
    private boolean isJumping=false;

    public ActorMovement(ActorEntity actor) {
        this.actor = actor;
        this.movementState = MovementState.STAND;
    }

    private float calibrate(boolean zAxis) {
        if (zAxis) {
            return Math.abs(actor.headYaw) / 90 - 1;
        } else {
            return (float) (2 / Math.PI * (Math.asin(Math.sin(this.actor.headYaw / 180 * Math.PI))));
        }
    }

    public boolean isStanding() {
        return this.movementState == MovementState.STAND;
    }

    public boolean isWalking() {
        return this.movementState == MovementState.WALK;
    }

    public boolean isRunning() {
        return this.movementState == MovementState.RUN;
    }

    public boolean isSneaking() {
        return this.movementState == MovementState.SNEAK;
    }

    public boolean isCrawling() {
        return this.movementState == MovementState.CRAWL;
    }

    public double getMovementSpeed() {
        if (isWalking()) {
            return walkSpeed;
        } else if (isRunning()) {
            return runSpeed;
        } else if (isCrawling()) {
            return crawlSpeed;
        } else if (isSneaking()) {
            return sneakSpeed;
        } else {
            return 0.0d;
        }
    }

    @Override
    public double forward() {
        return getMovementSpeed() * -calibrate(false);
    }

    @Override
    public double right() {
        return getMovementSpeed() * -calibrate(true);
    }

   /* private void oldMovement() {
        // look follow
        if (this.actor.getAi().action.getAction().equals(ActorAction.Actions.FOLLOW)) {
            this.actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.actor.getAi().action.getPlayerFollow().getEyePos());
        }

        // moving
        if (!isStanding() && this.goal != null) {
            if (this.actor.getPos().isInRange(this.goal, 1.5d)) {
                ActorAction action = this.actor.getAi().action;
                if (!action.getAction().equals(ActorAction.Actions.FOLLOW)) {
                    action.setAction(ActorAction.Actions.NONE);
                }
                this.movementState = MovementState.STAND;
                this.goal = null;
            } else {
                boolean following = this.actor.getAi().action.getAction().equals(ActorAction.Actions.FOLLOW);
                if (!following) this.actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.goal.add(0, 1, 0));
                this.actor.addVelocity(this.forward(), 0, this.right());
            }
        }
    }*/

    @Override
    public void execute() {
        if (!isStanding() && this.goal != null) {
            this.pathfinder.execute();
            jump();
            this.pathfinder.path.headYaw();
            if (this.actor.getAi().action.getAction().equals(ActorAction.Actions.FOLLOW)) {
                this.actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.actor.getAi().action.getPlayerFollow().getPos());
            }
            if (this.pathfinder.path.isStopped() && this.pathfinder.isPathCorrect()) {
                pathfinder.setPathGoals();

                if (pathfinder.isFinishedFollowing()) {
                    pathfinder.reset();
                    ActorAction action = this.actor.getAi().action;
                    if (!action.getAction().equals(ActorAction.Actions.FOLLOW)) {
                        action.setAction(ActorAction.Actions.NONE);
                    }
                    this.movementState = MovementState.STAND;
                    this.goal = null;
                }
            }
        }
    }
    public void jump() {
        if (pathfinder.path == null) return;
        List<BlockPos> steps = pathfinder.path.getSteps();
        if (steps.size() > 1) {
            BlockPos current = steps.get(0);
            BlockPos next = steps.get(1);
            if (next.getY() > current.getY() && next.getY()<=1.25d && !isJumping) {
                this.actor.jump();
                this.actor.addVelocity(this.forward()+0.4f,actor.getVelocity().y,this.right());
                isJumping=true;
            }
            if (next.getY()==current.getY() || next.getY()<current.getY())isJumping=false;
        }
    }

    public enum MovementState {
        STAND,
        WALK,
        SNEAK,
        RUN,
        CRAWL
    }

    public enum MovementDirection {
        FORWARD,
        RIGHT,
        LEFT,
        BACKWARD
    }
}
