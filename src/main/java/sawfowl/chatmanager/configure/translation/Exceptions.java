package sawfowl.chatmanager.configure.translation;

import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.localeapi.api.Translation;

@ConfigSerializable
public class Exceptions implements Translation {

	static Exceptions createRu() {
		Exceptions exceptions = new Exceptions();
		exceptions.unknownSender = exceptions.deserialize("&cКод команды не прописан для исполнителя " + ReplaceKeys.SENDER);
		exceptions.messageIsNotPresent = exceptions.deserialize("&cНужно ввести сообщение.");
		exceptions.playerIsNotPresent = exceptions.deserialize("&cНужно указать ник игрока. Игрок при этом должен быть онлайн.");
		exceptions.invalidWorld = exceptions.deserialize("&cКанал чата не предназначен для этого мира, либо нет миров для отправки сообщения в них.");
		exceptions.ignoreSelf = exceptions.deserialize("&cНельзя игнорировать себя.");
		exceptions.ignore = exceptions.deserialize("&b" + ReplaceKeys.PLAYER + " &cигнорирует вас.");
		return exceptions;
	}

	public Exceptions() {}

	@Setting("UnknownSender")
	private Component unknownSender = deserialize("&cThe command code is not spelled out for executor " + ReplaceKeys.SENDER);
	@Setting("MessageIsNotPresent")
	private Component messageIsNotPresent = deserialize("&cYou need to enter a message.");
	@Setting("PlayerIsNotPresent")
	private Component playerIsNotPresent = deserialize("&cYou must specify the player's nickname. The player must be online.");
	@Setting("InvalidWorld")
	private Component invalidWorld = deserialize("&cThe chat channel is not intended for this world, or there are no worlds to send a message to.");
	@Setting("IgnoreSelf")
	private Component ignoreSelf = deserialize("&cYou can't ignore yourself.");
	@Setting("Ignore")
	private Component ignore = deserialize("&b" + ReplaceKeys.PLAYER + " &cignores you.");

	public Component getUnknownSender(String sender) {
		return replace(unknownSender, ReplaceKeys.SENDER, sender);
	}

	public Component getMessageIsNotPresent() {
		return messageIsNotPresent;
	}

	public Component getPlayerIsNotPresent() {
		return playerIsNotPresent;
	}

	public Component getInvalidWorld() {
		return invalidWorld;
	}

	public Component getIgnoreSelf() {
		return ignoreSelf;
	}

	public Component getIgnore(ServerPlayer player) {
		return replace(ignore, ReplaceKeys.PLAYER , player.customName().map(Mutable::get).orElse(deserialize(player.name())));
	}

}
