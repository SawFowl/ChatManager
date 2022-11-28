package sawfowl.chatmanager.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.TextUtils;

public enum ChanelTypes {

	GLOBAL {
		@Override
		public String getType() {
			return "Global";
		}
		@Override
		public String getPrefix() {
			return "&7[&eG&7]&r";
		}
		@Override
		public char getSymbol() {
			return '!';
		}
		@Override
		public boolean isDefault() {
			return false;
		}
	},
	WORLDS {
		@Override
		public String getType() {
			return "Worlds";
		}
		@Override
		public String getPrefix() {
			return "&7[&2W&7]&r";
		}
		@Override
		public char getSymbol() {
			return '@';
		}
		@Override
		public boolean isDefault() {
			return false;
		}
		@Override
		public List<String> getAllowedWorlds() {
			return new ArrayList<>(Arrays.asList("minecraft:overworld", "minecraft:the_end", "minecraft:the_nether"));
		}
	},
	LOCAL {
		@Override
		public String getType() {
			return "Local";
		}
		@Override
		public String getPrefix() {
			return "&7[&fL&7]&r";
		}
		@Override
		public char getSymbol() {
			return '-';
		}
		@Override
		public boolean isDefault() {
			return true;
		}
	},
	CLAIM {
		@Override
		public String getType() {
			return "Claim";
		}
		@Override
		public String getPrefix() {
			return "&7[&5C&7]&r";
		}
		@Override
		public char getSymbol() {
			return '.';
		}
		@Override
		public boolean isDefault() {
			return false;
		}
	};

	public String getType() {
		return "Local";
	}

	public boolean isDefault() {
		return true;
	}

	public String getPrefix() {
		return "&7[&fL&7]&r";
	}

	public char getSymbol() {
		return '-';
	}

	public static ChanelTypes getType(String string) {
		return Stream.of(ChanelTypes.values()).filter(type -> (type.getType().equalsIgnoreCase(string))).findFirst().orElse(LOCAL);
	}

	public String getFormat() {
		return "%chanel% %prefix% &7[&f%player%&7]&r %suffix%&f:&r %message%";
	}

	public List<String> getAllowedWorlds() {
		return null;
	}

	public static List<Chanel> createDefaultChanels() {
		List<Chanel> chanels = new ArrayList<>();
		chanels.add(new Chanel(GLOBAL, null));
		chanels.add(new Chanel(WORLDS, null));
		chanels.add(new Chanel(LOCAL, 100));
		chanels.add(new Chanel(CLAIM, null));
		return chanels;
	}

	protected Component space() {
		return Component.text(" ");
	}

	protected String toString(Component component) {
		return TextUtils.serializeLegacy(component);
	}

	protected Component toText(String string) {
		return Component.text(string);
	}

}
