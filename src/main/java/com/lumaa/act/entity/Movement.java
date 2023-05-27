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
        moveTowardsPlayer(actor, player);
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
            else setCrouchingState(actor);
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

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player) {
        movementThread = new Thread(() -> {
            actor.isStuck = false;
            World world = actor.getEntityWorld();
            while (keepMoving && world.getDimension() == player.getWorld().getDimension()) {

                actor.checkAndTeleport(player);

                setCrouchingState(actor);

                double distance = Math.sqrt(actor.squaredDistanceTo(player));
                if (distance > 2) {
                    Vec3d direction2 = getDirection(actor, player);
                    direction2 = adjustDirectionForBlocks(actor, world, direction2);
                    setActorVelocity(actor, direction2);

                    if (actor.isTouchingWater()) actor.setSwimming(true);
                    else actor.setSwimming(false);
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

    private static void setCrouchingState(ActorEntity actor) {
        if(SpeedManagerStick.getState() == EMovementState.SNEAK) {
            actor.setSneaking(true);
            actor.setPose(EntityPose.CROUCHING);
        } else {
            actor.setPose(EntityPose.STANDING);
            actor.setSneaking(false);
        }
    }

    private static Vec3d getDirection(ActorEntity actor, PlayerEntity player) {
        Vec3d ActorPos = actor.getPos();
        Vec3d playerPos = player.getPos();
        return playerPos.subtract(ActorPos).normalize();
    }

    private static Vec3d adjustDirectionForBlocks(ActorEntity actor, World world, Vec3d direction2) {
        double speed = getMovementSpeed(SpeedManagerStick.getState());
        BlockPos blockPos = new BlockPos(BlockPos.ofFloored(actor.getPos().add(direction2.multiply(speed))));
        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.isAir()) {
            if (blockState.isOf(Blocks.WATER)) direction2 = direction2.subtract(0, 2, 0);
            else if (blockState.isOf(Blocks.LAVA)) direction2 = direction2.subtract(0, 3, 0);
            else direction2 = direction2.subtract(0, 5, 0);
        } else if (blockState.isFullCube(world, blockPos)) {
            actor.jump();
        }
        return direction2;
    }

    private static void setActorVelocity(ActorEntity actor, Vec3d direction2) {
        double speed = getMovementSpeed(SpeedManagerStick.getState());
        synchronized(actor) {
            actor.setSprinting(speed>0.10);
            actor.setVelocity(direction2.multiply(speed));
        }
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
