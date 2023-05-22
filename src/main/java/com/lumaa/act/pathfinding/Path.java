package com.lumaa.act.pathfinding;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class Path {
    private static ActorEntity actor;
    private static volatile boolean keepMoving = true;
    private static PlayerEntity playerFollow;
    private static Thread movementThread = null;
    private static int stuckCount=0,notMovingCount=0;
    private static double prevDistance = 0;


    public Path(ActorEntity actor) {
        Path.actor = actor;
    }


    public static void nextMove(PlayerEntity player, ActorEntity actor, BlockPos playerPos) {
        playerFollow = player;
        keepMoving = true;
        moveTowardsPlayer(actor, player, player.getMovementSpeed()+0.1f, 3d);
        if(actor.getDimensions(EntityPose.STANDING)!=player.getDimensions(EntityPose.STANDING))stopMoving();
        if(actor.isDead() || actor.notInAnyWorld) stopMoving();
    }

    public static void swimUp(ActorEntity actor,PlayerEntity player) {
        Vec3d vec;
        if (actor.isSubmergedInWater()) { // Check if the actor is in water
            if (actor.isTouchingWater() && player.isSubmergedInWater())
                vec = new Vec3d(0, -0.0008, 0);
            else if(player.isTouchingWater())
                vec = new Vec3d(0, 1, 0); // Create a vector pointing upwards
            else
                vec=new Vec3d(0,0.6,0);
            actor.setSwimming(true);
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        } else {
            actor.setSwimming(false);
        }
        if (actor.isInLava())
        {
            vec = new Vec3d(0, 0.2, 0); // Create a vector pointing upwards
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        }
    }

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player, double speed, double downSpeed) {
        movementThread = new Thread(() -> {
            //This code looks trash but works good

            actor.isStuck=false;
            while (keepMoving) {
                double distance = Math.sqrt(actor.squaredDistanceTo(player));
                if (distance > 3) {

                    actor.isFollowing=true;

                    Vec3d ActorPos = actor.getPos();
                    Vec3d playerPos = player.getPos();
                    Direction direction = getDirection(actor, player);
                    Vec3d vec = new Vec3d(direction.getOffsetX(), 0, direction.getOffsetZ());
                    double dy = player.getY() - actor.getY();

                    if (dy < 0) vec = vec.subtract(0, downSpeed, 0);
                    else vec = new Vec3d(vec.x, -100, vec.z); // For some reason if Y =0 then the actor wont move

                    vec = vec.normalize().multiply(speed);

                    Vec3d direction2 = playerPos.subtract(ActorPos).normalize();

                    World world = actor.getEntityWorld();
                    BlockPos blockPos = new BlockPos(BlockPos.ofFloored(ActorPos.add(direction2.multiply(speed))));
                    BlockState blockState = world.getBlockState(blockPos);

                    if (blockState.isAir()) {
                        // if there is no solid ground, move the player down
                        if (blockState.isOf(Blocks.WATER))
                            direction2 = direction2.subtract(0, 2, 0);//Gravity to affect less in water and lava so that it doesnt look like its struggling to float
                        else if(blockState.isOf(Blocks.LAVA))
                            direction2 = direction2.subtract(0, 3, 0);
                        else
                            direction2 = direction2.subtract(0, 5, 0); //Gravity to be extreme so that it does not start floating
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
                            player.sendMessage(Text.of(message), false);
                            // Set the isStuck flag to true so that the message is not displayed again
                            actor.isStuck = true;
                        }
                    } else {
                        // The actor is not stuck, reset the isStuck flag
                        actor.isStuck = false;
                    }

                    actor.setVelocity(direction2.multiply(speed));

                    actor.setSprinting(speed > 0.05);

                    if (actor.isTouchingWater())
                        actor.setSwimming(true);
                    else
                        actor.setSwimming(false);

                }
                actor.setSprinting(false);
                actor.isFollowing=false;
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
        if (distance > prevDistance && prevDistance-distance>=20) {
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
        if (velocity.lengthSquared() >= 0.02) {
            // The velocity is not near zero, reset the notMovingCount variable
            notMovingCount = 0;
        } else {
            // The velocity is near zero, increment the notMovingCount variable
            notMovingCount++;
            // If the actor has not been moving for more than 20 ticks, it is stuck
            return notMovingCount > 20;
        }
        // The actor is not stuck
        return false;
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
