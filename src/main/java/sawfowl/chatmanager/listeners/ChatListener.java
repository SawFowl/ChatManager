package sawfowl.chatmanager.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.adventure.ChatTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Locatable;

import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;

import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.Permissions;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.chatmanager.data.Chanel;
import sawfowl.chatmanager.utils.ChatUtils;
import sawfowl.chatmanager.utils.FilterResult;
import sawfowl.localeapi.api.TextUtils;

public class ChatListener {

	private final ChatManager plugin;
	private final boolean regions;
	private Map<String, MessageSettings> map = new HashMap<>();
	public ChatListener(ChatManager plugin, boolean regions) {
		this.plugin = plugin;
		this.regions = regions;
		Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).interval(10, TimeUnit.SECONDS).execute(() -> map.entrySet().removeIf(entry -> entry.getValue().isExpired())).build());
	}


	@Listener(order = Order.LAST)
	public void onMessage(PlayerChatEvent.Decorate event, @First Locatable locatable) {
		Chanel chanel = plugin.getConfig().getChanel(ChatUtils.firstSymbol(event.message()));
		boolean isPlayer = locatable instanceof ServerPlayer;
		ServerPlayer player = isPlayer ? (ServerPlayer) locatable : null;
		if(isPlayer && !chanel.hasSendPermission(player)) chanel = plugin.getConfig().getDefaultChanel();
		Component message = ChatUtils.removeFirstSymbol(event.message(), chanel.getSymbol());
		Predicate<ServerPlayer> predicate = getReceiversFilter(chanel, locatable);
		if(isPlayer) {
			predicate = predicate.and(ChatUtils.getNotIgnores(player, plugin.getIgnoresConfig()));
			if(!player.hasPermission(Permissions.STYLE)) message = Component.text(TextUtils.clearDecorations(message));
			FilterResult filterResult = ChatUtils.getFilterResult(plugin.getLocales(), plugin.getPluginContainer(), player, message, plugin.getConfig().getFilters(), chanel);
			if(filterResult.isDontSendMessage() || !filterResult.getMessage().isPresent()) {
				message = Component.empty();
				return;
			}
			if(isPlayer && plugin.getConfig().getAntiSpamSection().isEnable() && antiSpam(player)) {
				message = Component.empty();
				player.sendMessage(plugin.getLocales().getComponent(player.locale(), LocalesPaths.ANTISPAM));
				return;
			}
			message = filterResult.getMessage().get();
			message = ChatUtils.showItem(player, message);
			if(filterResult.isShowOnlySelf()) predicate = audience -> (!(audience instanceof ServerPlayer) || ((ServerPlayer) audience).uniqueId().equals(player.uniqueId()));
		}
		String key = (isPlayer ? player.uniqueId().toString() : locatable.blockPosition().toString()) + TextUtils.clearDecorations(message);
		if(map.containsKey(key)) map.remove(key);
		map.put(key, new MessageSettings(chanel, predicate));
		if(isPlayer) chatSpy(predicate, chanel, player, message, event.originalMessage());
		event.setMessage(message);
	}

	@Listener(order = Order.LAST)
	public void onMessage(PlayerChatEvent.Submit event, @First Locatable locatable) {
		if(TextUtils.clearDecorations(event.message()).isEmpty()) {
			event.setCancelled(true);
			return;
		}
		boolean isPlayer = locatable instanceof ServerPlayer;
		ServerPlayer player = isPlayer ? (ServerPlayer) locatable : null;
		String search = (isPlayer ? player.uniqueId().toString() : locatable.blockPosition().toString()) + TextUtils.clearDecorations(event.message());
		if(!map.containsKey(search)) {
			event.setCancelled(true);
			return;
		}
		MessageSettings messageSettings = map.get(search);
		map.remove(search);
		search = null;
		if(event.chatType().location().asString().equals("minecraft:chat")) {
			event.setSender(isPlayer ? messageSettings.chanel.getChatFormatter().buildFormatForPlayer(player) : messageSettings.chanel.getChatFormatter().buildFormatForCommandBlock(messageSettings.chanel, locatable.serverLocation().world()));
			event.setChatType(ChatTypes.CUSTOM_CHAT);
			event.setFilter(getReceiversFilter(messageSettings.chanel, locatable));
			if(!TextUtils.clearDecorations(event.message()).isEmpty()) mention(TextUtils.serializeLegacy(event.message()), messageSettings.predicate, isPlayer, player);
		}
		messageSettings = null;
	}

	private void chatSpy(Predicate<ServerPlayer> predicate, Chanel chanel, ServerPlayer player, Component message, Component original) {
		Sponge.server().onlinePlayers().stream().filter(predicate.negate()).filter(p -> (p.hasPermission(Permissions.CHAT_SPY))).forEach(p -> {
			p.sendMessage(TextUtils.deserializeLegacy("&8[&dSPY&8]&e ").hoverEvent(HoverEvent.showText(chanel.getChatFormatter().getTime(p.locale()).color(TextColor.color(255, 255, 0)))).append(TextUtils.deserializeLegacy(" &7" + TextUtils.clearDecorations(chanel.getChatFormatter().buildFormatForPlayer(player))).hoverEvent(HoverEvent.showText(original))));
		});
	}

	private Predicate<ServerPlayer> getReceiversFilter(Chanel chanel, Locatable source) {
		switch(chanel.getType()) {
			case WORLDS: {
				return ChatUtils.getWorldFilter(chanel);
			}
			case LOCAL: {
				return ChatUtils.getLocalFilter(source.serverLocation().world(), source.blockPosition(), chanel);
			}
			case CLAIM: {
				if(regions) return plugin.getRegionService().getClaimFilter(source, chanel);
				break;
			}
			case GLOBAL: {
				return ChatUtils.getPermissionFilter(chanel);
			}
			default: {
				return ChatUtils.getLocalFilter(source.serverLocation().world(), source.blockPosition(), chanel);
			}
		}
		return ChatUtils.getPermissionFilter(chanel);
	}

	private boolean antiSpam(ServerPlayer player) {
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

	private void mention(String message, Predicate<ServerPlayer> predicate, boolean isPlayer, ServerPlayer player) {
		if(message.contains("@")) {
			Sponge.server().onlinePlayers().stream().filter(predicate).filter(p -> (message.contains("@" + p.name()) && (!isPlayer || !p.name().equals(player.name())))).findFirst().ifPresent(p -> {
				p.playSound(Sound.sound(plugin.getConfig().getSound(), Sound.Source.VOICE, 100, 50));
				p.sendMessage(isPlayer ? plugin.getLocales().getText(p.locale(), LocalesPaths.MENTION_BY_PLAYER).replace(ReplaceKeys.PLAYER, player.customName().isPresent() ? player.customName().get().get() : Component.text(player.name())).get() : plugin.getLocales().getComponent(p.locale(), LocalesPaths.MENTION_BY_NOT_PLAYER));
			});
		}
	}

	private class MessageSettings {

		private Chanel chanel;
		private Predicate<ServerPlayer> predicate;
		private long time = System.currentTimeMillis();
		MessageSettings(Chanel chanel, Predicate<ServerPlayer> predicate) {
			this.chanel = chanel;
			this.predicate = predicate;
		}

		private boolean isExpired() {
			return time + 10000 < System.currentTimeMillis();
		}

	}

}
