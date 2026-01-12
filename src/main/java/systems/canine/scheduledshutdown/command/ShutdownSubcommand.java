package systems.canine.scheduledshutdown.command;

import javax.annotation.Nullable;

import net.minecraft.util.StringRepresentable;

public enum ShutdownSubcommand implements StringRepresentable {
	QUICK(0, "quick"),
	CANCEL(1, "cancel");
	
    private final int id;
    private final String subcommand_name;
    
    private ShutdownSubcommand(int id, String name) {
    	this.id = id;
    	this.subcommand_name = name;
    }
    
    public int getId() {
        return this.id;
    }

	@Override
	public String getSerializedName() {
		return this.subcommand_name;
	}

	public static ShutdownSubcommand fromString(String commandName) {
		return byName(commandName);
	}
	
	@Nullable
	public static ShutdownSubcommand byName(@Nullable String commandName) {
		for (ShutdownSubcommand cmd : ShutdownSubcommand.values()) {
			if (cmd.subcommand_name.equalsIgnoreCase(commandName)) {
				return cmd;
			}
		}
		return null;
	}
}
