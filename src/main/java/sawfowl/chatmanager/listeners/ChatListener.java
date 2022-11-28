package sawfowl.chatmanager.listeners;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;
import org.spongepowered.api.world.Locatable;

import net.kyori.adventure.audience.Audience;
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
	public ChatListener(ChatManager plugin, boolean regions) {
		this.plugin = plugin;
		this.regions = regions;
	}

	@Listener(order = Order.LAST)
	public void onMessage(PlayerChatEvent event, @First Locatable locatable) {
		if(event.isCancelled() || !TextUtils.serializeLegacy(event.message()).equals(TextUtils.serializeLegacy(event.originalMessage()))) return;
		Chanel chanel = plugin.getConfig().getChanel(ChatUtils.firstSymbol(event.message()));
		boolean isPlayer = locatable instanceof ServerPlayer;
		ServerPlayer player = isPlayer ? (ServerPlayer) locatable : null;
		if(isPlayer && !chanel.hasSendPermission(player)) chanel = plugin.getConfig().getDefaultChanel();
		Component message = ChatUtils.removeFirstSymbol(event.message(), chanel.getSymbol());
		if(isPlayer && plugin.getConfig().getAntiSpamSection().isEnable() && antiSpam(player)) {
			event.setCancelled(true);
			player.sendMessage(plugin.getLocales().getText(player.locale(), LocalesPaths.ANTISPAM));
			return;
		}
		if(TextUtils.clearDecorations(message).length() == 0) {
			event.setCancelled(true);
			return;
		}
		message = ChatUtils.deserialize(TextUtils.serializeLegacy(message));
		boolean showOnlySelf = false;
		Predicate<Audience> predicate = getReceiversFilter(chanel, player, showOnlySelf, locatable);
		if(isPlayer) {
			predicate = predicate.and(ChatUtils.getNotIgnores(player, plugin.getIgnoresConfig()));
			if(!player.hasPermission(Permissions.STYLE)) message = Component.text(TextUtils.clearDecorations(message));
			message = ChatUtils.showItem(player, message);
			FilterResult filterResult = ChatUtils.getFilterResult(plugin.getLocales(), plugin.getPluginContainer(), player, message, plugin.getConfig().getFilters(), chanel);
			showOnlySelf = filterResult.isShowOnlySelf();
			if(filterResult.isDontSendMessage()) {
				event.setCancelled(true);
				filterResult = null;
				return;
			}
			if(filterResult.getMessage().isPresent()) {
				message = filterResult.getMessage().get();
			} else {
				event.setCancelled(true);
				return;
			}
		}
		event.setMessage(message);
		event.setChatFormatter(chanel.getChatFormatter());
		event.filterAudience(predicate);
		String stringMessage = TextUtils.serializeLegacy(message);
		if(message.toString().contains("@")) {
			Sponge.server().onlinePlayers().stream().filter(predicate).filter(p -> (stringMessage.contains("@" + p.name()) && (!isPlayer || !p.name().equals(player.name())))).findFirst().ifPresent(p -> {
				p.playSound(Sound.sound(plugin.getConfig().getSound(), Sound.Source.VOICE, 100, 50));
				p.sendMessage(isPlayer ? TextUtils.replaceToComponents(plugin.getLocales().getText(p.locale(), LocalesPaths.MENTION_BY_PLAYER), new String[]{ReplaceKeys.PLAYER}, new Component[]{player.customName().isPresent() ? player.customName().get().get() : Component.text(player.name())}) : plugin.getLocales().getText(p.locale(), LocalesPaths.MENTION_BY_NOT_PLAYER));
			});
		}
		if(isPlayer) chatSpy(predicate, chanel, player, message, event.originalMessage());
	}

	private void chatSpy(Predicate<Audience> predicate, Chanel chanel, ServerPlayer player, Component message, Component original) {
		Sponge.server().onlinePlayers().stream().filter(predicate.negate()).filter(p -> (p.hasPermission(Permissions.CHAT_SPY))).forEach(p -> {
			p.sendMessage(TextUtils.deserializeLegacy("&8[&dSPY&8]&e ").hoverEvent(HoverEvent.showText(chanel.getChatFormatter().getTime(p.locale()).color(TextColor.color(255, 255, 0)))).append(TextUtils.deserializeLegacy(" &7" + TextUtils.clearDecorations(chanel.getChatFormatter().buildFormatForPlayer(player, message))).hoverEvent(HoverEvent.showText(original))));
		});
	}

	private Predicate<Audience> getReceiversFilter(Chanel chanel, ServerPlayer player, boolean showOnlySelf, Locatable locatable) {
		if(player != null && showOnlySelf) return audience -> (!(audience instanceof ServerPlayer) || ((ServerPlayer) audience).uniqueId().equals(player.uniqueId()));
		switch(chanel.getType()) {
			case WORLDS: {
				return ChatUtils.getWorldFilter(chanel);
			}
			case LOCAL: {
				return ChatUtils.getLocalFilter(locatable.serverLocation().world(), locatable.blockPosition(), chanel);
			}
			case CLAIM: {
				if(regions) return plugin.getRegionService().getClaimFilter(player, chanel);
				break;
			}
			case GLOBAL: {
				return ChatUtils.getPermissionFilter(chanel);
			}
			default: {
				return ChatUtils.getLocalFilter(locatable.serverLocation().world(), locatable.blockPosition(), chanel);
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
