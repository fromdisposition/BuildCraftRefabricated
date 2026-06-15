package buildcraft.lib.fluid.stack;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FluidStackTest {
   @BeforeAll
   static void bootstrap() {
      SharedConstants.tryDetectVersion();
      Bootstrap.bootStrap();
      // Vanilla fluid Holder.References have their component prototypes bound
      // during data-pack loading, which doesn't run in unit tests. Bind empty
      // prototypes manually so FluidStack constructors can call holder.components().
      for (Fluid fluid : BuiltInRegistries.FLUID) {
         Holder<Fluid> holder = BuiltInRegistries.FLUID.wrapAsHolder(fluid);
         if (holder instanceof Holder.Reference<Fluid> ref && !ref.areComponentsBound()) {
            ref.bindComponents(DataComponentMap.EMPTY);
         }
      }
   }

   private static FluidStack water(int amount) {
      return new FluidStack(Fluids.WATER, amount);
   }

   private static FluidStack lava(int amount) {
      return new FluidStack(Fluids.LAVA, amount);
   }

   // --- isEmpty ---

   @Test
   void emptyConstantIsEmpty() {
      assertTrue(FluidStack.EMPTY.isEmpty());
   }

   @Test
   void zeroAmountIsEmpty() {
      assertTrue(water(0).isEmpty());
   }

   @Test
   void nonZeroWaterIsNotEmpty() {
      assertFalse(water(1000).isEmpty());
   }

   // --- getAmount / setAmount / grow / shrink ---

   @Test
   void emptyReturnsZeroAmount() {
      assertEquals(0, FluidStack.EMPTY.getAmount());
   }

   @Test
   void setAmountMutates() {
      FluidStack stack = water(500);
      stack.setAmount(250);
      assertEquals(250, stack.getAmount());
   }

   @Test
   void growIncreasesAmount() {
      FluidStack stack = water(500);
      stack.grow(100);
      assertEquals(600, stack.getAmount());
   }

   @Test
   void shrinkDecreasesAmount() {
      FluidStack stack = water(500);
      stack.shrink(200);
      assertEquals(300, stack.getAmount());
   }

   // --- copy / copyWithAmount ---

   @Test
   void copyPreservesFluidAndAmount() {
      FluidStack original = water(1000);
      FluidStack copy = original.copy();
      assertEquals(original.getFluid(), copy.getFluid());
      assertEquals(original.getAmount(), copy.getAmount());
   }

   @Test
   void copyIsIndependent() {
      FluidStack original = water(1000);
      FluidStack copy = original.copy();
      copy.setAmount(500);
      assertEquals(1000, original.getAmount());
   }

   @Test
   void copyEmptyReturnsEmptyConstant() {
      assertSame(FluidStack.EMPTY, FluidStack.EMPTY.copy());
   }

   @Test
   void copyWithAmountChangesAmount() {
      FluidStack stack = water(1000);
      FluidStack copy = stack.copyWithAmount(250);
      assertEquals(250, copy.getAmount());
      assertEquals(stack.getFluid(), copy.getFluid());
   }

   // --- split ---

   @Test
   void splitReducesOriginalAndReturnsSlice() {
      FluidStack stack = water(1000);
      FluidStack slice = stack.split(400);
      assertEquals(400, slice.getAmount());
      assertEquals(600, stack.getAmount());
   }

   @Test
   void splitBeyondAmountClampsToAvailable() {
      FluidStack stack = water(300);
      FluidStack slice = stack.split(1000);
      assertEquals(300, slice.getAmount());
      assertEquals(0, stack.getAmount());
   }

   // --- matches / isSameFluid / isSameFluidSameComponents ---

   @Test
   void matchesTrueForIdenticalStacks() {
      FluidStack a = water(1000);
      FluidStack b = water(1000);
      assertTrue(FluidStack.matches(a, b));
   }

   @Test
   void matchesFalseForDifferentAmounts() {
      assertFalse(FluidStack.matches(water(1000), water(500)));
   }

   @Test
   void matchesFalseForDifferentFluids() {
      assertFalse(FluidStack.matches(water(1000), lava(1000)));
   }

   @Test
   void isSameFluidTrueRegardlessOfAmount() {
      assertTrue(FluidStack.isSameFluid(water(1000), water(500)));
   }

   @Test
   void isSameFluidFalseForDifferentFluids() {
      assertFalse(FluidStack.isSameFluid(water(1000), lava(1000)));
   }

   @Test
   void isSameFluidSameComponentsTrueForTwoWaterStacks() {
      assertTrue(FluidStack.isSameFluidSameComponents(water(1000), water(500)));
   }

   @Test
   void isSameFluidSameComponentsFalseForWaterAndLava() {
      assertFalse(FluidStack.isSameFluidSameComponents(water(1000), lava(1000)));
   }

   // --- limitSize ---

   @Test
   void limitSizeReducesWhenOverLimit() {
      FluidStack stack = water(1000);
      stack.limitSize(500);
      assertEquals(500, stack.getAmount());
   }

   @Test
   void limitSizeNoOpWhenUnderLimit() {
      FluidStack stack = water(200);
      stack.limitSize(500);
      assertEquals(200, stack.getAmount());
   }

   // --- hashFluidAndComponents ---

   @Test
   void hashConsistentForEqualStacks() {
      assertEquals(
         FluidStack.hashFluidAndComponents(water(1000)),
         FluidStack.hashFluidAndComponents(water(500)),
         "hash should depend on fluid+components, not amount"
      );
   }

   @Test
   void hashDiffersForDifferentFluids() {
      assertNotEquals(
         FluidStack.hashFluidAndComponents(water(1000)),
         FluidStack.hashFluidAndComponents(lava(1000))
      );
   }

   @Test
   void hashZeroForNull() {
      assertEquals(0, FluidStack.hashFluidAndComponents(null));
   }
}
