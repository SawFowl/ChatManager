package sawfowl.chatmanager.utils;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.ban.Ban;
import org.spongepowered.api.service.ban.BanService;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;
import org.spongepowered.plugin.PluginContainer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;

import sawfowl.chatmanager.Permissions;
import sawfowl.chatmanager.configure.Locales;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.chatmanager.data.Chanel;
import sawfowl.chatmanager.data.Ignores;
import sawfowl.chatmanager.data.filters.ChatFilter;
import sawfowl.chatmanager.data.filters.RuleTypes;
import sawfowl.chatmanager.data.filters.rules.CommandRule;
import sawfowl.chatmanager.data.filters.rules.PunishRule;
import sawfowl.chatmanager.data.filters.rules.ReplaceRule;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStack;

public class ChatUtils {

	private static final char[] delimiters = {'.', '!', '?', ';'};

	public static final char firstSymbol(Component component) {
		return TextUtils.clearDecorations(component).charAt(0);
	}

	public static final Predicate<ServerPlayer> getLocalFilter(ServerWorld world, Vector3i vector3i, Chanel chanel) {
		return audience -> chanel.hasRecievePermission(audience) && sameWorld(audience, world) && isAround(audience, vector3i, chanel.getRange());
	}

	public static final Predicate<ServerPlayer> getWorldFilter(Chanel chanel) {
		return audience -> chanel.hasRecievePermission(audience) && chanel.isAllowedWorld((audience).world());
	}

	public static final Predicate<ServerPlayer> getPermissionFilter(Chanel chanel) {
		return audience -> chanel.hasRecievePermission(audience);
	}

	public static final Predicate<ServerPlayer> getNotIgnores(ServerPlayer player, Ignores ignores) {
		return audience -> player.hasPermission(Permissions.IGNORE_BYPASS) || !ignores.isIgnore(audience, player);
	}

	public static final boolean sameWorld(ServerPlayer first, ServerWorld world) {
		return first.world().key().asString().equals(world.key().asString());
	}

	public static final Component replace(Component original, String match, Component to) {
		return original.replaceText(TextReplacementConfig.builder().match(match).replacement(to).build());
	}

	public static final Component getOption(ServerPlayer player, String option) {
		return player.option(option).isPresent() ? TextUtils.deserialize(player.option(option).get()) : Component.empty();
	}

	public static Component showItem(ServerPlayer player, Component component) {
		if(!component.toString().contains(ReplaceKeys.LINK_ITEM)) return component;
		SerializedItemStack itemStack = new SerializedItemStack(player.itemInHand(HandTypes.MAIN_HAND));
		return replace(component, ReplaceKeys.LINK_ITEM, itemStack.getItemStack().asComponent().hoverEvent(HoverEvent.showItem(itemStack.getItemKey(), itemStack.getQuantity())));
	}

	public static final Component removeFirstSymbol(Component component, char symbol) {
		String plain = TextUtils.clearDecorations(component);
		if(plain.charAt(0) != symbol) return component;
		return component.replaceText(TextReplacementConfig.builder().match(String.valueOf(symbol)).replacement(Component.empty()).times(1).build());
	}

	public static FilterResult getFilterResult(Locales locales, PluginContainer container, ServerPlayer player, Component message, List<ChatFilter> filters, Chanel chanel) {
		FilterResult result = new FilterResult();
		Component toReturn = message;
		for(ChatFilter filter : filters) {
			if(!filter.isIgnoreChanel(chanel) && !player.hasPermission(Permissions.ignoreFilterPerm(filter.getFilterName()))) {
				if(filter.getRuleType() == RuleTypes.ANTI_CAPS) {
					String anticaps = capitalize(TextUtils.serializeLegacy(toReturn));
					toReturn = TextUtils.deserializeLegacy(anticaps.replace(anticaps.charAt(0), Character.toUpperCase(anticaps.charAt(0))));
				}
				if(filter.isRegex(toReturn)) {
					Optional<Component> send = filter.getSendMessage().isPresent() && !locales.getPluginLocale(org.spongepowered.api.util.locale.Locales.DEFAULT).getLocaleNode(filter.getSendMessage().get()).virtual() ? Optional.ofNullable(locales.getComponent(player.locale(), filter.getSendMessage().get())) : Optional.empty();
					if(filter.getRuleType() == RuleTypes.SHOW_ONLY_SELF) {
						result.showOnlySelf = true;
						break;
					} else if(filter.getRuleType() == RuleTypes.REPLACE) {
						ReplaceRule rule = (ReplaceRule) filter.getRule();
						toReturn = Text.of(toReturn).replace(filter.getRegex(), TextUtils.deserialize(rule.getReplaceTo())).get();
					} else if(filter.getRuleType() == RuleTypes.COMMAND) {
						CommandRule rule = (CommandRule) filter.getRule();
						rule.run(player, container);
						if(!player.isOnline() || rule.getCommands().stream().filter(command -> (command.startsWith("kick") || command.startsWith("ban") || command.startsWith("mute"))).findFirst().isPresent()) {
							result.dontSendMessage = true;
							container.logger().info(player.name() + " try say: " + TextUtils.clearDecorations(toReturn));
							break;
						}
					} else if(filter.getRuleType() == RuleTypes.PUNISH) {
						PunishRule rule = (PunishRule) filter.getRule();
						if(rule.isKick()) {
							if(send.isPresent()) {
								player.kick(send.get());
							} else player.kick();
							container.logger().info(player.name() + " try say: " + TextUtils.clearDecorations(toReturn));
							result.dontSendMessage = filter.isDontSendMessage();
							break;
						} else if(rule.isBan()) {
							Optional<BanService> banService = Sponge.serviceProvider().provide(BanService.class);
							if(banService.isPresent()) {
								banService.get().add(send.isPresent() ? Ban.of(player.profile(), send.get()) : Ban.of(player.profile()));
								container.logger().info(player.name() + " try say: " + TextUtils.clearDecorations(toReturn));
								result.dontSendMessage = filter.isDontSendMessage();
								break;
							}
						}
					}
					if(filter.isDontSendMessage()) {
						result.dontSendMessage = true;
						break;
					}
					if(send.isPresent() && player.isOnline()) player.sendMessage(locales.getComponent(player.locale(), filter.getSendMessage().get()));
				}
			}
		}
		result.message = toReturn;
		return result;
	}

	public static String capitalize(final String str) {
		String[] players = Sponge.server().onlinePlayers().stream().filter(player -> (str.contains(player.name()))).map(ServerPlayer::name).toArray(String[]::new);
		final int delimLen = delimiters == null ? -1 : delimiters.length;
		if (StringUtils.isEmpty(str) || delimLen == 0) {
			return str;
		}
		final char[] buffer = str.toLowerCase().toCharArray();
		boolean capitalizeNext = true;
		for (int i = 0; i < buffer.length; i++) {
			final char ch = buffer[i];
			if(buffer.length > i + 1 && isDelimiter(ch, buffer[i + 1])) {
				capitalizeNext = true;
			} else if(capitalizeNext) {
				if(i == 0 || buffer.length > i + 1) buffer[i == 0 ? i : i + 1] = Character.toTitleCase(i == 0 ? ch : buffer[i + 1]);
				capitalizeNext = false;
			}
		}
		String toReturn = new String(buffer);
		if(players.length > 0) for(String name : players) if(toReturn.contains(name.toLowerCase())) toReturn = toReturn.replace(name.toLowerCase(), name);
		return toReturn;
	}
   
	private static boolean isDelimiter(final char ch, char ch2) {
		for(final char delimiter : delimiters) {
			if(ch == delimiter && ch2 == ' ') {
				return true;
			}
		}
		return false;
	}

	private static boolean isAround(ServerPlayer player, Vector3i vector3i, int range) {
		return Math.max(player.blockPosition().x(), vector3i.x()) - Math.min(player.blockPosition().x(), vector3i.x()) <= range && Math.max(player.blockPosition().y(), vector3i.y()) - Math.min(player.blockPosition().y(), vector3i.y()) <= range && Math.max(player.blockPosition().z(), vector3i.z()) - Math.min(player.blockPosition().z(), vector3i.z()) <= range;
	}

}
