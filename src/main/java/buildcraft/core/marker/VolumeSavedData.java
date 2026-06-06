package buildcraft.core.marker;

import com.google.common.collect.UnmodifiableIterator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class VolumeSavedData extends SavedData {
   public static final String ID = "buildcraft_marker_volume";
   public List<BlockPos> markerPositions = new ArrayList<>();
   public List<List<BlockPos>> markerConnections = new ArrayList<>();
   private VolumeSubCache subCache;
   private static final Codec<VolumeSavedData> CODEC = RecordCodecBuilder.create(
      instance -> instance.group(BlockPos.CODEC.listOf().optionalFieldOf("markers", List.of()).forGetter(d -> {
            d.syncFromSubCache();
            return d.markerPositions;
         }), BlockPos.CODEC.listOf().listOf().optionalFieldOf("connections", List.of()).forGetter(d -> d.markerConnections))
         .apply(instance, (positions, connections) -> {
            VolumeSavedData data = new VolumeSavedData();
            data.markerPositions = new ArrayList<>(positions);
            data.markerConnections = new ArrayList<>();

            for (List<BlockPos> conn : connections) {
               data.markerConnections.add(new ArrayList<>(conn));
            }

            return data;
         })
   );
   public static final SavedDataType<VolumeSavedData> TYPE = new SavedDataType(
      Identifier.withDefaultNamespace("buildcraft_marker_volume"), VolumeSavedData::new, CODEC, DataFixTypes.LEVEL
   );

   private VolumeSavedData() {
   }

   public void setSubCache(VolumeSubCache subCache) {
      this.subCache = subCache;
   }

   private void syncFromSubCache() {
      if (this.subCache != null) {
         this.markerPositions = new ArrayList<>(this.subCache.getAllMarkers());
         this.markerConnections = new ArrayList<>();
         UnmodifiableIterator var1 = this.subCache.getConnections().iterator();

         while (var1.hasNext()) {
            VolumeConnection connection = (VolumeConnection)var1.next();
            this.markerConnections.add(new ArrayList<>(connection.getMarkerPositions()));
         }
      }
   }

   public static VolumeSavedData getOrCreate(Level level) {
      return level.isClientSide() ? new VolumeSavedData() : (VolumeSavedData)((ServerLevel)level).getDataStorage().computeIfAbsent(TYPE);
   }
}
