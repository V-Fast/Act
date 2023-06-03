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
    // Actor Movement Speed

    public static void moveToPlayer(PlayerEntity player, ActorEntity actor) {
        actor.keepMovingToPlayer = true;
        moveTowardsPlayer(actor, player);
        if (actor.isDead() || actor.notInAnyWorld) stopMoving(actor);
    }

    public static void moveToBlockPos(BlockPos blockPos, ActorEntity actor) {
        actor.keepMovingToBlock = true;
        moveTowardsPosition(actor, blockPos.toCenterPos());
        if (actor.isDead() || actor.notInAnyWorld || actor.getBlockPos() == blockPos) stopMoving(actor);
    }

    public static void swim(ActorEntity actor, PlayerEntity player) {
        if (actor.isSubmergedInWater()) {
            Vec3d swimVector = getSwimVector(actor, player);
            actor.setPose(EntityPose.SWIMMING);
            actor.setSwimming(true);
            actor.setVelocity(swimVector);
        } else {
            actor.setSwimming(false);
            setCrouchingState(actor);
        }
        if (actor.isInLava()) {
            Vec3d lavaSwimVector = new Vec3d(actor.getVelocity().getX(), 0.1, actor.getVelocity().getZ());
            actor.setVelocity(lavaSwimVector);
        }
    }


    private static Vec3d getSwimVector(ActorEntity actor, PlayerEntity player) {
        double x = actor.getVelocity().getX();
        double y = actor.getVelocity().getY();
        double z = actor.getVelocity().getZ();

        if (actor.isTouchingWater() && player.isSubmergedInWater())
            y = -0.001;
        else if (player.isTouchingWater())
            y = 0.3;
        else
            y = 0.6;

        return new Vec3d(x, y, z);
    }

    public static void moveTowardsPosition(ActorEntity actor, Vec3d position) {
        if ( actor.positionmovementThread!=null) actor.positionmovementThread.interrupt();
        actor.positionmovementThread = new Thread(() -> {
            World world = actor.getEntityWorld();
            double distance = actor.getPos().distanceTo(position);
            if (distance < 0.5) {
                stopMovingToBlock(actor);
                return;
            }
            while ( actor.keepMovingToBlock) {
                setCrouchingState(actor);
                Vec3d direction2 = getDirection(actor, position);
                direction2 = adjustDirectionForBlocks(actor, world, direction2);
                setActorVelocity(actor, direction2);
                actor.setSwimming(actor.isSubmergedInWater());
            }
            if (actor.getPos()==position) actor.keepMovingToBlock=false;
        });
        actor.positionmovementThread.start();
    }


    private static Vec3d getDirection(ActorEntity actor, Vec3d position) {
        Vec3d ActorPos = actor.getPos();
        return position.subtract(ActorPos).normalize();
    }

    public static void moveTowardsPlayer(ActorEntity actor, PlayerEntity player) {
        actor.playermovementThread = new Thread(() -> {
            actor.isStuck = false;
            World world = actor.getEntityWorld();
            while ( actor.keepMovingToPlayer && world.getDimension() == player.getWorld().getDimension()) {

                actor.checkAndTeleport(player);

                setCrouchingState(actor);

                double distance = Math.sqrt(actor.squaredDistanceTo(player));
                if (distance > 2) {
                    Vec3d direction2 = getDirection(actor, player);
                    direction2 = adjustDirectionForBlocks(actor, world, direction2);
                    setActorVelocity(actor, direction2);

                    actor.setSwimming(actor.isSubmergedInWater());
                }
                synchronized (actor) {
                    actor.setSprinting(false);
                }
                lookAt(actor, player);
                swim(actor, player);
            }
        });
        actor.playermovementThread.start();
    }

    private static void setCrouchingState(ActorEntity actor) {
        if (SpeedManagerStick.getState() == EMovementState.SNEAK) {
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
        double speed = actor.getMovementSpeed(SpeedManagerStick.getState());
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
        double speed = actor.getMovementSpeed(SpeedManagerStick.getState());
        synchronized (actor) {
            if (actor.isSubmergedInWater()) speed = actor.swimSpeed;
            actor.setVelocity(direction2.multiply(speed));
            //  System.out.println("Velocity of actor: " + direction2.multiply(speed));
        }
    }
    public static void stopMovingToBlock(ActorEntity actor)
    {
        actor.keepMovingToBlock=false;
        if ( actor.positionmovementThread != null) {
            actor.positionmovementThread.interrupt();
            actor.positionmovementThread = null;
        }
        actor.isFollowingBlock = false;
    }

    public static void stopMoving(ActorEntity actor) {
        actor.keepMovingToPlayer = false;
        if ( actor.playermovementThread != null) {
            actor. playermovementThread.interrupt();
            actor.playermovementThread = null;
        }
        actor.isFollowingPlayer = false;
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
