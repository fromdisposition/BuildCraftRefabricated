package buildcraft.core.client;

import buildcraft.core.BCCoreSprites;
import buildcraft.lib.client.render.laser.LaserData_BC8;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;

public class BuildCraftLaserManager {
   public static final LaserData_BC8.LaserType MARKER_VOLUME_CONNECTED;
   public static final LaserData_BC8.LaserType MARKER_VOLUME_POSSIBLE;
   public static final LaserData_BC8.LaserType MARKER_VOLUME_SIGNAL;
   public static final LaserData_BC8.LaserType MARKER_PATH_CONNECTED;
   public static final LaserData_BC8.LaserType MARKER_PATH_POSSIBLE;
   public static final LaserData_BC8.LaserType MARKER_DEFAULT_POSSIBLE;
   public static final LaserData_BC8.LaserType STRIPES_READ;
   public static final LaserData_BC8.LaserType STRIPES_WRITE;
   public static final LaserData_BC8.LaserType STRIPES_WRITE_DIRECTION;
   public static final LaserData_BC8.LaserType POWER_LOW;
   public static final LaserData_BC8.LaserType POWER_MED;
   public static final LaserData_BC8.LaserType POWER_HIGH;
   public static final LaserData_BC8.LaserType POWER_FULL;
   public static final LaserData_BC8.LaserType[] POWERS;

   static {
      SpriteHolderRegistry.SpriteHolder sprite = BCCoreSprites.MARKER_VOLUME_CONNECTED;
      LaserData_BC8.LaserRow capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 2, 2);
      LaserData_BC8.LaserRow start = new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 2);
      LaserData_BC8.LaserRow[] middle = new LaserData_BC8.LaserRow[]{
         new LaserData_BC8.LaserRow(sprite, 0, 2, 16, 4),
         new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 6),
         new LaserData_BC8.LaserRow(sprite, 0, 6, 16, 8),
         new LaserData_BC8.LaserRow(sprite, 0, 8, 16, 10),
         new LaserData_BC8.LaserRow(sprite, 0, 10, 16, 12),
         new LaserData_BC8.LaserRow(sprite, 0, 12, 16, 14)
      };
      LaserData_BC8.LaserRow end = new LaserData_BC8.LaserRow(sprite, 0, 14, 16, 16);
      LaserData_BC8.LaserRow capEnd = new LaserData_BC8.LaserRow(sprite, 14, 14, 16, 16);
      MARKER_VOLUME_CONNECTED = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
      sprite = BCCoreSprites.MARKER_PATH_CONNECTED;
      capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 3, 3);
      start = new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 3);
      middle = new LaserData_BC8.LaserRow[]{
         new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 7, LaserData_BC8.LaserSide.TOP, LaserData_BC8.LaserSide.BOTTOM),
         new LaserData_BC8.LaserRow(sprite, 0, 8, 16, 11, LaserData_BC8.LaserSide.LEFT, LaserData_BC8.LaserSide.RIGHT)
      };
      end = new LaserData_BC8.LaserRow(sprite, 0, 12, 16, 15);
      capEnd = new LaserData_BC8.LaserRow(sprite, 13, 12, 16, 15);
      MARKER_PATH_CONNECTED = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
      sprite = BCCoreSprites.MARKER_VOLUME_POSSIBLE;
      capStart = new LaserData_BC8.LaserRow(sprite, 0, 0, 1, 1);
      start = new LaserData_BC8.LaserRow(sprite, 0, 0, 16, 1);
      middle = new LaserData_BC8.LaserRow[]{
         new LaserData_BC8.LaserRow(sprite, 0, 1, 16, 2),
         new LaserData_BC8.LaserRow(sprite, 0, 2, 16, 3),
         new LaserData_BC8.LaserRow(sprite, 0, 3, 16, 4),
         new LaserData_BC8.LaserRow(sprite, 0, 4, 16, 5),
         new LaserData_BC8.LaserRow(sprite, 0, 5, 16, 6),
         new LaserData_BC8.LaserRow(sprite, 0, 6, 16, 7),
         new LaserData_BC8.LaserRow(sprite, 0, 7, 16, 8),
         new LaserData_BC8.LaserRow(sprite, 0, 8, 16, 9),
         new LaserData_BC8.LaserRow(sprite, 0, 9, 16, 10),
         new LaserData_BC8.LaserRow(sprite, 0, 10, 16, 11),
         new LaserData_BC8.LaserRow(sprite, 0, 11, 16, 12),
         new LaserData_BC8.LaserRow(sprite, 0, 12, 16, 13),
         new LaserData_BC8.LaserRow(sprite, 0, 13, 16, 14),
         new LaserData_BC8.LaserRow(sprite, 0, 14, 16, 15)
      };
      end = new LaserData_BC8.LaserRow(sprite, 0, 15, 16, 16);
      capEnd = new LaserData_BC8.LaserRow(sprite, 15, 15, 16, 16);
      MARKER_VOLUME_POSSIBLE = new LaserData_BC8.LaserType(capStart, start, middle, end, capEnd);
      MARKER_VOLUME_SIGNAL = new LaserData_BC8.LaserType(MARKER_VOLUME_CONNECTED, BCCoreSprites.MARKER_VOLUME_SIGNAL);
      MARKER_PATH_POSSIBLE = new LaserData_BC8.LaserType(MARKER_VOLUME_POSSIBLE, BCCoreSprites.MARKER_PATH_POSSIBLE);
      MARKER_DEFAULT_POSSIBLE = new LaserData_BC8.LaserType(MARKER_VOLUME_POSSIBLE, BCCoreSprites.MARKER_DEFAULT_POSSIBLE);
      STRIPES_READ = new LaserData_BC8.LaserType(MARKER_VOLUME_CONNECTED, BCCoreSprites.STRIPES_READ);
      STRIPES_WRITE = new LaserData_BC8.LaserType(MARKER_VOLUME_CONNECTED, BCCoreSprites.STRIPES_WRITE);
      STRIPES_WRITE_DIRECTION = new LaserData_BC8.LaserType(MARKER_PATH_CONNECTED, BCCoreSprites.STRIPES_WRITE_DIRECTION);
      POWER_LOW = new LaserData_BC8.LaserType(MARKER_VOLUME_POSSIBLE, BCCoreSprites.LASER_POWER_LOW);
      POWER_MED = new LaserData_BC8.LaserType(MARKER_VOLUME_POSSIBLE, BCCoreSprites.LASER_POWER_MED);
      POWER_HIGH = new LaserData_BC8.LaserType(MARKER_VOLUME_POSSIBLE, BCCoreSprites.LASER_POWER_HIGH);
      POWER_FULL = new LaserData_BC8.LaserType(MARKER_VOLUME_POSSIBLE, BCCoreSprites.LASER_POWER_FULL);
      POWERS = new LaserData_BC8.LaserType[]{POWER_LOW, POWER_MED, POWER_HIGH, POWER_FULL};
   }
}
