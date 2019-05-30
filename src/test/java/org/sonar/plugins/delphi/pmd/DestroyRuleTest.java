package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class DestroyRuleTest extends BasePmdRuleTest {
    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("type");
        builder.appendDecl("  TClass = class");
        builder.appendDecl("  public");
        builder.appendDecl("    destructor Destroy; override;");
        builder.appendDecl("  end;" +
                "  TDoubleToInt = record\n" +
                "    case Byte of\n" +
                "      0: (Value: Double);\n" +
                "      1: (Part1: Integer; Part2: Integer);\n" +
                "  end;\n");

        builder.appendImpl("var V : TClass;");

        builder.appendImpl("procedure TClass.Destroy;");
        builder.appendImpl("begin");
        builder.appendImpl("  try");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("  finally");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("  end;");
        builder.appendImpl("  V.Free;");
        builder.appendImpl("  V.Destroy;");
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1));
    }

}
