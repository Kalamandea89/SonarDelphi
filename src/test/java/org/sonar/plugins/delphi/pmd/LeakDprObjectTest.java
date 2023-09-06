package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class LeakDprObjectTest extends BasePmdRuleTest {

    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.setFileSufix(".dpr");
        builder.appendImpl("" +
                "\n" +
                "procedure Test;\n" +
                "var\n" +
                "  ss: TStaticSet;\n" +
                "begin\n" +
                "  ss := TStaticSet.Create;\n" +
                "  ss.KeyFieldNames := RBParam.Keys;\n" +
                "  ss.Free;\n" +
                "end;\n" +
                "begin\n" +
                "Test;\n" +
                "");

        analyse(builder);
        assertThat(issues, is(hasSize(2))); //на код теста ругаются правила DprVariableRule и DprFunctionRule
    }

    @Test
    public void validRule2() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.setFileSufix(".dpr");
        builder.appendImpl("" +
                "\n" +
                "var\n" +
                "  ss: TStaticSet;\n" +
                "begin\n" +
                "  ss := TStaticSet.Create;\n" +
                "  ss.KeyFieldNames := RBParam.Keys;\n" +
                "  ss.Free;\n" +
                "");

        analyse(builder);
        assertThat(issues, is(hasSize(1))); //на код теста ругается правило DprVariableRule
    }
}
