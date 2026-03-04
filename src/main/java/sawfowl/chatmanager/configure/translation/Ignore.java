package sawfowl.chatmanager.configure.translation;

import org.spongepowered.api.data.value.Value.Mutable;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;

import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.localeapi.api.Translation;

@ConfigSerializable
public class Ignore implements Translation {

	static Ignore createRu() {
		Ignore ignore = new Ignore();
		ignore.ignored = ignore.deserialize("&aТеперь вы игнорируете игрока &b" + ReplaceKeys.PLAYER + "&a.");
		ignore.notIgnored = ignore.deserialize("&aВы больше не игнорируете игрока &b" + ReplaceKeys.PLAYER + "&a.");
		return ignore;
	}

	public Ignore() {}

	@Setting("Ignored")
	private Component ignored = deserialize("&aNow you ignore the player &b" + ReplaceKeys.PLAYER + "&a.");
	@Setting("NotIgnored")
	private Component notIgnored = deserialize("&aYou are no longer ignoring a player &b" + ReplaceKeys.PLAYER + "&a.");

	public Component getIgnored(ServerPlayer player) {
		return replace(ignored, ReplaceKeys.PLAYER , player.customName().map(Mutable::get).orElse(deserialize(player.name())));
	}

	public Component getNotIgnored(ServerPlayer player) {
		return replace(notIgnored, ReplaceKeys.PLAYER , player.customName().map(Mutable::get).orElse(deserialize(player.name())));
	}

}
