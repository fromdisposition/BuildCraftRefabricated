package buildcraft.transport.client;

import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.pipe.PipeDefinition;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.fabric.client.block.ClientBlockExtensions;
import buildcraft.lib.misc.SpriteUtil;
import buildcraft.transport.block.BlockPipeHolder;
import buildcraft.transport.client.model.PipeModelCachePluggable;
import buildcraft.transport.pipe.Pipe;
import buildcraft.transport.tile.TilePipeHolder;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SingleQuadParticle.Layer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public class PipeHolderClientExtensions implements ClientBlockExtensions {
   public static final PipeHolderClientExtensions INSTANCE = new PipeHolderClientExtensions();
   private final ItemStackRenderState renderState = new ItemStackRenderState();

   private PipeHolderClientExtensions() {
   }

   private @Nullable TextureAtlasSprite getPipeSprite(Level level, BlockPos pos) {
      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile && tile.getPipe() != null) {
         PipeDefinition def = tile.getPipe().getDefinition();
         if (def.textures != null && def.textures.length > 0) {
            TextureAtlasSprite sprite = SpriteUtil.getSprite(def.textures[0]);
            if (sprite != null && sprite != SpriteUtil.missingSprite()) {
               return sprite;
            }
         }
      }

      return null;
   }

   private @Nullable TextureAtlasSprite getPluggableSprite(PipePluggable pluggable) {
      if (pluggable instanceof IFacade facade) {
         IFacadePhasedState[] states = facade.getPhasedStates();
         if (states != null && states.length > 0) {
            BlockState state = states[0].getState().getBlockState();
            if (state != null) {
               Minecraft mc = Minecraft.getInstance();
               BlockStateModel model = mc.getModelManager().getBlockStateModelSet().get(state);
               if (model != null) {
                  for (Method m : model.getClass().getMethods()) {
                     String name = m.getName().toLowerCase();
                     if ((name.contains("particle") || name.contains("icon") || name.contains("sprite"))
                        && m.getParameterCount() == 0
                        && m.getReturnType() == TextureAtlasSprite.class) {
                        try {
                           m.setAccessible(true);
                           TextureAtlasSprite sprite = (TextureAtlasSprite)m.invoke(model);
                           if (sprite != null) {
                              return sprite;
                           }
                        } catch (Exception var14) {
                        }
                     }
                  }

                  for (Method m : model.getClass().getMethods()) {
                     if (m.getParameterCount() == 3 && m.getReturnType() == TextureAtlasSprite.class) {
                        try {
                           m.setAccessible(true);
                           TextureAtlasSprite sprite = (TextureAtlasSprite)m.invoke(model, mc.level, BlockPos.ZERO, state);
                           if (sprite != null) {
                              return sprite;
                           }
                        } catch (Exception var13) {
                        }
                     }
                  }
               }
            }
         }

         return null;
      } else {
         PluggableModelKey keyC = pluggable.getModelRenderKey("cutout");
         PluggableModelKey keyT = pluggable.getModelRenderKey("translucent");
         List<BakedQuad> quads = null;
         if (keyC != null) {
            quads = PipeModelCachePluggable.cacheCutoutSingle.bake(keyC);
         }

         if ((quads == null || quads.isEmpty()) && keyT != null) {
            quads = PipeModelCachePluggable.cacheTranslucentSingle.bake(keyT);
         }

         if (quads != null && !quads.isEmpty()) {
            BakedQuad quad = quads.get(0);

            for (Method m : quad.getClass().getMethods()) {
               if (m.getReturnType() == TextureAtlasSprite.class && m.getParameterCount() == 0) {
                  try {
                     return (TextureAtlasSprite)m.invoke(quad);
                  } catch (Exception var15) {
                  }
               }
            }
         }

         ItemStack stack = pluggable.getPickStack();
         if (!stack.isEmpty()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
               this.renderState.clear();
               ItemModelResolver resolver = mc.getItemModelResolver();
               resolver.appendItemLayers(this.renderState, stack, ItemDisplayContext.GUI, mc.level, null, 0);
               Baked particleMat = this.renderState.pickParticleMaterial(mc.level.getRandom());
               TextureAtlasSprite sprite = particleMat != null ? particleMat.sprite() : null;
               if (sprite != null && sprite != SpriteUtil.missingSprite()) {
                  return sprite;
               }
            }
         }

         return null;
      }
   }

   private PipeHolderClientExtensions.@Nullable HitSpriteInfo getHitSpriteInfo(Level level, BlockPos pos, @Nullable HitResult target) {
      if (!(target instanceof BlockHitResult blockHit && pos.equals(blockHit.getBlockPos()))) {
         return null;
      } else if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         double var16 = blockHit.getLocation().x - pos.getX();
         double ly = blockHit.getLocation().y - pos.getY();
         double lz = blockHit.getLocation().z - pos.getZ();
         Direction plugDir = BlockPipeHolder.getHitPluggable(tile, var16, ly, lz);
         if (plugDir != null) {
            PipePluggable plug = tile.getPluggable(plugDir);
            if (plug != null) {
               AABB box = plug.getBoundingBox();
               TextureAtlasSprite sprite = this.getPluggableSprite(plug);
               if (sprite != null && box != null) {
                  return new PipeHolderClientExtensions.HitSpriteInfo(box, sprite);
               }
            }
         }

         return this.getPipeSpriteInfo(level, pos, tile);
      } else {
         return null;
      }
   }

   private PipeHolderClientExtensions.@Nullable HitSpriteInfo getPipeSpriteInfo(Level level, BlockPos pos, TilePipeHolder tile) {
      Pipe pipe = tile.getPipe();
      if (pipe != null) {
         PipeDefinition def = pipe.getDefinition();
         if (def != null && def.textures != null && def.textures.length > 0) {
            TextureAtlasSprite sprite = SpriteUtil.getSprite(def.textures[0]);
            if (sprite != null) {
               return new PipeHolderClientExtensions.HitSpriteInfo(new AABB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75), sprite);
            }
         }
      }

      return null;
   }

   @Override
   public boolean addHitEffects(BlockState state, Level level, @Nullable HitResult target, ParticleEngine manager) {
      if (target instanceof BlockHitResult blockHit) {
         PipeHolderClientExtensions.HitSpriteInfo info = this.getHitSpriteInfo(level, blockHit.getBlockPos(), target);
         if (info != null) {
            BlockPos pos = blockHit.getBlockPos();
            Direction face = blockHit.getDirection();
            double x = pos.getX() + Math.random() * (info.aabb.maxX - info.aabb.minX) + info.aabb.minX;
            double y = pos.getY() + Math.random() * (info.aabb.maxY - info.aabb.minY) + info.aabb.minY;
            double z = pos.getZ() + Math.random() * (info.aabb.maxZ - info.aabb.minZ) + info.aabb.minZ;
            switch (face) {
               case DOWN:
                  y = pos.getY() + info.aabb.minY - 0.1;
                  break;
               case UP:
                  y = pos.getY() + info.aabb.maxY + 0.1;
                  break;
               case NORTH:
                  z = pos.getZ() + info.aabb.minZ - 0.1;
                  break;
               case SOUTH:
                  z = pos.getZ() + info.aabb.maxZ + 0.1;
                  break;
               case WEST:
                  x = pos.getX() + info.aabb.minX - 0.1;
                  break;
               case EAST:
                  x = pos.getX() + info.aabb.maxX + 0.1;
            }

            PipeHolderClientExtensions.PipeBreakParticle particle = new PipeHolderClientExtensions.PipeBreakParticle(
               (ClientLevel)level, x, y, z, 0.0, 0.0, 0.0, info.sprite
            );
            manager.add(particle);
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine manager) {
      PipeHolderClientExtensions.HitSpriteInfo info = this.getHitSpriteInfo(level, pos, Minecraft.getInstance().hitResult);
      if (info == null && level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
         info = this.getPipeSpriteInfo(level, pos, tile);
      }

      if (info == null) {
         return false;
      }

      double sizeX = info.aabb.maxX - info.aabb.minX;
      double sizeY = info.aabb.maxY - info.aabb.minY;
      double sizeZ = info.aabb.maxZ - info.aabb.minZ;
      int countX = (int)Math.max(2.0, 4.0 * sizeX);
      int countY = (int)Math.max(2.0, 4.0 * sizeY);
      int countZ = (int)Math.max(2.0, 4.0 * sizeZ);

      for (int x = 0; x < countX; x++) {
         for (int y = 0; y < countY; y++) {
            for (int z = 0; z < countZ; z++) {
               double _x = pos.getX() + info.aabb.minX + (x + 0.5) * sizeX / countX;
               double _y = pos.getY() + info.aabb.minY + (y + 0.5) * sizeY / countY;
               double _z = pos.getZ() + info.aabb.minZ + (z + 0.5) * sizeZ / countZ;
               PipeHolderClientExtensions.PipeBreakParticle particle = new PipeHolderClientExtensions.PipeBreakParticle(
                  (ClientLevel)level, _x, _y, _z, _x - pos.getX() - 0.5, _y - pos.getY() - 0.5, _z - pos.getZ() - 0.5, info.sprite
               );
               manager.add(particle);
            }
         }
      }

      return true;
   }

   public static boolean spawnRunningParticle(
      Level level, BlockPos pos, double entityX, double entityZ, double entityWidth, double motionX, double motionZ, double minY
   ) {
      if (!level.isClientSide()) {
         return false;
      }

      if (level.getBlockEntity(pos) instanceof TilePipeHolder tile && tile.getPipe() != null) {
         TextureAtlasSprite sprite = null;
         PipePluggable upPlug = tile.getPluggable(Direction.UP);
         if (upPlug != null) {
            sprite = INSTANCE.getPluggableSprite(upPlug);
         }

         if (sprite == null) {
            PipeDefinition def = tile.getPipe().getDefinition();
            if (def.textures != null && def.textures.length > 0) {
               sprite = SpriteUtil.getSprite(def.textures[0]);
               if (sprite == SpriteUtil.missingSprite()) {
                  sprite = null;
               }
            }
         }

         if (sprite == null) {
            return false;
         }

         RandomSource random = level.getRandom();
         double x = entityX + (random.nextFloat() - 0.5) * entityWidth;
         double y = minY + 0.1;
         double z = entityZ + (random.nextFloat() - 0.5) * entityWidth;
         PipeHolderClientExtensions.PipeBreakParticle particle = new PipeHolderClientExtensions.PipeBreakParticle(
            (ClientLevel)level, x, y, z, -motionX * 4.0, 1.5, -motionZ * 4.0, sprite
         );
         particle.setLifetime(particle.getLifetime() / 2);
         Minecraft.getInstance().particleEngine.add(particle);
         return true;
      } else {
         return false;
      }
   }

   public static void spawnLandingParticles(Level level, BlockPos pos, double x, double y, double z, int numberOfParticles) {
      if (level instanceof ClientLevel clientLevel) {
         PipeHolderClientExtensions.HitSpriteInfo info = null;
         if (level.getBlockEntity(pos) instanceof TilePipeHolder tile) {
            PipePluggable upPlug = tile.getPluggable(Direction.UP);
            if (upPlug != null) {
               TextureAtlasSprite sprite = INSTANCE.getPluggableSprite(upPlug);
               AABB box = upPlug.getBoundingBox();
               if (sprite != null && box != null) {
                  info = new PipeHolderClientExtensions.HitSpriteInfo(box, sprite);
               }
            }

            if (info == null) {
               info = INSTANCE.getPipeSpriteInfo(level, pos, tile);
            }
         }

         if (info != null) {
            RandomSource random = level.getRandom();

            for (int i = 0; i < numberOfParticles; i++) {
               double px = x + (random.nextFloat() - 0.5) * 0.5;
               double py = y;
               double pz = z + (random.nextFloat() - 0.5) * 0.5;
               double motionX = random.nextGaussian() * 0.15;
               double motionY = random.nextGaussian() * 0.15;
               double motionZ = random.nextGaussian() * 0.15;
               PipeHolderClientExtensions.PipeBreakParticle particle = new PipeHolderClientExtensions.PipeBreakParticle(
                  clientLevel, px, py, pz, motionX, motionY, motionZ, info.sprite
               );
               Minecraft.getInstance().particleEngine.add(particle);
            }
         }
      }
   }

   private static final class HitSpriteInfo {
      final AABB aabb;
      final TextureAtlasSprite sprite;

      HitSpriteInfo(AABB aabb, TextureAtlasSprite sprite) {
         this.aabb = aabb;
         this.sprite = sprite;
      }
   }

   private static class PipeBreakParticle extends SingleQuadParticle {
      private final float u0;
      private final float u1;
      private final float v0;
      private final float v1;

      PipeBreakParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd, TextureAtlasSprite sprite) {
         super(level, x, y, z, xd, yd, zd, sprite);
         this.gravity = 1.0F;
         this.quadSize /= 2.0F;
         float uRange = sprite.getU1() - sprite.getU0();
         float vRange = sprite.getV1() - sprite.getV0();
         float cellU = uRange / 4.0F;
         float cellV = vRange / 4.0F;
         this.u0 = sprite.getU0() + this.random.nextInt(4) * cellU;
         this.v0 = sprite.getV0() + this.random.nextInt(4) * cellV;
         this.u1 = this.u0 + cellU;
         this.v1 = this.v0 + cellV;
      }

      protected float getU0() {
         return this.u0;
      }

      protected float getU1() {
         return this.u1;
      }

      protected float getV0() {
         return this.v0;
      }

      protected float getV1() {
         return this.v1;
      }

      protected Layer getLayer() {
         return Layer.OPAQUE_TERRAIN;
      }
   }
}
