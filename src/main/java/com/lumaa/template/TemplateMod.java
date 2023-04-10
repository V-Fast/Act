package com.lumaa.template;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemplateMod implements ModInitializer {
    public Logger logger = LoggerFactory.getLogger("template");
    public String ID = "template";

    @Override
    public void onInitialize() {
        print("Template initialized");
    }

    public void print(String text) {
        logger.info("[TEMPLATE] " + text);
    }
}
