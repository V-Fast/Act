package com.lumaa.act.item.stick;

import com.lumaa.act.pathfinding.Path;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

public class SpeedManagerStick extends Item {
    public SpeedManagerStick(Item.Settings settings) {
        super(settings.maxCount(1));
    }
    private int state=0;
    private static Path.EMovementState movementState = Path.EMovementState.WALK;

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        state++;
        if (state>2) state=0;
        switch (state) {
            case 0 -> movementState = Path.EMovementState.WALK;
            case 1 -> movementState = Path.EMovementState.RUN;
            case 2 -> movementState = Path.EMovementState.SNEAK;
            default -> movementState= Path.EMovementState.WALK;
        }
        user.sendMessage(Text.of("Movement Speed State: "+movementState),true);
        return ActionResult.SUCCESS;
    }
    public static Path.EMovementState getState()
    {
        return movementState;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
