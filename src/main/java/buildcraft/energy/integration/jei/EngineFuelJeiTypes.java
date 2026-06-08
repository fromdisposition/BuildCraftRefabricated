/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.energy.integration.jei;

import buildcraft.api.fuels.IFuel;
import mezz.jei.api.recipe.types.IRecipeType;

public final class EngineFuelJeiTypes {
   public static final IRecipeType<IFuel> COMBUSTION_FUEL = IRecipeType.create("buildcraftenergy", "combustion_engine_fuel", IFuel.class);
   public static final IRecipeType<CombustionCoolantJei> COMBUSTION_COOLANT = IRecipeType.create(
      "buildcraftenergy", "combustion_engine_coolant", CombustionCoolantJei.class
   );
   public static final IRecipeType<StirlingFuelJei> STIRLING_FUEL = IRecipeType.create("buildcraftenergy", "stirling_engine_fuel", StirlingFuelJei.class);

   private EngineFuelJeiTypes() {
   }
}
