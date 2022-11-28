package sawfowl.chatmanager.data.filters.rules;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class PunishRule extends FilterRule {

	public PunishRule(){}

	public PunishRule(boolean kick, boolean ban) {
		this.kick = kick;
		this.ban = ban;
	}
	@Setting("Kick")
	private Boolean kick;
	@Setting("Ban")
	private Boolean ban;

	public boolean isKick() {
		return kick != null && kick;
	}

	public boolean isBan() {
		return ban != null && ban;
	}

}
