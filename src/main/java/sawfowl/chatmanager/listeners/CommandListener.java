package sawfowl.chatmanager.listeners;

import java.util.Arrays;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.localeapi.api.TextUtils;

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
					player.sendMessage(plugin.getLocales().getTextReplaced1(player.locale(), TextUtils.replaceMap(Arrays.asList(ReplaceKeys.PLAYER), Arrays.asList(p.name())), LocalesPaths.IGNORED_COMMAND));
				}
			});
		});
	}

}
