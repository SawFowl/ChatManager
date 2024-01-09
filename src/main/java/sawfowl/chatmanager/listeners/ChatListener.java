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
import org.spongepowered.api.util.Ticks;
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
	private Map<String, Chanel> map = new HashMap<>();
	public ChatListener(ChatManager plugin, boolean regions) {
		this.plugin = plugin;
		this.regions = regions;
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
			if(filterResult.isDontSendMessage()) {
				message = Component.empty();
				return;
			}
			if(!filterResult.getMessage().isPresent()) {
				message = Component.empty();
				return;
			} message = filterResult.getMessage().get();
			message = ChatUtils.showItem(player, message);
			if(filterResult.isShowOnlySelf()) predicate = audience -> (!(audience instanceof ServerPlayer) || ((ServerPlayer) audience).uniqueId().equals(player.uniqueId()));
		}
		if(isPlayer) chatSpy(predicate, chanel, player, message, event.originalMessage());
		event.setMessage(message);
		if(!TextUtils.clearDecorations(message).isEmpty()) {
			String stringMessage = TextUtils.serializeLegacy(message);
			String key = (isPlayer ? player.uniqueId().toString() : locatable.blockPosition().toString()) + TextUtils.clearDecorations(message);
			map.put(key, chanel);
			Sponge.asyncScheduler().submit(Task.builder().plugin(plugin.getPluginContainer()).delay(Ticks.of(5)).execute(() -> {if(map.containsKey(key)) map.remove(key);}).build());
			if(message.toString().contains("@")) {
				Sponge.server().onlinePlayers().stream().filter(predicate).filter(p -> (stringMessage.contains("@" + p.name()) && (!isPlayer || !p.name().equals(player.name())))).findFirst().ifPresent(p -> {
					p.playSound(Sound.sound(plugin.getConfig().getSound(), Sound.Source.VOICE, 100, 50));
					p.sendMessage(isPlayer ? plugin.getLocales().getText(p.locale(), LocalesPaths.MENTION_BY_PLAYER).replace(ReplaceKeys.PLAYER, player.customName().isPresent() ? player.customName().get().get() : Component.text(player.name())).get() : plugin.getLocales().getComponent(p.locale(), LocalesPaths.MENTION_BY_NOT_PLAYER));
				});
			}
		}
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
		Chanel chanel = map.containsKey(search) ? map.get(search) : plugin.getConfig().getDefaultChanel();
		if(map.containsKey(search)) map.remove(search);
		search = null;
		if(isPlayer && plugin.getConfig().getAntiSpamSection().isEnable() && antiSpam(player)) {
			event.setCancelled(true);
			player.sendMessage(plugin.getLocales().getComponent(player.locale(), LocalesPaths.ANTISPAM));
			return;
		}
		event.setChatType(ChatTypes.CUSTOM_CHAT);
		event.setSender(isPlayer ? chanel.getChatFormatter().buildFormatForPlayer(player) : chanel.getChatFormatter().buildFormatForCommandBlock(chanel, locatable.serverLocation().world()));
		event.setFilter(getReceiversFilter(chanel, locatable));
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

}
