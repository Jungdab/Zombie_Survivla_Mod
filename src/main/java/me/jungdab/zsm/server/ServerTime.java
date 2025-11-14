package me.jungdab.zsm.server;

import me.jungdab.zsm.entity.BossZombieEntity;
import me.jungdab.zsm.registry.ModAdvancements;
import me.jungdab.zsm.registry.ModEntities;
import me.jungdab.zsm.util.TimeUtil;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.List;

public class ServerTime {

    private static long serverTime;

    public static boolean isBossSpawn = false;


    public static void onTick(MinecraftServer server) {
        serverTime = server.getOverworld().getTimeOfDay();

        if(serverTime > 215500 && serverTime < 226000 && serverTime % 10 == 0) {
            List<ServerPlayerEntity> players = server.getOverworld().getPlayers();
            if(players == null || players.isEmpty()) return;

            for(ServerPlayerEntity entity : players) entity.getAdvancementTracker().grantCriterion(ModAdvancements.LAST_NIGHT, "last_morning");
        }

        if(!isBossSpawn && serverTime > 230000 && serverTime % 200 == 0) {
            List<ServerPlayerEntity> players = server.getOverworld().getPlayers();
            if(players == null || players.isEmpty()) return;

            summonBoss(server, players.getLast());
        }
    }

    public static int getServerDay() {
        return TimeUtil.dayTimeToDay(serverTime);
    }

    private static void summonBoss(MinecraftServer server, PlayerEntity player) {
        World world = server.getOverworld();
        int xOffset = world.random.nextInt(21) + 10 * (world.random.nextBoolean() ? 1 : -1);
        int yOffset = world.random.nextInt(21) + 10 * (world.random.nextBoolean() ? 1 : -1);

        BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, player.getBlockPos().add(xOffset, 0, yOffset));

        BossZombieEntity boss = ModEntities.BOSS_ZOMBIE.create(world, SpawnReason.EVENT);
        if(boss == null) return;

        boss.refreshPositionAndAngles(pos, 0, 0);
        world.spawnEntity(boss);

        isBossSpawn = true;
    }
}
