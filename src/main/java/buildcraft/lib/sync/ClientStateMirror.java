package buildcraft.lib.sync;

public interface ClientStateMirror {
   void applyFullSync(Runnable var1);

   void applyDelta(Runnable var1);
}
