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

public record MessageSelectRecipe(int recipe) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PortableStonecutter.MOD_ID, "select_recipe");
    public static final CustomPacketPayload.Type<MessageSelectRecipe> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, MessageSelectRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MessageSelectRecipe::recipe,
            MessageSelectRecipe::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MessageSelectRecipe message, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.containerMenu instanceof PortableStonecutterContainer container)) {
            return;
        }

        container.selectRecipe(message.recipe);
    }
}
