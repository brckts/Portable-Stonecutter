package xyz.brckts.portablestonecutter.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.function.Supplier;

public class MessageLockRecipe {

    public MessageLockRecipe() {

    }

    public static MessageLockRecipe decode(PacketBuffer buf) {
        return new MessageLockRecipe();
    }

    public static void encode(MessageLockRecipe message, PacketBuffer buf) { }

    public static void handle(MessageLockRecipe message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();;

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }

            if (!(player.openContainer instanceof PortableStonecutterContainer)) {
                return;
            }

            PortableStonecutterContainer container = (PortableStonecutterContainer) player.openContainer;
            container.toggleRecipeLock();
        });
        context.setPacketHandled(true);
    }
}
