package sawfowl.chatmanager;

public class Permissions {
	public static final String STYLE = "chatmanager.user.style";
	public static final String IGNORE = "chatmanager.user.ignore";
	public static final String ALLOW_SPAM = "chatmanager.user.allowspam";
	public static final String RELOAD = "chatmanager.staff.reload";
	public static final String IGNORE_BYPASS = "chatmanager.staff.bypassignore";
	public static final String CHAT_SPY = "chatmanager.staff.spy";

	public static final String chanelSendPerm(String name) {
		return "chatmanager.chanel." + name.toLowerCase() + ".send";
	}

	public static final String chanelRecievePerm(String name) {
		return "chatmanager.chanel." + name.toLowerCase() + ".recieve";
	}

	public static final String ignoreFilterPerm(String filter) {
		return "chatmanager.ignorefilter." + filter.toLowerCase();
	}

}
