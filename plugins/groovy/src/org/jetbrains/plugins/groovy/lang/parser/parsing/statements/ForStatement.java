/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.groovy.lang.parser.parsing.statements;

import com.intellij.lang.PsiBuilder;
import org.jetbrains.plugins.groovy.GroovyBundle;
import org.jetbrains.plugins.groovy.lang.parser.GroovyElementTypes;
import org.jetbrains.plugins.groovy.lang.parser.GroovyParser;
import org.jetbrains.plugins.groovy.lang.parser.parsing.auxiliary.modifiers.Modifiers;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.declaration.Declaration;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.StrictContextExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.statements.expressions.arithmetic.ShiftExpression;
import org.jetbrains.plugins.groovy.lang.parser.parsing.types.TypeSpec;
import org.jetbrains.plugins.groovy.lang.parser.parsing.util.ParserUtils;

/**
 * @autor: ilyas
 */
public class ForStatement implements GroovyElementTypes {

  public static boolean forClauseParse(PsiBuilder builder, GroovyParser parser) {
    ParserUtils.getToken(builder, mNLS);
    return forInClauseParse(builder, parser) || tradForClauseParse(builder, parser);
  }

  private static boolean tradForClauseParse(PsiBuilder builder, GroovyParser parser) {

    PsiBuilder.Marker marker = builder.mark();

    if (ParserUtils.getToken(builder, mSEMI) || (Declaration.parse(builder, false, parser) && ParserUtils.getToken(builder, mSEMI))) {
      StrictContextExpression.parse(builder, parser);
      ParserUtils.getToken(builder, mSEMI, GroovyBundle.message("semi.expected"));
      ParserUtils.getToken(builder, mNLS);
      if (!mRPAREN.equals(builder.getTokenType())) {
        controlExpressionListParse(builder, parser);
      }
    }
    else {
      marker.rollbackTo();
      marker = builder.mark();
      controlExpressionListParse(builder, parser);
      ParserUtils.getToken(builder, mSEMI, GroovyBundle.message("semi.expected"));
      StrictContextExpression.parse(builder, parser);
      ParserUtils.getToken(builder, mSEMI, GroovyBundle.message("semi.expected"));
      ParserUtils.getToken(builder, mNLS);
      if (!mRPAREN.equals(builder.getTokenType())) {
        controlExpressionListParse(builder, parser);
      }
    }

    marker.done(FOR_TRADITIONAL_CLAUSE);
    return true;
  }

  /*
   * Parses list of control expression in for condition
   */
  private static void controlExpressionListParse(PsiBuilder builder, GroovyParser parser) {

    if (!StrictContextExpression.parse(builder, parser)) return;

    while (mCOMMA.equals(builder.getTokenType())) {

      if (ParserUtils.lookAhead(builder, mCOMMA, mNLS, mRPAREN) || ParserUtils.lookAhead(builder, mCOMMA, mRPAREN)) {
        ParserUtils.getToken(builder, mCOMMA);
        builder.error(GroovyBundle.message("expression.expected"));
      }
      else {
        ParserUtils.getToken(builder, mCOMMA);
      }
      ParserUtils.getToken(builder, mNLS);
      if (!StrictContextExpression.parse(builder, parser)) {
        ParserUtils.getToken(builder, mNLS);
        if (!mRPAREN.equals(builder.getTokenType()) && !mSEMI.equals(builder.getTokenType())) {
          builder.error(GroovyBundle.message("expression.expected"));
        }
        if (!mRPAREN.equals(builder.getTokenType()) &&
            !mSEMI.equals(builder.getTokenType()) &&
            !mCOMMA.equals(builder.getTokenType()) &&
            !mNLS.equals(builder.getTokenType())) {
          builder.advanceLexer();
        }
      }
    }
  }

  /*
   * Parses Groovy-style 'in' clause
   */
  private static boolean forInClauseParse(PsiBuilder builder, GroovyParser parser) {

    PsiBuilder.Marker marker = builder.mark();

    PsiBuilder.Marker parameterMarker = builder.mark();
    Modifiers.parse(builder, parser);
    if (ParserUtils.lookAhead(builder, mIDENT, kIN) || ParserUtils.lookAhead(builder, mIDENT, mCOLON)) {
      ParserUtils.getToken(builder, mIDENT);
    }
    else {
      TypeSpec.parseStrict(builder);
      ParserUtils.getToken(builder, mIDENT, GroovyBundle.message("identifier.expected"));
    }
    parameterMarker.done(PARAMETER);

    if (!ParserUtils.getToken(builder, kIN) && !ParserUtils.getToken(builder, mCOLON)) {
      builder.error(GroovyBundle.message("in.expected"));
      marker.drop();
      return false;
    }

    if (!ShiftExpression.parse(builder, parser)) {
      builder.error(GroovyBundle.message("expression.expected"));
    }
    marker.done(FOR_IN_CLAUSE);
    return true;
  }
}
