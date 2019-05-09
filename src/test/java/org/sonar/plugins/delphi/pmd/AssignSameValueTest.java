package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class AssignSameValueTest extends BasePmdRuleTest {

    @Test
    public void validRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("procedure Test();");
        builder.appendImpl("procedure Test();");
        builder.appendImpl("begin");

        builder.appendImpl("  Result := GetURMStatus = 1;");
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
        builder.appendImpl("    result := false;");
        builder.appendImpl("    sleep(0);");
        builder.appendImpl("  end;");


        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(4));
    }

    @Test
    public void validRule2() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendImpl("procedure Test();");

        builder.appendImpl("function TCanRun.DoPrimary: Variant;");
        builder.appendImpl("var");
        builder.appendImpl("IsGuardExists: boolean;");
        builder.appendImpl("startup_info: TStartupInfo;");
        builder.appendImpl("process_information: TProcessInformation;");
        builder.appendImpl("begin");
        builder.appendImpl("Result := GetURMStatus = 1;");

        builder.appendImpl("if not result then");
        builder.appendImpl("result := false;");

        builder.appendImpl("if not Result then");
        builder.appendImpl("begin");
        builder.appendImpl("Result := false;");
        builder.appendImpl("ShowMessage('test');");
        builder.appendImpl("FillChar(startup_info, SizeOf(startup_info), 0);");
        builder.appendImpl("startup_info.cb := SizeOf(startup_info);");
        builder.appendImpl("if not CreateProcess(");
        builder.appendImpl("PChar(GetURMUpdaterFileName),//-- Application name --");
        builder.appendImpl("PChar(Format('\"%s\" \"%s\"', [GetURMUpdaterFileName, 'restore'])),  //-- Command line --");
        builder.appendImpl("nil,  //-- Process security attributes --");
        builder.appendImpl("        nil,  //-- Thread security attributes --");
        builder.appendImpl("        false, //-- Inherit handles --");
        builder.appendImpl("       CREATE_DEFAULT_ERROR_MODE + NORMAL_PRIORITY_CLASS, //-- Creation flags --");
        builder.appendImpl("nil,  //-- Environment block --");
        builder.appendImpl("PChar(ExtractFilePath(GetURMUpdaterFileName)), //-- CurrentDirectory --");
        builder.appendImpl("startup_info,");
        builder.appendImpl("process_information");
        builder.appendImpl(")");
        builder.appendImpl("then raise Myexception.Create('Ошибка запуска файла [Updater.exe]');");
        builder.appendImpl("Exit;");
        builder.appendImpl("end;");
        builder.appendImpl("end;");

        analyse(builder);

        assertThat(issues.toString(), issues, hasSize(2));

    }


}
