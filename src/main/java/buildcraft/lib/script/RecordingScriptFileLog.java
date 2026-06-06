package buildcraft.lib.script;

import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RecordingScriptFileLog implements IScriptFileLog {
   public final List<String> contents = new ArrayList<>();
   public final List<RecordingScriptFileLog.InfoMarker> markers = new ArrayList<>();

   @Override
   public void populateFile(SourceFile file, List<String> lines) {
      this.contents.addAll(lines);
   }

   @Override
   public void error(int line, int startIndex, int endIndex, String message) {
      this.markers.add(new RecordingScriptFileLog.InfoMarker(line, startIndex, line, endIndex, message));
   }

   @Override
   public void errorMissingArgument(int line, int argIndex, String argDesc) {
   }

   @Override
   public void infoSkippingIfBlock(int line) {
   }

   @Override
   public void infoEndSkipping(int line) {
   }

   @Override
   public void infoConditionalResult(int tokenStart, int startIndex, int endIndex, boolean shouldCall) {
   }

   @Override
   public void errorFunctionUnknown(int line, int startIndex, int endIndex, Collection<String> knownFunctions) {
   }

   @Override
   public void errorStdMissingName(int line) {
   }

   @Override
   public void errorStdInvalidJson(int line, JsonSyntaxException jse) {
   }

   @Override
   public void errorStdUnknownFile(int line, String file) {
   }

   @Override
   public void errorImportNotFound(int line, String sourceFile) {
   }

   @Override
   public void errorImportMissingStarter(int line, String sourceFile) {
   }

   @Override
   public void errorImportRecursiveReplace(int line, String newSourceFile) {
   }

   @Override
   public void errorAliasInvalidArgCount(int line, int startIndex, int endIndex, Integer parsed) {
   }

   @Override
   public void replace(int removeStart, int removeEnd, SourceFile from, int fromStart, List<String> newLines) {
   }

   public static class InfoMarker {
      public final int startLine;
      public final int startLineIndex;
      public final int endLine;
      public final int endLineIndex;
      public final String message;

      public InfoMarker(int startLine, int startLineIndex, int endLine, int endLineIndex, String message) {
         this.startLine = startLine;
         this.startLineIndex = startLineIndex;
         this.endLine = endLine;
         this.endLineIndex = endLineIndex;
         this.message = message;
      }
   }
}
