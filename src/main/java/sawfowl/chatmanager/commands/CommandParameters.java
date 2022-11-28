package sawfowl.chatmanager.commands;

import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

public class CommandParameters {

	public static final Parameter.Value<ServerWorld> WORLD = Parameter.world().requirements(cause -> (cause.audience() instanceof SystemSubject || cause.audience() instanceof CommandBlock)).optional().key("World").build();

	public static final Parameter.Value<String> MESSAGE = Parameter.remainingJoinedStrings().optional().key("Message").build();

	public static final Parameter.Value<ServerPlayer> PLAYER = Parameter.player().optional().key("Player").build();

}
