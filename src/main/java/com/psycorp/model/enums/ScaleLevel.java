package com.psycorp.model.enums;

import com.psycorp.model.objects.ValuesDifferencesComment;

/**
 * Scale levels, depending on which {@link ValuesDifferencesComment} is selected.
 * @author Maryna Kontar
 * @see ValuesDifferencesComment
 */
public enum ScaleLevel {
    FULL_MATCH,
    MINOR_DIFFERENCES,
    MODERATE_DIFFERENCES,
    STRONG_DIFFERENCES
}