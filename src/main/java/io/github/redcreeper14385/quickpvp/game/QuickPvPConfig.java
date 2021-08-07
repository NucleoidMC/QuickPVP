package io.github.redcreeper14385.quickpvp.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import xyz.nucleoid.plasmid.game.common.config.PlayerConfig;
import io.github.redcreeper14385.quickpvp.game.map.QuickPvPMapConfig;

import java.util.Collections;
import java.util.List;

public record QuickPvPConfig(PlayerConfig playerConfig, QuickPvPMapConfig mapConfig, int timeLimitSecs, List<ItemStack> gear, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {
    public static final Codec<QuickPvPConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerConfig.CODEC.fieldOf("players").forGetter(config -> config.playerConfig),
            QuickPvPMapConfig.CODEC.fieldOf("map").forGetter(config -> config.mapConfig),
            Codec.INT.fieldOf("time_limit_secs").forGetter(config -> config.timeLimitSecs),
            ItemStack.CODEC.listOf().optionalFieldOf("gear", Collections.emptyList()).forGetter(config -> config.gear),
            ItemStack.CODEC.optionalFieldOf("helmet", ItemStack.EMPTY).forGetter(config -> config.helmet),
            ItemStack.CODEC.optionalFieldOf("chestplate", ItemStack.EMPTY).forGetter(config -> config.chestplate),
            ItemStack.CODEC.optionalFieldOf("leggings", ItemStack.EMPTY).forGetter(config -> config.leggings),
            ItemStack.CODEC.optionalFieldOf("boots", ItemStack.EMPTY).forGetter(config -> config.boots)
    ).apply(instance, QuickPvPConfig::new));
}
