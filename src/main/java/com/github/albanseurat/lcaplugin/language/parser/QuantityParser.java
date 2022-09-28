package com.github.albanseurat.lcaplugin.language.parser;

import javax.measure.format.MeasurementParseException;
import javax.measure.format.UnitFormat;

import java.util.Objects;

import com.github.albanseurat.lcaplugin.psi.LcaTypes;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.psi.tree.IElementType;
import tech.units.indriya.format.SimpleUnitFormat;

import static com.github.albanseurat.lcaplugin.psi.LcaTypes.*;
import static java.lang.String.format;

public class QuantityParser extends GeneratedParserUtilBase
{
    static UnitFormat parser = SimpleUnitFormat.getInstance();

    public static boolean parseQuantity(PsiBuilder builder, int level)
    {
        var tokenType = builder.getTokenType();
        PsiBuilder.Marker marker = builder.mark();
        String tokenText = builder.getTokenText();
        builder.advanceLexer();
        if (UNIT.equals(tokenType))
        {
            try
            {
                parser.parse(tokenText);
                marker.done(QUANTITY);

            } catch (MeasurementParseException e)
            {
                marker.error(format("%s is not a valid unit", tokenText));
            }
            return true;
        } else
        {
            marker.rollbackTo();
            return false;
        }
    }
}
