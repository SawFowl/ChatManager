package sawfowl.chatmanager.listeners;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.configure.ReplaceKeys;

public class CommandListener {

	private final ChatManager plugin;
	public CommandListener(ChatManager plugin) {
		this.plugin = plugin;
	}

	@Listener
	public void onCommand(ExecuteCommandEvent.Pre event, @First ServerPlayer player) {
		plugin.getConfig().getBlockCommands().stream().filter(c -> (event.command().startsWith(c))).findFirst().ifPresent(command -> {
			Sponge.server().onlinePlayers().stream().filter(p -> (event.arguments().contains(p.name()))).findFirst().ifPresent(p -> {
				if(plugin.getIgnoresConfig().isIgnore(p, player)) {
					event.setCancelled(true);
					player.sendMessage(plugin.getLocales().getText(player.locale(), LocalesPaths.IGNORED_COMMAND).replace(ReplaceKeys.PLAYER, p.name()).get());
				}
			});
		});
	}

}
