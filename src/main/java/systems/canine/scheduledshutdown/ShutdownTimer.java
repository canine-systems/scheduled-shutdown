package systems.canine.scheduledshutdown;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.BossEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ShutdownTimer {
    enum State {
        IDLE,
        WAITING,
        NEEDS_UPDATE,
        NEEDS_SHUTDOWN,
    }

    private static ShutdownTimer instance = new ShutdownTimer();

    public static final Logger LOGGER = LogUtils.getLogger();

    private final long NANOS_PER_MIN = 60_000_000_000l;
    private final long NANOS_PER_SEC = 1_000_000_000l;
    private final long NUM_SEGMENTS = 10;

    private long startTime = -1;
    private long endTime = -1;
    private long secsPerSegment = 0;
    private long segmentsLeft = 0;
    State state = State.IDLE;

    private final ServerBossEvent bossEvent = new ServerBossEvent(Component.literal("Server Shutdown"),
        BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);

    private ShutdownTimer() {
    }

    private long nanosUntilEndTime() {
        return endTime - System.nanoTime();
    }

    public void restart(int minutes) {
        startTime = System.nanoTime();
        endTime = startTime + (minutes * NANOS_PER_MIN);
        secsPerSegment = (endTime - startTime) / NANOS_PER_SEC / NUM_SEGMENTS;
        segmentsLeft = Long.MAX_VALUE;
        state = State.WAITING;
    }

    public void cancel() {
        this.bossEvent.removeAllPlayers();
        state = State.IDLE;
    }

    private void handleUpdate(MinecraftServer server) {
        long nanosLeft = nanosUntilEndTime();
        long secsLeft = Math.ceilDiv(nanosLeft, NANOS_PER_SEC);

        if (nanosLeft < 0) {
            state = State.NEEDS_SHUTDOWN;
            return;
        }
        state = State.WAITING;

        float progress = segmentsLeft / 10f;
        this.bossEvent.setProgress(progress);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            this.bossEvent.addPlayer(player);
        }

        announceShutdown(server.getPlayerList(), secsLeft);
    }

    private void handleShutdown(MinecraftServer server) {
        state = State.IDLE;
        server.halt(false);
    }

    private void handleWaiting() {
        long secsLeft = Math.ceilDiv(nanosUntilEndTime(), NANOS_PER_SEC);
        long possibleSegmentsLeft = Math.ceilDiv(secsLeft, secsPerSegment);
        if (possibleSegmentsLeft != segmentsLeft) {
            segmentsLeft = possibleSegmentsLeft;
            state = State.NEEDS_UPDATE;
        }
    }

    private void announceShutdown(PlayerList playerList, long secsLeft) {
        MutableComponent msg;

        if (secsLeft < 60) {
            msg = Component.literal("Server is shutting down in ")
                .append(Component.literal(String.format("%d seconds", secsLeft)).withStyle(ChatFormatting.RED))
                .append(Component.literal("!").withStyle(ChatFormatting.RESET));
        } else {
            msg = Component.literal("Server is shutting down in ")
                .append(Component.literal(String.format("%d minutes", secsLeft / 60)).withStyle(ChatFormatting.RED))
                .append(Component.literal("!").withStyle(ChatFormatting.RESET));
        }
        playerList.broadcastSystemMessage(msg, true);
    }

    @SubscribeEvent
    public void onTick(ServerTickEvent.Post event) {
        switch (state) {
        case IDLE:
            break;
        case WAITING:
            handleWaiting();
            break;
        case NEEDS_UPDATE:
            handleUpdate(event.getServer());
            break;
        case NEEDS_SHUTDOWN:
            handleShutdown(event.getServer());
            break;
        }
    }

    public static ShutdownTimer getInstance() {
        return instance;
    }
}