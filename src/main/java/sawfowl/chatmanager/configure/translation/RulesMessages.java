package sawfowl.chatmanager.configure.translation;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.Translation;

@ConfigSerializable
public class RulesMessages implements Translation {

	static RulesMessages createRu() {
		RulesMessages messages = new RulesMessages();
		messages.noExpressions = messages.deserialize("&cНе выражаться!");
		return messages;
	}

	public RulesMessages() {}

	@Setting("NoExpressions")
	private Component noExpressions = deserialize("&cNo expressions!");

	public Component getNoExpressions() {
		return noExpressions;
	}

}
