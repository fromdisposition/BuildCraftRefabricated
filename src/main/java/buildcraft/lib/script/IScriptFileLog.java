package buildcraft.lib.script;

import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import com.google.gson.JsonSyntaxException;

public interface IScriptFileLog {

    static final int END_OF_LINE = Integer.MAX_VALUE;

    void error(int line, int startIndex, int endIndex, String message);

    default void error(int line, String message) {
        error(line, 0, END_OF_LINE, message);
    }

    void populateFile(@Nullable SourceFile file, List<String> lines);

    void errorMissingArgument(int line, int argIndex, String argDesc);

    void infoSkippingIfBlock(int line);

    void infoEndSkipping(int line);

    void infoConditionalResult(int tokenStart, int startIndex, int endIndex, boolean shouldCall);

    void errorFunctionUnknown(int line, int startIndex, int endIndex, Collection<String> knownFunctions);

    default void errorStdMissingName(int line) {
        error(line, "Missing name: ");
    }

    void errorStdInvalidJson(int line, JsonSyntaxException jse);

    void errorStdUnknownFile(int line, String file);

    default void errorImportMissingFile(int line) {
        error(line, "Cannot find the file ");
    }

    void errorImportNotFound(int line, String sourceFile);

    void errorImportMissingStarter(int line, String sourceFile);

    void errorImportRecursiveReplace(int line, String newSourceFile);

    void errorAliasInvalidArgCount(int line, int startIndex, int endIndex, @Nullable Integer parsed);

    default void errorAliasMissingName(int tokenStart) {
        errorMissingArgument(tokenStart, 0, "The custom name for the function");
    }

    default void errorAliasMissingArgCount(int tokenStart) {
        errorMissingArgument(tokenStart, 1, "The number of arguments for the function");
    }

    default void errorAliasMissingReplacement(int line) {
        errorMissingArgument(line, 2,
            "The replacement for the alias. This can include ${1} and ${2} etc for the aliased arguments.");
    }

    void replace(int removeStart, int removeEnd, @Nullable SourceFile from, int fromStart, List<String> newLines);
}
