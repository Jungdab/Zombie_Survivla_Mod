package me.jungdab.zsm.mixin;


import me.jungdab.zsm.block.ReviveBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Shadow @Final private List<ServerPlayerEntity> players;

    @Shadow @Final private MinecraftServer server;

    @Shadow public abstract void sendStatusEffects(ServerPlayerEntity player);

    @Shadow public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow @Final private Map<UUID, ServerPlayerEntity> playerMap;

    /**
     * @author goranidan
     * @reason no
     */
    @Overwrite
    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason) {
        boolean wasCreative = player.isCreative() || player.isSpectator();
        if(!wasCreative) player.changeGameMode(GameMode.SPECTATOR);

        this.players.remove(player);
        player.getServerWorld().removePlayer(player, removalReason);
        TeleportTarget teleportTarget = player.getRespawnTarget(alive, TeleportTarget.NO_OP);
        ServerWorld serverWorld = teleportTarget.world();
        ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, serverWorld, player.getGameProfile(), player.getClientOptions());
        serverPlayerEntity.networkHandler = player.networkHandler;
        serverPlayerEntity.copyFrom(player, alive);
        serverPlayerEntity.setId(player.getId());
        serverPlayerEntity.setMainArm(player.getMainArm());
        if (!teleportTarget.missingRespawnBlock()) {
            serverPlayerEntity.setSpawnPointFrom(player);
        }

        for (String string : player.getCommandTags()) {
            serverPlayerEntity.addCommandTag(string);
        }

        if(wasCreative) {
            Vec3d vec3d = teleportTarget.position();
            serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, teleportTarget.yaw(), teleportTarget.pitch());

            if (teleportTarget.missingRespawnBlock()) {
                serverPlayerEntity.networkHandler
                        .sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, GameStateChangeS2CPacket.DEMO_OPEN_SCREEN));
            }
        }
        else {
            ReviveBlock.createReviveBlock(player);

            Vec3d vec3d = player.getPos();
            serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, player.getYaw(), player.getPitch());
        }

        byte b = alive ? PlayerRespawnS2CPacket.KEEP_ATTRIBUTES : 0;
        ServerWorld serverWorld2 = serverPlayerEntity.getServerWorld();
        WorldProperties worldProperties = serverWorld2.getLevelProperties();
        serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.createCommonPlayerSpawnInfo(serverWorld2), b));
        serverPlayerEntity.networkHandler
                .requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
        serverPlayerEntity.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(serverWorld.getSpawnPos(), serverWorld.getSpawnAngle()));
        serverPlayerEntity.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        serverPlayerEntity.networkHandler
                .sendPacket(new ExperienceBarUpdateS2CPacket(serverPlayerEntity.experienceProgress, serverPlayerEntity.totalExperience, serverPlayerEntity.experienceLevel));
        this.sendStatusEffects(serverPlayerEntity);
        this.sendWorldInfo(serverPlayerEntity, serverWorld);
        this.sendCommandTree(serverPlayerEntity);
        serverWorld.onPlayerRespawned(serverPlayerEntity);
        this.players.add(serverPlayerEntity);
        this.playerMap.put(serverPlayerEntity.getUuid(), serverPlayerEntity);
        serverPlayerEntity.onSpawn();
        serverPlayerEntity.setHealth(serverPlayerEntity.getHealth());
        if (!alive) {
            BlockPos blockPos = BlockPos.ofFloored(teleportTarget.position());
            BlockState blockState = serverWorld.getBlockState(blockPos);
            if (blockState.isOf(Blocks.RESPAWN_ANCHOR)) {
                serverPlayerEntity.networkHandler
                        .sendPacket(
                                new PlaySoundS2CPacket(
                                        SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                                        SoundCategory.BLOCKS,
                                        blockPos.getX(),
                                        blockPos.getY(),
                                        blockPos.getZ(),
                                        1.0F,
                                        1.0F,
                                        serverWorld.getRandom().nextLong()
                                )
                        );
            }
        }

        return serverPlayerEntity;
    }
}
