/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.pmd;

import com.google.common.io.LineReader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

public class DelphiUnitBuilderTest {

    private StringBuilder declaration = new StringBuilder();
    private StringBuilder implementation = new StringBuilder();

    private int offset;
    private int offsetDecl;
    private String unitName = "Unit1";
    private String fileSufix = ".pas";

    public void setFileSufix(String fileSufix) {
        this.fileSufix = fileSufix;
    }

    public String getFileSufix() {
        return fileSufix;
    }

    public DelphiUnitBuilderTest appendDecl(String value) {
        declaration.append(value).append("\n");
        offset++;
        return this;
    }

    public DelphiUnitBuilderTest appendImpl(String value) {
        implementation.append(value).append("\n");
        return this;
    }

    private String declaration() {
        return declaration.toString();
    }

    public String implementation() {
        return implementation.toString();
    }

    DelphiUnitBuilderTest unitName(String unitName) {
        this.unitName = unitName;
        return this;
    }

    File buildFile(File baseDir, String sufix) {
        StringBuilder source = getSourceCode();

        try {
            File file = File.createTempFile("unit", sufix, baseDir);
            file.deleteOnExit();

            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(source.toString());
                fileWriter.flush();
            }
            return file;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public StringBuilder getSourceCode() {
        // fixed lines
        offsetDecl = 4;
        offset = offset + 6;

        StringBuilder source = new StringBuilder();
        if (fileSufix.equals(".pas")){
            source.append(String.format("unit %s;\n", this.unitName));
            source.append("\n");
            source.append("interface\n");
            source.append("\n");
        }else {
            source.append(String.format("program %s;\n", this.unitName));
        }

        if (this.declaration.length() > 0) {
            source.append(this.declaration()).append("\n");
            offset++;
        }
        if (fileSufix.equals(".pas")){
            source.append("implementation\n");
            source.append("\n");
        }

        if (this.implementation.length() > 0) {
            source.append(this.implementation()).append("\n");
        }
        source.append("end.\n");

        printSourceCode(source);

        return source;
    }

    private void printSourceCode(StringBuilder source) {
        Readable reader = new StringReader(source.toString());
        LineReader lineReader = new LineReader(reader);
        String line;
        int lineNumber = 0;
        try {
            while ((line = lineReader.readLine()) != null) {
                System.out.println(String.format("%03d %s", ++lineNumber, line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to print source code.", e);
        }
    }

    int getOffSet() {
        return offset;
    }

    int getOffsetDecl() {
        return offsetDecl;
    }
}
