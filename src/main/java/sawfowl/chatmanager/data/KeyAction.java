package sawfowl.chatmanager.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import sawfowl.chatmanager.configure.ReplaceKeys;
import sawfowl.localeapi.api.TextUtils;

@ConfigSerializable
public class KeyAction {

	public KeyAction(){}

	public KeyAction(KeyActionTypes type) {
		this.type = type;
		actionType = type.getKey();
		action = type.getAction();
	}

	private KeyActionTypes type;
	@Setting("ActionType")
	private String actionType;
	@Setting("Action")
	private String action;

	public KeyActionTypes getType() {
		return type != null ? type : (type = KeyActionTypes.getType(actionType));
	}

	public boolean isRunCommand() {
		return getType() == KeyActionTypes.RUN_COMMAND;
	}

	public boolean isSuggestCommand() {
		return getType() == KeyActionTypes.SUGGEST_COMMAND;
	}

	public boolean isShowText() {
		return getType() == KeyActionTypes.SHOW_TEXT;
	}

	public Component prepareAction(ServerPlayer player, Component component) {
		String toReturn = action;
		toReturn = toReturn.replace(ReplaceKeys.PLAYER, player.name()).replace(ReplaceKeys.UUID, player.uniqueId().toString());
		if(toReturn.contains(ReplaceKeys.BALANCE)) {
			if(Sponge.server().serviceProvider().economyService().isPresent() && Sponge.server().serviceProvider().economyService().get().findOrCreateAccount(player.uniqueId()).isPresent()) {
				List<String> balances = Sponge.server().serviceProvider().economyService().get().findOrCreateAccount(player.uniqueId()).get().balances().entrySet().stream().map(entry -> (TextUtils.clearDecorations(entry.getKey().symbol()) + entry.getValue())).collect(Collectors.toList());
				int listSize = balances.size();
				if(listSize != 0) {
					String balance = "";
					for(String b : balances) {
						balance += b;
						if(listSize > 1) balance += "\n";
						listSize--;
					}
					toReturn = toReturn.replace(ReplaceKeys.BALANCE, balance);
				} else toReturn = toReturn.replace(ReplaceKeys.BALANCE, "0");
			} else toReturn = toReturn.replace(ReplaceKeys.BALANCE, "0");
		}
		if(toReturn.contains(ReplaceKeys.WORLD)) toReturn = toReturn.replace(ReplaceKeys.WORLD, player.world().key().toString());
		if(toReturn.contains(ReplaceKeys.TIME)) toReturn = toReturn.replace(ReplaceKeys.TIME, getTime(player));
		return isRunCommand() ? component.clickEvent(ClickEvent.runCommand(toReturn)) : isSuggestCommand() ? component.clickEvent(ClickEvent.suggestCommand(toReturn)) : isShowText() ? component.hoverEvent(HoverEvent.showText(TextUtils.deserializeLegacy(toReturn))) : component;
	}

	private String getTime(ServerPlayer player) {
		SimpleDateFormat format = new SimpleDateFormat("d.MM.yyyy HH:mm:ss");
		Calendar calendar = Calendar.getInstance(player.locale());
		calendar.setTimeInMillis(System.currentTimeMillis());
		return format.format(calendar.getTime());
	}

}
