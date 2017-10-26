/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.util.ui.accessibility;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.accessibility.Accessible;
import java.awt.*;

public class AccessibleContextUtil {
  //@VisibleForTesting
  static final String PUNCTUATION_CHARACTER = ".";
  //@VisibleForTesting
  static final String PUNCTUATION_SEPARATOR = "  ";

  public static void setName(@NotNull Component component, @NotNull String name) {
    setAccessibleName(component, name);
  }

  public static void setName(@NotNull Component component, @NotNull Component source) {
    setName(component, getAccessibleName(source));
  }

  public static void setCombinedName(@NotNull Component component,
                                     @NotNull Component j1, @NotNull String separator, @NotNull Component j2) {
    setAccessibleName(component,
      combineAccessibleStrings(
        getAccessibleName(j1),
        separator,
        getAccessibleName(j2)));
  }

  public static void setCombinedName(@NotNull Component component,
                                     @NotNull Component j1, @NotNull String separator1,
                                     @NotNull Component j2, @NotNull String separator2, @NotNull Component j3) {
    setAccessibleName(component,
      combineAccessibleStrings(
        getAccessibleName(j1),
        separator1,
        getAccessibleName(j2),
        separator2,
        getAccessibleName(j3)));
  }

  public static String getCombinedName(@NotNull Component j1, @NotNull String separator, @NotNull Component j2) {
    return combineAccessibleStrings(getAccessibleName(j1), separator, getAccessibleName(j2));
  }

  public static String getCombinedName(@NotNull Component j1, @NotNull String separator1,
                                       @NotNull Component j2, @NotNull String separator2, @NotNull Component j3) {
    return combineAccessibleStrings(getAccessibleName(j1), separator1, getAccessibleName(j2), separator2, getAccessibleName(j3));
  }

  public static void setDescription(@NotNull Component component, @NotNull Component source) {
    setAccessibleDescription(component, getAccessibleDescription(source));
  }

  public static void setDescription(@NotNull Component component, @Nullable String description) {
    setAccessibleDescription(component, description);
  }

  public static void setCombinedDescription(@NotNull Component component, @NotNull Component j1,
                                            @NotNull String separator, @NotNull Component j2) {
    setAccessibleDescription(component,
      combineAccessibleStrings(
        getAccessibleDescription(j1),
        separator,
        getAccessibleDescription(j2)));
  }

  public static void setCombinedDescription(@NotNull Component component, @NotNull Component j1, @NotNull String separator1,
                                            @NotNull Component j2, @NotNull String separator2, @NotNull Component j3) {
    setAccessibleDescription(component,
      combineAccessibleStrings(
        getAccessibleDescription(j1),
        separator1,
        getAccessibleDescription(j2),
        separator2,
        getAccessibleDescription(j3)));
  }

  public static String getCombinedDescription(@NotNull Component j1, @NotNull String separator, @NotNull Component j2) {
    return combineAccessibleStrings(getAccessibleDescription(j1), separator, getAccessibleDescription(j2));
  }

  public static String getCombinedDescription(@NotNull Component j1, @NotNull String separator1,
                                              @NotNull Component j2, @NotNull String separator2, @NotNull Component j3) {
    return combineAccessibleStrings(getAccessibleDescription(j1), separator1,
                                    getAccessibleDescription(j2), separator2, getAccessibleDescription(j3));
  }

  public static void setParent(@NotNull Component component, @Nullable Component newParent) {
    if (newParent instanceof Accessible) {
      component.getAccessibleContext().setAccessibleParent((Accessible)newParent);
      return;
    }
    component.getAccessibleContext().setAccessibleParent(null);
  }

  public static @Nullable String combineAccessibleStrings(@Nullable String s1, @NotNull String separator, @Nullable String s2) {
    if (StringUtil.isEmpty(s1))
      return s2;
    if (StringUtil.isEmpty(s2))
      return s1;
    return String.format("%s%s%s", s1, separator, s2);
  }

  public static @Nullable String combineAccessibleStrings(@Nullable String s1, @NotNull String separator1, @Nullable  String s2,
                                                          @NotNull String separator2, @Nullable  String s3) {
    return combineAccessibleStrings(combineAccessibleStrings(s1, separator1, s2), separator2, s3);
  }

  /**
   * Given a multi-line string, return an single line string where new line separators
   * are replaced with a punctuation character. This is useful for returning text to
   * screen readers, as they tend to ignore new line separators during speech, but
   * they do pause at punctuation characters.
   */
  public static @NotNull String replaceLineSeparatorsWithPunctuation(@Nullable String text) {
    if (StringUtil.isEmpty(text))
      return "";

    // Split by new line, removing empty lines and white-spaces at end of lines.
    String[] lines = StringUtil.splitByLines(text);

    // Join lines, ensuring each line end with a punctuation.
    final StringBuilder result = new StringBuilder();
    boolean first = true;
    for (String line : lines) {
      line = line.trim();
      if (!StringUtil.isEmpty(line)) {
        if (first)
          first = false;
        else
          result.append(PUNCTUATION_SEPARATOR);
        result.append(line);
        if (!line.endsWith(PUNCTUATION_CHARACTER)) {
          result.append(PUNCTUATION_CHARACTER);
        }
      }
    }
    return result.toString();
  }

  private static String getAccessibleName(@NotNull Component component) {
    if (component instanceof Accessible) {
      return component.getAccessibleContext().getAccessibleName();
    }
    return null;
  }

  private static void setAccessibleName(@NotNull Component component, @Nullable String name) {
    if (component instanceof Accessible) {
      component.getAccessibleContext().setAccessibleName(name);
    }
  }

  private static String getAccessibleDescription(@NotNull Component component) {
    if (component instanceof Accessible) {
      return component.getAccessibleContext().getAccessibleDescription();
    }
    return null;
  }

  private static void setAccessibleDescription(@NotNull Component component, @Nullable String description) {
    if (component instanceof Accessible) {
      component.getAccessibleContext().setAccessibleDescription(description);
    }
  }
}
