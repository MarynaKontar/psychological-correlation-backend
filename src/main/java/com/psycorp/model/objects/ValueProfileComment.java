package com.psycorp.model.objects;

import com.psycorp.model.entity.ValueCompatibilityAnswersEntity;
import com.psycorp.model.enums.Scale;
import com.psycorp.util.ValueProfileCommentUtil;
import lombok.Data;

import java.util.List;

/**
 * Object data level for collecting and transforming data from a database.
 * Comments that are added to {@link ValueProfileIndividual} when this profile
 * is calculated from {@link ValueCompatibilityAnswersEntity}.
 * Describes result of value profile test for given {@link Scale}.
 * <p>
 * These comments are added using {@link ValueProfileCommentUtil}.
 * Text for the comment is stored in the resources/valueprofile/scalescomment{localization}.properties
 * files depending on the localization
 * @author Maryna Kontar
 */
@Data
public class ValueProfileComment {

    private Double result;
    private String scale;
    private String head;
    private String header;
    private List<String> list;
    private String footer;
}
