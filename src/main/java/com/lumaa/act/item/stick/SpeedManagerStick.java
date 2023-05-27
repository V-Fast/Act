package com.lumaa.act.item.stick;

import com.lumaa.act.entity.Movement;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;

public class SpeedManagerStick extends Item {
    public SpeedManagerStick(Item.Settings settings) {
        super(settings.maxCount(1));
    }
    private int state=0;
    private static Movement.EMovementState movementState = Movement.EMovementState.WALK;

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        state++;
        if (state>2) state=0;
        switch (state) {
            case 0 -> movementState = Movement.EMovementState.WALK;
            case 1 -> movementState = Movement.EMovementState.RUN;
            case 2 -> movementState = Movement.EMovementState.SNEAK;
            default -> movementState= Movement.EMovementState.WALK;
        }
        user.sendMessage(Text.of("Movement Speed State: "+movementState).copy().formatted(Formatting.YELLOW),true);
        return ActionResult.SUCCESS;
    }
    public static Movement.EMovementState getState()
    {
        return movementState;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return true;
    }
}
