package io.github.redcreeper14385.quickpvp.game;

import io.github.redcreeper14385.quickpvp.game.map.QuickPvPMapBuilder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import xyz.nucleoid.plasmid.game.*;
import xyz.nucleoid.plasmid.game.common.GameWaitingLobby;
import xyz.nucleoid.plasmid.game.event.GameActivityEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import io.github.redcreeper14385.quickpvp.game.map.QuickPvPMap;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.plasmid.game.event.GamePlayerEvents;
import xyz.nucleoid.stimuli.event.player.*;

public class QuickPvPWaiting {
    private final ServerWorld world;
    private final GameSpace gameSpace;
    private final QuickPvPMap map;
    private final QuickPvPConfig config;
    private final QuickPvPSpawnLogic spawnLogic;

    private QuickPvPWaiting(ServerWorld world, GameSpace gameSpace, QuickPvPMap map, QuickPvPConfig config) {
        this.world = world;
        this.gameSpace = gameSpace;
        this.map = map;
        this.config = config;
        this.spawnLogic = new QuickPvPSpawnLogic(world, gameSpace, map);
    }

    public static GameOpenProcedure open(GameOpenContext<QuickPvPConfig> context) {
        QuickPvPConfig config = context.config();
        QuickPvPMapBuilder builder = new QuickPvPMapBuilder(config);
        QuickPvPMap map = builder.create();

        RuntimeWorldConfig worldConfig = new RuntimeWorldConfig()
                .setGenerator(map.asGenerator(context.server()));

        return context.openWithWorld(worldConfig, (activity, world) -> {
            QuickPvPWaiting waiting = new QuickPvPWaiting(world, activity.getGameSpace(), map, context.config());

            GameWaitingLobby.applyTo(activity, config.playerConfig());

            activity.listen(GameActivityEvents.REQUEST_START, waiting::requestStart);
            activity.listen(GamePlayerEvents.JOIN, waiting::addPlayer);
            activity.listen(PlayerDeathEvent.EVENT, waiting::onPlayerDeath);
        });
    }

    private GameResult requestStart() {
        QuickPvPActive.open(this.world, this.gameSpace, this.map, this.config);
        return GameResult.ok();
    }

    private void addPlayer(ServerPlayerEntity player) {
        this.spawnPlayer(player);
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        player.setHealth(20.0f);
        this.spawnPlayer(player);
        return ActionResult.FAIL;
    }

    private void spawnPlayer(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }
}
