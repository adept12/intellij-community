// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.structuralsearch.inspection;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.structuralsearch.Matcher;
import com.intellij.structuralsearch.StructuralSearchException;
import com.intellij.structuralsearch.plugin.ui.Configuration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene.Kudelevsky
 */
class SSBasedInspectionCompiledPatternsCache {

  private static final Logger LOG = Logger.getInstance(SSBasedInspectionCompiledPatternsCache.class);
  private static final Key<Map<Configuration, Matcher>> COMPILED_OPTIONS_KEY = Key.create("SSR_INSPECTION_COMPILED_OPTIONS_KEY");

  @NotNull
  static Map<Configuration, Matcher> getCompiledOptions(@NotNull List<Configuration> configurations, @NotNull Project project) {
    final Map<Configuration, Matcher> cache =
      getCompiledOptions(configurations, project, project.getUserData(COMPILED_OPTIONS_KEY));
    project.putUserData(COMPILED_OPTIONS_KEY, cache);
    return cache;
  }

  static Map<Configuration, Matcher> getCompiledOptions(@NotNull List<Configuration> configurations,
                                                        @NotNull Project project,
                                                        @Nullable Map<Configuration, Matcher> cache) {
    if (areConfigurationsInCache(configurations, cache)) {
      return cache;
    }
    final Map<Configuration, Matcher> newCache = new HashMap<>();
    if (cache != null && !cache.isEmpty()) {
      newCache.putAll(cache);
      newCache.keySet().retainAll(configurations);
    }
    if (configurations.size() != newCache.size()) {
      for (Configuration configuration : configurations) {
        if (newCache.containsKey(configuration)) {
          continue;
        }
        try {
          final Matcher matcher = new Matcher(project, configuration.getMatchOptions());
          newCache.put(configuration, matcher);
        }
        catch (StructuralSearchException e) {
          LOG.warn("Malformed structural search inspection pattern \"" + configuration.getName() + '"', e);
          newCache.put(configuration, null);
        }
      }
    }
    return Collections.unmodifiableMap(newCache);
  }

  @Contract("_, null -> false")
  private static boolean areConfigurationsInCache(List<Configuration> configurations, @Nullable Map<Configuration, Matcher> cache) {
    return cache != null && configurations.size() == cache.size() && configurations.stream().allMatch(key -> cache.containsKey(key));
  }
}