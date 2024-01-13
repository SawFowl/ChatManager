package sawfowl.chatmanager.utils;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.world.Locatable;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import sawfowl.chatmanager.data.Chanel;
import sawfowl.regionguard.RegionGuard;
import sawfowl.regionguard.api.RegionAPI;
import sawfowl.regionguard.api.data.Region;

public class RegionService {

	private RegionAPI regionAPI;
	public RegionService() {}

	public Predicate<ServerPlayer> getClaimFilter(Locatable locatable, Chanel chanel) {
		checkAPI();
		ServerWorld world = locatable.serverLocation().world();
		Vector3i position = locatable.blockPosition();
		Region region = regionAPI.findRegion(world, position);
		if(region.isGlobal()) return audience -> (!(audience instanceof ServerPlayer) || chanel.hasRecievePermission((ServerPlayer) audience) && ((ServerPlayer) audience).world().key().equals(world.key()) && regionAPI.findRegion(((ServerPlayer) audience).world(), ((ServerPlayer) audience).blockPosition()).isGlobal());
		List<ServerPlayer> players = regionAPI.findRegion(world, position).getPlayers();
		Stream<ServerPlayer> stream = players.size() > 20 ? players.parallelStream() : players.stream();
		return audience -> (!(audience instanceof ServerPlayer) || (chanel.hasRecievePermission((ServerPlayer) audience) && stream.filter(p -> (p.uniqueId().equals(((ServerPlayer) audience).uniqueId()))).findFirst().isPresent()));
	}

	public Predicate<ServerPlayer> getClaimFilterForCommand(Locatable locatable, Chanel chanel) {
		checkAPI();
		ServerWorld world = locatable.serverLocation().world();
		Vector3i position = locatable.blockPosition();
		Region region = regionAPI.findRegion(world, position);
		if(region.isGlobal()) return audience -> (!(audience instanceof ServerPlayer) || chanel.hasRecievePermission((ServerPlayer) audience) && ((ServerPlayer) audience).world().key().equals(world.key()) && regionAPI.findRegion(((ServerPlayer) audience).world(), ((ServerPlayer) audience).blockPosition()).isGlobal());
		List<ServerPlayer> players = regionAPI.findRegion(world, position).getPlayers();
		Stream<ServerPlayer> stream = players.size() > 20 ? players.parallelStream() : players.stream();
		return audience -> (audience instanceof ServerPlayer && (chanel.hasRecievePermission((ServerPlayer) audience) && stream.filter(p -> (p.uniqueId().equals(((ServerPlayer) audience).uniqueId()))).findFirst().isPresent()));
	}

	public boolean isGlobalRegion(ServerPlayer player) {
		checkAPI();
		return regionAPI.findRegion(player.world(), player.blockPosition()).isGlobal();
	}

	public String getRegionName(ServerPlayer player) {
		checkAPI();
		Region region = regionAPI.findRegion(player.world(), player.blockPosition());
		return region.getPlainName(player.locale()).orElse(region.isGlobal() ? "&7[&bGlobalRegion&7]&r" : (region.isAdmin() ? "&7[&4AdminClaim&7]&r" : "&7[&eClaim&8(&6" + region.getOwnerName() + "&8)&7]&r"));
	}

	private void checkAPI() {
		if(regionAPI == null) this.regionAPI = ((RegionGuard) Sponge.pluginManager().plugin("regionguard").get().instance()).getAPI();
	}

	@Listener
	public void getAPI(RegionAPI.PostAPI event) {
		regionAPI = event.getAPI();
		Sponge.eventManager().unregisterListeners(this);
	}

}
