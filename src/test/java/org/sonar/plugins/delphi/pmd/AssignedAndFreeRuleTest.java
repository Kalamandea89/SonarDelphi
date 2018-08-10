package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * Created by mahalov on 03.07.2018.
 */
public class AssignedAndFreeRuleTest extends BasePmdRuleTest {
    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("var V : TV;");
        builder.appendImpl("procedure Test();");
        builder.appendImpl("begin");
        builder.appendImpl("  V.Free;");
        builder.appendImpl("  FreeAndNil(V);");
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues, is(empty()));
    }


    @Test
    public void validRule2() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("var v : TV;");
        builder.appendImpl("procedure Test();");
        builder.appendImpl("begin");
        builder.appendImpl("  v := GetV;");
        builder.appendImpl("  if Assigned(v) then");
        builder.appendImpl("    ShowMessage(v.message);");
        builder.appendImpl("  v.Free;"); // was false positive
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void AssignedAndFreeTest() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("var A, B, C : TV;");
        builder.appendImpl("procedure Test();");
        builder.appendImpl("begin");
        builder.appendImpl("  if Assigned(A) then ");
        builder.appendImpl("    A.Free;");
        builder.appendImpl("  if A <> nil then ");
        builder.appendImpl("    A.Free;");
        builder.appendImpl("  if Assigned(B) then ");
        builder.appendImpl("    FreeAndNil(B);");

        builder.appendImpl("  if Assigned(C) then ");
        builder.appendImpl("  begin");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("    C.Free;");
        builder.appendImpl("  end;");

        builder.appendImpl("  if C <> nil then ");
        builder.appendImpl("  begin");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("    FreeAndNil(C);");
        builder.appendImpl("  end;");

        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(5));
    }

}
