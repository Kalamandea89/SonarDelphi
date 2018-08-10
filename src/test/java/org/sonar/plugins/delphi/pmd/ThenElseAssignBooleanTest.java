package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

/**
 * Created by mahalov on 06.07.2018.
 */
public class ThenElseAssignBooleanTest extends BasePmdRuleTest  {
    @Test
    public void TestRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();");
        builder.appendImpl("var a, b, c : boolean;");
        builder.appendImpl("begin");
        builder.appendImpl("  if a then");
        builder.appendImpl("    b := false");
        builder.appendImpl("  else");
        builder.appendImpl("    b := true;");
        builder.appendImpl("");
        builder.appendImpl("  if not a then");
        builder.appendImpl("    c := true");
        builder.appendImpl("  else");
        builder.appendImpl("    c := false;");
        builder.appendImpl("");
        builder.appendImpl("  if a then");
        builder.appendImpl("    b := true");
        builder.appendImpl("  else");
        builder.appendImpl("    c := true;");
        builder.appendImpl("");
        builder.appendImpl("");
        // TODO надо бы такое находить. а ещё одинаковые операции по then и else
//        builder.appendImpl("  if a then");
//        builder.appendImpl("  begin");
//        builder.appendImpl("    b := true;");
//        builder.appendImpl("  end");
//        builder.appendImpl("  else");
//        builder.appendImpl("  begin");
//        builder.appendImpl("    b := false;");
//        builder.appendImpl("  end;");
//        builder.appendImpl("");
        builder.appendImpl("");
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(2));
    }

}
