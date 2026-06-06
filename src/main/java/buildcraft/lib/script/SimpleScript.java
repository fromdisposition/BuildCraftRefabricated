package buildcraft.lib.script;

import buildcraft.api.core.BCDebugging;
import buildcraft.api.core.BCLog;
import buildcraft.lib.fabric.loader.FabricModResources;
import buildcraft.lib.fabric.loader.GamePaths;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.resources.Identifier;

public class SimpleScript {
   public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.script");
   static final Gson GSON = new Gson();
   static final Map<String, SimpleScript.ScriptActionLoader> functions = new HashMap<>();
   static BufferedWriter logWriter;
   public final String domain;
   public final Path scriptDirRoot;
   public final Path scriptFolder;
   public final String scriptName;
   public final List<SimpleScript.ScriptAction> actions = new ArrayList<>();
   public final Map<String, ScriptAliasFunction> customFunctions = new HashMap<>();
   final SimpleScript.MutableLineList lines;
   boolean isDebugEnabled;
   Set<String> printedFunctions = null;
   ScriptAliasDocumentation currentDocumentation = null;
   private static File logDir;

   public SimpleScript(
      ScriptableRegistry<?> registry,
      Path scriptDirRoot,
      String scriptDomain,
      Path scriptFolder,
      Path scriptFile,
      List<Path> roots,
      List<String> scriptContents
   ) {
      this.scriptDirRoot = scriptDirRoot;
      this.domain = scriptDomain;
      this.scriptFolder = scriptFolder;
      this.scriptName = scriptFile.getFileName().toString();
      this.logPure("Found script: ", scriptFile);
      this.lines = new SimpleScript.MutableLineList(new SourceFile(this.scriptName, scriptContents.size()), scriptContents);
      int conditionalLevel = 0;
      int skipLevel = 0;

      SimpleScript.LineToken token;
      while ((token = this.lines.nextToken(true)) != null) {
         if (token.isValid) {
            switch (token.type) {
               case QUOTED_STRING:
               case BACKTICK_STRING:
                  this.log("Found unrelated quoted string!");
                  break;
               case SEPARATE:
                  if (token.type != SimpleScript.TokenType.COMMENT) {
                     assert token.lines.length == 1 : "The parser shouldn't return tokens with a different length for TokenType.SEPARATE!";
                     String function = token.lines[0];
                     assert !function.isEmpty() : "The parser shouldn't return empty tokens for TokenType.SEPARATE!";
                     if (skipLevel > 0) {
                        if ("endif".equals(function)) {
                           skipLevel--;
                           conditionalLevel--;
                           this.log("endif -- skipped block");
                        }
                     } else {
                        switch (function) {
                           case "if":
                              conditionalLevel++;
                              SimpleScript.LineToken conditional = this.lines.nextToken(false);
                              if (conditional == null) {
                                 this.log("Expected a conditional expression in a quote, but found nothing!");
                              } else {
                                 if (conditional.isValid && conditional.type.isString) {
                                    String func = conditional.joinLines(false);
                                    boolean shouldCall = false;
                                    if (func.startsWith("is_mod_loaded(") && func.endsWith(")")) {
                                       String modId = func.substring("is_mod_loaded(".length(), func.length() - 1).replace("\"", "").replace("'", "").trim();
                                       shouldCall = FabricModResources.isModLoaded(modId);
                                       this.log("(" + func + ") = " + shouldCall + " (simple eval)");
                                    } else {
                                       shouldCall = true;
                                       this.log("(" + func + ") = true (expression system unavailable, defaulting to true)");
                                    }

                                    if (!shouldCall) {
                                       skipLevel++;
                                    }
                                    continue;
                                 }

                                 this.log("Found a token that wasn't a string! (or was invalid) '" + Arrays.toString(conditional.lines));
                              }
                              break;
                           case "endif":
                              if (conditionalLevel <= 0) {
                                 this.log("cannot end if without starting one!");
                              }

                              conditionalLevel--;
                              this.log("endif -- executed block");
                              break;
                           case "import":
                              SimpleScript.LineToken srcToken = this.lines.nextToken(false);
                              if (srcToken != null && srcToken.isValid && srcToken.type == SimpleScript.TokenType.QUOTED_STRING) {
                                 String source = srcToken.joinLines(false);
                                 List<String> replacements = this.loadLinesFromLib(source, registry, roots);
                                 if (replacements == null) {
                                    break;
                                 }

                                 LineData[] rdata = new LineData[replacements.size()];
                                 SourceFile file = new SourceFile(source, replacements.size());

                                 for (int i = 0; i < replacements.size(); i++) {
                                    rdata[i] = new LineData(replacements.get(i), file, i);
                                 }

                                 this.lines.lineIterator.next();
                                 if (!this.lines.replace(token.datas[0], rdata, s -> s)) {
                                    this.log("Recursive import!");
                                 }
                                 break;
                              }

                              this.log("Unknown/invalid import statement!");
                              break;
                           case "alias":
                              SimpleScript.LineToken nameToken = this.lines.nextToken(false);
                              if (nameToken != null && nameToken.isValid && nameToken.type == SimpleScript.TokenType.SEPARATE) {
                                 function = nameToken.joinLines(false);
                                 int startLine = this.lines.lineIterator.previousIndex();
                                 SimpleScript.LineToken argToken = this.lines.nextToken(false);
                                 if (argToken == null) {
                                    this.log("Missing argument count!");
                                    break;
                                 }

                                 if (argToken.isValid && argToken.type == SimpleScript.TokenType.SEPARATE) {
                                    String argCount = argToken.joinLines(false);

                                    int argCountNumber;
                                    try {
                                       argCountNumber = Integer.parseInt(argCount);
                                       if (argCountNumber < 0 || argCountNumber > 50) {
                                          throw new NumberFormatException();
                                       }
                                    } catch (NumberFormatException nfe) {
                                       this.log("Expected a number between 0 and 50, but got " + argCount);
                                       break;
                                    }

                                    SimpleScript.LineToken next = this.lines.nextToken(false);
                                    if (next != null && next.isValid) {
                                       SimpleScript.LineToken extra = this.lines.nextToken(false);
                                       if (extra != null) {
                                          this.log("Found additional data!");
                                          break;
                                       }

                                       LineData[] datas = new LineData[next.lines.length];

                                       for (int i = 0; i < datas.length; i++) {
                                          String text = next.lines[i];
                                          LineData data = next.datas[i];
                                          datas[i] = new LineData(data, text);
                                       }

                                       ScriptAliasFunction.AliasBuilder builder = new ScriptAliasFunction.AliasBuilder();
                                       builder.name = function;
                                       builder.argCount = argCountNumber;
                                       builder.rawOutputs = datas;
                                       builder.startLine = startLine;
                                       builder.docs = this.currentDocumentation;
                                       this.customFunctions.put(function, new ScriptAliasFunction(builder));
                                       this.currentDocumentation = null;
                                       break;
                                    }

                                    this.log("Expected replcement but got nothing!");
                                    break;
                                 }

                                 this.log("Invalid argument count!");
                                 break;
                              }

                              this.log("Missing name!");
                              break;
                           default:
                              SimpleScript.ScriptActionLoader loader = functions.get(function);
                              if (loader != null) {
                                 List<SimpleScript.ScriptAction> loadedActions = loader.load(this);
                                 if (loadedActions != null) {
                                    this.actions.addAll(loadedActions);
                                 }
                              } else {
                                 LineData start = token.datas[0];
                                 ScriptAliasFunction alias = this.customFunctions.get(function);
                                 if (alias != null) {
                                    String[] values = this.parseArgValues(alias.argCount);
                                    if (values != null && !this.lines.replace(start, alias.rawOutput, createAliasTransform(values))) {
                                       this.log("Overlapped alias functions!");
                                    }
                                 } else {
                                    this.log("Unknown function " + function);
                                 }
                              }
                        }
                     }
                  }
               case COMMENT:
                  break;
               case FUNC_DOCS:
                  this.currentDocumentation = ScriptAliasDocumentation.parse(token.lines);
                  break;
               default:
                  throw new IllegalStateException("Unknown/new enum value: " + token.type);
            }
         }
      }

      this.logPure("");
   }

   private static Function<String, String> createAliasTransform(String[] values) {
      switch (values.length) {
         case 0:
            return Function.identity();
         case 1:
            return s -> s.replace("%0", values[0]);
         case 2:
            return s -> s.replace("%0", values[0]).replace("%1", values[1]);
         case 3:
            return s -> s.replace("%0", values[0]).replace("%1", values[1]).replace("%2", values[2]);
         default:
            return s -> {
               for (int i = values.length - 1; i >= 0; i--) {
                  s = s.replace("%" + i, values[i]);
               }

               return s;
            };
      }
   }

   @Nullable
   private List<String> loadLinesFromLib(String from, ScriptableRegistry<?> registry, List<Path> roots) {
      int colonIndex = from.indexOf(58);
      if (colonIndex > 0 && colonIndex + 1 != from.length()) {
         String libDomain = from.substring(0, colonIndex);
         String path = from.substring(colonIndex + 1);
         String fullPath = libDomain + "/compat/" + registry.getEntryType() + "/" + path + ".txt";

         for (Path root : roots) {
            Path full = root.resolve(fullPath);
            if (Files.exists(full)) {
               try {
                  List<String> list = new ArrayList<>(Files.readAllLines(full));
                  if (list.isEmpty()) {
                     this.log("Found a library without any lines! We can't load from this! (" + root + ")");
                  } else {
                     if ("~{buildcraft/json/lib}".equals(list.get(0))) {
                        list.set(0, "// Valid library declaration was here");
                        int i = 1;
                        String next = list.get(i);
                        if ("/**".equals(next)) {
                           do {
                              if (++i >= list.size()) {
                                 this.log("Found endless comment in " + root);
                                 return null;
                              }

                              next = list.get(i).trim();
                              if (next.endsWith("*/")) {
                                 i++;
                                 break;
                              }
                           } while (next.startsWith("*"));
                        }

                        next = list.get(i);
                        String[] argValues = null;
                        if (next.startsWith("~args")) {
                           String countStr = next.substring("~args".length()).trim();

                           int count;
                           try {
                              count = Integer.parseInt(countStr);
                              if (count < 0 || count > 50) {
                                 throw new NumberFormatException();
                              }
                           } catch (NumberFormatException nfe) {
                              this.log("Expected a number between 0 and 50, but got " + countStr);
                              break;
                           }

                           list.set(i, "// valid args: " + next);
                           argValues = this.parseArgValues(count);
                        }

                        if (argValues != null) {
                           for (int a = 0; a < argValues.length; a++) {
                              for (int lineIndex = 0; lineIndex < list.size(); lineIndex++) {
                                 String str = list.get(lineIndex);
                                 str = str.replace("${" + a + "}", argValues[a]);
                                 list.set(lineIndex, str);
                              }
                           }
                        }

                        for (int lineIndex = 0; lineIndex < list.size(); lineIndex++) {
                           String str = list.get(lineIndex);
                           str = str.replace("${domain}", this.domain);
                           list.set(lineIndex, str);
                        }

                        return list;
                     }

                     this.log("Found a library that isn't declared as '~{buildcraft/json/lib}'! We can't load from this! (" + root + ")");
                  }
               } catch (IOException e) {
                  this.log(e.getMessage() + "");
               }
            }
         }

         this.log("Couldn't find the library file " + fullPath + " in any of the known locations!");
         return null;
      } else {
         this.log("Expected a separated string (like buildcraftcore:util), but didn't find a colon in '" + from + "'");
         return null;
      }
   }

   @Nullable
   private String[] parseArgValues(int count) {
      String[] args = new String[count];
      boolean invalid = false;

      for (int i = 0; i < count; i++) {
         SimpleScript.LineToken next = this.lines.nextToken(false);
         if (next == null) {
            this.log("Expected a value, got nothing for the " + toIndexStr(i + 1) + " argument!");
            invalid = true;
            args[i] = "";
         } else if (!next.isValid) {
            this.log("Expected a value, got an invalid token (" + next + ") for the " + toIndexStr(i + 1) + " argument!");
            invalid = true;
            args[i] = "";
         } else {
            args[i] = next.joinLines(true);
         }
      }

      return invalid ? null : args;
   }

   private static String toIndexStr(int val) {
      int end = val % 10;
      String strEnd = "th";
      if (end == 1) {
         strEnd = "st";
      } else if (end == 2) {
         strEnd = "nd";
      } else if (end == 3) {
         strEnd = "rd";
      }

      return val + strEnd;
   }

   private String getLineNumber() {
      if (!this.lines.lineIterator.hasPrevious()) {
         return "0";
      }

      this.lines.lineIterator.previous();
      return this.lines.lineIterator.next().lineNumbers;
   }

   void log(String line) {
      log0(this.getLineNumber() + ": " + line);
   }

   void log(String line, Path path) {
      log0(this.getLineNumber() + ": " + line + this.scriptDirRoot.relativize(path));
   }

   void logPure(String line) {
      log0(line);
   }

   void logPure(String line, Path path) {
      log0(line + this.scriptDirRoot.relativize(path));
   }

   public static void logForAll(String line) {
      log0(line);
   }

   private static void log0(String line) {
      if (logWriter != null) {
         try {
            logWriter.write(line);
            logWriter.newLine();
         } catch (IOException io) {
            BCLog.logger.warn("[lib.script] Failed to write to the log file!", io);
            closeLog();
         }
      }

      if (DEBUG) {
         BCLog.logger.info(line);
      }
   }

   public static AutoCloseable createLogFile(String path) {
      logDir = GamePaths.GAMEDIR.resolve("logs/buildcraft/scripts").toFile();

      try {
         logDir.mkdirs();
         File logFile = new File(logDir, path + ".log");
         logFile.getParentFile().mkdirs();
         logWriter = new BufferedWriter(new FileWriter(logFile));
         return SimpleScript::closeLog;
      } catch (IOException io) {
         BCLog.logger.warn("[lib.script] Failed to open the log file! (" + logDir + ")", io);
         closeLog();
         return () -> {};
      }
   }

   private static void closeLog() {
      if (logWriter != null) {
         try {
            try {
               logWriter.flush();
            } finally {
               logWriter.close();
               logWriter = null;
            }
         } catch (IOException io) {
            BCLog.logger.warn("[lib.script] Failed to close the log file, so it might not be complete! (" + logDir + ")", io);
         }
      }
   }

   String nextSimpleArg() {
      SimpleScript.LineToken next = this.lines.nextToken(false);
      String ret;
      if (next != null && next.isValid && next.type == SimpleScript.TokenType.SEPARATE) {
         ret = next.joinLines(false);
      } else {
         ret = "";
      }

      return ret;
   }

   @Nullable
   String nextQuotedArg() {
      SimpleScript.LineToken next = this.lines.nextToken(false);
      return next != null && next.isValid ? next.joinLines(false) : null;
   }

   @Nullable
   String[] nextQuotedArgAsArray() {
      String[] arr = null;
      SimpleScript.LineToken next = this.lines.nextToken(false);
      if (next != null && next.isValid) {
         arr = next.lines;
      }

      return arr;
   }

   @Nullable
   JsonObject nextJson() {
      String multiLine = this.nextQuotedArg();

      try {
         return (JsonObject)GSON.fromJson(multiLine, JsonObject.class);
      } catch (JsonSyntaxException jse) {
         this.log("Invalid JSON: " + jse.getMessage());
         return null;
      }
   }

   @Nullable
   JsonObject loadJson(String path) {
      Path jsonPath = this.scriptFolder.resolve(path + ".json");
      if (Files.exists(jsonPath)) {
         try (BufferedReader reader = Files.newBufferedReader(jsonPath)) {
            return (JsonObject)GSON.fromJson(reader, JsonObject.class);
         } catch (IOException io) {
            this.log("Unable to read the file! " + io.getMessage());
            return null;
         } catch (JsonSyntaxException jse) {
            this.log("Invalid JSON: " + jse.getMessage());
            return null;
         }
      } else {
         this.log("Couldn't find the resource: ", jsonPath);
         return null;
      }
   }

   static {
      functions.put("add", script -> {
         String name = script.nextQuotedArg();
         if (name == null) {
            script.log("Missing name!");
            return null;
         }

         JsonObject json = script.nextJson();
         if (json == null) {
            json = script.loadJson(name);
         }

         Identifier id = Identifier.fromNamespaceAndPath(script.domain, name);
         return ImmutableList.of(new SimpleScript.ScriptActionAdd(id, json));
      });
      functions.put("remove", script -> {
         String name = script.nextQuotedArg();
         if (name == null) {
            script.log("Missing name!");
            return null;
         } else {
            return ImmutableList.of(new SimpleScript.ScriptActionRemove(name));
         }
      });
      functions.put("replace", script -> {
         String toRemove = script.nextQuotedArg();
         String toAdd = script.nextQuotedArg();
         if (toRemove == null) {
            script.log("Missing to_remove!");
            return null;
         }

         if (toAdd == null) {
            script.log("Missing to_add!");
            return null;
         }

         JsonObject json = script.nextJson();
         if (json == null) {
            json = script.loadJson(toAdd);
         }

         Identifier id = Identifier.fromNamespaceAndPath(script.domain, toAdd);
         return ImmutableList.of(new SimpleScript.ScriptActionReplace(toRemove, id, json, false));
      });
      functions.put("modify", script -> {
         String toRemove = script.nextQuotedArg();
         String toAdd = script.nextQuotedArg();
         if (toRemove == null) {
            script.log("Missing to_remove!");
            return null;
         }

         if (toAdd == null) {
            script.log("Missing to_add!");
            return null;
         }

         JsonObject json = script.nextJson();
         if (json == null) {
            json = script.loadJson(toAdd);
         }

         Identifier id = Identifier.fromNamespaceAndPath(script.domain, toAdd);
         return ImmutableList.of(new SimpleScript.ScriptActionReplace(toRemove, id, json, true));
      });
   }

   public static final class LineToken {
      public final String[] lines;
      public final LineData[] datas;
      public final SimpleScript.TokenType type;
      public final boolean isValid;
      public final int startIndex;
      public final int endIndex;

      public LineToken(String singleLine, LineData data, SimpleScript.TokenType type, boolean isValid, int startIndex, int endIndex) {
         this(new String[]{singleLine}, new LineData[]{data}, type, isValid, startIndex, endIndex);
      }

      public String joinLines(boolean separateWithNewLine) {
         switch (this.lines.length) {
            case 0:
               return "";
            case 1:
               return this.lines[0];
            default:
               StringBuilder sb = new StringBuilder();
               sb.append(this.lines[0]);

               for (int i = 1; i < this.lines.length; i++) {
                  if (separateWithNewLine) {
                     sb.append('\n');
                  }

                  sb.append(this.lines[i]);
               }

               return sb.toString();
         }
      }

      public LineToken(String[] lines, LineData[] datas, SimpleScript.TokenType type, boolean isValid, int startIndex, int endIndex) {
         if (type == SimpleScript.TokenType.BACKTICK_STRING || type == SimpleScript.TokenType.QUOTED_STRING) {
            char ctype = (char)(type == SimpleScript.TokenType.BACKTICK_STRING ? 96 : 34);
            StringBuilder sb = new StringBuilder();

            for (int l = 0; l < lines.length; l++) {
               String line = lines[l];
               if (line.length() > 1) {
                  for (int i = 0; i < line.length(); i++) {
                     char c = line.charAt(i);
                     char n = i + 1 == line.length() ? '-' : line.charAt(i + 1);
                     if (c == '\\' && n == ctype) {
                        i++;
                        sb.append(n);
                     } else {
                        sb.append(c);
                     }
                  }

                  lines[l] = sb.toString();
                  sb.setLength(0);
               }
            }
         }

         this.lines = lines;
         this.datas = datas;
         this.type = type;
         this.isValid = isValid;
         this.startIndex = startIndex;
         this.endIndex = endIndex;
      }
   }

   public static class MutableLineList {
      public final SourceFile file;
      private final List<LineData> lines = new LinkedList<>();
      private final ListIterator<LineData> lineIterator = this.lines.listIterator();
      private int currentIndexInLine = -1;

      public MutableLineList(SourceFile file, List<String> rawData) {
         this.file = file;

         for (int i = rawData.size() - 1; i >= 0; i--) {
            String line = rawData.get(i);
            this.lineIterator.add(new LineData(line, file, i));
            this.lineIterator.previous();
         }
      }

      @Nullable
      public SimpleScript.LineToken nextToken(boolean jumpToNextLine) {
         boolean isComment = false;
         int start = -1;
         boolean foundNextLineSymbol = false;

         LineData data;
         String line;
         do {
            foundNextLineSymbol = false;
            if (!this.lineIterator.hasNext()) {
               return null;
            }

            line = (data = this.lineIterator.next()).text;
            boolean isMultiLine = false;
            char end = ' ';
            int i = Math.max(0, this.currentIndexInLine);

            while (i < line.length()) {
               char c = line.charAt(i);
               boolean tokenStartFound = false;
               switch (c) {
                  case ' ':
                     break;
                  case '"':
                     end = '"';
                     start = i + 1;
                     tokenStartFound = true;
                     break;
                  case '/':
                     if (i + 1 == line.length()) {
                        return new SimpleScript.LineToken(line.substring(i), data, SimpleScript.TokenType.COMMENT, false, i, line.length());
                     }

                     isComment = true;
                     if (!line.startsWith("/**", i)) {
                        this.currentIndexInLine = -1;
                        return new SimpleScript.LineToken(
                           line.substring(i), data, SimpleScript.TokenType.COMMENT, line.startsWith("//", i), i, line.length()
                        );
                     }

                     start = i + 3;
                     tokenStartFound = true;
                     break;
                  case '`':
                     end = '`';
                     isMultiLine = true;
                     start = i + 1;
                     tokenStartFound = true;
                     break;
                  case '¬':
                     boolean isLast = true;

                     for (int j = i; j < line.length(); j++) {
                        if (!Character.isWhitespace(line.charAt(j))) {
                           if (!line.startsWith("//", j)) {
                              isLast = false;
                           }
                           break;
                        }
                     }

                     if (isLast) {
                        foundNextLineSymbol = true;
                        this.currentIndexInLine = -1;
                        break;
                     }
                  default:
                     if (!Character.isWhitespace(c)) {
                        for (int j = i; j < line.length(); j++) {
                           char d = line.charAt(j);
                           if (Character.isWhitespace(d)) {
                              this.currentIndexInLine = j + 1;
                              this.lineIterator.previous();
                              return new SimpleScript.LineToken(line.substring(i, j), data, SimpleScript.TokenType.SEPARATE, true, i, j);
                           }
                        }

                        this.currentIndexInLine = line.length();
                        this.lineIterator.previous();
                        return new SimpleScript.LineToken(line.substring(i), data, SimpleScript.TokenType.SEPARATE, true, i, line.length());
                     }
               }

               if (foundNextLineSymbol) {
                  break;
               }

               if (!tokenStartFound) {
                  i++;
                  continue;
               }

               if (start >= 0) {
                  SimpleScript.LineToken stringToken = this.checkForString(isComment, start, data, line, isMultiLine, end);
                  if (stringToken != null) {
                     return stringToken;
                  }

                  return start < 0 ? null : this.handleMultiLineToken(isComment, start, data, line);
               }

               this.currentIndexInLine = -1;
               break;
            }
            if (foundNextLineSymbol) {
               continue;
            }
            break;
         } while (jumpToNextLine || foundNextLineSymbol);

         return start < 0 ? null : this.handleMultiLineToken(isComment, start, data, line);
      }

      @Nullable
      private SimpleScript.LineToken handleMultiLineToken(boolean isComment, int start, LineData data, String line) {
         List<String> tokenLines = new ArrayList<>();
         List<LineData> tokenData = new ArrayList<>();
         tokenLines.add(line.substring(start));
         tokenData.add(data);

         while (true) {
            if (!this.lineIterator.hasNext()) {
               return null;
            }

            line = (data = this.lineIterator.next()).text;
            if (isComment) {
               if (!line.trim().startsWith("*")) {
                  break;
               }

               int end = line.indexOf("*/");
               if (end >= 0) {
                  this.currentIndexInLine = end + 2;
                  this.lineIterator.previous();
                  tokenLines.add(line.substring(0, end));
                  tokenData.add(data);
                  break;
               }

               line = line.substring(line.indexOf(42) + 1);
            } else {
               for (int i = 0; i < line.length(); i++) {
                  char c = line.charAt(i);
                  if (c == '\\') {
                     i++;
                  } else if (c == '`') {
                     this.currentIndexInLine = i + 1;
                     this.lineIterator.previous();
                     tokenLines.add(line.substring(0, i));
                     tokenData.add(data);
                     return new SimpleScript.LineToken(
                        tokenLines.toArray(new String[0]),
                        tokenData.toArray(new LineData[0]),
                        isComment ? SimpleScript.TokenType.FUNC_DOCS : SimpleScript.TokenType.BACKTICK_STRING,
                        true,
                        start,
                        this.currentIndexInLine
                     );
                  }
               }
            }

            tokenLines.add(line);
            tokenData.add(data);
         }

         return new SimpleScript.LineToken(
            tokenLines.toArray(new String[0]),
            tokenData.toArray(new LineData[0]),
            isComment ? SimpleScript.TokenType.FUNC_DOCS : SimpleScript.TokenType.BACKTICK_STRING,
            true,
            start,
            this.currentIndexInLine
         );
      }

      @Nullable
      private SimpleScript.LineToken checkForString(boolean isComment, int start, LineData data, String line, boolean isMultiLine, char end) {
         if (isComment) {
            for (int i = start; i < line.length(); i++) {
               if (line.startsWith("*/", i)) {
                  this.currentIndexInLine = i + 3;
                  this.lineIterator.previous();
                  return new SimpleScript.LineToken(line.substring(start, i + 3), data, SimpleScript.TokenType.FUNC_DOCS, true, start, i + 3);
               }
            }
         } else {
            for (int i = start; i < line.length(); i++) {
               char c = line.charAt(i);
               if (c == '\\') {
                  i++;
               } else if (c == end) {
                  this.currentIndexInLine = i + 1;
                  this.lineIterator.previous();
                  return new SimpleScript.LineToken(line.substring(start, i), data, SimpleScript.TokenType.QUOTED_STRING, true, start, i);
               }
            }

            if (!isMultiLine) {
               this.currentIndexInLine = line.length();
               this.lineIterator.previous();
               return new SimpleScript.LineToken(line.substring(start + 1), data, SimpleScript.TokenType.BACKTICK_STRING, false, start + 1, line.length());
            }
         }

         return null;
      }

      public boolean replace(LineData start, LineData[] with, Function<String, String> transform) {
         SourceLine srcLine = with[0].original;
         List<LineData> removed = new ArrayList<>();
         this.lineIterator.next();

         while (true) {
            LineData line = this.lineIterator.previous();
            if (line == start) {
               this.lineIterator.remove();
               removed = null;
               break;
            }

            if (line.firstLineSources.contains(srcLine)) {
               BCLog.logger.warn("Overlap: " + srcLine + ", " + line);
               break;
            }

            this.lineIterator.remove();
            removed.add(line);
         }

         if (removed != null) {
            for (LineData line : removed) {
               this.lineIterator.add(line);
            }

            return false;
         } else {
            int line = this.lineIterator.nextIndex();

            for (LineData other : with) {
               this.lineIterator.add(other.createReplacement(transform.apply(other.text), srcLine, line++));
            }

            for (int i = 0; i < with.length; i++) {
               this.lineIterator.previous();
            }

            this.currentIndexInLine = -1;
            return true;
         }
      }

      public int size() {
         return this.lines.size();
      }
   }

   public abstract static class ScriptAction {
      public JsonObject getJson() {
         throw new UnsupportedOperationException(this.getClass() + " doesn't support getJson()!");
      }
   }

   public static class ScriptActionAdd extends SimpleScript.ScriptAction {
      public final Identifier name;
      public final JsonObject json;

      public ScriptActionAdd(Identifier name, JsonObject json) {
         this.name = name;
         this.json = json;
      }

      @Override
      public JsonObject getJson() {
         return this.json;
      }
   }

   public interface ScriptActionLoader {
      List<SimpleScript.ScriptAction> load(SimpleScript script);
   }

   public static class ScriptActionRemove extends SimpleScript.ScriptAction {
      public final Identifier name;

      public ScriptActionRemove(String name) {
         this.name = Identifier.parse(name);
      }
   }

   public static class ScriptActionReplace extends SimpleScript.ScriptAction {
      public final Identifier toReplace;
      public final Identifier name;
      public final boolean inheritTags;
      public final JsonObject json;

      public ScriptActionReplace(String toReplace, Identifier name, JsonObject json, boolean inheritTags) {
         this.toReplace = Identifier.parse(toReplace);
         this.name = name;
         this.json = json;
         this.inheritTags = inheritTags;
      }

      public SimpleScript.ScriptActionAdd convertToAdder() {
         return new SimpleScript.ScriptActionAdd(this.name, this.json);
      }

      @Override
      public JsonObject getJson() {
         return this.json;
      }
   }

   public enum TokenType {
      QUOTED_STRING(true),
      BACKTICK_STRING(true),
      SEPARATE(false),
      COMMENT(false),
      FUNC_DOCS(false);

      public final boolean isString;

      TokenType(boolean isString) {
         this.isString = isString;
      }
   }
}
