package com.epherical.jeepershops.command;

import com.epherical.jeepershops.BozoFabric;
import com.epherical.jeepershops.ShopManager;
import com.epherical.jeepershops.menu.ShopMenu;
import com.epherical.jeepershops.shop.Shop;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class ShopCommand {

    private final BozoFabric mod;

    public ShopCommand(CommandDispatcher<CommandSourceStack> dispatcher, BozoFabric mod) {
        this.mod = mod;
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("shop")
                .then(Commands.literal("create")
                        .executes(this::createShop))
                .then(Commands.literal("open")
                        .executes(this::createShop)
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(this::openShop)))
                .then(Commands.literal("insert")
                        .then(Commands.argument("price", DoubleArgumentType.doubleArg(0.0))
                                .executes(this::insertItemIntoShop)));
        dispatcher.register(command);
    }

    private int openShop(CommandContext<CommandSourceStack> stack) {
        try {
            ServerPlayer player = stack.getSource().getPlayer();
            if (player != null && mod.getManager() != null) {
              ShopManager manager = mod.getManager();
              String arg = StringArgumentType.getString(stack, "name");
                Shop shop = manager.getShop(arg);
                if (shop != null) {
                    shop.openShop(player);
                } else {
                    player.sendSystemMessage(Component.translatable("The shop entered: %s does not exist.", arg));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }


    private int createShop(CommandContext<CommandSourceStack> stack) {
        try {
            ServerPlayer player = stack.getSource().getPlayer();
            if (player != null && mod.getManager() != null) {
                ShopManager manager = mod.getManager();
                Shop shop = manager.getOrCreateShop(player.getUUID(), player.getGameProfile().getName());
                shop.openShop(player);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private int insertItemIntoShop(CommandContext<CommandSourceStack> stack) {
        ServerPlayer player = stack.getSource().getPlayer();
        if (player != null) {
            ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
            if (itemInHand.isEmpty()) {
                player.sendSystemMessage(Component.nullToEmpty("You must be holding an item in your hand"));
                return 1;
            }
            Shop shop = mod.getManager().getOrCreateShop(player.getUUID(), player.getGameProfile().getName());
            if (shop.addItem(itemInHand, DoubleArgumentType.getDouble(stack, "price"))) {
                player.sendSystemMessage(Component.nullToEmpty("Item Added to shop."));
            } else {
                player.sendSystemMessage(Component.nullToEmpty("Shop is full, item could not be added."));
            }
        }

        return 1;
    }

}