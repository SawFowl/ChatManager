package sawfowl.chatmanager.data;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;

@ConfigSerializable
public class PlaceholderKey {

	public PlaceholderKey(){}

	public PlaceholderKey(String key, KeyAction action) {
		this.key = key;
		this.actions = Arrays.asList(action);
	}

	@Setting("Key")
	private String key;
	@Setting("Actions")
	private List<KeyAction> actions;

	public String getKey() {
		return key;
	}

	public Component prepareAction(ServerPlayer player, Component component) {
		for(KeyAction action : actions) component = action.prepareAction(player, component);
		return component;
	}

}
