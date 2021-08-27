package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class FreeLocalObjectInFinallyRuleTest extends BasePmdRuleTest {

    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();\n" +
                "var sl : TStringList;\n" +
                "begin\n" +
                "  sl := TStringList.Create;\n" +
                "  try\n" +
                "    sl.LoadFormFile('c:\\test.txt');\n" +
                "    ShowMessage(sl.text);\n" +
                "  finally\n" +
                "    sl.Free;\n" +
                "    FreeAndNil(sl);\n" +
                "    sl.Destroy;\n" +
                "  end;\n" +
                "end;\n" +
                "");
        analyse(builder);

        assertThat(issues, hasSize(1)); // don't use Destroy give one issue
    }

    @Test
    public void issue() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();\n" +
                "var sl : TStringList;\n" +
                "begin\n" +
                "  sl := TStringList.Create;\n" +
                "    sl.LoadFormFile('c:\\test.txt');\n" +
                "    ShowMessage(sl.text);\n" +
                "    sl.Free;\n" +
                "    FreeAndNil(sl);\n" +
                "    sl.Destroy;\n" +
                "end;\n" +
                "");
        analyse(builder);

        assertThat(issues, hasSize(4));
    }

    @Test
    public void nestedBegin() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();\n" +
                "var sl : TStringList;\n" +
                "begin\n" +
                "  sl := TStringList.Create;\n" +
                "  try\n" +
                "    sl.LoadFormFile('c:\\test.txt');\n" +
                "    ShowMessage(sl.text);\n" +
                "  finally\n" +
//                "    if sl.count > 1 then\n" +
                "    begin\n" +
                "      sl.Free;\n" +
                "    end;\n" +
                "  end;\n" +
                "end;\n" +
                "");
        analyse(builder);

        assertThat(issues, is(empty()));
    }


    @Test
    public void inExcept() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();\n" +
                "var sl : TStringList;\n" +
                "begin\n" +
                "  sl := TStringList.Create;\n" +
                "  try\n" +
                "    sl.LoadFormFile('c:\\test.txt');\n" +
                "  finally\n" +
                "    sl.clear;\n" +
                "  end;" +
                "  try\n" +
                "    sl.LoadFormFile('c:\\test.txt');\n" +
                "    ShowMessage(sl.text);\n" +
                "  except\n" +
//                "    if sl.count > 1 then\n" +
//                "    begin\n" +
                "      sl.Free;\n" +
//                "    end;\n" +
                "  end;\n" +
                "end;\n" +
                "");
        analyse(builder);

        assertThat(issues, hasSize(1));
    }

    @Test
    public void exceptInFinally() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();\n" +
                "var sl : TStringList;\n" +
                "begin\n" +
                "  sl := TStringList.Create;\n" +
                "  try\n" +
                "    sl.LoadFormFile('c:\\test.txt');\n" +
                "    ShowMessage(sl.text);\n" +
                "  finally\n" +
                "    try\n" +
//                "      sl.Free;\n" +
                "    except\n" +
                "      sl.free;\n" +
                "    end;\n" +
                "  end;\n" +
                "end;\n" +
                "");
        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void unitVariables() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("\n" +
                "type\n" +
                "  TInverseProc = function (var ProcessInfo : TDataProcessInfo; var ErrorInfo : pointer) : BOOL;stdcall;\n" +
                "\n" +
                "var sl: TStringList;\n" +
                "procedure Test;\n" +
                "begin\n" +
                "  try\n" +
                "    sl.free;\n" +
                "  except\n" +
                "    on Err: MyException do begin\n" +
                "      ModelExcept(Err, ExceptAddr);\n" +
                "  end;" +
                "end;\n" +
                "");
        analyse(builder);

        assertThat(issues, is(empty()));
    }

}
