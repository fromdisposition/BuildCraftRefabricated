/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.api.core;

import java.io.IOException;

public class InvalidInputDataException extends IOException {
   private static final long serialVersionUID = 1L;

   public InvalidInputDataException() {
   }

   public InvalidInputDataException(String message) {
      super(message);
   }

   public InvalidInputDataException(Throwable cause) {
      super(cause);
   }

   public InvalidInputDataException(String message, Throwable cause) {
      super(message, cause);
   }
}
