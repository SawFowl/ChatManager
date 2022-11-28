package sawfowl.chatmanager.configure;

import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.utils.AbstractLocaleUtil;

public class Locales {

	private final LocaleService localeService;
	private final boolean json;
	private final String pluginid = "chatmanager";
	public Locales(LocaleService localeService, boolean json) {
		this.localeService = localeService;
		this.json = json;
		generateDefault(localeService.createPluginLocale(pluginid, ConfigTypes.JSON, org.spongepowered.api.util.locale.Locales.DEFAULT));
		generateRu(localeService.createPluginLocale(pluginid, ConfigTypes.JSON, org.spongepowered.api.util.locale.Locales.RU_RU));
	}

	public String getString(Locale locale, Object... path) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(getText(locale, path));
	}

	public Component getText(Locale locale, Object... path) {
		return getAbstractLocaleUtil(locale).getComponent(json, path);
	}

	public Component getTextReplaced1(Locale locale, Map<String, String> map, Object... path) {
		return getAbstractLocaleUtil(locale).getComponentReplaced1(map, json, path);
	}

	public Component getTextReplaced2(Locale locale, Map<String, Component> map, Object... path) {
		return getAbstractLocaleUtil(locale).getComponentReplaced2(map, json, path);
	}

	public Component getTextFromDefault(Object... path) {
		return getAbstractLocaleUtil(org.spongepowered.api.util.locale.Locales.DEFAULT).getComponent(json, path);
	}

	public LocaleService getLocaleService() {
		return localeService;
	}

	public AbstractLocaleUtil getAbstractLocaleUtil(Locale locale) {
		return localeService.getPluginLocales(pluginid).get(locale);
	}

	private Component toText(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private boolean check(boolean save, AbstractLocaleUtil localeUtil, Component value, String comment, Object... path) {
		return localeUtil.checkComponent(json, value, comment, path) || save;
	}

	private void save(AbstractLocaleUtil localeUtil) {
		localeUtil.saveLocaleNode();
	}

	private void generateDefault(AbstractLocaleUtil localeUtil) {

		boolean save = check(false, localeUtil, toText("&cPlease do not spam!"), null, LocalesPaths.ANTISPAM);
		save = check(false, localeUtil, toText("&cNo expressions!"), null, Stream.of(LocalesPaths.NOEXPRESSIONS).toArray());
		save = check(false, localeUtil, toText("The command code is not spelled out for executor " + ReplaceKeys.SENDER), null, LocalesPaths.UNKNOWN_SENDER);
		save = check(false, localeUtil, toText("You need to enter a message."), null, LocalesPaths.MESSAGE_IS_NOT_PRESENT);
		save = check(false, localeUtil, toText("You must specify the player's nickname. The player must be online."), null, LocalesPaths.PLAYER_IS_NOT_PRESENT);
		save = check(false, localeUtil, toText("The chat channel is not intended for this world, or there are no worlds to send a message to."), null, LocalesPaths.INVALID_WORLD);
		save = check(false, localeUtil, toText("&aThe plugin is reloaded. Commands for chat channels can only be changed if the server is completely rebooted."), null, LocalesPaths.RELOAD);
		save = check(false, localeUtil, toText("&b" + ReplaceKeys.PLAYER + " &amentioned you in his message."), null, LocalesPaths.MENTION_BY_PLAYER);
		save = check(false, localeUtil, toText("&aYou were mentioned in the chat."), null, LocalesPaths.MENTION_BY_NOT_PLAYER);
		save = check(false, localeUtil, toText("&aNow you ignore the player &b" + ReplaceKeys.PLAYER + "&a."), null, LocalesPaths.IGNORED);
		save = check(false, localeUtil, toText("&aYou are no longer ignoring a player &b" + ReplaceKeys.PLAYER + "&a."), null, LocalesPaths.NOT_IGNORED);
		save = check(false, localeUtil, toText("&b" + ReplaceKeys.PLAYER + " &cignores you."), null, LocalesPaths.IGNORED_COMMAND);
		
		if(save) save(localeUtil);
	}

	private void generateRu(AbstractLocaleUtil localeUtil) {

		boolean save = check(false, localeUtil, toText("&cНе спамьте!"), null, LocalesPaths.ANTISPAM);
		save = check(false, localeUtil, toText("&cНе выражаться!"), null, Stream.of(LocalesPaths.NOEXPRESSIONS).toArray());
		save = check(false, localeUtil, toText("Код команды не прописан для исполнителя " + ReplaceKeys.SENDER), null, LocalesPaths.UNKNOWN_SENDER);
		save = check(false, localeUtil, toText("Нужно ввести сообщение."), null, LocalesPaths.MESSAGE_IS_NOT_PRESENT);
		save = check(false, localeUtil, toText("Нужно указать ник игрока. Игрок при этом должен быть онлайн."), null, LocalesPaths.PLAYER_IS_NOT_PRESENT);
		save = check(false, localeUtil, toText("Канал чата не предназначен для этого мира, либо нет миров для отправки сообщения в них."), null, LocalesPaths.INVALID_WORLD);
		save = check(false, localeUtil, toText("&aПлагин перезагружен. Команды для каналов чата могут быть изменены только при полной перезагрузке сервера."), null, LocalesPaths.RELOAD);
		save = check(false, localeUtil, toText("&b" + ReplaceKeys.PLAYER + " &aупомянул(а) вас в своем сообщении."), null, LocalesPaths.MENTION_BY_PLAYER);
		save = check(false, localeUtil, toText("&aВы были упомянуты в чате."), null, LocalesPaths.MENTION_BY_NOT_PLAYER);
		save = check(false, localeUtil, toText("&aТеперь вы игнорируете игрока &b" + ReplaceKeys.PLAYER + "&a."), null, LocalesPaths.IGNORED);
		save = check(false, localeUtil, toText("&aВы больше не игнорируете игрока &b" + ReplaceKeys.PLAYER + "&a."), null, LocalesPaths.NOT_IGNORED);
		save = check(false, localeUtil, toText("&b" + ReplaceKeys.PLAYER + " &cигнорирует вас."), null, LocalesPaths.IGNORED_COMMAND);

		if(save) save(localeUtil);
	}

}
