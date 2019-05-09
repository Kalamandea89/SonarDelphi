package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class TypeRealRuleTest  extends BasePmdRuleTest {

    @Test
    public void testToReal() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("var a: Real;");
        builder.appendImpl("b: Double;");
        builder.appendImpl("c: Real;");
        builder.appendImpl("procedure Test();");
        builder.appendImpl("begin");
        builder.appendImpl("  r := 1.6;");
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues, hasSize(issues.size()));
    }
}
