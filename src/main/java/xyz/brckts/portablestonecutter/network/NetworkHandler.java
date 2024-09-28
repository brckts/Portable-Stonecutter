package xyz.brckts.portablestonecutter.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {
    public static final String MESSAGE_PROTOCOL_VERSION = "1";

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MESSAGE_PROTOCOL_VERSION);
        registrar.playToServer(MessageButtonPressed.TYPE, MessageButtonPressed.STREAM_CODEC, MessageButtonPressed::handle);
        registrar.playToServer(MessageSelectRecipe.TYPE, MessageSelectRecipe.STREAM_CODEC, MessageSelectRecipe::handle);
        registrar.playToServer(MessageLockRecipe.TYPE, MessageLockRecipe.STREAM_CODEC, MessageLockRecipe::handle);
    }
}
