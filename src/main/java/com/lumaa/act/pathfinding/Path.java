package com.lumaa.act.pathfinding;

import com.lumaa.act.ai.ActorMovement;
import com.lumaa.act.ai.Pathfinder;
import com.lumaa.act.entity.ActorEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;

public class Path {
    private static ArrayList<BlockPos> steps = new ArrayList<>();
    private static BlockPos currentGoal;
    private static BlockPos futureGoal;
    private static final double runSpeed = 0.15;
    private static final double walkSpeed = 0.1;
    private static final double sneakSpeed = 0.035;
    private static final double crawlSpeed = 0.035;
    public static MovementState movementState;
    private static ActorEntity actor;
    private static volatile boolean keepMoving = true;
    private static PlayerEntity playerFollow;
    private static Thread movementThread = null;


    public Path(ActorEntity actor) {
        this.actor = actor;
    }


    public boolean isStanding() {
        return movementState == MovementState.STAND;
    }

    public static boolean isWalking() {
        return movementState == MovementState.WALK;
    }

    public static boolean isRunning() {
        return movementState == MovementState.RUN;
    }

    public static boolean isSneaking() {
        return movementState == MovementState.SNEAK;
    }

    public static boolean isCrawling() {
        return movementState == MovementState.CRAWL;
    }

    public static double getMovementSpeed() {
        if (isWalking()) {
            return walkSpeed;
        } else if (isRunning()) {
            return runSpeed;
        } else if (runToFollow()) {
            return runSpeed;
        } else if (isCrawling()) {
            return crawlSpeed;
        } else if (isSneaking()) {
            return sneakSpeed;
        } else {
            return 0.0d;
        }
    }

    public static void nextMove(PlayerEntity player, ActorEntity actor, BlockPos playerPos) {
        playerFollow = player;
        //setMovementState();
        keepMoving = true;
        moveTowardsPlayer(actor, player, player.getMovementSpeed()+0.1f, 3d);
        //stopMoving();
 /*for (BlockPos steps : steps) {
 actor.getAi().moveTo(ActorMovement.MovementState.WALK,new Vec3d(steps.getX(),steps.getY(),steps.getZ()));
 }*/
    }

    public static boolean runToFollow() {
        return actor.getPos().isInRange(playerFollow.getPos(), 3.5d);
    }

    public static void setMovementState() {
        if (runToFollow())
            movementState = MovementState.RUN;
        else
            movementState = MovementState.WALK;
        if (actor.isSubmergedInWater())
            movementState = MovementState.RUN;
        if (actor.getPos().isInRange(playerFollow.getPos(), 1.5d)) {
            movementState = MovementState.STAND;
        }
    }

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player2, double speed, double downSpeed) {
        movementThread = new Thread(() -> {
            while (keepMoving) {
                double distance = Math.sqrt(actor.squaredDistanceTo(player2));
                if (distance > 1) {
                    Direction direction = getDirection(actor, player2);
                    Vec3d vec = new Vec3d(direction.getOffsetX(), 0, direction.getOffsetZ());
                    double dy = player2.getY() - actor.getY();
                    if (dy < 0) {
                        vec = vec.subtract(0, downSpeed, 0);
                    } else {
                        vec = new Vec3d(vec.x, 0, vec.z);
                    }
                    if (shouldJump(actor)||(actor.getY()<player2.getY() && player2.getY()-actor.getY()<=1.07d)) {
                        actor.jump();
                    }
                    vec = vec.normalize().multiply(speed);
                    actor.setVelocity(vec);
                }
                lookAt(actor, player2);
                // Check for blocks below the actor
                World world = actor.getEntityWorld();
                BlockPos posBelowActor = actor.getBlockPos().down();
                Block blockBelowActor = world.getBlockState(posBelowActor).getBlock();
                if (!(actor.getY() > player2.getY())) {
                    if (blockBelowActor == Blocks.AIR || blockBelowActor == Blocks.WATER || blockBelowActor == Blocks.LAVA) { // If there's no block below the actor or it's a water/lava block
                        // Check for blocks beside the empty block
                        Direction[] directionsToCheck = {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
                        for (Direction dir : directionsToCheck) { // Check for blocks in all four directions
                            BlockPos posBesideEmptyBlock = posBelowActor.offset(dir);
                            Block blockBesideEmptyBlock = world.getBlockState(posBesideEmptyBlock).getBlock();
                            if (blockBesideEmptyBlock != Blocks.AIR && blockBesideEmptyBlock != Blocks.WATER && blockBesideEmptyBlock != Blocks.LAVA) { // If there's a solid block beside the empty block
                                // Move towards the solid block
                                Vec3d vecBesideEmptyBlock = new Vec3d(dir.getOffsetX(), 0, dir.getOffsetZ());
                                vecBesideEmptyBlock.normalize().multiply(speed);
                                actor.setVelocity(vecBesideEmptyBlock);
                                break; // Stop checking other directions
                            }
                            else {
                                Vec3d NoSoldBlock = new Vec3d(0, 0, 0);
                                actor.setVelocity(NoSoldBlock);
                            }
                        }
                    }
                }
            }
        });
        movementThread.start();
    }

    public static void stopMoving() {
        keepMoving = false;
        if (movementThread != null) {
            movementThread.interrupt();
            movementThread = null;
        }
    }

    public static boolean shouldJump(ActorEntity actor) {
        World world = actor.getEntityWorld();
        BlockPos pos = actor.getBlockPos();
        Direction direction = actor.getMovementDirection();
        BlockPos offsetPos = pos.offset(direction);
        boolean shouldJump = !world.getBlockState(offsetPos).isAir() && world.getBlockState(offsetPos.up()).isAir();
        System.out.println("shouldJump: " + shouldJump); // Print the value of shouldJump
        return shouldJump;
    }


    public static void lookAt(ActorEntity actor, PlayerEntity player2) {
        double dx = player2.getX() - actor.getX();
        double dz = player2.getZ() - actor.getZ();
        double dy = player2.getEyeY() - actor.getEyeY();
        double distance = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180 / Math.PI)) - 90;
        float pitch = (float) -(MathHelper.atan2(dy, distance) * (180 / Math.PI));
        actor.setYaw(yaw);
        actor.setPitch(pitch);
    }

    public static Direction getDirection(ActorEntity actor, PlayerEntity player2) {
        BlockPos pos1 = actor.getBlockPos();
        BlockPos pos2 = player2.getBlockPos();
        int dx = pos2.getX() - pos1.getX();
        int dz = pos2.getZ() - pos1.getZ();
        if (Math.abs(dx) > Math.abs(dz)) {
            if (dx > 0) {
                return Direction.EAST;
            } else {
                return Direction.WEST;
            }
        } else {
            if(dz > 0){
                return Direction.SOUTH;
            }else{
                return Direction.NORTH;
            }
        }
    }
    public enum MovementState {
        STAND,
        WALK,
        SNEAK,
        RUN,
        CRAWL
    }
}
