package com.h1ggsk.baqdupe.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class BaqdupeClient implements ClientModInitializer {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public void onInitializeClient() {
        // Register the /dupe command

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dupe").executes(this::dupe)));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("dupehelp").executes(this::dupeInfo)));
    }

    private int dupe(CommandContext<FabricClientCommandSource> context) {
        if (mc.player == null) {
            return 1; // Player not loaded
        }

        // Ensure the player is holding a writable book
        if (!(mc.player.getInventory().getMainHandStack().getItem() == Items.WRITABLE_BOOK)) {
            mc.player.sendMessage(Text.of("Please hold a writable book!"));
            return 1;
        }

        // Iterate through inventory slots
        for (int i = 9; i < 44; i++) {
            if (36 + mc.player.getInventory().selectedSlot == i) continue;

            mc.player.networkHandler.sendPacket(new ClickSlotC2SPacket(
                    mc.player.currentScreenHandler.syncId,
                    mc.player.currentScreenHandler.getRevision(),
                    i,
                    1,
                    SlotActionType.THROW,
                    ItemStack.EMPTY,
                    Int2ObjectMaps.emptyMap()
            ));
        }

        // Send the BookUpdateC2SPacket
        mc.player.networkHandler.sendPacket(new BookUpdateC2SPacket(
                mc.player.getInventory().selectedSlot,
                List.of("The dupe failed."), // The player also only would see this text if the dupe failed
                Optional.of("The dupe failed, it may be patched on here!") // The player also only would see this text if the dupe failed
        ));

        mc.player.sendMessage(Text.of("Duplication packet sent!"));
        return 0;
    }

    private int dupeInfo(CommandContext<FabricClientCommandSource> context) {
        if (mc.player == null) {
            return 1; // Player not loaded
        }

        mc.player.sendMessage(Text.of("/dupe command"));
        mc.player.sendMessage(Text.of("Unknown original author, ported by Theo (@h1ggsk) from Benefit by Lefty (@leftydupes)"));
        mc.player.sendMessage(Text.of("Works on vanilla, Fabric, and Forge servers (1.20.5-1.21.1), and Paper servers running 1.20.5 through 1.21 (not 1.21.1). It does not work on Purpur or any other server plugin softwares."));
        mc.player.sendMessage(Text.of("Before you dupe make sure to log out and back in to save player data. Then hold a writable book and type \"/dupe\". You will be kicked from the server, rejoin and pick up your duped items."));
        return 0;
    }
}
