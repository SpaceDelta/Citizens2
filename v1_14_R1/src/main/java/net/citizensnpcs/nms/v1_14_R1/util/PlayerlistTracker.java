package net.citizensnpcs.nms.v1_14_R1.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import net.citizensnpcs.nms.v1_14_R1.entity.EntityHumanNPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.citizensnpcs.util.NMS;
import net.minecraft.server.v1_14_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_14_R1.Entity;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTrackerEntry;
import net.minecraft.server.v1_14_R1.PlayerChunk;
import net.minecraft.server.v1_14_R1.PlayerChunkMap;
import net.minecraft.server.v1_14_R1.PlayerChunkMap.EntityTracker;
import net.minecraft.server.v1_14_R1.Vec3D;

public class PlayerlistTracker extends PlayerChunkMap.EntityTracker {
    private final PlayerChunkMap map;
    private final Entity tracker;
    private final EntityTrackerEntry trackerEntry;
    private final int trackingDistance;

    public PlayerlistTracker(PlayerChunkMap map, Entity entity, int i, int j, boolean flag) {
        map.super(entity, i, j, flag);
        this.map = map;
        this.tracker = getTracker(this);
        this.trackerEntry = getTrackerEntry(this);
        this.trackingDistance = i;
    }

    public PlayerlistTracker(PlayerChunkMap map, EntityTracker entry) {
        this(map, getTracker(entry), getI(entry), getD(entry), getE(entry));
    }

    private int getA(PlayerChunkMap map2) {
        try {
            return A.getInt(map2);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getb(ChunkCoordIntPair chunkcoordintpair, EntityPlayer entityplayer, boolean b) {
        try {
            return (int) B.invoke(map, chunkcoordintpair, entityplayer, b);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private PlayerChunk getVisibleChunk(long pair) {
        try {
            return (PlayerChunk) GET_VISIBLE_CHUNK.invoke(map, pair);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void updatePlayer(final EntityPlayer entityplayer) {
        // prevent updates to NPC "viewers"
        if (entityplayer instanceof EntityHumanNPC)
            return;
        Entity tracker = getTracker(this);
        final Vec3D vec3d = new Vec3D(entityplayer.locX, entityplayer.locY, entityplayer.locZ).d(this.trackerEntry.b());
        final int i = Math.min(this.trackingDistance, (getA(map) - 1) * 16);
        final boolean flag = vec3d.x >= -i && vec3d.x <= i && vec3d.z >= -i && vec3d.z <= i
                && this.tracker.a(entityplayer);
        if (entityplayer != tracker && flag && tracker instanceof SkinnableEntity) {
            boolean flag1 = this.tracker.attachedToPlayer;
            if (!flag1) {
                ChunkCoordIntPair chunkcoordintpair = new ChunkCoordIntPair(this.tracker.chunkX, this.tracker.chunkZ);
                PlayerChunk playerchunk = getVisibleChunk(chunkcoordintpair.pair());
                if (playerchunk.getChunk() != null) {
                    flag1 = getb(chunkcoordintpair, entityplayer, false) <= getA(map);
                }
            }
            if (flag1) {
                SkinnableEntity skinnable = (SkinnableEntity) tracker;

                Player player = skinnable.getBukkitEntity();
                if (!entityplayer.getBukkitEntity().canSee(player))
                    return;

                skinnable.getSkinTracker().updateViewer(entityplayer.getBukkitEntity());
            }
        }
        super.updatePlayer(entityplayer);
    }

    private static int getD(EntityTracker entry) {
        try {
            return D.getInt(TRACKER_ENTRY.get(entry));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean getE(EntityTracker entry) {
        try {
            return E.getBoolean(TRACKER_ENTRY.get(entry));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static int getI(EntityTracker entry) {
        try {
            return (Integer) I.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static Entity getTracker(EntityTracker entry) {
        try {
            return (Entity) TRACKER.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static EntityTrackerEntry getTrackerEntry(EntityTracker entry) {
        try {
            return (EntityTrackerEntry) TRACKER_ENTRY.get(entry);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Field A = NMS.getField(PlayerChunkMap.class, "A");
    private static Method B = NMS.getMethod(PlayerChunkMap.class, "b", true, ChunkCoordIntPair.class,
            EntityPlayer.class, boolean.class);
    private static Field D = NMS.getField(EntityTrackerEntry.class, "d");
    private static Field E = NMS.getField(EntityTrackerEntry.class, "e");
    private static final Method GET_VISIBLE_CHUNK = NMS.getMethod(PlayerChunkMap.class, "getVisibleChunk", true,
            long.class);
    private static Field I = NMS.getField(EntityTracker.class, "trackingDistance");
    private static Field TRACKER = NMS.getField(EntityTracker.class, "tracker");
    private static Field TRACKER_ENTRY = NMS.getField(EntityTracker.class, "trackerEntry");
}