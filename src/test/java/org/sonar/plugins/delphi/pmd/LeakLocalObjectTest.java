package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class LeakLocalObjectTest extends BasePmdRuleTest {
    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure GetUserText;\n" +
                "var localList : TStringList;\n" +
                "begin\n" +
                "  localList := TStringList.Create;\n" +
                "  try\n" +
                "    result := result + ' ' + curStr;\n" +
                "  finally\n" +
                "    localList.Free;\n" +
                "  end;\n" +
                "end;\n" +
                "\n" +
                "function oiParamsProperties0CloseUpList: Boolean;\n" +
                "var list: TList;\n" +
                "begin\n" +
                "  list := TList.Create;\n" +
                "  try\n" +
                "    sleep(0);\n" +
                "  finally\n" +
                "    list.Destroy;\n" +
                "  end;\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1)); // DestroyRule in results
    }

    @Test
    public void validRule2() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure Test(var list : TList);\n" +
                "begin\n" +
                "  list := TList.Create;\n" +
                "  list.add(self);\n" +
                "  list.add(this);\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void passAsFirstrParameter() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure Test;\n" +
                "var list: TList;\n" +
                "begin\n" +
                "  list := TList.Create;\n" +
                "  GlobalList.Add(list);\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1)); //may be rule give size 1
    }

    @Test
    public void pasAsLastParameter() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure Test;\n" +
                "var list: TList;\n" +
                "begin\n" +
                "  list := TList.Create;\n" +
                "  GlobalList.AddObject(str1 + 'str2', list);\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1)); //may be rule give size 1
    }

    @Test
    public void AssignOtherField() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure Test;\n" +
                "var list: TList;\n" +
                "begin\n" +
                "  list := TList.Create;\n" +
                "  FClassField := list;\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void passAsMiddleParameter() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure Test;\n" +
                "var list: TList;\n" +
                "begin\n" +
                "  list := TList.Create;\n" +
                "  RememberAndFree(one, list, -5 + abc);\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1)); //may be rule give size 1
    }

    @Test
    public void LeakLocalObjectIssue() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                        "  function GetRightCommaText(Text: String): String;\n" +
                        "  var\n" +
                        "    slText : TStringList;\n" +
                        "    i      : Integer; \n" +
                        "  begin\n" +
                        "    slText := TStringList.Create;\n" +
                        "    try\n" +
                        "      slText.CommaText := Text;\n" +
                        "          Result := Result + ',' + slText[i];\n" +
                        "    finally\n" +
                        "      Result := slText.CommaText;\n" +
                        "    end;\n" +
                        "  end;\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(2)); // may be rule give size 2
    }


    @Test
    public void checkBeginLevel() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("" +
                "procedure ParseParams;\n" +
                "\n" +
                "  procedure ParseExtra;\n" +
                "  begin\n" +
                "    beep1;\n" +
                "  end;\n" +
                "\n" +
                "begin\n" +
                "  beep2;\n" +
                "end;\n" +
                "\n" +
                "procedure ClsEditButtonClick;\n" +
                "\n" +
                "  function GetRightCommaText(Text: String): String;\n" +
                "  var\n" +
                "    slText : TStringList;\n" +
                "    i      : Integer; \n" +
                "  begin\n" +
                "    slText := TStringList.Create;\n" +
                "    slText.Text := Text;" +
                "      Result := slText.CommaText;\n" +
                "  end;\n" +
                "\n" +
                "begin\n" +
                "  beep3;\n" +
                "end;\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(2)); // may be rule give size 2
    }

}
