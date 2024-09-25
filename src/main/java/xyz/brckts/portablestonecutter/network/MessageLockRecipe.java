package xyz.brckts.portablestonecutter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

public record MessageLockRecipe(int recipeIndex, boolean lockStatus) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(PortableStonecutter.MOD_ID, "lock_recipe");

    public MessageLockRecipe(final FriendlyByteBuf buffer) {
        this(buffer.readInt(), buffer.readBoolean());
    }

    @Override
    public void write(final FriendlyByteBuf buffer) {
        buffer.writeInt(recipeIndex);
        buffer.writeBoolean(lockStatus);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(final MessageLockRecipe message, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            ServerPlayer player = (ServerPlayer) context.player()
                    .filter(p -> p instanceof ServerPlayer)
                    .orElse(null);
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
    }
}
