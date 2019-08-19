package com.psycorp.model.objects;

import com.psycorp.model.enums.Scale;
import com.psycorp.model.enums.ScaleLevel;
import com.psycorp.util.UserMatchCommentUtil;
import lombok.Data;

import java.util.List;

/**
 * Object data level for collecting and transforming data from a database.
 * Comments that are added to {@link ValueProfileMatching} when matching {@link ValueProfile}
 * for set of  users.
 * Describes differences or matches for given scale (@see {@link Scale})
 * for set of users.
 * <p>
 * These comments are added using {@link UserMatchCommentUtil}.
 * Text for the comment is stored in the resources/match/scalescomment{localization}.properties
 * files depending on the localization
 * @author Maryna Kontar
 */
@Data
public class ValuesDifferencesComment {

    private String scale;
    private Integer result;
    private ScaleLevel level;
    private String levelName;
    private List<String> text;
}
