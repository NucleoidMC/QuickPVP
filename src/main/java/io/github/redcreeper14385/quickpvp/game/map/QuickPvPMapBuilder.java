package io.github.redcreeper14385.quickpvp.game.map;

import io.github.redcreeper14385.quickpvp.QuickPvP;
import io.github.redcreeper14385.quickpvp.game.QuickPvPConfig;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.NotNull;
import xyz.nucleoid.plasmid.game.GameOpenException;
import xyz.nucleoid.plasmid.map.template.MapTemplate;
import xyz.nucleoid.plasmid.map.template.MapTemplateSerializer;
import xyz.nucleoid.plasmid.util.BlockBounds;

import java.io.IOException;

public class QuickPvPMapBuilder {
    private final QuickPvPMapConfig config;

    public QuickPvPMapBuilder(@NotNull QuickPvPConfig config){
        this.config = config.mapConfig();
    }

    public @NotNull QuickPvPMap create() throws GameOpenException {
        MapTemplate template;
        try {
            template = MapTemplateSerializer.INSTANCE.loadFromResource(this.config.id);
        } catch (IOException e) {
            throw new GameOpenException(new TranslatableText("duels.error.load_map", this.config.id.toString()), e);
        }

        BlockBounds spawn1 = template.getMetadata().getFirstRegionBounds("spawn1");
        BlockBounds spawn2 = template.getMetadata().getFirstRegionBounds("spawn2");
        if (spawn1 == null || spawn2 == null) {
            QuickPvP.LOGGER.error("Insufficient spawn data! Game will not work.");
            throw new GameOpenException(new LiteralText("Insufficient spawn data!"));
        }

        return new QuickPvPMap(template, spawn1, spawn2, config);
    }
}