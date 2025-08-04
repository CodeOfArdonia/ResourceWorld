package com.iafenvoy.resourceworld.config;

import com.iafenvoy.resourceworld.util.GameRuleCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ResourceWorldData {
    public static final Codec<ResourceWorldData> CODEC = RecordCodecBuilder.create(i -> i.group(
            Identifier.CODEC.fieldOf("targetWorld").forGetter(ResourceWorldData::getTargetWorld),
            Codec.BOOL.fieldOf("enabled").forGetter(ResourceWorldData::isEnabled),
            Codec.LONG.fieldOf("seed").forGetter(ResourceWorldData::getSeed),
            Difficulty.CODEC.optionalFieldOf("difficulty", Difficulty.EASY).forGetter(ResourceWorldData::getDifficulty),
            GameRuleCodec.CODEC.optionalFieldOf("gamerules", new GameRules()).forGetter(ResourceWorldData::getGameRules),
            Settings.CODEC.optionalFieldOf("settings", new Settings()).forGetter(ResourceWorldData::getSettings)
    ).apply(i, ResourceWorldData::new));
    private final Identifier targetWorld;
    private boolean enabled;
    private long seed;
    private Difficulty difficulty;
    private final GameRules gameRules;
    private final Settings settings;

    public ResourceWorldData(Identifier targetWorld) {
        this(targetWorld, false, 0, Difficulty.EASY, new GameRules(), new Settings());
    }

    public ResourceWorldData(Identifier targetWorld, boolean enabled, long seed, Difficulty difficulty, GameRules gameRules, Settings settings) {
        this.targetWorld = targetWorld;
        this.enabled = enabled;
        this.seed = seed;
        this.difficulty = difficulty;
        this.gameRules = gameRules;
        this.settings = settings;
    }

    public Identifier getTargetWorld() {
        return this.targetWorld;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public Difficulty getDifficulty() {
        return this.difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    public Settings getSettings() {
        return this.settings;
    }

    public static final class Settings {
        public static final Codec<Settings> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("centerX").forGetter(Settings::getCenterX),
                Codec.INT.fieldOf("centerZ").forGetter(Settings::getCenterZ),
                Codec.INT.fieldOf("range").forGetter(Settings::getRange),
                BlockPos.CODEC.optionalFieldOf("spawnPoint").forGetter(Settings::getSpawnPoint),
                Codec.INT.fieldOf("cooldown").forGetter(Settings::getCooldown),
                Codec.BOOL.fieldOf("hideSeedHash").forGetter(Settings::isHideSeedHash),
                Codec.BOOL.fieldOf("allowHomeCommand").forGetter(Settings::isAllowHomeCommand)
        ).apply(i, Settings::new));

        private int centerX;
        private int centerZ;
        private int range;
        private Optional<BlockPos> spawnPoint;
        private int cooldown;
        private boolean hideSeedHash;
        private boolean allowHomeCommand;
        private boolean worldBorderInfoDirty;

        public Settings() {
            this(0, 0, 4096, Optional.empty(), 30, false, true);
        }

        public Settings(int centerX, int centerZ, int range, Optional<BlockPos> spawnPoint, int cooldown, boolean hideSeedHash, boolean allowHomeCommand) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.range = range;
            this.spawnPoint = spawnPoint;
            this.cooldown = cooldown;
            this.hideSeedHash = hideSeedHash;
            this.allowHomeCommand = allowHomeCommand;
            this.worldBorderInfoDirty = true;
        }

        public int getCenterX() {
            return this.centerX;
        }

        public void setCenterX(int centerX) {
            this.centerX = centerX;
            this.worldBorderInfoDirty = true;
        }

        public int getCenterZ() {
            return this.centerZ;
        }

        public void setCenterZ(int centerZ) {
            this.centerZ = centerZ;
            this.worldBorderInfoDirty = true;
        }

        public int getRange() {
            return this.range;
        }

        public void setRange(int range) {
            this.range = range;
            this.worldBorderInfoDirty = true;
        }

        public Optional<BlockPos> getSpawnPoint() {
            return this.spawnPoint;
        }

        public void setSpawnPoint(@Nullable BlockPos spawnPoint) {
            this.spawnPoint = Optional.ofNullable(spawnPoint);
        }

        public int getCooldown() {
            return this.cooldown;
        }

        public void setCooldown(int cooldown) {
            this.cooldown = cooldown;
        }

        public boolean isHideSeedHash() {
            return this.hideSeedHash;
        }

        public void setHideSeedHash(boolean hideSeedHash) {
            this.hideSeedHash = hideSeedHash;
        }

        public boolean isAllowHomeCommand() {
            return this.allowHomeCommand;
        }

        public void setAllowHomeCommand(boolean allowHomeCommand) {
            this.allowHomeCommand = allowHomeCommand;
        }

        public boolean isWorldBorderInfoDirty() {
            boolean b = this.worldBorderInfoDirty;
            this.worldBorderInfoDirty = false;
            return b;
        }
    }
}
