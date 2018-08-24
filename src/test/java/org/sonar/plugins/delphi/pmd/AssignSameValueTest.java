package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class AssignSameValueTest extends BasePmdRuleTest {

    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();");
        builder.appendImpl("var result : boolean;");
        builder.appendImpl("begin");

        builder.appendImpl("  result := a and b;");
        builder.appendImpl("  if result then");
        builder.appendImpl("    result := true;");
        builder.appendImpl("  ");


        builder.appendImpl("  result := a and b;");
        builder.appendImpl("  if not result then");
        builder.appendImpl("    result := false;");


        builder.appendImpl("  if result then");
        builder.appendImpl("  begin");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("    result := true;");
        builder.appendImpl("  end;");

        builder.appendImpl("  if not result then");
        builder.appendImpl("  begin");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("    result := false;");
        builder.appendImpl("  end;");


        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(4));
    }

}
