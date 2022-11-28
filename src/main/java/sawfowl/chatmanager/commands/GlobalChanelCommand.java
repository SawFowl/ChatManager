package sawfowl.chatmanager.commands;

import java.util.Locale;
import java.util.function.Predicate;

import org.spongepowered.api.command.Command.Parameterized;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.server.ServerWorld;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.data.Chanel;
import sawfowl.chatmanager.utils.ChatUtils;
import sawfowl.chatmanager.utils.FilterResult;

public class GlobalChanelCommand extends AbstractCommand {

	public GlobalChanelCommand(ChatManager plugin, Chanel chanel) {
		super(plugin, chanel);
	}

	@Override
	public CommandResult execute(CommandContext context) throws CommandException {
		Audience audience = context.cause().audience();
		Locale locale = audience instanceof LocaleSource ? ((LocaleSource) audience).locale() : Locales.DEFAULT;
		if(!context.one(CommandParameters.MESSAGE).isPresent()) exception(plugin.getLocales().getText(locale, LocalesPaths.MESSAGE_IS_NOT_PRESENT));
		ServerWorld world = audience instanceof Locatable ? ((Locatable) audience).serverLocation().world() : getWorldForConsole();
		Predicate<ServerPlayer> predicate = player -> (chanel.hasRecievePermission(player));
		Component message = toText(context.one(CommandParameters.MESSAGE).get());
		boolean isPlayer = audience instanceof ServerPlayer;
		if(isPlayer) {
			if(plugin.getConfig().getAntiSpamSection().isEnable() && antiSpam((ServerPlayer) audience)) {
				audience.sendMessage(plugin.getLocales().getText(locale, LocalesPaths.ANTISPAM));
				return success();
			}
			predicate = predicate.and(ChatUtils.getNotIgnores((ServerPlayer) audience, plugin.getIgnoresConfig()));
			FilterResult filterResult = ChatUtils.getFilterResult(plugin.getLocales(), plugin.getPluginContainer(), (ServerPlayer) audience, message, plugin.getConfig().getFilters(), chanel);
			if(filterResult.isDontSendMessage()) return success();
			if(filterResult.isShowOnlySelf()) predicate = showOnlySelf((ServerPlayer) audience);
			if(filterResult.getMessage().isPresent()) {
				message = filterResult.getMessage().get();
			} else return success();
		}
		message = message(context.cause(), audience, world, message);
		sendMessage(message, toText(context.one(CommandParameters.MESSAGE).get()), predicate, isPlayer, audience);
		playerMention(message, audience, predicate);
		return success();
	}

	@Override
	public Parameterized build() {
		return builder()
				.executionRequirements(cause -> (!chanel.isNeedPerm() || cause.hasPermission(chanel.getSendPermission())))
				.addParameter(CommandParameters.MESSAGE)
				.executor(this)
				.build();
	}

}
