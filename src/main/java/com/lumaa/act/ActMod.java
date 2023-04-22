package com.lumaa.act;

import com.lumaa.act.command.ActorCommand;
import com.lumaa.act.item.ActItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActMod implements ModInitializer {
    public static final String MODID = "act";
    public static final Logger logger = LoggerFactory.getLogger(MODID);

    ActorCommand npc = new ActorCommand();

    @Override
    public void onInitialize() {
        npc.register();
        ActItems.registerModItems();

        // print mod motto
        print("Act as if you had friends... But you are in a singleplayer world.");
    }

    public static void print(String message) {
        logger.info("[Act] " + message);
    }
}