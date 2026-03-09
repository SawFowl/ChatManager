package sawfowl.chatmanager.commands;

import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.util.locale.LocaleSource;

import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.Permissions;

public class ReloadCommand extends AbstractCommand {

	public ReloadCommand(ChatManager plugin) {
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException {
		plugin.reload();
		context.cause().audience().sendMessage(plugin.getLocales().getAsReferenced(context.subject() instanceof LocaleSource localeSource ? localeSource.locale() : plugin.getLocales().getSystemOrDefaultLocale()).getCommands().getReload());
		return success();
	}

	@Override
	public Parameterized build() {
		return builder()
				.permission(Permissions.RELOAD)
				.executor(this)
				.build();
	}

}
