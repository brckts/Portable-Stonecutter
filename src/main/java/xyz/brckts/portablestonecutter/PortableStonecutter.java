package xyz.brckts.portablestonecutter;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.brckts.portablestonecutter.client.gui.PortableStonecutterScreen;
import xyz.brckts.portablestonecutter.network.NetworkHandler;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PortableStonecutter.MOD_ID)
public class PortableStonecutter {
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "portable_stonecutter";

    public PortableStonecutter(IEventBus modBus) {
        modBus.addListener(this::registerMenuScreens);
        RegistryHandler.init(modBus);
        modBus.addListener(NetworkHandler::register);
    }

    private void registerMenuScreens(final RegisterMenuScreensEvent event) {
        event.register(RegistryHandler.PORTABLE_STONECUTTER_CONTAINER.get(), PortableStonecutterScreen::new);
    }
}
