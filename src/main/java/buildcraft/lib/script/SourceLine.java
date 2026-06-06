package buildcraft.lib.script;

import java.util.Objects;

public final class SourceLine {
   public final SourceFile file;
   public final int line;

   public SourceLine(SourceFile file, int line) {
      this.file = file;
      this.line = line;
   }

   public void appendLineNumber(StringBuilder sb) {
      sb.append(this.line);
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.file, this.line);
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      } else if (obj != null && obj.getClass() == this.getClass()) {
         SourceLine other = (SourceLine)obj;
         return this.line == other.line && this.file.equals(other.file);
      } else {
         return false;
      }
   }

   @Override
   public String toString() {
      return this.file.name + "." + (this.line + 1);
   }
}
