package sawfowl.chatmanager.data.filters;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sawfowl.chatmanager.data.filters.rules.FilterRule;
import sawfowl.chatmanager.configure.LocalesPaths;
import sawfowl.chatmanager.data.filters.rules.CommandRule;
import sawfowl.chatmanager.data.filters.rules.PunishRule;
import sawfowl.chatmanager.data.filters.rules.ReplaceRule;

public enum RuleTypes {

	ANTI_CAPS {

		@Override
		public String getName() {
			return "AntiCaps";
		}

		@Override
		public String[] getMessagePath() {
			return null;
		}

		@Override
		public boolean dontSendMessage() {
			return false;
		}

		@Override
		ChatFilter createDefaultFilter() {
			return new ChatFilter(new FilterRule() {}, ANTI_CAPS, null, null);
		}
		
	},
	REPLACE {

		@Override
		public String getName() {
			return "Replace";
		}

		@Override
		public String[] getMessagePath() {
			return null;
		}

		@Override
		public boolean dontSendMessage() {
			return false;
		}

		@Override
		ChatFilter createDefaultFilter() {
			return new ChatFilter(new ReplaceRule("Ban me please!"), REPLACE, "(?i).*give me op.*|.*can i have op.*", null);
		}
		
	},
	PUNISH {

		@Override
		public String getName() {
			return "Punish";
		}

		@Override
		public String[] getMessagePath() {
			return null;
		}

		@Override
		public boolean dontSendMessage() {
			return true;
		}

		@Override
		ChatFilter createDefaultFilter() {
			return new ChatFilter(new PunishRule(true, false), PUNISH, "(?i)(f+u+c+k+|f+u+k+|f+v+c+k+|f+u+q+)", LocalesPaths.NOEXPRESSIONS);
		}
		
	},
	SHOW_ONLY_SELF {

		@Override
		public String getName() {
			return "ShowOnlySelf";
		}

		@Override
		public String[] getMessagePath() {
			return null;
		}

		@Override
		public boolean dontSendMessage() {
			return false;
		}

		@Override
		ChatFilter createDefaultFilter() {
			return new ChatFilter(new FilterRule(){}, SHOW_ONLY_SELF, "(^.*([0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}).*$)|(^.*((mc|play)\\.(.*)\\.(com|net))|((.+)\\.(.*(mine|craft).*)\\.(com|net)).*$)", null);
		}
		
	},
	COMMAND {

		@Override
		public String getName() {
			return "Command";
		}

		@Override
		public String[] getMessagePath() {
			return null;
		}

		@Override
		public boolean dontSendMessage() {
			return true;
		}

		@Override
		ChatFilter createDefaultFilter() {
			return new ChatFilter(new CommandRule(), COMMAND, "test command rule", null);
		}
		
	};

	public abstract String getName();
	public abstract String[] getMessagePath();
	public abstract boolean dontSendMessage();
	abstract ChatFilter createDefaultFilter();

	public static RuleTypes getType(String string) {
		return Stream.of(RuleTypes.values()).filter(rule -> (rule.getName().equalsIgnoreCase(string))).findFirst().orElse(SHOW_ONLY_SELF);
	}

	public static List<ChatFilter> createDefaultFilters() {
		return Stream.of(RuleTypes.values()).map(RuleTypes::createDefaultFilter).collect(Collectors.toList());
	}

}
