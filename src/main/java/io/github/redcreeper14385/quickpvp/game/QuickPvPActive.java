package io.github.redcreeper14385.quickpvp.game;

import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameCloseReason;
import xyz.nucleoid.plasmid.game.GameSpace;
import xyz.nucleoid.plasmid.game.common.GlobalWidgets;
import xyz.nucleoid.plasmid.game.event.*;
import xyz.nucleoid.plasmid.game.player.PlayerOffer;
import xyz.nucleoid.plasmid.game.player.PlayerOfferResult;
import xyz.nucleoid.plasmid.game.player.PlayerSet;
import xyz.nucleoid.plasmid.game.rule.GameRuleType;
import xyz.nucleoid.plasmid.util.PlayerRef;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.GameMode;
import io.github.redcreeper14385.quickpvp.game.map.QuickPvPMap;
import xyz.nucleoid.stimuli.event.player.PlayerDamageEvent;
import xyz.nucleoid.stimuli.event.player.PlayerDeathEvent;

import java.util.*;
import java.util.stream.Collectors;

public class QuickPvPActive {
    private final QuickPvPConfig config;
    public final GameSpace gameSpace;
    private final QuickPvPMap map;
    private final ServerWorld world;
    private final Object2ObjectMap<ServerPlayerEntity, QuickPvPPlayer> participants;
    private final QuickPvPSpawnLogic spawnLogic;
    private final QuickPvPStageManager stageManager;
    private final boolean ignoreWinState;
    private final QuickPvPTimerBar timerBar;

    private QuickPvPActive(ServerWorld world, GameSpace gameSpace, QuickPvPMap map, GlobalWidgets widgets, QuickPvPConfig config, Set<ServerPlayerEntity> participants) {
        this.world = world;
        this.gameSpace = gameSpace;
        this.config = config;
        this.map = map;
        this.spawnLogic = new QuickPvPSpawnLogic(world, gameSpace, map);
        this.participants = new Object2ObjectOpenHashMap<>();

        for (ServerPlayerEntity player : participants) {
            this.participants.put(player, new QuickPvPPlayer());
        }

        this.stageManager = new QuickPvPStageManager();
        this.ignoreWinState = this.participants.size() <= 1;
        this.timerBar = new QuickPvPTimerBar(widgets);
    }

    public static void open(ServerWorld world, GameSpace gameSpace, QuickPvPMap map, QuickPvPConfig config) {
        gameSpace.setActivity(gameSpace.getSourceConfig(), activity -> {
            var widgets = GlobalWidgets.addTo(activity);
            Set<ServerPlayerEntity> participants = Sets.newHashSet(gameSpace.getPlayers());
            var active = new QuickPvPActive(world, gameSpace, map, widgets, config, participants);

            activity.deny(GameRuleType.CRAFTING);
            activity.deny(GameRuleType.PORTALS);
            activity.deny(GameRuleType.PVP);
            activity.deny(GameRuleType.HUNGER);
            activity.deny(GameRuleType.FALL_DAMAGE);
            activity.deny(GameRuleType.INTERACTION);
            activity.deny(GameRuleType.BLOCK_DROPS);
            activity.deny(GameRuleType.THROW_ITEMS);
            activity.deny(GameRuleType.UNSTABLE_TNT);

            activity.listen(GameActivityEvents.ENABLE, active::onEnable);
            activity.listen(GameActivityEvents.TICK, active::tick);
            activity.listen(PlayerDamageEvent.EVENT, active::onPlayerDamage);
            activity.listen(PlayerDeathEvent.EVENT, active::onPlayerDeath);
        });
    }

    private PlayerOfferResult offerPlayer(PlayerOffer offer) {
        return offer.accept(world, Vec3d.ofCenter(this.map.spawn))
                .and(() -> offer.player().changeGameMode(GameMode.SPECTATOR));
    }

    private void onEnable() {
        for (var player : this.gameSpace.getPlayers()) {
            this.spawnParticipant(player);
        }
    }

    private void onClose() {
        // TODO teardown logic
    }

    private void removePlayer(ServerPlayerEntity player) {
        this.participants.remove(PlayerRef.of(player));
    }

    private ActionResult onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        // TODO handle damage
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private ActionResult onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        // TODO handle death
        this.spawnParticipant(player);
        return ActionResult.FAIL;
    }

    private void spawnParticipant(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.ADVENTURE);
        this.spawnLogic.spawnPlayer(player);
    }

    private void spawnSpectator(ServerPlayerEntity player) {
        this.spawnLogic.resetPlayer(player, GameMode.SPECTATOR);
        this.spawnLogic.spawnPlayer(player);
    }

    private void tick() {
        long time = world.getTime();

        QuickPvPStageManager.IdleTickResult result = this.stageManager.tick(time, gameSpace);

        switch (result) {
            case CONTINUE_TICK:
                break;
            case TICK_FINISHED:
                return;
            case GAME_FINISHED:
                this.broadcastWin(this.checkWinResult());
                return;
            case GAME_CLOSED:
                this.gameSpace.close(GameCloseReason.FINISHED);
                return;
        }

        this.timerBar.update(this.stageManager.finishTime - time, this.config.timeLimitSecs() * 20);

        // TODO tick logic
    }

    private void broadcastWin(WinResult result) {
        ServerPlayerEntity winningPlayer = result.getWinningPlayer();

        Text message;
        if (winningPlayer != null) {
            message = winningPlayer.getDisplayName().shallowCopy().append(" has won the game!").formatted(Formatting.GOLD);
        } else {
            message = new LiteralText("The game ended, but nobody won!").formatted(Formatting.GOLD);
        }

        PlayerSet players = this.gameSpace.getPlayers();
        players.sendMessage(message);
        players.playSound(SoundEvents.ENTITY_VILLAGER_YES);
    }

    private WinResult checkWinResult() {
        // for testing purposes: don't end the game if we only ever had one participant
        if (this.ignoreWinState) {
            return WinResult.no();
        }

        ServerPlayerEntity winningPlayer = null;

        // TODO win result logic
        return WinResult.no();
    }

    static class WinResult {
        final ServerPlayerEntity winningPlayer;
        final boolean win;

        private WinResult(ServerPlayerEntity winningPlayer, boolean win) {
            this.winningPlayer = winningPlayer;
            this.win = win;
        }

        static WinResult no() {
            return new WinResult(null, false);
        }

        static WinResult win(ServerPlayerEntity player) {
            return new WinResult(player, true);
        }

        public boolean isWin() {
            return this.win;
        }

        public ServerPlayerEntity getWinningPlayer() {
            return this.winningPlayer;
        }
    }
}
