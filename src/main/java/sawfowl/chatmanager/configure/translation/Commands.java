package sawfowl.chatmanager.configure.translation;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.Translation;

@ConfigSerializable
public class Commands implements Translation {

	public static Commands createRu() {
		Commands commands = new Commands();
		commands.exceptions = Exceptions.createRu();
		commands.ignore = Ignore.createRu();
		commands.reload = commands.deserialize("&aПлагин перезагружен. Команды для каналов чата могут быть изменены только при полной перезагрузке сервера.");
		return commands;
	}

	public Commands() {}

	@Setting("Exceptions")
	private Exceptions exceptions = new Exceptions();
	@Setting("Ignore")
	private Ignore ignore = new Ignore();
	@Setting("Reload")
	private Component reload = deserialize("&aThe plugin is reloaded. Commands for chat channels can only be changed if the server is completely rebooted.");

	public Exceptions getExceptions() {
		return exceptions;
	}

	public Ignore getIgnore() {
		return ignore;
	}

	public Component getReload() {
		return reload;
	}

}
