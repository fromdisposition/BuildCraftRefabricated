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
