package xyz.brckts.portablestonecutter.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.function.Supplier;

public class MessageSelectRecipe {
    private final int recipe;

    public MessageSelectRecipe(int recipeSelected) {
        this.recipe = recipeSelected;
    }

    public static MessageSelectRecipe decode(PacketBuffer buf) {
        int recipeSelected = buf.readInt();
        return new MessageSelectRecipe(recipeSelected);
    }

    public static void encode(MessageSelectRecipe message, PacketBuffer buf) {
        buf.writeInt(message.recipe);
    }

    public static void handle(MessageSelectRecipe message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }

            if (!(player.containerMenu instanceof PortableStonecutterContainer)) {
                return;
            }

            PortableStonecutterContainer container = (PortableStonecutterContainer) player.containerMenu;
            container.selectRecipe(message.recipe);
        });
        context.setPacketHandled(true);
    }
}
