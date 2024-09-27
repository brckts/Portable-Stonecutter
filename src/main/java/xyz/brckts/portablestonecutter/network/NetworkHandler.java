package xyz.brckts.portablestonecutter.network;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
import xyz.brckts.portablestonecutter.PortableStonecutter;

public class NetworkHandler {
    public static void register(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(PortableStonecutter.MOD_ID);
        registrar.play(MessageButtonPressed.ID, MessageButtonPressed::new, handler -> handler.server(MessageButtonPressed::handle));
        registrar.play(MessageSelectRecipe.ID, MessageSelectRecipe::new, handler -> handler.server(MessageSelectRecipe::handle));
        registrar.play(MessageLockRecipe.ID, MessageLockRecipe::new, handler -> handler.server(MessageLockRecipe::handle));
    }
}
