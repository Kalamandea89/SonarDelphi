package org.sonar.plugins.delphi.pmd;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class InheritedInDestroyFromTComObjectTest extends BasePmdRuleTest {

    @Test
    public void TestRule() {
        DelphiUnitBuilderTest builder = new DelphiUnitBuilderTest();
        builder.appendDecl("");
        builder.appendDecl("uses SysUtils, StdVcl, uTestItf, Classes, Windows, ComObj, ActiveX;");
        builder.appendDecl("");
        builder.appendDecl("type");
        builder.appendDecl("");
        builder.appendDecl("TTest = class(TCOMObject, ITest)");
        builder.appendDecl("public");
        builder.appendDecl("  destructor Destroy; override;");
        builder.appendDecl("end;");
        builder.appendDecl("");
        builder.appendImpl("");
        builder.appendImpl("uses ComServ;");
        builder.appendImpl("");
        builder.appendImpl("destructor TTest.Destroy;");
        builder.appendImpl("const CtxName = 'test';");
//TODO this break test. Find begin ahead 3 very cool!
//        builder.appendImpl("var a : boolean;");
// TODO inner procedure also break test;
//        builder.appendImpl("  procedure TestInner;");
//        builder.appendImpl("  begin");
//        builder.appendImpl("    sleep(0);");
//        builder.appendImpl("  end;");

        builder.appendImpl("begin");
        builder.appendImpl("  PushErrorContext('CtxName');");
        builder.appendImpl("  try");
        builder.appendImpl("    if IsActive then Close;");
        builder.appendImpl("  finally");
        builder.appendImpl("    PopErrorContext;");
        builder.appendImpl("  end;");
//        builder.appendImpl("  inherited;");
        builder.appendImpl("end;");
        builder.appendImpl("");
        builder.appendImpl("initialization");
        builder.appendImpl("TComObjectFactory.Create(ComServer, TTest, Class_TTest, 'Test','test', ciMultiInstance);");
        builder.appendImpl("end.");
        builder.appendImpl("");

        
        analyse(builder);
        // todo make all tests for one current rule. Or don't make. Other test sometime found error
        // size 2 because other rules included
        assertThat(issues.toString(), issues, hasSize(2));

    }
}
