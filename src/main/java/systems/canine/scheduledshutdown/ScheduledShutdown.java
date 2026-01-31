package systems.canine.scheduledshutdown;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import systems.canine.scheduledshutdown.command.ShutdownCommand;

@Mod(ScheduledShutdown.MODID)
public class ScheduledShutdown {
    public static final String MODID = "scheduledshutdown";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ScheduledShutdown(IEventBus modEventBus, ModContainer modContainer) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LOGGER.info("Not doing anything -- ScheduledShutdown is running on the client");
            return;
        }

        NeoForge.EVENT_BUS.register(ShutdownTimer.getInstance());

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ShutdownCommand.init(modEventBus);
    }
}