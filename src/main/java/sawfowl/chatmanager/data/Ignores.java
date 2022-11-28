package sawfowl.chatmanager.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class Ignores {

	public Ignores(){}

	@Setting("IgnoresMap")
	private Map<UUID, List<UUID>> map = new HashMap<UUID, List<UUID>>();

	public boolean isIgnore(UUID reciever, UUID sender) {
		return map.containsKey(reciever) && map.get(reciever).contains(sender);
	}

	public boolean isIgnore(ServerPlayer reciever, ServerPlayer sender) {
		return isIgnore(reciever.uniqueId(), sender.uniqueId());
	}

	public void addIgnore(UUID reciever, UUID sender) {
		if(isIgnore(reciever, sender)) return;
		if(!map.containsKey(reciever)) {
			map.put(reciever, new ArrayList<>(Arrays.asList(sender)));
		} else map.get(reciever).add(sender);
	}

	public void addIgnore(ServerPlayer reciever, ServerPlayer sender) {
		addIgnore(reciever.uniqueId(), sender.uniqueId());
	}

	public void removeIgnore(UUID reciever, UUID sender) {
		if(!isIgnore(reciever, sender)) return;
		map.get(reciever).remove(sender);
		if(map.get(reciever).isEmpty()) map.remove(reciever);
	}

	public boolean switchIgnore(UUID reciever, UUID sender) {
		if(isIgnore(reciever, sender)) {
			removeIgnore(reciever, sender);
			return false;
		} else addIgnore(reciever, sender);
		return true;
	}

	public boolean switchIgnore(ServerPlayer reciever, ServerPlayer sender) {
		return switchIgnore(reciever.uniqueId(), sender.uniqueId());
	}
	

}
