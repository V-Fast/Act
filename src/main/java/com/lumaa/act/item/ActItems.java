package com.lumaa.act.item;

import com.lumaa.act.ActMod;
import com.lumaa.act.item.stick.FollowStick;
import com.lumaa.act.item.stick.LookStick;
import com.lumaa.act.item.stick.TestStick;
import com.lumaa.act.item.stick.TravelStick;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Objects;

public class ActItems {
    public static final Item FOLLOW_STICK = registerItem("follow_stick", new FollowStick(new Item.Settings()), ItemGroups.FUNCTIONAL);
    public static final Item LOOK_STICK = registerItem("look_stick", new LookStick(new Item.Settings()), ItemGroups.FUNCTIONAL);
    public static final Item TRAVEL_STICK = registerItem("travel_stick", new TravelStick(new Item.Settings()), ItemGroups.FUNCTIONAL);
//    public static final Item TEST_STICK = registerItem("test_stick", new TestStick(new Item.Settings()), ItemGroups.OPERATOR);

    public static Item registerItem(String name, Item item, ItemGroup group) {
        Item newItem = Registry.register(Registries.ITEM, new Identifier(ActMod.MODID, name), item);

        // put in item group
        ItemGroupEvents.modifyEntriesEvent(group).register(content -> content.add(newItem));

        return newItem;
    }

    // can be used for new 1.19.3+ creative inventory system
    private static Item registerItem(String name, Item item, List<ItemGroup> tabs) {
        Item newItem = Registry.register(Registries.ITEM, new Identifier(ActMod.MODID, name), item);

        // put in item group
        for (int i = 0; i < tabs.size(); i++) {
            ItemGroupEvents.modifyEntriesEvent(tabs.get(i)).register(content -> content.add(newItem));
        }
        return newItem;
    }

    public static Item registerModItems() {
        return null;
    }
}
