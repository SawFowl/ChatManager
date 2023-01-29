package sawfowl.chatmanager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.localeapi.event.LocaleServiseEvent;
import sawfowl.chatmanager.commands.ClaimChanelCommand;
import sawfowl.chatmanager.commands.GlobalChanelCommand;
import sawfowl.chatmanager.commands.IgnoreCommand;
import sawfowl.chatmanager.commands.LocalChanelCommand;
import sawfowl.chatmanager.commands.ReloadCommand;
import sawfowl.chatmanager.commands.WorldChanelCommand;
import sawfowl.chatmanager.configure.Config;
import sawfowl.chatmanager.configure.Locales;
import sawfowl.chatmanager.data.ChanelTypes;
import sawfowl.chatmanager.data.Ignores;
import sawfowl.chatmanager.listeners.ChatListener;
import sawfowl.chatmanager.listeners.CommandListener;
import sawfowl.chatmanager.utils.ChatFormatter;
import sawfowl.chatmanager.utils.RegionService;

@Plugin("chatmanager")
public class ChatManager {

	private static ChatManager instance;
	private PluginContainer pluginContainer;
	private Logger logger;
	private Locales locales;
	private Path configDir;
	private RegionService regionService;

	private ConfigurationReference<CommentedConfigurationNode> configurationReference;
	private ValueReference<Config, CommentedConfigurationNode> config;
	private ConfigurationReference<CommentedConfigurationNode> configurationReferenceIgnores;
	private ValueReference<Ignores, CommentedConfigurationNode> ignoresConfig;
	private ConfigurationOptions options;
	private Map<UUID, Long> antispamMap = new HashMap<>();

	@Inject
	public ChatManager(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		instance = this;
		logger = LogManager.getLogger("ChatManager");
		this.pluginContainer = pluginContainer;
		configDir = configDirectory;
	}

	@Listener
	public void onPostLocaleAPI(LocaleServiseEvent.Construct event) {
		options = event.getLocaleService().getConfigurationOptions();
		try {
			Path defaultConfig = configDir.resolve("Config.conf");
			configurationReference = HoconConfigurationLoader.builder().defaultOptions(options).path(defaultConfig).build().loadToReference();
			this.config = configurationReference.referenceTo(Config.class);
			if(!defaultConfig.toFile().exists()) {
				configurationReference.save();
			} else configurationReference.load();
			Path ignoresConfig = configDir.resolve("Ignores.conf");
			configurationReferenceIgnores = HoconConfigurationLoader.builder().defaultOptions(options).path(configDir.resolve("Ignores.conf")).build().loadToReference();
			this.ignoresConfig = configurationReferenceIgnores.referenceTo(Ignores.class);
			if(!ignoresConfig.toFile().exists()) {
				configurationReferenceIgnores.save();
			} configurationReferenceIgnores.load();
		} catch (ConfigurateException e) {
			logger.error(e.getLocalizedMessage());
		}
		locales = new Locales(event.getLocaleService(), getConfig().isJsonLocales());
		boolean regions = Sponge.pluginManager().plugin("regionguard").isPresent();
		if(regions) regionService = new RegionService();
		Sponge.eventManager().registerListeners(pluginContainer, new ChatListener(instance, regions));
		Sponge.eventManager().registerListeners(pluginContainer, new CommandListener(instance));
		Sponge.asyncScheduler().submit(Task.builder().plugin(pluginContainer).interval(1, TimeUnit.MINUTES).execute(() -> {
			antispamMap.entrySet().removeIf(entry -> (TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()) - entry.getValue() > getConfig().getAntiSpamSection().getDelay()));
		}).build());
	}

	@Listener
	public void onCommandRegister(RegisterCommandEvent<Command.Parameterized> event) {
		event.register(pluginContainer, new ReloadCommand(instance).build(), "chatreload");
		event.register(pluginContainer, new IgnoreCommand(instance).build(), "chatignore", "ignore");
		boolean regions = Sponge.pluginManager().plugin("regionguard").isPresent();
		getConfig().getChanels().forEach(chanel -> {
			chanel.setChatFormatter(new ChatFormatter(instance, chanel, regions));
			if(chanel.getType() == ChanelTypes.GLOBAL) {
				event.register(pluginContainer, new GlobalChanelCommand(instance, chanel).build(), chanel.getName().toLowerCase());
			} else if(chanel.getType() == ChanelTypes.WORLDS) {
				event.register(pluginContainer, new WorldChanelCommand(instance, chanel).build(), chanel.getName().toLowerCase());
			} else if(chanel.getType() == ChanelTypes.LOCAL) {
				event.register(pluginContainer, new LocalChanelCommand(instance, chanel).build(), chanel.getName().toLowerCase());
			} else if(regions && chanel.getType() == ChanelTypes.CLAIM) event.register(pluginContainer, new ClaimChanelCommand(instance, chanel).build(), chanel.getName().toLowerCase());
		});
	}

	public Map<UUID, Long> getAntiSpamMap() {
		return antispamMap;
	}

	public void reload() {
		try {
			configurationReference.load();
			config = configurationReference.referenceTo(Config.class);
			configurationReferenceIgnores = HoconConfigurationLoader.builder().defaultOptions(options).path(configDir.resolve("Ignores.conf")).build().loadToReference();
			this.ignoresConfig = configurationReferenceIgnores.referenceTo(Ignores.class);
		} catch (ConfigurateException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void updateIgnores() {
		ignoresConfig.setAndSave(getIgnoresConfig());
	}

	public static ChatManager getInstance() {
		return instance;
	}

	public PluginContainer getPluginContainer() {
		return pluginContainer;
	}

	public Path getConfigDir() {
		return configDir;
	}

	public Logger getLogger() {
		return logger;
	}

	public Locales getLocales() {
		return locales;
	}

	public Config getConfig() {
		return config.get();
	}

	public Ignores getIgnoresConfig() {
		return ignoresConfig.get();
	}

	public ConfigurationOptions getOptions() {
		return options;
	}

	public RegionService getRegionService() {
		return regionService;
	}

}
