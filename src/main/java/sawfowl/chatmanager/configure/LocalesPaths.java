package sawfowl.chatmanager.configure;

public class LocalesPaths {

	private static final Object COMMANDS = "Commands";
	private static final Object MENTION = "Mention";
	private static final Object EXCEPTIONS = "Exceptions";
	private static final Object IGNORE_COMMAND = "Exceptions";

	public static final String[] NOEXPRESSIONS = {"RulesMessages", "NoExpressions"};
	public static final Object[] ANTISPAM = {"AntiSpam"};
	public static final Object[] UNKNOWN_SENDER = {COMMANDS, EXCEPTIONS, "UnknownSender"};
	public static final Object[] MESSAGE_IS_NOT_PRESENT = {COMMANDS, EXCEPTIONS, "MessageIsNotPresent"};
	public static final Object[] PLAYER_IS_NOT_PRESENT = {COMMANDS, EXCEPTIONS, "PlayerIsNotPresent"};
	public static final Object[] INVALID_WORLD = {COMMANDS, EXCEPTIONS, "InvalidWorld"};
	public static final Object[] IGNORED_COMMAND = {COMMANDS, EXCEPTIONS, "Ignore"};
	public static final Object[] IGNORED = {COMMANDS, IGNORE_COMMAND, "Ignored"};
	public static final Object[] NOT_IGNORED = {COMMANDS, IGNORE_COMMAND, "NotIgnored"};
	public static final Object[] RELOAD = {COMMANDS, "Reload"};
	public static final Object[] MENTION_BY_PLAYER = {MENTION, "ByPlayer"};
	public static final Object[] MENTION_BY_NOT_PLAYER = {MENTION, "ByNotPlayer"};

}
