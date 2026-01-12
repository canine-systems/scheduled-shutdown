package systems.canine.scheduledshutdown.command;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

public class ShutdownCommandArgumentType implements ArgumentType<ShutdownSubcommand> {
	public static final DynamicCommandExceptionType ERROR_INVALID = new DynamicCommandExceptionType(
		obj -> Component.translatableEscape("argument.shutdown.invalid", obj)
	);
	
	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
		if (context.getSource() instanceof CommandSourceStack) {
			return SharedSuggestionProvider.suggest(Stream.of(ShutdownSubcommand.values()).map(ShutdownSubcommand::getSerializedName).toList(), builder);
		} else if (context.getSource() instanceof SharedSuggestionProvider) {
			SharedSuggestionProvider provider = (SharedSuggestionProvider) context.getSource();
			return provider.customSuggestion(context);
		}
		return Suggestions.empty();
	}

	@Override
	public ShutdownSubcommand parse(StringReader reader) throws CommandSyntaxException {
		String s = reader.readUnquotedString();
		ShutdownSubcommand subcommand = ShutdownSubcommand.byName(s);
		if (subcommand == null) {
			throw ERROR_INVALID.createWithContext(reader, s);
		}
		return subcommand;
	}
	
	public static ShutdownCommandArgumentType newInstance() {
		return new ShutdownCommandArgumentType();
	}
}
