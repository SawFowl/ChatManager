package sawfowl.chatmanager.configure;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.effect.sound.SoundTypes;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.chatmanager.data.Chanel;
import sawfowl.chatmanager.data.ChanelTypes;
import sawfowl.chatmanager.data.KeyActionTypes;
import sawfowl.chatmanager.data.PlaceholderKey;
import sawfowl.chatmanager.data.filters.ChatFilter;
import sawfowl.chatmanager.data.filters.RuleTypes;

@ConfigSerializable
public class Config {

	public Config() {}
	private final Chanel defaultChanel = new Chanel(ChanelTypes.LOCAL, 100).setDefault(true);

	@Setting("JsonLocales")
	@Comment("If true, then the localization files will use the json string format, which allows you to add functionality to the plugin's messages.\nIf false, then regular strings with formatting using `&` character will be used.\nAfter changing this parameter, you need to delete the localization files and restart the server.")
	private boolean jsonLocales = true;
	@Setting("Antispam")
	@Comment("Anti-spam settings.")
	private AntiSpamSection antiSpamSection = new AntiSpamSection();
	@Setting("MentionSound")
	@Comment("The identifier of the sound the player will hear when mentioned in the chat.")
	private String mentionSound = "minecraft:block.note_block.guitar";
	@Setting("Chanels")
	@Comment("Available placeholders:\n%time% - Message time.\n%chanel% - Chanel prefix.\n%prefix% - Player prefix(Metaperm).\n%rank% - Player rank(Metaperm).\n%player% - Original player name.\n%displayname% - Custom player name.\n%suffix% - Player suffix(Metaperm).\n%world% - Player world or specified world in command.\n%region% - Region in the player's location. Optional support for RegionGuard plugin. %message% - Message")
	private List<Chanel> chanels = ChanelTypes.createDefaultChanels();
	@Setting("Filters")
	@Comment("There are five types of rules. Each type has its own settings. If the settings are wrong, you may get a missing method or NPE error.\nThe message that needs to be sent to the player is taken from the localization files. The rule specifies only the path to the section with the message. The message sent to the player can be used in any rule if you need it.\nYou can see other examples of rules by following this link to the page of the plugin from which I borrowed the idea of filter types - https://www.spigotmc.org/resources/bungeechatfilter.20596/")
	private List<ChatFilter> filters = RuleTypes.createDefaultFilters();
	@Setting("PlaceholderKeys")
	@Comment("Setting up placeholders. Here you can set the display of some text or automatic command input. These settings do not apply to the `%message%` placeholder.")
	private List<PlaceholderKey> placeholderKeys = KeyActionTypes.createDefaultActions();
	@Setting("BlockCommands")
	@Comment("A list of commands that a player will not be able to apply to another player if he is ignored.")
	private List<String> blockCommands = Arrays.asList("tell", "say", "m", "msg");
	

	public boolean isJsonLocales() {
		return jsonLocales;
	}

	public List<Chanel> getChanels() {
		return chanels;
	}

	public Chanel getChanel(char symbol) {
		return chanels.stream().filter(chanel -> (chanel.getSymbol() == symbol)).findFirst().orElse(getDefaultChanel());
	}

	public Chanel getDefaultChanel() {
		return chanels.stream().filter(Chanel::isDefault).findFirst().orElse(defaultChanel);
	}

	public List<ChatFilter> getFilters() {
		return filters;
	}

	public List<PlaceholderKey> getPlaceholderKeys() {
		return placeholderKeys;
	}

	public List<String> getBlockCommands() {
		return blockCommands;
	}

	public AntiSpamSection getAntiSpamSection() {
		return antiSpamSection;
	}

	public SoundType getSound() {
		return SoundTypes.registry().findValue(ResourceKey.resolve(mentionSound)).orElse(SoundTypes.BLOCK_NOTE_BLOCK_GUITAR.get());
	}

}
