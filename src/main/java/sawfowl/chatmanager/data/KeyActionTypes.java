package sawfowl.chatmanager.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sawfowl.chatmanager.configure.ReplaceKeys;

public enum KeyActionTypes {

	RUN_COMMAND {

		@Override
		public String getKey() {
			return "RunCommand";
		}

		@Override
		public String getAction() {
			return "/tell " + ReplaceKeys.PLAYER + " Hello!";
		}

		@Override
		public PlaceholderKey createKey() {
			return new PlaceholderKey(ReplaceKeys.PREFIX, new KeyAction(RUN_COMMAND));
		}
		
	},
	SUGGEST_COMMAND {

		@Override
		public String getKey() {
			return "SuggestCommand";
		}

		@Override
		public String getAction() {
			return "/tell " + ReplaceKeys.PLAYER + " Hello!";
		}

		@Override
		public PlaceholderKey createKey() {
			return new PlaceholderKey(ReplaceKeys.PLAYER, new KeyAction(SUGGEST_COMMAND));
		}
		
	},
	SHOW_TEXT {

		@Override
		public String getKey() {
			return "ShowText";
		}

		@Override
		public String getAction() {
			return "&e" + ReplaceKeys.TIME;
		}

		@Override
		public PlaceholderKey createKey() {
			return new PlaceholderKey(ReplaceKeys.CHANEL, new KeyAction(SHOW_TEXT));
		}
		
	};

	public abstract String getKey();
	public abstract String getAction();
	public abstract PlaceholderKey createKey();

	public static KeyActionTypes getType(String key) {
		return Stream.of(values()).filter(type -> (type.getKey().equalsIgnoreCase(key))).findFirst().orElse(SHOW_TEXT);
	}

	public static List<PlaceholderKey> createDefaultActions() {
		return Stream.of(values()).map(KeyActionTypes::createKey).collect(Collectors.toList());
	}

}
