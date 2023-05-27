package com.lumaa.act.pathfinding;

import com.lumaa.act.entity.ActorEntity;
import com.lumaa.act.item.stick.SpeedManagerStick;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Path {
    private static volatile boolean keepMoving = true;
    private static Thread movementThread = null;
    private static int stuckCount = 0, notMovingCount = 0;
    private static double prevDistance = 0;

    // Actor Movement Speed
    private static final double runSpeed = 0.17;
    private static final double walkSpeed = 0.1;
    private static final double sneakSpeed = 0.04;

    public static double getMovementSpeed(EMovementState movementState) {
        if (movementState == EMovementState.WALK) {
            return walkSpeed;
        } else if (movementState == EMovementState.RUN) {
            return runSpeed;
        } else if (movementState == EMovementState.SNEAK) {
            return sneakSpeed;
        } else {
            return 0.0d;
        }
    }


    public static void nextMove(PlayerEntity player, ActorEntity actor) {
        keepMoving=true;
        moveTowardsPlayer(actor, player, 3d);
        if(actor.isDead() || actor.notInAnyWorld) stopMoving(actor);
    }

    public static void swimUp(ActorEntity actor,PlayerEntity player) {
        Vec3d vec;
        if (actor.isSubmergedInWater()) { // Check if the actor is in water
            if (actor.isTouchingWater() && player.isSubmergedInWater())
                vec = new Vec3d(0, -0.01, 0);
            else if(player.isTouchingWater())
                vec = new Vec3d(0, 0.7, 0); // Create a vector pointing upwards
            else
                vec = new Vec3d(0,0.6,0);
            actor.setSwimming(true);
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        } else {
            actor.setSwimming(false);
        }
        if (actor.isInLava())
        {
            vec = new Vec3d(0, 0.3, 0); // Create a vector pointing upwards
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        }
    }

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player, double downSpeed) {
        movementThread = new Thread(() -> {
            // This code looks trash but works good
            actor.isStuck = false;
            while (keepMoving && actor.getWorld().getDimension()==player.getWorld().getDimension()) // So that it stops moving when player and actor are not in same dimension
            {
                actor.checkAndTeleport(player);
                double speed = getMovementSpeed(SpeedManagerStick.getState());
                if(SpeedManagerStick.getState() == EMovementState.SNEAK) {
                    actor.setSneaking(true);
                    actor.setPose(EntityPose.CROUCHING);
                } else {
                    actor.setPose(EntityPose.STANDING);
                    actor.setSneaking(false);
                }

                // Set Sneaking to true if actor is supposed to sneak move
                double distance = Math.sqrt(actor.squaredDistanceTo(player));
                if (distance > 3) {
                    Vec3d ActorPos = actor.getPos();
                    Vec3d playerPos = player.getPos();

                    Vec3d direction2 = playerPos.subtract(ActorPos).normalize();

                    World world = actor.getEntityWorld();
                    BlockPos blockPos = new BlockPos(BlockPos.ofFloored(ActorPos.add(direction2.multiply(speed))));
                    BlockState blockState = world.getBlockState(blockPos);

                    if (blockState.isAir()) {
                        // if there is no solid ground, move the player down
                        if (blockState.isOf(Blocks.WATER)) direction2 = direction2.subtract(0, 2, 0);//Gravity to affect less in water and lava so that it doesnt look like its struggling to float
                        else if (blockState.isOf(Blocks.LAVA)) direction2 = direction2.subtract(0, 3, 0);
                        else direction2 = direction2.subtract(0, 5, 0); //Gravity to be extreme so that it does not start floating
                    } else if (blockState.isFullCube(world, blockPos)) {
                        // if there is a solid block in the way, make the player jump
                        actor.jump();
                    }
                    if (isStuck(actor, player)) {
                        // The actor is stuck
                        if (!actor.isStuck) {
                            // The message has not been displayed yet, display it now
                            System.out.println("Actor is stuck");
                            Vec3d actorPos = actor.getPos();
                            String formattedActorPos = String.format("(%f, %f, %f)", actorPos.x, actorPos.y, actorPos.z);
                            String message = String.format("%s is stuck at %s", actor.getName().getString(), formattedActorPos);
                            player.sendMessage(Text.of(message).copy().formatted(Formatting.WHITE), false);
                            // Set the isStuck flag to true so that the message is not displayed again
                            actor.isStuck = true;
                        }
                    } else {
                        // The actor is not stuck, reset the isStuck flag
                        actor.isStuck = false;
                    }
                    synchronized(actor) {
                        actor.setSprinting(speed > 0.03);
                        actor.setVelocity(direction2.multiply(speed));
                    }

                    if (actor.isTouchingWater()) actor.setSwimming(true);
                    else actor.setSwimming(false);
                }
                synchronized(actor) {
                    actor.setSprinting(false);
                }
                lookAt(actor, player);
                swimUp(actor,player);
            }
        });
        movementThread.start();
    }


    /*
    To check if the actor is stuck and then display the message if it is. Currently, awful
     */
    public static boolean isStuck(ActorEntity actor, PlayerEntity player) {
        // Get the distance between the actor and the player
        double distance = actor.distanceTo(player);
        // Check if the distance is increasing
        if (distance > prevDistance && prevDistance - distance >= 20) {
            // The distance is increasing, increment the stuckCount variable
            stuckCount++;
            if (stuckCount > 20) {
                // If the actor has been moving away from the player for more than 20 ticks, it is stuck
                return true;
            }
        } else {
            // The distance is not increasing, reset the stuckCount variable
            stuckCount = 0;
        }
        // Update prevDistance for the next tick
        prevDistance = distance;
        // Get the velocity of the actor
        Vec3d velocity = actor.getVelocity();
        // Check if the velocity is near zero
        if (velocity.lengthSquared() >= 0) {
            // The velocity is not near zero, reset the notMovingCount variable
            notMovingCount = 0;
        } else {
            // The velocity is near zero, increment the notMovingCount variable
            if(!actor.isTouchingWater() || !actor.isSubmergedInWater() ||!actor.isInLava()) notMovingCount++;
            // If the actor has not been moving for more than 20 ticks, it is stuck
            return notMovingCount > 20;
        }
        // The actor is not stuck
        return false;
    }



    public static void stopMoving(ActorEntity actor) {
        keepMoving = false;
        if (movementThread != null) {
            movementThread.interrupt();
            movementThread = null;
        }
        actor.isFollowing=false;
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
    public enum EMovementState {
        WALK,
        SNEAK,
        RUN,
    }
}
