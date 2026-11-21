package com.example.modernkit.entity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks the wind burst "fall floor" for players, reproducing the vanilla
 * rule: a player launched by a wind burst accumulates fall damage only when
 * falling below the Y level where the burst hit them.
 *
 * While the player's feet are at or above the recorded Y, their fall
 * distance is cleared every tick, so wind charge jumps land softly. Once
 * they drop below it, fall damage accumulates normally.
 *
 * The entry lives until the player lands again after being launched. A
 * burst at the feet starts while the player is still standing on the
 * ground, so the entry must NOT be dropped just because they are on the
 * ground for one tick (the old code did that, which silently lost the
 * protection on the very burst that needed it). Players who never leave
 * the ground (weak hit, ceiling) are cleaned up after a grace period;
 * death and logout always clean up.
 */
public final class WindChargeFloors {

    /** Ticks a floor may wait for the player to leave the ground. */
    private static final int LAUNCH_GRACE_TICKS = 40;

    private static final Map<UUID, Floor> FLOORS = new HashMap<>();

    private WindChargeFloors() {
    }

    private static final class Floor {
        private final double y;
        private int grace = LAUNCH_GRACE_TICKS;
        private boolean launched;

        private Floor(double y) {
            this.y = y;
        }
    }

    /** Records (or updates) the burst Y for a player hit by a wind burst. */
    public static void setFloor(PlayerEntity player, double burstY) {
        FLOORS.put(player.getUuid(), new Floor(burstY));
    }

    /** Per-tick maintenance; called from the server tick event. */
    public static void tick(MinecraftServer server) {
        if (FLOORS.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<UUID, Floor>> iterator = FLOORS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Floor> entry = iterator.next();
            ServerPlayerEntity player =
                    server.getPlayerManager().getPlayer(entry.getKey());

            if (player == null || !player.isAlive()) {
                iterator.remove();
                continue;
            }

            Floor floor = entry.getValue();

            // No fall damage while at or above the burst height.
            if (player.getY() >= floor.y) {
                player.fallDistance = 0.0F;
            }

            if (!player.isOnGround()) {
                floor.launched = true;
            } else if (floor.launched) {
                // Landed again after the launch: the jump is over.
                iterator.remove();
            } else if (--floor.grace <= 0) {
                // Never left the ground: nothing to protect.
                iterator.remove();
            }
        }
    }
}
