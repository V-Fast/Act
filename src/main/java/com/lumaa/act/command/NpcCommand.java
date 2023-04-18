package com.lumaa.act.command;

import com.lumaa.act.entity.NPCEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NpcCommand {
    public static final String name = "npc";

    public int onRun(CommandContext<ServerCommandSource> command, String username) {
        UUID uuid = lookForPlayer(username);

        ServerCommandSource source = command.getSource();
        Vec3d pos = source.getPosition();
        NPCEntity npcEntity = new NPCEntity(source.getServer(), source.getWorld(), new GameProfile(uuid, username));

        source.getWorld().spawnEntity(npcEntity);

        List<PlayerEntity> players = command.getSource().getWorld().getPlayers().stream().filter(serverPlayerEntity -> serverPlayerEntity.getClass() == ServerPlayerEntity.class).collect(Collectors.toList());

        for (PlayerEntity p : players) {
            ((ServerPlayerEntity) p).networkHandler.sendPacket(npcEntity.createSpawnPacket());
            ((ServerPlayerEntity) p).networkHandler.sendPacket(new PlayerListS2CPacket(PlayerListS2CPacket.Action.ADD_PLAYER, npcEntity));
        }

        npcEntity.teleport(pos.getX(), pos.getY(), pos.getZ());
        source.sendMessage(Text.literal("Spawned " + username));
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
        return NpcCommand.name;
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
