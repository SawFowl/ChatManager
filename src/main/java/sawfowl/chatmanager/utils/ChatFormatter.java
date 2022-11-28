package sawfowl.chatmanager.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.PlayerChatFormatter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.api.world.server.ServerWorld;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import sawfowl.chatmanager.ChatManager;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.chatmanager.data.Chanel;
import sawfowl.localeapi.api.TextUtils;

public class ChatFormatter implements PlayerChatFormatter {

	private final ChatManager plugin;
	private final Chanel chanel;
	private final boolean regions;
	public ChatFormatter(ChatManager plugin, Chanel chanel, boolean regions) {
		this.plugin = plugin;
		this.chanel = chanel;
		this.regions = regions;
	}

	@Override
	public Optional<Component> format(ServerPlayer player, Audience target, Component message, Component originalMessage) {
		return Optional.ofNullable(buildFormatForPlayer(player, message));
	}

	public Component buildFormatForPlayer(ServerPlayer player, Component message) {
		String pattern = chanel.getFormat();
		Component toReturn = chanel.deserializedFormat();
		if(pattern.contains(ReplaceKeys.TIME)) toReturn = replace(toReturn, ReplaceKeys.TIME, createPart(player, ReplaceKeys.TIME, getTime(player.locale())));
		if(pattern.contains(ReplaceKeys.CHANEL)) toReturn = replace(toReturn, ReplaceKeys.CHANEL, createPart(player, ReplaceKeys.CHANEL, chanel.getPrefix()));
		if(pattern.contains(ReplaceKeys.DISPLAY_NAME)) toReturn = replace(toReturn, ReplaceKeys.DISPLAY_NAME, createPart(player, ReplaceKeys.DISPLAY_NAME, player.customName().isPresent() ? player.customName().get().get() : ChatUtils.deserialize(player.name())));
		if(pattern.contains(ReplaceKeys.PLAYER)) toReturn = replace(toReturn, ReplaceKeys.PLAYER, createPart(player, ReplaceKeys.PLAYER, Component.text(player.name())));
		if(pattern.contains(ReplaceKeys.PREFIX)) toReturn = replace(toReturn, ReplaceKeys.PREFIX, createPart(player, ReplaceKeys.PREFIX, ChatUtils.getOption(player, "prefix")));
		if(pattern.contains(ReplaceKeys.RANK)) toReturn = replace(toReturn, ReplaceKeys.RANK, createPart(player, ReplaceKeys.RANK, ChatUtils.getOption(player, "rank")));
		if(pattern.contains(ReplaceKeys.REGION)) toReturn = replace(toReturn, ReplaceKeys.REGION, createPart(player, ReplaceKeys.REGION, regions ? ChatUtils.deserialize(plugin.getRegionService().getRegionName(player)) : Component.empty()));
		if(pattern.contains(ReplaceKeys.SUFFIX)) toReturn = replace(toReturn, ReplaceKeys.SUFFIX, createPart(player, ReplaceKeys.SUFFIX, ChatUtils.getOption(player, "suffix")));
		if(pattern.contains(ReplaceKeys.WORLD)) toReturn = replace(toReturn, ReplaceKeys.WORLD, createPart(player, ReplaceKeys.WORLD, ChatUtils.deserialize(player.world().key().value())));
		if(pattern.contains(ReplaceKeys.MESSAGE)) toReturn = replace(toReturn, ReplaceKeys.MESSAGE, message);
		return ChatUtils.deserialize(TextUtils.serializeJson(toReturn));
	}

	public Component buildFormatForConsole(Chanel chanel, ServerWorld world, Component message) {
		String pattern = chanel.getFormat();
		Component toReturn = chanel.deserializedFormat();
		if(pattern.contains(ReplaceKeys.TIME)) toReturn = replace(toReturn, ReplaceKeys.TIME, getTime(Locales.DEFAULT));
		if(pattern.contains(ReplaceKeys.CHANEL)) toReturn = replace(toReturn, ReplaceKeys.CHANEL, chanel.getPrefix());
		if(pattern.contains(ReplaceKeys.DISPLAY_NAME)) toReturn = replace(toReturn, ReplaceKeys.DISPLAY_NAME, Component.text("Server"));
		if(pattern.contains(ReplaceKeys.PLAYER)) toReturn = replace(toReturn, ReplaceKeys.PLAYER, Component.text("Server"));
		if(pattern.contains(ReplaceKeys.PREFIX)) toReturn = replace(toReturn, ReplaceKeys.PREFIX, Component.empty());
		if(pattern.contains(ReplaceKeys.RANK)) toReturn = replace(toReturn, ReplaceKeys.RANK, Component.empty());
		if(pattern.contains(ReplaceKeys.REGION)) toReturn = replace(toReturn, ReplaceKeys.REGION, Component.empty());
		if(pattern.contains(ReplaceKeys.SUFFIX)) toReturn = replace(toReturn, ReplaceKeys.SUFFIX, Component.empty());
		if(pattern.contains(ReplaceKeys.WORLD)) toReturn = replace(toReturn, ReplaceKeys.WORLD, ChatUtils.deserialize(world.key().value()));
		if(pattern.contains(ReplaceKeys.MESSAGE)) toReturn = replace(toReturn, ReplaceKeys.MESSAGE, message);
		return ChatUtils.deserialize(TextUtils.serializeLegacy(toReturn));
	}

	public Component buildFormatForCommandBlock(Chanel chanel, ServerWorld world, Component message) {
		String pattern = chanel.getFormat();
		Component toReturn = chanel.deserializedFormat();
		if(pattern.contains(ReplaceKeys.TIME)) toReturn = replace(toReturn, ReplaceKeys.TIME, getTime(Locales.DEFAULT));
		if(pattern.contains(ReplaceKeys.CHANEL)) toReturn = replace(toReturn, ReplaceKeys.CHANEL, chanel.getPrefix());
		if(pattern.contains(ReplaceKeys.DISPLAY_NAME)) toReturn = replace(toReturn, ReplaceKeys.DISPLAY_NAME, Component.text("CommandBlock"));
		if(pattern.contains(ReplaceKeys.PLAYER)) toReturn = replace(toReturn, ReplaceKeys.PLAYER, Component.text("CommandBlock"));
		if(pattern.contains(ReplaceKeys.PREFIX)) toReturn = replace(toReturn, ReplaceKeys.PREFIX, Component.empty());
		if(pattern.contains(ReplaceKeys.RANK)) toReturn = replace(toReturn, ReplaceKeys.RANK, Component.empty());
		if(pattern.contains(ReplaceKeys.REGION)) toReturn = replace(toReturn, ReplaceKeys.REGION, Component.empty());
		if(pattern.contains(ReplaceKeys.SUFFIX)) toReturn = replace(toReturn, ReplaceKeys.SUFFIX, Component.empty());
		if(pattern.contains(ReplaceKeys.WORLD)) toReturn = replace(toReturn, ReplaceKeys.WORLD, ChatUtils.deserialize(world.key().value()));
		if(pattern.contains(ReplaceKeys.MESSAGE)) toReturn = replace(toReturn, ReplaceKeys.MESSAGE, message);
		return ChatUtils.deserialize(TextUtils.serializeLegacy(toReturn));
	}

	public Component getTime(Locale locale) {
		SimpleDateFormat format = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");
		Calendar calendar = Calendar.getInstance(locale);
		calendar.setTimeInMillis(System.currentTimeMillis());
		return Component.text(format.format(calendar.getTime()));
	}

	private Component createPart(ServerPlayer player, String partKey, Component component) {
		return plugin.getConfig().getPlaceholderKeys().stream().filter(key -> (key.getKey().equals(partKey))).map(key -> (key.prepareAction(player, component))).findFirst().orElse(component);
	}

	private Component replace(Component text, String key, Component value) {
		if(TextUtils.clearDecorations(value).length() == 0) {
			text = ChatUtils.replace(text, " " + key, value);
			text = ChatUtils.replace(text, key + " ", value);
		} else text = ChatUtils.replace(text, key, value);
		if(text.toString().contains(key)) text = ChatUtils.replace(text, key, value);
		return text;
	}

}
