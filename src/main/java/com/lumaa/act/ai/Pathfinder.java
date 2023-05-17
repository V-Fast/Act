package com.lumaa.act.ai;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
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
    public List<Vec3d> prevPositions = new ArrayList<>();

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

    /**
     * Moves the actor to the next goal
     */
    private void move() {
        // Check if the actor has moved far enough
        if (prevPositions.size() > 10) {
            Vec3d firstPos = prevPositions.get(0);
            Vec3d lastPos = prevPositions.get(prevPositions.size() - 1);
            double distance = firstPos.distanceTo(lastPos);
            if (distance < 1.0) {
                // The actor has not moved far enough, try changing its path or movement direction
                this.currentGoal = this.destination;
                this.movement.goal = this.currentGoal.toCenterPos();
            }
            prevPositions.remove(0);
        }
        prevPositions.add(this.actor.getPos());

        l += 1;
        if (l > path.getSteps().size() - 1 || l < 0) l = 0;
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

    /**
     * Returns true if the actor is next to a goal
     * @param goal null = currentGoal
     */
    private boolean isNearGoal(@Nullable BlockPos goal) {
        boolean following = this.action.getAction().equals(ActorAction.Actions.FOLLOW);
        if (goal == null) {
            System.out.println("Current Goal: "+this.currentGoal);
            if (this.currentGoal == null) {
                return true;
            } else {
                goal = this.currentGoal;
                System.out.println("Goal: "+goal);
            }
        }
        double range = 0.85d;
        if (following) range = 2d;
        else if (this.movement.isRunning()) range = 1.5d;
        else if (this.movement.isWalking()) range = 1.0d;

        // Check if the actor is within the horizontal and vertical range of its goal
        boolean isInRange = this.actor.getPos().isInRange(goal.toCenterPos(), range);
        boolean isWithinVerticalRange = Math.abs(this.actor.getY() - goal.getY()) <= 2;
        System.out.println("IsinRange: "+isInRange+", Vertical: "+isWithinVerticalRange);
        return isInRange && isWithinVerticalRange;
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
