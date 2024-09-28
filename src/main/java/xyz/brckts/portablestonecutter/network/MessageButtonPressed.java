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

public record MessageButtonPressed(int buttonPressed) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(PortableStonecutter.MOD_ID, "button_pressed");
    public static final CustomPacketPayload.Type<MessageButtonPressed> TYPE = new CustomPacketPayload.Type<>(ID);
    public static final StreamCodec<ByteBuf, MessageButtonPressed> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MessageButtonPressed::buttonPressed,
            MessageButtonPressed::new
    );

    public static final int CRAFT_ALL_BUTTON = 1;
    public static final int CRAFT_64_BUTTON = 2;

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(final MessageButtonPressed message, final IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        if (!(player.containerMenu instanceof PortableStonecutterContainer container)) {
            return;
        }

        switch (message.buttonPressed) {
            case CRAFT_ALL_BUTTON -> container.craftAll(player);
            case CRAFT_64_BUTTON -> container.craft64(player);
            default -> PortableStonecutter.LOGGER.warn("Invalid buttonPressed: " + message.buttonPressed);
        }
    }
}
