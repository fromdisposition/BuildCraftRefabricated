/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.statements;

import buildcraft.api.core.EnumPipePart;
import java.util.Arrays;
import java.util.Objects;

public class StatementSlot {
   public IStatement statement;
   public IStatementParameter[] parameters;
   public EnumPipePart part = EnumPipePart.CENTER;

   @Override
   public boolean equals(Object o) {
      if (!(o instanceof StatementSlot s)) {
         return false;
      } else if (s.statement == this.statement && this.parameters.length == s.parameters.length) {
         for (int i = 0; i < this.parameters.length; i++) {
            IStatementParameter p1 = this.parameters[i];
            IStatementParameter p2 = s.parameters[i];
            if (p1 == null) {
               if (p2 != null) {
                  return false;
               }
            } else {
               if (p2 == null) {
                  return false;
               }

               if (!p1.equals(p2)) {
                  return false;
               }
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.statement, Arrays.deepHashCode(this.parameters));
   }
}
