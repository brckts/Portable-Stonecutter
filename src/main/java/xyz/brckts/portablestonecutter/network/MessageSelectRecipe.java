package xyz.brckts.portablestonecutter.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

public record MessageSelectRecipe(int recipe) implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(PortableStonecutter.MOD_ID, "select_recipe");
    public MessageSelectRecipe(final FriendlyByteBuf buffer) {
        this(buffer.readInt());
    }

    @Override
    public void write(final FriendlyByteBuf buffer) {
        buffer.writeInt(recipe);
    }

    @Override
    public ResourceLocation id() {
        return ID;
    }

    public static void handle(final MessageSelectRecipe message, final PlayPayloadContext context) {
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
            container.selectRecipe(message.recipe);
        });
    }
}
