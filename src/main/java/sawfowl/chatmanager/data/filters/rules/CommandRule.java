package sawfowl.chatmanager.data.filters.rules;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.chatmanager.configure.ReplaceKeys;

@ConfigSerializable
public class CommandRule extends FilterRule {

	public CommandRule() {}

	@Setting("Commands")
	private List<String> commands = Arrays.asList("tell %player% Command rule is working!");

	public List<String> getCommands() {
		return commands;
	}

	public void run(ServerPlayer player, PluginContainer container) {
		if(commands != null && !commands.isEmpty()) Sponge.server().scheduler().executor(container).execute(() -> {
			commands.forEach(command -> {
				try {
					Sponge.server().commandManager().process(replace(player, command));
				} catch (CommandException e) {
					container.logger().error(e.getLocalizedMessage());
				}
			});
		});
	}

	private String replace(ServerPlayer player, String command) {
		return command.replace(ReplaceKeys.PLAYER, player.name()).replace(ReplaceKeys.UUID, player.uniqueId().toString());
	}

}
