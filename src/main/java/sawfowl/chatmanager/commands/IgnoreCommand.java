package sawfowl.chatmanager.commands;

import org.spongepowered.api.command.Command.Parameterized;

import java.util.Arrays;

import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.Permissions;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.localeapi.api.TextUtils;

public class IgnoreCommand extends AbstractCommand {

	public IgnoreCommand(ChatManager plugin) {
		super(plugin);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException {
		ServerPlayer executor = (ServerPlayer) context.cause().audience();
		if(!context.one(CommandParameters.PLAYER).isPresent()) exception(toText("Нужно указать ник игрока. Игрок при этом должен быть онлайн."));
		ServerPlayer player = context.one(CommandParameters.PLAYER).get();
		if(executor.uniqueId().equals(player.uniqueId())) exception(toText("Нельзя игнорировать себя."));
		if(plugin.getIgnoresConfig().switchIgnore(executor, player)) {
			executor.sendMessage(plugin.getLocales().getTextReplaced1(executor.locale(), TextUtils.replaceMap(Arrays.asList(ReplaceKeys.PLAYER), Arrays.asList(player.name())), LocalesPaths.IGNORED));
		} else executor.sendMessage(plugin.getLocales().getTextReplaced1(executor.locale(), TextUtils.replaceMap(Arrays.asList(ReplaceKeys.PLAYER), Arrays.asList(player.name())), LocalesPaths.NOT_IGNORED));
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
