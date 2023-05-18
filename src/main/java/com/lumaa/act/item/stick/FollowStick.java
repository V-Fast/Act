package com.lumaa.act.item.stick;

import com.lumaa.act.ai.ActorAction;
import com.lumaa.act.entity.ActorEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class FollowStick extends Item {
    public FollowStick(Settings settings) {
        super(settings.maxCount(1));
    }

    //TODO: Make it actually Follow (fix r-click twice to follow)
    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ActorEntity) {
            ActorEntity actor = (ActorEntity) entity;
            boolean follows = actor.getAi().action.getAction().equals(ActorAction.Actions.FOLLOW);

            user.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, follows ? 0.8f : 1f);

            if (follows) {
                actor.getAi().action.setAction(ActorAction.Actions.NONE);
            } else {
                actor.getAi().followEntity(user);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
