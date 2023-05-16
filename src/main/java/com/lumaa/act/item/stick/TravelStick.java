package com.lumaa.act.item.stick;

import com.lumaa.act.ai.ActorMovement;
import com.lumaa.act.entity.ActorEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.Objects;

public class TravelStick extends Item {
    public TravelStick(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ActorEntity actor && actor.canSee(user)) {
            user.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f);
            //actor.getAi().followEntity(user);
            actor.getAi().moveToEntity(user, ActorMovement.MovementState.RUN);
            if (actor.getAi().action.getPlayerFollow()!=null) actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, actor.getAi().action.getPlayerFollow().getPos());
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context)
    {
        if (context.getStack().hasNbt() && !context.getWorld().isClient) {
            int entityId = Objects.requireNonNull(context.getStack().getNbt()).getInt("ActorId");
            ActorEntity actor = (ActorEntity) context.getWorld().getEntityById(entityId);
            PlayerEntity player= MinecraftClient.getInstance().player;
            if (player!=null && actor!=null) actor.getAi().moveTo(ActorMovement.MovementState.RUN,player.getPos());
        }
        return ActionResult.FAIL;
    }


    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }

}
