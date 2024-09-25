package xyz.brckts.portablestonecutter.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class NetworkHandler {
    public static final String MESSAGE_PROTOCOL_VERSION = "1.0";

    public static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(MESSAGE_PROTOCOL_VERSION);
        registrar.play(MessageButtonPressed.ID, MessageButtonPressed::new, handler -> handler.server(MessageButtonPressed::handle));
        registrar.play(MessageSelectRecipe.ID, MessageSelectRecipe::new, handler -> handler.server(MessageSelectRecipe::handle));
        registrar.play(MessageLockRecipe.ID, MessageLockRecipe::new, handler -> handler.server(MessageLockRecipe::handle));
    }
}
