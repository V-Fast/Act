package com.lumaa.act;

import com.lumaa.act.command.NpcCommand;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActMod implements ModInitializer {
    public static final String MODID = "act";
    public static final Logger logger = LoggerFactory.getLogger(MODID);

    NpcCommand npc = new NpcCommand();

    @Override
    public void onInitialize() {
        npc.register();

        // print mod motto
        print("Act as if you had friends... But you are in a singleplayer world.");
    }

    public static void print(String message) {
        logger.info("[Act] " + message);
    }
}