package com.lumaa.act.item.stick;

import com.lumaa.act.ai.ActorAction;
import com.lumaa.act.entity.ActorEntity;
import com.lumaa.act.entity.Movement;
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

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ActorEntity actor) {
            boolean follows = actor.getAi().action.getAction().equals(ActorAction.Actions.FOLLOW);
            actor.isFollowingPlayer = !actor.isFollowingPlayer; // Update the isFollowinglayer state for this actor
            user.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, follows ? 0.8f : 1f);

            if (actor.isFollowingPlayer) {
                Movement.moveToPlayer(user, actor);
            } else {
                Movement.stopMoving(actor);
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
