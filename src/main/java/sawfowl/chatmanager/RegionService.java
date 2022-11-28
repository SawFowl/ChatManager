package sawfowl.chatmanager;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import net.kyori.adventure.audience.Audience;
import sawfowl.regionguard.RegionGuard;
import sawfowl.regionguard.api.RegionAPI;
import sawfowl.regionguard.api.data.Region;

public class RegionService {

	private final RegionAPI regionAPI;
	public RegionService() {
		this.regionAPI = ((RegionGuard) Sponge.pluginManager().plugin("regionguard").get().instance()).getAPI();
	}

	public Predicate<Audience> getClaimFilter(ServerPlayer player) {
		List<ServerPlayer> players = regionAPI.findRegion(player.world(), player.blockPosition()).getPlayers();
		Stream<ServerPlayer> stream = players.size() > 20 ? players.parallelStream() : players.stream();
		return new Predicate<Audience>() {

			@Override
			public boolean test(Audience a) {
				return !(a instanceof ServerPlayer) || stream.filter(p -> (p.uniqueId().equals(((ServerPlayer) a).uniqueId()))).findFirst().isPresent();
			}

		};
	}

	public boolean isGlobalRegion(ServerPlayer player) {
		return regionAPI.findRegion(player.world(), player.blockPosition()).isGlobal();
	}

	public String getRegionName(ServerPlayer player) {
		Region region = regionAPI.findRegion(player.world(), player.blockPosition());
		return region.getName(player.locale()).orElse(region.isGlobal() ? "GlobalRegion" : (region.isAdmin() ? "AdminClaim" : region.getOwnerName()));
	}

}
