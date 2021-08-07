package io.github.redcreeper14385.quickpvp.game;

import net.minecraft.util.math.Vec3d;
import xyz.nucleoid.plasmid.game.GameSpace;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameMode;
import io.github.redcreeper14385.quickpvp.QuickPvP;
import io.github.redcreeper14385.quickpvp.game.map.QuickPvPMap;

public class QuickPvPSpawnLogic {
    private final ServerWorld world;
    private final GameSpace gameSpace;
    private final QuickPvPMap map;

    public QuickPvPSpawnLogic(ServerWorld world, GameSpace gameSpace, QuickPvPMap map) {
        this.world = world;
        this.gameSpace = gameSpace;
        this.map = map;
    }

    public void resetPlayer(ServerPlayerEntity player, GameMode gameMode) {
        player.changeGameMode(gameMode);
        player.setVelocity(Vec3d.ZERO);
        player.fallDistance = 0.0f;

        player.addStatusEffect(new StatusEffectInstance(
                StatusEffects.NIGHT_VISION,
                20 * 60 * 60,
                1,
                true,
                false
        ));
    }

    public void spawnPlayer(ServerPlayerEntity player) {
        BlockPos pos = this.map.spawn;
        if (pos == null) {
            QuickPvP.LOGGER.error("Cannot spawn player! No spawn is defined in the map!");
            return;
        }

        float radius = 4.5f;
        float x = pos.getX() + MathHelper.nextFloat(player.getRandom(), -radius, radius);
        float z = pos.getZ() + MathHelper.nextFloat(player.getRandom(), -radius, radius);

        player.teleport(world, x, pos.getY(), z, 0.0F, 0.0F);
    }
}
