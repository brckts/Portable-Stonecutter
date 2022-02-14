package xyz.brckts.portablestonecutter.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import java.util.Optional;

import static net.minecraftforge.network.NetworkDirection.PLAY_TO_SERVER;

public class NetworkHandler {

    public static SimpleChannel channel;
    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";
    public static final int BUTTON_PRESSED_MESSAGE_ID = 0;
    public static final int RECIPE_SELECTED_MESSAGE_ID = 1;
    public static final int RECIPE_LOCK_MESSAGE_ID = 2;
    public static final ResourceLocation simpleChannelRL = new ResourceLocation(PortableStonecutter.MOD_ID, "network");

    public static void init() {
        channel = NetworkRegistry.newSimpleChannel(simpleChannelRL, () -> MESSAGE_PROTOCOL_VERSION, it -> true, it -> true);

        channel.registerMessage(BUTTON_PRESSED_MESSAGE_ID, MessageButtonPressed.class, MessageButtonPressed::encode, MessageButtonPressed::decode, MessageButtonPressed::handle, Optional.of(PLAY_TO_SERVER));
        channel.registerMessage(RECIPE_SELECTED_MESSAGE_ID, MessageSelectRecipe.class, MessageSelectRecipe::encode, MessageSelectRecipe::decode, MessageSelectRecipe::handle, Optional.of(PLAY_TO_SERVER));
        channel.registerMessage(RECIPE_LOCK_MESSAGE_ID, MessageLockRecipe.class, MessageLockRecipe::encode, MessageLockRecipe::decode, MessageLockRecipe::handle, Optional.of(PLAY_TO_SERVER));
    }
}
