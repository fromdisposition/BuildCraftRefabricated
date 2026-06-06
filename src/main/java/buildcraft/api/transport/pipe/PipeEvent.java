package buildcraft.api.transport.pipe;

public abstract class PipeEvent {
   public final boolean canBeCancelled;
   public final IPipeHolder holder;
   private boolean canceled = false;

   public PipeEvent(IPipeHolder holder) {
      this.canBeCancelled = false;
      this.holder = holder;
   }

   protected PipeEvent(boolean canBeCancelled, IPipeHolder holder) {
      this.canBeCancelled = canBeCancelled;
      this.holder = holder;
   }

   public void cancel() {
      if (this.canBeCancelled) {
         this.canceled = true;
      }
   }

   public boolean isCanceled() {
      return this.canceled;
   }

   public String checkStateForErrors() {
      return this.canceled & !this.canBeCancelled ? "Somehow cancelled an event that isn't marked as such!" : null;
   }
}
