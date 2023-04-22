package com.lumaa.act.item;

import com.lumaa.act.ActMod;
import com.lumaa.act.item.stick.FollowStick;
import com.lumaa.act.item.stick.InventoryStick;
import com.lumaa.act.item.stick.LookStick;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;

public class ActItems {
    public static final Item FOLLOW_STICK = registerItem("follow_stick", new FollowStick(new Item.Settings()), ItemGroups.FUNCTIONAL);
//    public static final Item LOOK_STICK = registerItem("look_stick", new LookStick(new Item.Settings()), ItemGroups.FUNCTIONAL);
//    public static final Item INVENTORY_STICK = registerItem("inventory_stick", new InventoryStick(new Item.Settings()), ItemGroups.FUNCTIONAL);

    public static Item registerItem(String name, Item item, ItemGroup group) {
        Item newItem = Registry.register(Registries.ITEM, new Identifier(ActMod.MODID, name), item);

        // put in item group
        ItemGroupEvents.modifyEntriesEvent(group).register(content -> {
            content.add(newItem);
        });

        return newItem;
    }

    // can be used for new 1.19.3+ creative inventory system
    private static Item registerItem(String name, Item item, List<ItemGroup> tabs) {
        Item newItem = Registry.register(Registries.ITEM, new Identifier(ActMod.MODID, name), item);

        // put in item group
        for (int i = 0; i < tabs.size(); i++) {
            ItemGroupEvents.modifyEntriesEvent(tabs.get(i)).register(content -> {
                content.add(newItem);
            });
        }
        return newItem;
    }

    public static Item registerModItems() {
        return null;
    }
}
