package sawfowl.chatmanager.configure.translation;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.Translation;

@ConfigSerializable
public class PluginLocale implements Translation {

	public static PluginLocale createRu() {
		PluginLocale locale = new PluginLocale();
		locale.commands = Commands.createRu();
		locale.mention = Mention.createRu();
		locale.rulesMessages = RulesMessages.createRu();
		locale.antiSpam = locale.deserialize("&cНе спамьте!");
		return locale;
	}

	public PluginLocale() {}

	@Setting("Commands")
	private Commands commands = new Commands();
	@Setting("Mention")
	private Mention mention = new Mention();
	@Setting("RulesMessages")
	private RulesMessages rulesMessages = new RulesMessages();
	@Setting("AntiSpam")
	private Component antiSpam = deserialize("&cPlease do not spam!");

	public Commands getCommands() {
		return commands;
	}

	public Mention getMention() {
		return mention;
	}

	public RulesMessages getRulesMessages() {
		return rulesMessages;
	}

	public Component getAntiSpam() {
		return antiSpam;
	}

}
