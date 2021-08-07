package io.github.redcreeper14385.quickpvp;

import net.fabricmc.api.ModInitializer;
import xyz.nucleoid.plasmid.game.GameType;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.github.redcreeper14385.quickpvp.game.QuickPvPConfig;
import io.github.redcreeper14385.quickpvp.game.QuickPvPWaiting;

public class QuickPvP implements ModInitializer {

    public static final String ID = "quickpvp";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    public static final GameType<QuickPvPConfig> TYPE = GameType.register(
            new Identifier(ID, "quickpvp"),
            QuickPvPConfig.CODEC,
            QuickPvPWaiting::open
    );

    @Override
    public void onInitialize() {}
}
