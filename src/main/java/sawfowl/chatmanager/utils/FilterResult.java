package sawfowl.chatmanager.utils;

import java.util.Optional;

import net.kyori.adventure.text.Component;

public class FilterResult {

	Component message;
	boolean showOnlySelf = false;
	boolean dontSendMessage = false;

	public Optional<Component> getMessage() {
		return Optional.ofNullable(message);
	}

	public boolean isShowOnlySelf() {
		return showOnlySelf;
	}

	public boolean isDontSendMessage() {
		return dontSendMessage;
	}

	

}
