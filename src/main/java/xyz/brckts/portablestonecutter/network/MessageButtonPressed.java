package xyz.brckts.portablestonecutter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

public record MessageButtonPressed(int buttonPressed) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(PortableStonecutter.MOD_ID, "button_pressed");
    public static final int CRAFT_ALL_BUTTON = 1;
    public static final int CRAFT_64_BUTTON = 2;

    public MessageButtonPressed(final FriendlyByteBuf buffer) {
        this(buffer.readInt());
    }

    @Override
    public void write(final FriendlyByteBuf buffer) {
        buffer.writeInt(buttonPressed);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(final MessageButtonPressed message, final PlayPayloadContext context) {
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

            PortableStonecutterContainer container = (PortableStonecutterContainer) player.containerMenu;
            if (message.buttonPressed == CRAFT_ALL_BUTTON) {
                container.craftAll(player);
            } else if (message.buttonPressed == CRAFT_64_BUTTON) {
                container.craft64(player);
            } else {
                PortableStonecutter.LOGGER.warn("Invalid messageId !");
            }
        });
    }
}
