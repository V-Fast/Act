package com.lumaa.act.command;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public interface ISimpleCommand {
    int onRun(CommandContext<ServerCommandSource> command);

    default void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(getName()).executes(this::onRun));
        });
    }

    String getName();
}
