package xyz.brckts.portablestonecutter;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.brckts.portablestonecutter.client.gui.PortableStonecutterScreen;
import xyz.brckts.portablestonecutter.network.NetworkHandler;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(PortableStonecutter.MOD_ID)
public class PortableStonecutter
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "portable_stonecutter";

    public PortableStonecutter(FMLJavaModLoadingContext context) {
        IEventBus modBus = context.getModEventBus();
        modBus.addListener(this::clientSetup);
        RegistryHandler.init(modBus);
        NetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MenuScreens.register(RegistryHandler.PORTABLE_STONECUTTER_CONTAINER.get(), PortableStonecutterScreen::new);
    }

    public static final CreativeModeTab TAB = new CreativeModeTab("portableStonecutter") {
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get());
        }
    };
}
