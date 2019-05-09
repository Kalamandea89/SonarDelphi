package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class ThenRaiseElseRuleTest extends BasePmdRuleTest {
    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();");
        builder.appendImpl("begin");
        builder.appendImpl("  if getBool then");
        builder.appendImpl("    raise TMyException.Create('test')");
        builder.appendImpl("  else");
        builder.appendImpl("    DoSomeThing;");
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1));
    }


    @Test
    public void checkFalsePositive() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure TestIf();");
        builder.appendImpl("begin");
        builder.appendImpl("  if getBoolA then");
        builder.appendImpl("    if getBoolB then");
        builder.appendImpl("      raise TMyException.Create('test')");
        builder.appendImpl("    else");
        builder.appendImpl("      DoSomeThing;");
        builder.appendImpl("end;");

        builder.appendImpl("procedure TestElse();");
        builder.appendImpl("begin");
        builder.appendImpl("  if getBoolA then");
        builder.appendImpl("      DoSomeThing1");
        builder.appendImpl("  else ");
        builder.appendImpl("    if getBoolB then");
        builder.appendImpl("      raise TMyException.Create('test')");
        builder.appendImpl("    else");
        builder.appendImpl("      DoSomeThing;");
        builder.appendImpl("end;");

        builder.appendImpl("procedure TestElseB();");
        builder.appendImpl("begin");
        builder.appendImpl("  if getBoolA then");
        builder.appendImpl("  begin");
        builder.appendImpl("      DoSomeThing1;");
        builder.appendImpl("      DoSomeThing2;");
        builder.appendImpl("  end");
        builder.appendImpl("  else ");
        builder.appendImpl("    if getBoolB then");
        builder.appendImpl("      raise TMyException.Create('test')");
        builder.appendImpl("    else");
        builder.appendImpl("      DoSomeThing;");
        builder.appendImpl("end;");

        builder.appendImpl("procedure TestDO();");
        builder.appendImpl("var i: byte;");
        builder.appendImpl("begin");
        builder.appendImpl("  for i := 0 to GetCount - 1 do");
        builder.appendImpl("    if getBoolB then");
        builder.appendImpl("      raise TMyException.Create('test')");
        builder.appendImpl("    else");
        builder.appendImpl("      DoSomeThing;");
        builder.appendImpl("end;");

        builder.appendImpl("procedure TestCase();");
        builder.appendImpl("var i: byte;");
        builder.appendImpl("begin");
        builder.appendImpl("  case i of");
        builder.appendImpl("    1 : if getBoolB then");
        builder.appendImpl("          raise TMyException.Create('test')");
        builder.appendImpl("        else");
        builder.appendImpl("          DoSomeThing;");
        builder.appendImpl("  else");
        builder.appendImpl("    if isBool then");
        builder.appendImpl("      raise MyException.Create('test')");
        builder.appendImpl("    else");
        builder.appendImpl("      DoSomeThing;");
        builder.appendImpl("  end;");
        builder.appendImpl("end;");

        analyse(builder);

        //assertThat(issues, is(empty()));
        // because of delph:NoBeginAfterDoRule
        assertThat(issues.toString(), issues, hasSize(1));
    }

}
