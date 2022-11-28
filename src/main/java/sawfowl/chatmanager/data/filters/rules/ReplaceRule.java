package sawfowl.chatmanager.data.filters.rules;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class ReplaceRule extends FilterRule {

	public ReplaceRule(){}

	public ReplaceRule(String replaceTo) {
		this.replaceTo = replaceTo;
	}

	@Setting("ReplaceTo")
	private String replaceTo;

	public String getReplaceTo() {
		return replaceTo;
	}

}
