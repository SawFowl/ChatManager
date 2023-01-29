package sawfowl.chatmanager.data.filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import sawfowl.chatmanager.data.Chanel;
import sawfowl.chatmanager.data.filters.rules.CommandRule;
import sawfowl.chatmanager.data.filters.rules.FilterRule;
import sawfowl.chatmanager.data.filters.rules.PunishRule;
import sawfowl.chatmanager.data.filters.rules.ReplaceRule;
import sawfowl.localeapi.api.TextUtils;

@ConfigSerializable
public class ChatFilter {
	public ChatFilter(){}

	public ChatFilter(FilterRule rule, RuleTypes type, String regex, String[] sendMessage) {
		this.filterName = type.getName();
		ruleType = type.getName();
		if(type == RuleTypes.COMMAND) {
			commandRule = (CommandRule) rule;
		} else if(type == RuleTypes.PUNISH) {
			punishRule = (PunishRule) rule;
		} else if(type == RuleTypes.REPLACE) {
			replaceRule = (ReplaceRule) rule;
		}
		this.regex = regex;
		dontSendMessage = type.dontSendMessage();
		this.sendMessage = sendMessage;
	}

	private RuleTypes rule;
	@Setting("FilterName")
	private String filterName;
	@Setting("RuleType")
	private String ruleType;
	@Setting("CommandRule")
	private CommandRule commandRule = null;
	@Setting("PunishRule")
	private PunishRule punishRule = null;
	@Setting("ReplaceRule")
	private ReplaceRule replaceRule = null;
	@Setting("Regex")
	private String regex;
	@Setting("DontSendMessage")
	private boolean dontSendMessage;
	@Setting("IgnoreChanels")
	private List<String> ignoreChanels = new ArrayList<>();
	@Setting("SendMessage")
	private String[] sendMessage;

	public String getFilterName() {
		return filterName;
	}

	public RuleTypes getRuleType() {
		return rule != null ? rule : (rule = RuleTypes.getType(ruleType));
	}

	public FilterRule getRule() {
		if(rule == null) rule = getRuleType();
		return rule == RuleTypes.COMMAND ? commandRule : rule == RuleTypes.PUNISH ? punishRule : rule == RuleTypes.REPLACE ? replaceRule : null;
	}

	public String getRegex() {
		return regex;
	}

	public boolean isIgnoreChanel(Chanel chanel) {
		return ignoreChanels != null && ignoreChanels.contains(chanel.getName());
	}

	public boolean isRegex(Component component) {
		return regex != null && (TextUtils.clearDecorations(component).toLowerCase().matches(regex) || TextUtils.clearDecorations(component).toLowerCase().contains(regex));
	}

	public boolean isDontSendMessage() {
		return dontSendMessage;
	}

	public Optional<Object[]> getSendMessage() {
		return sendMessage == null || sendMessage.length == 0 ? Optional.empty() : Optional.ofNullable(sendMessage);
	}

	@Override
	public String toString() {
		return "ChatFilter [rule=" + rule + ", filterName=" + filterName + ", ruleType=" + ruleType + ", commandRule="
				+ commandRule + ", punishRule=" + punishRule + ", replaceRule=" + replaceRule + ", regex=" + regex
				+ ", dontSendMessage=" + dontSendMessage + ", ignoreChanels=" + ignoreChanels + ", sendMessage="
				+ Arrays.toString(sendMessage) + "]";
	}


}
