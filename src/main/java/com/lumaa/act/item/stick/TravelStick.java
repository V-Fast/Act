package com.lumaa.act.item.stick;

import com.lumaa.act.ai.ActorMovement;
import com.lumaa.act.entity.ActorEntity;
import com.lumaa.act.entity.Movement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;

import static com.lumaa.act.entity.Movement.*;

public class TravelStick extends Item {
    private ActorEntity actor=null;

    public TravelStick(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    //TODO: When R-Click, put ActorId in nbt
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ActorEntity actor && actor.canSee(user)) {
            user.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
            actor.getAi().moveToEntity(user, ActorMovement.MovementState.RUN);
            if (actor.getAi().action.getPlayerFollow()!=null) actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, actor.getAi().action.getPlayerFollow().getPos());
            setActor(actor);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }
    private void setActor(ActorEntity actor)
    {
        this.actor=actor;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        BlockPos blockPos = context.getBlockPos();
        assert MinecraftClient.getInstance().player != null;
        if (actor == null) {
            MinecraftClient.getInstance().player.sendMessage(Text.of("Right-Click on an Actor first").copy().formatted(Formatting.RED), false);
            MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1f, 1f);
        } else {
            MinecraftClient.getInstance().player.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
            actor.setVelocity(0,0,0);
            stopMoving(actor);
            moveToBlockPos(blockPos,actor);
            if (actor.getBlockPos()==blockPos)stopMoving(actor);
        }
        return super.useOnBlock(context);
    }


    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

}
