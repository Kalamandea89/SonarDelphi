package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class LeakDprHandlerTest extends BasePmdRuleTest {

    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.setFileSufix(".dpr");
        builder.appendImpl("" +
                "\n" +
                "procedure Test;\n" +
                "var\n" +
                "  h: THandle;\n" +
                "begin\n" +
                "  h := CreateMutex(@sa, bInitialOwner, lpName);\n" +
                "  CloseHandle(h);\n" +
                "end;\n" +
                "begin\n" +
                "Test;\n" +
                "");

        analyse(builder);
        assertThat(issues, is(hasSize(2))); //на код теста ругаются правила DprVariableRule и DprFunctionRule
    }
}
