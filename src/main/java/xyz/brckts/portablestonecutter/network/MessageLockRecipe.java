package xyz.brckts.portablestonecutter.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.function.Supplier;

public class MessageLockRecipe {

    private final int recipeIndex;
    private final boolean lockStatus;
    public MessageLockRecipe(int recipeIndex, boolean lockStatus) {
        this.recipeIndex = recipeIndex;
        this.lockStatus = lockStatus;
    }

    public static MessageLockRecipe decode(PacketBuffer buf) {
        int recipeIndex = buf.readInt();
        boolean lockStatus = buf.readBoolean();
        return new MessageLockRecipe(recipeIndex, lockStatus);
    }

    public static void encode(MessageLockRecipe message, PacketBuffer buf) {
        buf.writeInt(message.recipeIndex);
        buf.writeBoolean(message.lockStatus);
    }

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

            int recipeIndex = message.recipeIndex;

            PortableStonecutterContainer container = (PortableStonecutterContainer) player.openContainer;
            container.setRecipeLocked(message.lockStatus);
            if(message.lockStatus) {
                container.onRecipeLocked(recipeIndex, player);
            } else {
                container.onRecipeUnlocked(player);
            }
        });
        context.setPacketHandled(true);
    }
}
