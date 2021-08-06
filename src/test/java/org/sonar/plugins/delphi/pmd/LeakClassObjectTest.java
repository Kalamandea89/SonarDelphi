package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

public class LeakClassObjectTest extends BasePmdRuleTest {

    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class\n" +
                "    FList : TList;\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  FList := TList.Create;\n" +
                "  FList.add(nil);\n" +
                "end;\n" +
                "\n" +
                "procedure Test.Show;\n" +
                "begin\n" +
                "  FList.Free;\n" +
                "end;\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void validWithOwner() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class(TForm)\n" +
                "    FList : TStaticSet;\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  FList := TStaticSet.Create(Self);\n" +
                "  FList.add(nil);\n" +
                "end;\n" +
                "\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void validRule2() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class\n" +
                "    FList : TList;\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "\n" +
                "procedure Test.Show;\n" +
                "begin\n" +
                "  FList.Free;\n" +
                "end;\n" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  FList := TList.Create;\n" +
                "  FList.add(nil);\n" +
                "end;\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }


    @Test
    public void validTypeFunc() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class\n" +
                "    FList : TList;\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "type\n" +
                "  TFreeResFunc = function(Id: integer): integer; stdcall;\n" +
                "\n" +
                "procedure Test.Show;\n" +
                "begin\n" +
                "  FList.Free;\n" +
                "end;\n" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  FList := TList.Create;\n" +
                "  FList.add(nil);\n" +
                "end;\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void validObject() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "type\n " +
                        "   TBorders = object\n" +
                        "    public\n" +
                        "    Cell: TCell;\n" +
                        "    Info: TBorderArray;\n" +
                        "    procedure SetBorder(Index: Integer; Style: TPenStyle; Width: Integer; Color: TColor);\n" +
                        "    procedure SetAll(Style: TPenStyle; Width: Integer; Color: TColor);\n" +
                        "    procedure SetInfo(AInfo: TBorderArray);\n" +
                        "    function GetStyle(Index: Integer): TBorderStyle;\n" +
                        "    procedure SetStyle(Index: Integer; Style: TBorderStyle);\n" +
                        "    function GetColor(Index: Integer): TColor;\n" +
                        "    procedure SetColor(Index: Integer; Color: TColor);\n" +
                        "    function IsStandart: Boolean;\n" +
                        "    end;\n" +
                        "\n"
                );

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void validTButtonElements() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "type\n " +
                "   TButtonElements = class(TElementCollection)\n" +
                "  private\n" +
                "    FList: TList;\n" +
                "    function GetButton(Index: Integer): TPrjToolButton;\n" +
                "  protected\n" +
                "    procedure Update(Element: TElement); override;\n" +
                "  public\n" +
                "    destructor Destroy; override;\n" +
                "    procedure Invalidate;\n" +
                "    property Buttons[Index: Integer]: TPrjToolButton read GetButton; default;\n" +
                "  end;\n  " +
                "\n"
        );

        builder.appendImpl("" +
                "destructor TButtonElements.Destroy;\n" +
                "begin\n" +
                "  inherited;\n" +
                "  FList.Free;\n" +
                "end;\n" +
                "procedure TButtonElements.Invalidate;\n" +
                "var\n" +
                "  i, j: Integer;\n" +
                "  h: Cardinal;\n" +
                "  Btn: TPrjToolButton;\n" +
                "begin\n" +
                "  if FList = nil then FList := TList.Create;" +
                "  FList.Clear;\n" +
                "  for i := 0 to Count - 1 do begin\n" +
                "    Btn := GetButton(i);\n" +
                "    for j := 0 to Btn.FBoundList.Count - 1 do begin\n" +
                "      h := IBtnBound(Btn.FBoundList[j]).Handle;\n" +
                "      if h = 0 then Continue;\n" +
                "      if FList.IndexOf(Pointer(h)) < 0 then FList.Add(Pointer(h));\n" +
                "    end;\n" +
                "  end;\n" +
                "  for i := 0 to FList.Count - 1 do\n" +
                "  begin\n" +
                "    PostMessage(Cardinal(FList[i]), WM_PAINT, 0, 0);\n" +
                "  end;" +
                "end;" +
                "end." +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }


    @Test
    public void validRuleManyFields() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class\n" +
                "    FList, FList2, flist3 : TList;\n" +
                "    Slist, \n" +
                "    sLIST2,\n" +
                "    SLIST3\n" +
                "    : TStringList;\n" +
                "    procedure Test;\n" +
                "    AList: \n" +
                "    TList; \n" +
                "\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "\n" +
                "procedure Test.Show;\n" +
                "begin\n" +
                "  FList.Free;\n" +
                "  if Assign(FList2) then\n" +
                "    FList2.Free;\n" +
                "  if flist3 <> nil then\n" +
                "  begin\n" +
                "    slist2.free();\n" +
                "  end;\n" +
                "  AList := TList.Create;\n" +
                "  try \n" +
                "    AList.Add(nil);\n" +
                "  finally \n" +
                "    AList.Free;\n" +
                "  end;\n" +
                "end;\n" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  FList := TList.Create;\n" +
                "  FList.add(nil);\n" +
                "end;\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

    @Test
    public void LeakClassObjectIssue() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "  type\n" +
                "  Test = class\n" +
                "    FList : TList;\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "\n" +
                "procedure Test.Test;\n" +
                "begin\n" +
                "  FList := TList.Create;\n" +
                "  FList.add(nil);\n" +
                "end;\n" +
                "initialization\n" +
                "  TComObjectFactory.Create(ComServer, TcpFDSignLog, Class_TcpFDSignLog,\n" +
                "      'FDSignLog', '', ciMultiInstance, tmApartment);\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(1));
    }


    @Test
    public void TfrmMainForm() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("" +
                "type\n" +
                "  TfrmMainForm = class(TForm)\n" +
                "    procedure   acImportOIDsExecute(Sender: TObject);\n" +
                "  end;\n" +
                "\n");
        builder.appendImpl("" +
                "procedure TfrmMainForm.acImportOIDsExecute(Sender: TObject);\n" +
                "var\n" +
                "  List: TStringList;\n" +
                "begin\n" +
                "  List := TStringList.Create;\n" +
                "  try\n" +
                "    List.CommaText := OIDBody;\n" +
                "  finally\n" +
                "    List.Free;\n" +
                "    GlobalVar.Free;" +
                "  end;\n" +
                "end;\n\n" +
                "end.\n" +
                "");

        analyse(builder);

        assertThat(issues, is(empty()));
    }

}
