package io.github.redcreeper14385.quickpvp.game.map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class QuickPvPMapConfig {
    public static final Codec<QuickPvPMapConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(config -> config.id)
    ).apply(instance, QuickPvPMapConfig::new));

    public final Identifier id;

    public QuickPvPMapConfig(@NotNull Identifier id) {
        this.id = id;
    }
}