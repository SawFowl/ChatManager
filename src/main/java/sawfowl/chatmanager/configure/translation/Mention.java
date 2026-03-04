package sawfowl.chatmanager.configure.translation;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;

import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.localeapi.api.Translation;

@ConfigSerializable
public class Mention implements Translation {

	static Mention createRu() {
		Mention mention = new Mention();
		mention.byPlayer = mention.deserialize("&b" + ReplaceKeys.PLAYER + " &aупомянул(а) вас в своем сообщении.");
		mention.byNotPlayer = mention.deserialize("&aВы были упомянуты в чате.");
		return mention;
	}

	@Setting("ByPlayer")
	private Component byPlayer = deserialize("&b" + ReplaceKeys.PLAYER + " &amentioned you in his message.");
	@Setting("ByNotPlayer")
	@Comment("Support key " + ReplaceKeys.DISPLAY_NAME)
	private Component byNotPlayer = deserialize("&aYou were mentioned in the chat.");

	public Component getMessage(boolean isPlayer, Component name) {
		return isPlayer ? getByPlayer(name) : getByNotPlayer(name);
	}

	private Component getByPlayer(Component player) {
		return replace(byPlayer, ReplaceKeys.PLAYER , player);
	}

	private Component getByNotPlayer(Component name) {
		return replace(byNotPlayer, ReplaceKeys.DISPLAY_NAME , name);
	}

}
