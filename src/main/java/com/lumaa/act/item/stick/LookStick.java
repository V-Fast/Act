package com.lumaa.act.item.stick;

import com.lumaa.act.entity.ActorEntity;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.LookAtS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class LookStick extends Item {
    public LookStick(Item.Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ActorEntity) {
            user.playSound(SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1f, 1f);

            ((ActorEntity) entity).lookAtEntity(EntityAnchorArgumentType.EntityAnchor.EYES, user, EntityAnchorArgumentType.EntityAnchor.EYES);
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getStack().hasNbt() && !context.getWorld().isClient) {
            int entityId = context.getStack().getNbt().getInt("ActorId");
            ActorEntity actor = (ActorEntity) context.getWorld().getEntityById(entityId);
            actor.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, context.getHitPos());
        }
        return ActionResult.FAIL;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
