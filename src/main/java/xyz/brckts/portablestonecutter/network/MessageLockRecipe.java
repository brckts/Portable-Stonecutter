package xyz.brckts.portablestonecutter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.function.Supplier;

public class MessageLockRecipe {

    private final int recipeIndex;
    private final boolean lockStatus;
    public MessageLockRecipe(int recipeIndex, boolean lockStatus) {
        this.recipeIndex = recipeIndex;
        this.lockStatus = lockStatus;
    }

    public static MessageLockRecipe decode(FriendlyByteBuf buf) {
        int recipeIndex = buf.readInt();
        boolean lockStatus = buf.readBoolean();
        return new MessageLockRecipe(recipeIndex, lockStatus);
    }

    public static void encode(MessageLockRecipe message, FriendlyByteBuf buf) {
        buf.writeInt(message.recipeIndex);
        buf.writeBoolean(message.lockStatus);
    }

    public static void handle(MessageLockRecipe message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) {
                return;
            }

            if (!(player.containerMenu instanceof PortableStonecutterContainer)) {
                return;
            }

            int recipeIndex = message.recipeIndex;

            PortableStonecutterContainer container = (PortableStonecutterContainer) player.containerMenu;
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
