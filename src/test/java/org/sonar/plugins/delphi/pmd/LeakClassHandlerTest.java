package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

public class LeakClassHandlerTest extends BasePmdRuleTest {
    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class\n" +
                "    h: THandle;\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  h := CreateMutex(@sa, bInitialOwner, lpName);\n" +
                "end;\n" +
                "\n" +
                "procedure Test.Show;\n" +
                "begin\n" +
                "  h := 0;\n" +
                "  CloseHandle(h);\n" +
                "end;\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void validRule2() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "\n" +
                "procedure Test;\n" +
                "begin\n" +
                "  h := CreateMutex(@sa, bInitialOwner, lpName);\n" +
                "end;\n" +
                "\n" +
                "procedure Show;\n" +
                "begin\n" +
                "  h := 0;\n" +
                "  CloseHandle(h);\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }
}
