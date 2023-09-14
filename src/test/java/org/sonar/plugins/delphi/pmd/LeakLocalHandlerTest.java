package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LeakLocalHandlerTest extends BasePmdRuleTest {
    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
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
        assertThat(issues, is(empty())); //на код теста ругаются правила DprVariableRule и DprFunctionRule
    }
}
