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
		exceptions.unknownSender = exceptions.deserialize("Код команды не прописан для исполнителя " + ReplaceKeys.SENDER);
		exceptions.messageIsNotPresent = exceptions.deserialize("Нужно ввести сообщение.");
		exceptions.playerIsNotPresent = exceptions.deserialize("Нужно указать ник игрока. Игрок при этом должен быть онлайн.");
		exceptions.invalidWorld = exceptions.deserialize("Канал чата не предназначен для этого мира, либо нет миров для отправки сообщения в них.");
		exceptions.ignore = exceptions.deserialize("&b" + ReplaceKeys.PLAYER + " &cигнорирует вас.");
		return exceptions;
	}

	public Exceptions() {}

	@Setting("UnknownSender")
	private Component unknownSender = deserialize("The command code is not spelled out for executor " + ReplaceKeys.SENDER);
	@Setting("MessageIsNotPresent")
	private Component messageIsNotPresent = deserialize("You need to enter a message.");
	@Setting("PlayerIsNotPresent")
	private Component playerIsNotPresent = deserialize("You must specify the player's nickname. The player must be online.");
	@Setting("InvalidWorld")
	private Component invalidWorld = deserialize("The chat channel is not intended for this world, or there are no worlds to send a message to.");
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

	public Component getIgnore(ServerPlayer player) {
		return replace(ignore, ReplaceKeys.PLAYER , player.customName().map(Mutable::get).orElse(deserialize(player.name())));
	}

}
