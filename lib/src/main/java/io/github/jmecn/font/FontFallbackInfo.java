package io.github.jmecn.font;

import java.util.ArrayList;

public class FontFallbackInfo {

   private ArrayList<String> linkedFontFiles;
   private ArrayList<String> linkedFontNames;
   private ArrayList<FontFile> linkedFonts;

   public FontFallbackInfo() {
      linkedFontFiles = new ArrayList<String>();
      linkedFontNames = new ArrayList<String>();
      linkedFonts = new ArrayList<FontFile>();
   }

   public void add(String name, String file, FontFile font) {
       linkedFontNames.add(name);
       linkedFontFiles.add(file);
       linkedFonts.add(font);
   }

   public boolean containsName(String name) {
       return (name != null) && linkedFontNames.contains(name);
   }

   public boolean containsFile(String file) {
       return (file != null) && linkedFontFiles.contains(file);
   }

   public String[] getFontNames() {
      return linkedFontNames.toArray(new String[0]);
   }

   public String[] getFontFiles() {
      return linkedFontFiles.toArray(new String[0]);
   }

   public FontFile[] getFonts() {
      return linkedFonts.toArray(new FontFile[0]);
   }
}