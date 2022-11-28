package sawfowl.chatmanager.data;

import java.util.List;
import java.util.Optional;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.chatmanager.Permissions;
import sawfowl.chatmanager.utils.ChatFormatter;

@ConfigSerializable
public class Chanel {

	public Chanel(){}

	public Chanel(ChanelTypes chanelType, Integer range) {
		type = chanelType.getType();
		name = chanelType.getType();
		prefix = chanelType.getPrefix();
		symbol = chanelType.getSymbol();
		defaultChanel = chanelType.isDefault();
		this.range = range;
		messageFormat = chanelType.getFormat();
		sendPermission = Permissions.chanelSendPerm(name);
		recievePermission = Permissions.chanelRecievePerm(name);
		allowedWorlds = chanelType.getAllowedWorlds();
	}

	private ChatFormatter chatFormatter;

	@Setting("ChanelType")
	private String type;
	@Setting("Name")
	private String name;
	@Setting("Prefix")
	private String prefix;
	@Setting("Symbol")
	private char symbol;
	@Setting("Range")
	private Integer range;
	@Setting("NeedPerm")
	private boolean needPerm = false;
	@Setting("Default")
	private boolean defaultChanel = false;
	@Setting("MessageFormat")
	private String messageFormat;
	@Setting("SendPermission")
	private String sendPermission;
	@Setting("RecievePermission")
	private String recievePermission;
	@Setting("AllowedWorld")
	private List<String> allowedWorlds;

	public ChanelTypes getType() {
		return ChanelTypes.getType(type);
	}

	public Component getPrefix() {
		return deserialize(prefix);
	}

	public String getName() {
		return name;
	}

	public String plainPrefix() {
		return prefix;
	}

	public char getSymbol() {
		return symbol;
	}

	public boolean isRanged() {
		return range != null && range > 0;
	}

	public int getRange() {
		return range;
	}

	public boolean isNeedPerm() {
		return needPerm;
	}

	public boolean isDefault() {
		return defaultChanel;
	}

	public Chanel setDefault(boolean def) {
		defaultChanel = def;
		return this;
	}

	public String getFormat() {
		return messageFormat;
	}

	public Component deserializedFormat() {
		return deserialize(messageFormat);
	}

	public String getSendPermission() {
		return sendPermission;
	}

	public String getRecievePermission() {
		return recievePermission;
	}

	public boolean hasSendPermission(ServerPlayer player) {
		if(sendPermission == null) sendPermission = Permissions.chanelSendPerm(name);
		return !needPerm || player.hasPermission(sendPermission);
	}

	public boolean hasRecievePermission(ServerPlayer player) {
		if(recievePermission == null) recievePermission = Permissions.chanelRecievePerm(name);
		return !needPerm || (player.hasPermission(recievePermission) && hasSendPermission(player));
	}

	private Component deserialize(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	public ChatFormatter getChatFormatter() {
		return chatFormatter;
	}

	public void setChatFormatter(ChatFormatter chatFormatter) {
		this.chatFormatter = chatFormatter;
	}

	public Optional<List<String>> getAllowedWorlds() {
		return Optional.ofNullable(allowedWorlds);
	}

	public boolean isAllowedWorld(ServerWorld world) {
		return allowedWorlds == null ? true : allowedWorlds.contains(world.key().asString());
	}

}
