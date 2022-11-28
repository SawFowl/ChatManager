package sawfowl.chatmanager.data.filters.rules;

import java.util.Optional;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import sawfowl.chatmanager.data.filters.RuleTypes;

@ConfigSerializable
public abstract class AbstractFilterRule {

	public AbstractFilterRule(RuleTypes ruleType) {
		rule = ruleType;
		this.ruleType = ruleType.getName();
		this.ruleName = ruleType.getName();
		this.message = ruleType.getMessagePath();
	}

	private RuleTypes rule;
	@Setting("RuleType")
	private final String ruleType;
	@Setting("RuleType")
	private final String ruleName;
	@Setting("MessagePath")
	private final String[] message;

	public RuleTypes getRuleType() {
		return rule != null ? rule : (rule = RuleTypes.getType(ruleType));
	}

	public Optional<String[]> getMessage() {;
		return Optional.ofNullable(message);
	}

}
