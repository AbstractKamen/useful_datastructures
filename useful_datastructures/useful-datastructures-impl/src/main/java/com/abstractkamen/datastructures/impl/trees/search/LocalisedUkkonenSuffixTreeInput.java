package com.abstractkamen.datastructures.impl.trees.search;

import com.abstractkamen.datastructures.impl.utils.Pair;

import java.util.List;
import java.util.Locale;

public record LocalisedUkkonenSuffixTreeInput<T>(List<Pair<String, Locale>> keyLocalePairs, T value) {

    public LocalisedUkkonenSuffixTreeInput(String key, Locale locale, T value) {
        this(List.of(Pair.of(key, locale)), value);
    }
}