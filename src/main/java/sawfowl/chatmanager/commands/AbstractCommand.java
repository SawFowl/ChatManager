package sawfowl.chatmanager.commands;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.SystemSubject;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.command.CommandCause;
import org.spongepowered.api.command.Command.Builder;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.locale.LocaleSource;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.server.ServerWorld;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.Permissions;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.chatmanager.data.Chanel;
import sawfowl.chatmanager.utils.ChatUtils;
import sawfowl.localeapi.api.TextUtils;

public abstract class AbstractCommand implements CommandExecutor {


	final Chanel chanel;
	final ChatManager plugin;

	public AbstractCommand(ChatManager plugin) {
		this.plugin = plugin;
		chanel = null;
	}

	public AbstractCommand(ChatManager plugin, Chanel chanel) {
		this.plugin = plugin;
		this.chanel = chanel;
	}

	public abstract Command.Parameterized build();

	Builder builder() {
		return Command.builder();
	}

	CommandResult success() {
		return CommandResult.success();
	}

	CommandException exception(Component text) throws CommandException {
		throw new CommandException(text);
	}

	Component toText(String string) {
		try {
			return GsonComponentSerializer.gson().deserialize(string);
		} catch (Exception e) {
			return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
		}
	}

	void playerMention(Component component, Audience audience, Predicate<ServerPlayer> predicate) {
		if(component.toString().contains("@")) {
			String stringMessage = TextUtils.serializeLegacy(component);
			boolean isPlayer = audience instanceof ServerPlayer;
			ServerPlayer player = isPlayer ? (ServerPlayer) audience : null;
			Sponge.server().onlinePlayers().stream().filter(predicate).filter(p -> (stringMessage.contains("@" + p.name()) && (!isPlayer || !p.name().equals(player.name())))).findFirst().ifPresent(p -> {
				p.playSound(Sound.sound(plugin.getConfig().getSound(), Sound.Source.VOICE, 100, 50));
				p.sendMessage(isPlayer ? TextUtils.replaceToComponents(plugin.getLocales().getText(p.locale(), LocalesPaths.MENTION_BY_PLAYER), new String[]{ReplaceKeys.PLAYER}, new Component[] {player.customName().isPresent() ? player.customName().get().get() : Component.text(player.name())}) : plugin.getLocales().getText(p.locale(), LocalesPaths.MENTION_BY_NOT_PLAYER));
			});
		}
	}

	Component message(CommandCause cause, Audience audience, ServerWorld world, Component text) throws CommandException {
		Locale locale = audience instanceof LocaleSource ? ((LocaleSource) audience).locale() : Locales.DEFAULT;
		if(audience instanceof ServerPlayer) {
			ServerPlayer player = (ServerPlayer) audience;
			Component message = ChatUtils.showItem(player, !player.hasPermission(Permissions.STYLE) ? toText(TextUtils.clearDecorations(text)) : text);
			return chanel.getChatFormatter().buildFormatForPlayer(player, message);
		}
		if(audience instanceof CommandBlock) return chanel.getChatFormatter().buildFormatForCommandBlock(chanel, world, text);
		if(audience instanceof SystemSubject) return chanel.getChatFormatter().buildFormatForConsole(chanel, world, text);
		throw exception(TextUtils.replace(plugin.getLocales().getText(locale, LocalesPaths.UNKNOWN_SENDER), new String[]{ReplaceKeys.SENDER}, new String[] {audience.getClass().getName()}));
	}

	void sendMessage(Component message, Component original, Predicate<ServerPlayer> filter, boolean isPlayer, Audience player) {
		Sponge.systemSubject().sendMessage(message);
		Sponge.server().onlinePlayers().stream().filter(filter).forEach(p -> {
			p.sendMessage(message);
		});
		if(isPlayer) Sponge.server().onlinePlayers().stream().filter(filter.negate()).filter(p -> (p.hasPermission(Permissions.CHAT_SPY))).forEach(p -> {
			p.sendMessage(TextUtils.deserializeLegacy("&8[&dSPY&8]&e ").hoverEvent(HoverEvent.showText(chanel.getChatFormatter().getTime(p.locale()).color(TextColor.color(255, 255, 0)))).append(TextUtils.deserializeLegacy(" &7" + TextUtils.clearDecorations(message)).hoverEvent(HoverEvent.showText(original))));
		});
	}

	ServerWorld defaultWorld() {
		return Sponge.server().worldManager().defaultWorld();
	}

	ServerWorld getWorldForConsole() {
		return !chanel.getAllowedWorlds().isPresent() || chanel.getAllowedWorlds().get().isEmpty() ? defaultWorld() : chanel.getAllowedWorlds().get().stream().filter(key -> (Sponge.server().worldManager().world(ResourceKey.resolve(key)).isPresent())).map(key -> (Sponge.server().worldManager().world(ResourceKey.resolve(key)).get())).findFirst().orElse(defaultWorld());
	}

	static Predicate<ServerPlayer> showOnlySelf(ServerPlayer sender) {
		return player -> (player.uniqueId().equals(sender.uniqueId()));
	}

	boolean antiSpam(ServerPlayer player) {
		long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
		if(player.hasPermission(Permissions.ALLOW_SPAM)) return false;
		if(!plugin.getAntiSpamMap().containsKey(player.uniqueId())) {
			plugin.getAntiSpamMap().put(player.uniqueId(), currentTime);
		} else {
			if(currentTime - plugin.getAntiSpamMap().get(player.uniqueId()) > plugin.getConfig().getAntiSpamSection().getDelay()) {
				plugin.getAntiSpamMap().remove(player.uniqueId());
				plugin.getAntiSpamMap().put(player.uniqueId(), currentTime);
			} else return true;
		}
		return false;
	}

}
