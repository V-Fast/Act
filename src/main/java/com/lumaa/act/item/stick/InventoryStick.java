package com.lumaa.act.item.stick;

import com.lumaa.act.ai.ActorAction;
import com.lumaa.act.entity.ActorEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class InventoryStick extends Item {
    public InventoryStick(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (entity instanceof ActorEntity) {
            ActorEntity actor = (ActorEntity) entity;
            MinecraftClient mc = MinecraftClient.getInstance();
            mc.setScreen(new InventoryScreen(actor));
            return ActionResult.SUCCESS;
        }
        return ActionResult.FAIL;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
