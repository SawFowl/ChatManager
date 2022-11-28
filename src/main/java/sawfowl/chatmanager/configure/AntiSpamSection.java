package sawfowl.chatmanager.configure;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class AntiSpamSection {

	@Setting("Enable")
	private boolean enable = true;
	@Setting("Delay")
	private long delay = 3;

	public boolean isEnable() {
		return enable;
	}

	public long getDelay() {
		return delay;
	}

}
