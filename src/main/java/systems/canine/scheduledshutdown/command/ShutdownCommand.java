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
    private static final String NEEDS_UPDATE_FILE = "minecraft.needs-update";

    // Don't ever construct this directly
    private ShutdownCommand() {
    }

    public static void initCommon() {
        NeoForge.EVENT_BUS.addListener(ShutdownCommand::registerCommands);
    }

    public static void initServer(IEventBus modBus) {
        COMMAND_ARGUMENT_TYPES.register(modBus);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(
            Commands.literal("shutdown")
                .requires(cs -> cs.hasPermission(OP_LEVEL))
                .executes(context -> handleShutdown(context.getSource(), false))
                .then(
                    Commands.argument("subcommand", ShutdownCommandArgumentType.newInstance())
                        .executes(context -> handleSubcommand(context.getSource(), false,
                            context.getArgument("subcommand", ShutdownSubcommand.class)))));

        dispatcher.register(
            Commands.literal("update")
                .requires(cs -> cs.hasPermission(OP_LEVEL))
                .executes(context -> handleShutdown(context.getSource(), true))
                .then(
                    Commands.argument("subcommand", ShutdownCommandArgumentType.newInstance())
                        .executes(context -> handleSubcommand(context.getSource(), true,
                            context.getArgument("subcommand", ShutdownSubcommand.class)))));
    }

    private static int handleShutdown(CommandSourceStack source, boolean needs_update) {
        return startShutdown(source, DEFAULT_DURATION, needs_update);
    }

    private static int handleSubcommand(CommandSourceStack source, boolean needs_update, ShutdownSubcommand cmd) {
        switch (cmd) {
        case ShutdownSubcommand.QUICK:
            return startShutdown(source, QUICK_DURATION, needs_update);
        case ShutdownSubcommand.CANCEL:
            return cancelShutdown(source);
        default:
            return 1;
        }
    }

    private static int startShutdown(CommandSourceStack source, int duration, boolean needs_update) {
        createFile(source, NO_RESTART_FILE);
        if (needs_update) {
            createFile(source, NEEDS_UPDATE_FILE);
        }
        ShutdownTimer.getInstance().start(duration);
        source.sendSuccess(() -> Component.literal("Started shutdown timer."), false);
        return 0;
    }

    private static int cancelShutdown(CommandSourceStack source) {
        removeFile(source, NO_RESTART_FILE);
        removeFile(source, NEEDS_UPDATE_FILE);
        ShutdownTimer.getInstance().cancel();
        source.sendSuccess(() -> Component.literal("Stopped oustanding shutdown timer, if there is one."), false);
        return 0;
    }

    private static void createFile(CommandSourceStack source, String name) {
        try {
            source.getServer().getFile(name).toFile().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void removeFile(CommandSourceStack source, String name) {
        source.getServer().getFile(name).toFile().delete();
    }
}