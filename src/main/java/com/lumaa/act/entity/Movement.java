package com.lumaa.act.entity;

import com.lumaa.act.item.stick.SpeedManagerStick;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Movement {
    private static volatile boolean keepMoving = true;
    private static Thread movementThread = null;
    // Actor Movement Speed
    private static final double runSpeed = 0.2;
    private static final double walkSpeed = 0.1;
    private static final double sneakSpeed = 0.04;
    private static final double swimSpeed = 0.30;

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

    public static void swim(ActorEntity actor,PlayerEntity player) {
        Vec3d vec;
        if (actor.isSubmergedInWater()) { // Check if the actor is in water
            if (actor.isTouchingWater() && player.isSubmergedInWater())
                vec = new Vec3d(actor.getVelocity().getX(), -0.001, actor.getVelocity().getZ());
            else if(player.isTouchingWater())
                vec = new Vec3d(actor.getVelocity().getX(), 0.3,  actor.getVelocity().getZ()); // Create a vector pointing upwards
            else if (actor.isSubmergedInWater() && actor.hurtByWater())
                vec = new Vec3d( actor.getVelocity().getX(),0.6, actor.getVelocity().getZ()); // To move the actor upwards if it is taking damage in water
            else
                vec = new Vec3d(actor.getVelocity().getX(), actor.getVelocity().getY(), actor.getVelocity().getZ());
            if (actor.isSubmergedInWater())actor.setPose(EntityPose.SWIMMING);
            else actor.setPose(EntityPose.STANDING);
            actor.setSwimming(true);
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        } else {
            actor.setSwimming(false);
            actor.setPose(EntityPose.STANDING);
        }
        if (actor.isInLava())
        {
            vec = new Vec3d( actor.getVelocity().getX(), 0.1,  actor.getVelocity().getZ()); // Create a vector pointing upwards
            actor.setVelocity(vec); // Set the actor's velocity to the upwards vector
        }
    }

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player, double downSpeed) {
        movementThread = new Thread(() -> {
            // This code looks trash but works good
            actor.isStuck = false;
            while (keepMoving && actor.getWorld().getDimension() == player.getWorld().getDimension()) // So that it stops moving when player and actor are not in same dimension
            {

                actor.checkAndTeleport(player);//From ActorEntity.class

                double speed = getMovementSpeed(SpeedManagerStick.getState());

                if (actor.isSubmergedInWater())speed=swimSpeed;

                synchronized (actor) {
                    if (SpeedManagerStick.getState() == EMovementState.SNEAK) {
                        actor.setSneaking(true);
                        actor.setPose(EntityPose.CROUCHING);
                    } else {
                        actor.setPose(EntityPose.STANDING);
                        actor.setSneaking(false);
                    }
                    actor.setSprinting(speed>0.10);
                }

                // Set Sneaking to true if actor is supposed to sneak move
                double distance = Math.sqrt(actor.squaredDistanceTo(player));
                if (distance > 2) {
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
                    synchronized(actor) {
                        actor.setVelocity(direction2.multiply(speed));
                        if (actor.isTouchingWater()) actor.setSwimming(true);
                        else actor.setSwimming(false);
                    }
                }
                synchronized(actor) {
                    actor.setSprinting(false);
                }
                lookAt(actor, player);
                swim(actor,player);
            }
        });
        movementThread.start();
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
