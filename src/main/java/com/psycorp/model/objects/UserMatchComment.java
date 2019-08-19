package com.psycorp.model.objects;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.AspectLevel;
import com.psycorp.util.UserMatchCommentUtil;
import lombok.Data;


/**
 * Object data level for collecting and transforming data from a database.
 * Comments that are added to {@link Matching} when calculate {@link UserMatch}
 * for set of users.
 * Describes differences or matches for given aspect (@see {@link Area})
 * for set of users.
 * <p>
 * These comments are added using {@link UserMatchCommentUtil}.
 * Text for the comment is stored in the resources/match/areacomment{localization}.properties
 * files depending on the localization
 * @author Maryna Kontar
 */
@Data
public class UserMatchComment {

    private String aspect;
    private String aspectDescription;
    private AspectLevel level;
    private String levelName;
    private String header;
    private String foreword; // предисловие
    private String accent; // то, что будем выделять (bold,...)
    private String mainText;


}
