package xyz.brckts.portablestonecutter;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import xyz.brckts.portablestonecutter.network.NetworkHandler;
import xyz.brckts.portablestonecutter.util.RegistryHandler;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("portable_stonecutter")
public class PortableStonecutter
{
    public static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "portable_stonecutter";

    public PortableStonecutter() {
        RegistryHandler.init();
        NetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) { }

    private void doClientStuff(final FMLClientSetupEvent event) { }

    public static final ItemGroup TAB = new ItemGroup("portableStonecutter") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(RegistryHandler.PORTABLE_STONECUTTER.get());
        }
    };
}
