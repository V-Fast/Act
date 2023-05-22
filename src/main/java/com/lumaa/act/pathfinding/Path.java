package com.lumaa.act.pathfinding;

import com.lumaa.act.entity.ActorEntity;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
public class Path {
    private static ActorEntity actor;
    private static volatile boolean keepMoving = true;
    private static PlayerEntity playerFollow;
    private static Thread movementThread = null;


    public Path(ActorEntity actor) {
        Path.actor = actor;
    }


    public static void nextMove(PlayerEntity player, ActorEntity actor, BlockPos playerPos) {
        playerFollow = player;
        keepMoving = true;
        moveTowardsPlayer(actor, player, player.getMovementSpeed()+0.1f, 3d);
        if(actor.isDead() || actor.notInAnyWorld) stopMoving();
    }

    public static void swimUp(ActorEntity actor) {
        Vec3d vec;
        if (actor.isSubmergedInWater() || actor.isInLava()) { // Check if the actor is in water
            if (actor.isTouchingWater())
                vec = new Vec3d(0, -0.001, 0);
            else
                vec = new Vec3d(0, 1, 0); // Create a vector pointing upwards
            actor.setSwimming(true);
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        } else {
            actor.setSwimming(false);
        }
    }

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player, double speed, double downSpeed) {
        movementThread = new Thread(() -> {
            //This code looks trash but works good
            Vec3d prevPos = actor.getPos();
            while (keepMoving) {
                double distance = Math.sqrt(actor.squaredDistanceTo(player));
                if (distance > 3) {
                    Vec3d ActorPos = actor.getPos();
                    Vec3d player2Pos = player.getPos();
                    Direction direction = getDirection(actor, player);
                    Vec3d vec = new Vec3d(direction.getOffsetX(), 0, direction.getOffsetZ());
                    double dy = player.getY() - actor.getY();
                    if (dy < 0) {vec = vec.subtract(0, downSpeed, 0);}
                    else {vec = new Vec3d(vec.x, 0, vec.z);}
                    vec = vec.normalize().multiply(speed);
                    Vec3d direction2 = player2Pos.subtract(ActorPos).normalize();
                    World world = actor.getEntityWorld();
                    BlockPos blockPos = new BlockPos(BlockPos.ofFloored(ActorPos.add(direction2.multiply(speed))));
                    BlockState blockState = world.getBlockState(blockPos);
                    if (blockState.isAir()) {
                        // if there is no solid ground, move the player down
                        if (blockState.isOf(Blocks.WATER) || blockState.isOf(Blocks.LAVA)) direction2 = direction2.subtract(0, 1.5, 0); //Gravity to affect less in water and lava so that it doesnt look like its struggling to float
                        else direction2 = direction2.subtract(0, 5, 0); //Gravity to be extreme so that it does not start floating
                    } else if (blockState.isFullCube(world, blockPos)) {
                        // if there is a solid block in the way, make the player jump
                        actor.jump();
                    }
                    actor.setVelocity(direction2.multiply(speed));
                    actor.setSprinting(speed > 0.05);
                    isStuck(prevPos, ActorPos, player);
                }
                lookAt(actor, player);
                swimUp(actor);
                // Check if the actor is in water and set its swimming animation
                if (actor.isTouchingWater()) {
                    actor.setSwimming(true);
                } else {
                    actor.setSwimming(false);
                }
                prevPos = actor.getPos(); // update prevPos for next iteration
            }
        });
        movementThread.start();
    }

    /*
    To check if the actor is stuck and then display the message if it is.
     */
    public static void isStuck(Vec3d prevPos, Vec3d ActorPos, PlayerEntity player) {
        int stuckCount = 0;
        // Check if the actor is stuck
        if (ActorPos.distanceTo(prevPos) < 0.1) {
            stuckCount++;
            if (stuckCount > 10) {
                // If the actor is stuck for more than 10 ticks, display a message in the chat
                String message = String.format("%s is stuck at %s", actor.getName().getString(), ActorPos);
                player.sendMessage(Text.literal(message), false);
                stuckCount = 0;
            }
        } else {
            stuckCount = 0;
        }
    }

    public static void stopMoving() {
        keepMoving = false;
        if (movementThread != null) {
            movementThread.interrupt();
            movementThread = null;
        }
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
            if (dz > 0) {
                return Direction.SOUTH;
            } else {
                return Direction.NORTH;
            }
        }
    }

}
