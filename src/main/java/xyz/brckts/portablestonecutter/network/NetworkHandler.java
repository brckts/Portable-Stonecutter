package xyz.brckts.portablestonecutter.network;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import xyz.brckts.portablestonecutter.PortableStonecutter;

import java.util.Optional;

import static net.minecraftforge.fml.network.NetworkDirection.PLAY_TO_SERVER;

public class NetworkHandler {

    public static SimpleChannel channel;
    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";
    public static final int BUTTON_PRESSED_MESSAGE_ID = 12;
    public static final ResourceLocation simpleChannelRL = new ResourceLocation(PortableStonecutter.MOD_ID, "network");

    public static void init() {
        channel = NetworkRegistry.newSimpleChannel(simpleChannelRL, () -> MESSAGE_PROTOCOL_VERSION, MessageHandlerOnClient::isThisProtocolAcceptedByClient, MessageHandlerOnClient::isThisProtocolAcceptedByClient);

        channel.registerMessage(BUTTON_PRESSED_MESSAGE_ID, MessagePortableStonecutterButtonPressed.class, MessagePortableStonecutterButtonPressed::encode, MessagePortableStonecutterButtonPressed::decode, MessagePortableStonecutterButtonPressed::handle, Optional.of(PLAY_TO_SERVER));
    }
}
