package org.sonar.plugins.delphi;

import java.io.File;
import java.nio.charset.Charset;

import net.sourceforge.pmd.ast.ParseException;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;

/**
 * Created by mahalov on 31.05.2018.
 */
public class PasToXML {
    public static void main(String[] args) {

        //File pmdFile = new File("k:\\sonar\\urmnew\\uCPParamAdapter.pas");
        File pmdFile = new File("k:\\sonar\\test\\test.pas");


        if (pmdFile == null || !pmdFile.exists()) {
            System.out.println("error file");
        }

        DelphiAST ast = new DelphiAST(pmdFile, Charset.defaultCharset().name());
        if (ast.isError()) {
            throw new ParseException("grammar error");
        }

        // генерируем ast tree и сохраняем в xml
        ast.generateXML(pmdFile.getAbsolutePath() + ".xml");
    }
}
