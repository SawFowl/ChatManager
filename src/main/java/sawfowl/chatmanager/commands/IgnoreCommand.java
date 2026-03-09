package sawfowl.chatmanager.commands;

import org.spongepowered.api.command.Command.Parameterized;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.Permissions;

public class IgnoreCommand extends AbstractCommand {

	public IgnoreCommand(ChatManager plugin) {
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException {
		ServerPlayer executor = (ServerPlayer) context.cause().audience();
		if(!context.one(CommandParameters.PLAYER).isPresent()) exception(plugin.getLocales().getAsReferenced(executor).getCommands().getExceptions().getPlayerIsNotPresent());
		ServerPlayer player = context.one(CommandParameters.PLAYER).get();
		if(executor.uniqueId().equals(player.uniqueId())) exception(plugin.getLocales().getAsReferenced(executor).getCommands().getExceptions().getIgnoreSelf());
		if(plugin.getIgnoresConfig().switchIgnore(executor, player)) {
			executor.sendMessage(plugin.getLocales().getAsReferenced(executor).getCommands().getIgnore().getIgnored(player));
		} else executor.sendMessage(plugin.getLocales().getAsReferenced(executor).getCommands().getIgnore().getNotIgnored(player));
		plugin.updateIgnores();
		return success();
	}

	@Override
	public Parameterized build() {
		return builder()
				.executionRequirements(cause -> (cause.audience() instanceof ServerPlayer && cause.hasPermission(Permissions.IGNORE)))
				.addParameter(CommandParameters.PLAYER)
				.executor(this)
				.build();
	}

}
