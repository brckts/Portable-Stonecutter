package xyz.brckts.portablestonecutter.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import xyz.brckts.portablestonecutter.PortableStonecutter;
import xyz.brckts.portablestonecutter.containers.PortableStonecutterContainer;

import java.util.function.Supplier;


public class MessageButtonPressed {

    private final int buttonPressed;
    public static final int CRAFT_ALL_BUTTON = 1;
    public static final int CRAFT_64_BUTTON = 2;

    public MessageButtonPressed(int buttonPressedIn) {
        this.buttonPressed = buttonPressedIn;
    }

    public static MessageButtonPressed decode(PacketBuffer buf) {
        int buttonPressed = buf.readInt();
        return new MessageButtonPressed(buttonPressed);
    }

    public static void encode(MessageButtonPressed message, PacketBuffer buf) {
        buf.writeInt(message.buttonPressed);
    }

    public static void handle(MessageButtonPressed message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            if (player == null) {
                return;
            }

            if (!(player.openContainer instanceof PortableStonecutterContainer)) {
                return;
            }

            PortableStonecutterContainer container = (PortableStonecutterContainer) player.openContainer;
            if (message.buttonPressed == CRAFT_ALL_BUTTON) {
                container.craftAll(player);
            } else if (message.buttonPressed == CRAFT_64_BUTTON) {
                container.craft64(player);
            } else {
                PortableStonecutter.LOGGER.warn("Invalid messageId !");
            }
        });
        context.setPacketHandled(true);
    }
}
