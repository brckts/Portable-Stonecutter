package xyz.brckts.portablestonecutter.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

public record MessageLockRecipe(int recipeIndex, boolean lockStatus) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PortableStonecutter.MOD_ID, "lock_recipe");
    public static final CustomPacketPayload.Type<MessageLockRecipe> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, MessageLockRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MessageLockRecipe::recipeIndex,
            ByteBufCodecs.BOOL,
            MessageLockRecipe::lockStatus,
            MessageLockRecipe::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MessageLockRecipe message, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.containerMenu instanceof PortableStonecutterContainer container)) {
            return;
        }

        container.setRecipeLocked(message.lockStatus);

        if (message.lockStatus) {
            container.onRecipeLocked(message.recipeIndex, player);
        } else {
            container.onRecipeUnlocked(player);
        }
    }
}
