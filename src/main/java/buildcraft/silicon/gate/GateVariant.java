/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import buildcraft.lib.nbt.BcNbt;
import java.util.Objects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class GateVariant {
   public final EnumGateLogic logic;
   public final EnumGateMaterial material;
   public final EnumGateModifier modifier;
   public final int numSlots;
   public final int numTriggerArgs;
   public final int numActionArgs;
   private final int hash;

   public GateVariant(EnumGateLogic logic, EnumGateMaterial material, EnumGateModifier modifier) {
      this.logic = logic;
      this.material = material;
      this.modifier = modifier;
      this.numSlots = material.numSlots / modifier.slotDivisor;
      this.numTriggerArgs = modifier.triggerParams;
      this.numActionArgs = modifier.actionParams;
      this.hash = Objects.hash(logic, material, modifier);
   }

   public GateVariant(CompoundTag nbt) {
      this.logic = EnumGateLogic.getByOrdinal(BcNbt.getByte(nbt, "logic", (byte)0));
      this.material = EnumGateMaterial.getByOrdinal(BcNbt.getByte(nbt, "material", (byte)0));
      this.modifier = EnumGateModifier.getByOrdinal(BcNbt.getByte(nbt, "modifier", (byte)0));
      this.numSlots = this.material.numSlots / this.modifier.slotDivisor;
      this.numTriggerArgs = this.modifier.triggerParams;
      this.numActionArgs = this.modifier.actionParams;
      this.hash = Objects.hash(this.logic, this.material, this.modifier);
   }

   public CompoundTag writeToNBT() {
      CompoundTag nbt = new CompoundTag();
      nbt.putByte("logic", (byte)this.logic.ordinal());
      nbt.putByte("material", (byte)this.material.ordinal());
      nbt.putByte("modifier", (byte)this.modifier.ordinal());
      return nbt;
   }

   public GateVariant(FriendlyByteBuf buffer) {
      this.logic = EnumGateLogic.getByOrdinal(buffer.readUnsignedByte());
      this.material = EnumGateMaterial.getByOrdinal(buffer.readUnsignedByte());
      this.modifier = EnumGateModifier.getByOrdinal(buffer.readUnsignedByte());
      this.numSlots = this.material.numSlots / this.modifier.slotDivisor;
      this.numTriggerArgs = this.modifier.triggerParams;
      this.numActionArgs = this.modifier.actionParams;
      this.hash = Objects.hash(this.logic, this.material, this.modifier);
   }

   public void writeToBuffer(FriendlyByteBuf buffer) {
      buffer.writeByte(this.logic.ordinal());
      buffer.writeByte(this.material.ordinal());
      buffer.writeByte(this.modifier.ordinal());
   }

   public String getVariantName() {
      return this.material.canBeModified ? this.material.tag + "_" + this.logic.tag + "_" + this.modifier.tag : this.material.tag;
   }

   public Component getLocalizedName() {
      return this.material == EnumGateMaterial.CLAY_BRICK
         ? Component.translatable("gate.name.basic")
         : Component.translatable(
            "gate.name", new Object[]{Component.translatable("gate.material." + this.material.tag), Component.translatable("gate.logic." + this.logic.tag)}
         );
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (obj.getClass() != this.getClass()) {
         return false;
      }

      GateVariant other = (GateVariant)obj;
      return other.logic == this.logic && other.material == this.material && other.modifier == this.modifier;
   }

   @Override
   public int hashCode() {
      return this.hash;
   }
}
