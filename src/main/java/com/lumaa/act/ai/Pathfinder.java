package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Pathfinder {
    private ActorAI ai;
    private ActorEntity actor;
    private ActorMovement movement;
    private ActorAction action;
    private boolean positionsSet = false;
    private boolean follows = false;
    private int l = 0;
    private BlockPos currentGoal;
    private boolean finishedFollowing = false;
    public Path path;
    public World world;
    public BlockPos origin;
    public BlockPos destination;

    public Pathfinder(ActorAI ai) {
        this.ai = ai;
        this.actor = ai.actor;
        this.movement = ai.movement;
        this.action = ai.action;
        this.world = ai.actor.world;
    }

    public void setPositions(BlockPos origin, BlockPos destination) {
        this.origin = origin;
        this.destination = destination;
        this.path = new Path(this);
        this.positionsSet = true;
    }

    public boolean isPathCorrect() {
        if (this.path.isStopped()) {
            List<BlockPos> steps = this.path.getSteps();
            BlockPos lastStep = steps.get(steps.size() - 1);
            return lastStep.equals(this.destination);
        }
        return false;
    }

    /**
     * Set path goals according to the Actor's position. Also moves the Actor
     * Overrides the values from {@link com.lumaa.act.ai.ActorMovement#goal}
     */
    public void setPathGoals() {
        if (!this.isFollowing()) return;
        move();
    }

    private void move() {
        l += 1;
        if (l > path.getSteps().size() - 1 ||  l < 0) l = 0;
        if (this.isFollowing() && isNearGoal(null)) {
            this.currentGoal = path.getSteps().get(l);
            this.movement.goal = this.currentGoal.toCenterPos();
        }

        if (this.isFollowing() && !isNearGoal(null)) {
            this.actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, this.currentGoal.add(0, 1, 0).toCenterPos());
            this.actor.addVelocity(this.movement.forward(), 0, this.movement.right());
        }

        if (isNearEnd()) {
            this.setFinishedFollowing(true);
            this.setFollows(false);
            l = 0;
        }
    }

    private boolean isNearGoal(@Nullable BlockPos goal) {
        boolean following = this.action.getAction().equals(ActorAction.Actions.FOLLOW);
        if (goal == null) {
            if (this.currentGoal != null) {
                goal = this.currentGoal;
            } else {
                return true;
            }
        }
        return this.actor.getPos().isInRange(goal.toCenterPos(), following ? 2.5d : 0.85d);
    }

    private boolean isNearEnd() {
        return this.isNearGoal(this.destination);
    }

    public boolean isFollowing() {
        return follows;
    }

    public void setFollows(boolean follows) {
        this.follows = follows;
    }

    public boolean isFinishedFollowing() {
        return finishedFollowing;
    }

    public void setFinishedFollowing(boolean finishedFollowing) {
        this.finishedFollowing = finishedFollowing;
    }

    public void execute() {
        if (positionsSet) {
            this.path.tick();
        }
    }

    public void reset() {
        this.setFinishedFollowing(false);
        this.setFollows(false);
        this.l = 0;
        this.path = null;
        this.origin = null;
        this.destination = null;
    }

    @Override
    public String toString() {
        return "Pathfinder{" +
                "origin=" + origin +
                ", destination=" + destination +
                '}';
    }
}
