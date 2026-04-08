package sawfowl.chatmanager;

import java.lang.invoke.MethodHandles;
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
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import com.google.inject.Inject;

import sawfowl.chatmanager.commands.ClaimChanelCommand;
import sawfowl.chatmanager.commands.GlobalChanelCommand;
import sawfowl.chatmanager.commands.IgnoreCommand;
import sawfowl.chatmanager.commands.LocalChanelCommand;
import sawfowl.chatmanager.commands.ReloadCommand;
import sawfowl.chatmanager.commands.WorldChanelCommand;
import sawfowl.chatmanager.configure.Config;
import sawfowl.chatmanager.configure.translation.PluginLocale;
import sawfowl.chatmanager.data.ChanelTypes;
import sawfowl.chatmanager.data.Ignores;
import sawfowl.chatmanager.listeners.ChatListener;
import sawfowl.chatmanager.listeners.CommandListener;
import sawfowl.chatmanager.utils.ChatFormatter;
import sawfowl.chatmanager.utils.RegionService;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocalesList;
import sawfowl.localeapi.api.config.ReferencedConfig;
import sawfowl.localeapi.api.serializetools.ItemStackSerializerType;
import sawfowl.localeapi.api.services.ConfigurationService;
import sawfowl.localeapi.api.services.LocaleService;

@Plugin("chatmanager")
public class ChatManager {

	private static ChatManager instance;
	private PluginContainer pluginContainer;
	private Logger logger;
	private Path configDir;
	private LocalesList<PluginLocale> locales;
	private RegionService regionService;

	private ReferencedConfig<Config> config;
	private ReferencedConfig<Ignores> ignores;
	private Map<UUID, Long> antispamMap = new HashMap<>();

	@Inject
	public ChatManager(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		instance = this;
		logger = LogManager.getLogger("ChatManager");
		this.pluginContainer = pluginContainer;
		configDir = configDirectory;
		locales = LocaleService.getInstance().createLocales(pluginContainer, PluginLocale.class);
		if(!locales.contains(Locales.DEFAULT)) locales.createReferencedTranslation(ConfigTypes.HOCON, Locales.DEFAULT, PluginLocale.class);
		if(!locales.contains(Locales.RU_RU)) locales.createReferencedTranslation(ConfigTypes.HOCON, Locales.RU_RU, PluginLocale.createRu());
		config = ConfigurationService.getInstance().createReferencedConfig(pluginContainer, Config.class).setPath(configDirectory).setName("Config").setType(ConfigTypes.HOCON).setItemStackSerializerType(ItemStackSerializerType.JSON).build();
		ignores = ConfigurationService.getInstance().createReferencedConfig(pluginContainer, Ignores.class).setPath(configDirectory).setName("Ignores").setType(ConfigTypes.HOCON).setItemStackSerializerType(ItemStackSerializerType.JSON).build();
	}

	@Listener
	public void onConstruct(ConstructPluginEvent event) {
		boolean regions = Sponge.pluginManager().plugin("regionguard").isPresent();
		if(regions) regionService = new RegionService();
		Sponge.eventManager().registerListeners(pluginContainer, new ChatListener(instance, regions), MethodHandles.lookup());
		Sponge.eventManager().registerListeners(pluginContainer, new CommandListener(instance), MethodHandles.lookup());
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
		config.load();
		ignores.load();
	}

	public void updateIgnores() {
		ignores.save();
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

	public LocalesList<PluginLocale> getLocales() {
		return locales;
	}

	public Config getConfig() {
		return config.get();
	}

	public Ignores getIgnoresConfig() {
		return ignores.get();
	}

	public RegionService getRegionService() {
		return regionService;
	}

}
