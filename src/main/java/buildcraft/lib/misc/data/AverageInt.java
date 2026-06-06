package buildcraft.lib.misc.data;

public class AverageInt {
   private int[] data;
   private int pos;
   private int precise;
   private int averageRaw;
   private int tickValue;

   public AverageInt(int precise) {
      this.precise = precise;
      this.clear();
   }

   public void clear() {
      this.data = new int[this.precise];
      this.pos = 0;
   }

   public double getAverage() {
      return (double)this.averageRaw / this.precise;
   }

   public void tick(int value) {
      this.internalTick(this.tickValue + value);
      this.tickValue = 0;
   }

   public void tick() {
      this.internalTick(this.tickValue);
      this.tickValue = 0;
   }

   private void internalTick(int value) {
      this.pos = ++this.pos % this.precise;
      int oldValue = this.data[this.pos];
      this.data[this.pos] = value;
      if (this.pos == 0) {
         this.averageRaw = 0;

         for (int iValue : this.data) {
            this.averageRaw += iValue;
         }
      } else {
         this.averageRaw = this.averageRaw - oldValue + value;
      }
   }

   public void push(int value) {
      this.tickValue += value;
   }
}
