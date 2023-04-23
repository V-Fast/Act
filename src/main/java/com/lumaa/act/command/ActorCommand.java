package com.lumaa.act.command;

import com.lumaa.act.entity.ActorEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;

public class ActorCommand {
    public static final String name = "actor";

    public int onRun(CommandContext<ServerCommandSource> command, String username) {
        UUID uuid = lookForPlayer(username);

        ServerCommandSource source = command.getSource();
        Vec3d pos = source.getPosition();
        ActorEntity actorEntity = new ActorEntity(source.getServer(), source.getWorld(), new GameProfile(uuid, username));

        List<ServerPlayerEntity> players = command.getSource().getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getClass() == ServerPlayerEntity.class).toList();
        for (ServerPlayerEntity p : players) {
            p.networkHandler.sendPacket(actorEntity.createSpawnPacket());
            p.networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, actorEntity));
        }

        source.getWorld().spawnEntity(actorEntity);

        ServerCommandSource s = command.getSource();
//        actorEntity.teleport(s.getWorld(), pos.getX(), pos.getY(), pos.getZ(), (s.getEntity() != null) ? s.getEntity().getYaw() : 0, (s.getEntity() != null) ? s.getEntity().getPitch() : 0);
        source.sendMessage(Text.literal("Spawned " + username));
        actorEntity.getAi().crawlTo(new BlockPos((int) pos.getX(), (int) pos.getY(), (int) pos.getZ()));
        return 1;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> argument() {
        return CommandManager.argument("ign", MessageArgumentType.message());
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal(getName())
                    .then(argument().executes(context -> onRun(context, MessageArgumentType.getMessage(context, "ign").getString()))));
        });
    }

    public String getName() {
        return ActorCommand.name;
    }

    public UUID lookForPlayer(String arg) {
        try {
            URL url = new URL("https://minecraft-api.com/api/uuid/" + arg);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            if (response.toString() == "Player not found !") { // api string
                throw new Exception("No players has been found using the username: " + arg);
            }

            return UUID.fromString(formatUuid(response.toString()));
        } catch (Exception e) {
            return UUID.randomUUID();
            // do literally nothing about it lmao
        }
    }

    public static String formatUuid(String uuidString) {
        String formattedUuidString = uuidString.replaceAll(
                "(.{8})(.{4})(.{4})(.{4})(.+)",
                "$1-$2-$3-$4-$5"
        );
        UUID uuid = UUID.fromString(formattedUuidString);
        return uuid.toString();
    }
}
