package systems.canine.scheduledshutdown.command;

import java.io.IOException;
import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import systems.canine.scheduledshutdown.ScheduledShutdown;
import systems.canine.scheduledshutdown.ShutdownTimer;

/*
 * /shutdown
 * /shutdown quick
 * /shutdown cancel
 */
public class ShutdownCommand {
    private static final int OP_LEVEL = 4;
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister
        .create(Registries.COMMAND_ARGUMENT_TYPE, ScheduledShutdown.MODID);

    private static final Supplier<SingletonArgumentInfo<ShutdownCommandArgumentType>> SHUTDOWN_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES
        .register("shutdown_subcommand",
            () -> ArgumentTypeInfos.registerByClass(ShutdownCommandArgumentType.class,
                SingletonArgumentInfo.contextFree(ShutdownCommandArgumentType::newInstance)));

    private static final int DEFAULT_DURATION = 10 * 60;
    private static final int QUICK_DURATION = 100;

    private static final String NO_RESTART_FILE = "minecraft.disable";

    // Don't ever construct this directly
    private ShutdownCommand() {
    }

    public static void init(IEventBus modBus) {
        COMMAND_ARGUMENT_TYPES.register(modBus);

        NeoForge.EVENT_BUS.addListener(ShutdownCommand::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("shutdown")
                .requires(cs -> cs.hasPermission(OP_LEVEL))
                .executes(context -> initiateShutdown(context.getSource()))
                .then(
                    Commands.argument("subcommand", ShutdownCommandArgumentType.newInstance())
                        .executes(context -> handleSubcommand(context.getSource(),
                            context.getArgument("subcommand", ShutdownSubcommand.class)))));
    }

    private static void createNoRestartFile(CommandSourceStack source) {
        try {
            source.getServer().getFile(NO_RESTART_FILE).toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeNoRestartFile(CommandSourceStack source) {
        source.getServer().getFile(NO_RESTART_FILE).toFile().delete();
    }

    private static int initiateShutdown(CommandSourceStack source) {
        createNoRestartFile(source);
        ShutdownTimer.getInstance().restart(DEFAULT_DURATION);
        source.sendSuccess(() -> Component.literal("Started shutdown timer."), false);
        return 0;
    }

    private static int handleSubcommand(CommandSourceStack source, ShutdownSubcommand cmd) {
        switch (cmd) {
        case ShutdownSubcommand.QUICK:
            ShutdownTimer.getInstance().restart(QUICK_DURATION);
            createNoRestartFile(source);
            source.sendSuccess(() -> Component.literal("Started short shutdown timer."), false);
            break;
        case ShutdownSubcommand.CANCEL:
            removeNoRestartFile(source);
            ShutdownTimer.getInstance().cancel();
            source.sendSuccess(() -> Component.literal("Stopped oustanding shutdown timer, if there is one."), false);
            break;
        }

        return 0;
    }
}
