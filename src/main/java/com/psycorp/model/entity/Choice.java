package com.psycorp.model.entity;

import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * Entity data level for saving data in database.
 * Comparison of a pair of {@link Scale} for given {@link Area}.
 * If chosenScale = null, the test not yet passed.
 * Embedded class for {@link ValueCompatibilityAnswersEntity}
 * <p>
 * Such architecture is suitable for pairwise comparison.
 * For other types of testing (Psychological Compatibility,
 * Sexual Compatibility, Family Compatibility, Group Compatibility)
 * may not be suitable.
 *
 * @author Maryna Kontar
 * @author Vitaliy Proskura
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Choice extends AbstractEntity {

    @NotEmpty @NotNull
    private Area area;
    @NotEmpty
    private Scale firstScale;
    @NotEmpty
    private Scale secondScale;

    private Scale chosenScale;

    public Choice(@NotEmpty Scale firstScale, @NotEmpty Scale secondScale, Scale chosenScale) {
        this.firstScale = firstScale;
        this.secondScale = secondScale;
        this.chosenScale = chosenScale;
    }
}
