package systems.canine.scheduledshutdown.command;

import java.util.function.Supplier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;

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
	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, ScheduledShutdown.MODID);
	
	private static final Supplier<SingletonArgumentInfo<ShutdownCommandArgumentType>> SHUTDOWN_COMMAND_ARGUMENT_TYPE = COMMAND_ARGUMENT_TYPES.register("shutdown_subcommand",
			() -> ArgumentTypeInfos.registerByClass(ShutdownCommandArgumentType.class, SingletonArgumentInfo.contextFree(ShutdownCommandArgumentType::newInstance)));
	
	private static final int DEFAULT_DURATION = 10;
	private static final int QUICK_DURATION = 1;
	
	// Don't ever construct this directly
	private ShutdownCommand() {}
	
	public static void init(IEventBus modBus) {
		COMMAND_ARGUMENT_TYPES.register(modBus);
		
		NeoForge.EVENT_BUS.addListener(ShutdownCommand::registerCommands);
	}
	
	private static void registerCommands(RegisterCommandsEvent event) {
		CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
		LiteralCommandNode<CommandSourceStack> mainNode = dispatcher.register(
			Commands.literal("shutdown")
				.requires(cs -> cs.hasPermission(OP_LEVEL))
				.executes(context -> initiateShutdown(context.getSource()))
				.then(
					Commands.argument("subcommand", ShutdownCommandArgumentType.newInstance())
						.executes(context -> handleSubcommand(context.getSource(), context.getArgument("subcommand", ShutdownSubcommand.class)))
				)
		);
	}
	
	private static int initiateShutdown(CommandSourceStack source) {
		source.sendSuccess(() -> Component.literal("got /shutdown (no subcommand)"), false);
		ShutdownTimer.getInstance().restart(DEFAULT_DURATION);
		return 0;
	}
	
	private static int handleSubcommand(CommandSourceStack source, ShutdownSubcommand cmd) {
		switch (cmd) {
		case ShutdownSubcommand.QUICK:
			ShutdownTimer.getInstance().restart(QUICK_DURATION);
			source.sendSuccess(() -> Component.literal("got /shutdown quick"), false);
			break;
		case ShutdownSubcommand.CANCEL:
			ShutdownTimer.getInstance().stop();
			source.sendSuccess(() -> Component.literal("got /shutdown cancel"), false);
			break;
		}
		
		return 0;
	}
}
