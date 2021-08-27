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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BasePmdRuleTest {

  private static final String ROOT_DIR_NAME = Paths.get("target/test-classes/org/sonar/plugins/delphi/PMDTest").toAbsolutePath().toString();
  private static final File ROOT_DIR = new File(ROOT_DIR_NAME);

  private ResourcePerspectives perspectives;
  private DelphiProjectHelper delphiProjectHelper;
  private Issuable issuable;

  DelphiPmdSensor sensor;
  //protected Project project=new Project("porjectstring", "projectbranch", "projectname");
  protected Project project = new Project(ProjectDefinition.create());
  protected List<Issue> issues = new LinkedList<>();
  private File testFile;
  private DelphiPmdProfileExporter profileExporter;
  private RulesProfile rulesProfile;

  public void analyse(DelphiUnitBuilderTest builder) {
    testFile = builder.buildFile(ROOT_DIR);
    configureTest();

    DebugSensorContext sensorContext = new DebugSensorContext();
    sensor.analyse(project, sensorContext);

    assertThat("Errors: " + sensor.getErrors(), sensor.getErrors(), is(empty()));
  }

  private void configureTest() {

    //String relativePathTestFile = DelphiUtils.getRelativePath(testFile, Arrays.asList(ROOT_DIR));

    //configureTest(ROOT_DIR_NAME + "/" + relativePathTestFile);
      //configureTest(testFile.getPath());

      perspectives = mock(ResourcePerspectives.class);
      delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

      // Don't pollute current working directory
      when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

      //File srcFile = DelphiUtils.getResource(testFileName);
      File srcFile = testFile;

      //InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5", srcFile.getPath()).setModuleBaseDir(Paths.get(ROOT_DIR_NAME));
      System.out.println(srcFile.getPath());
      InputFile inputFile = TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5", new File(ROOT_DIR_NAME), srcFile).build();

      DelphiProject delphiProject = new DelphiProject("Default Project");
      delphiProject.setSourceFiles(Arrays.asList(inputFile));

      issuable = mock(Issuable.class);

      when(delphiProjectHelper.getWorkgroupProjects()).thenReturn(Arrays.asList(delphiProject));
      when(delphiProjectHelper.getFile(anyString())).thenAnswer(new Answer<InputFile>() {
          @Override
          public InputFile answer(InvocationOnMock invocation) throws Throwable {
              //InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5", (new File((String) invocation
              //      .getArguments()[0])).getPath()).setModuleBaseDir(Paths.get(ROOT_DIR_NAME));
              File file = new File((String) invocation.getArguments()[0]);
              InputFile inputFile = TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5",
                      file.getPath()).setModuleBaseDir(Paths.get(ROOT_DIR_NAME))
                      .setContents(FileUtils.readFileToString(file, Charset.defaultCharset().name())).build();
              // else exception what lines -1

              when(perspectives.as(Issuable.class, inputFile)).thenReturn(issuable);

              when(issuable.newIssueBuilder()).thenReturn(new StubIssueBuilder());

              return inputFile;
          }
      });

      when(issuable.addIssue(Matchers.any(Issue.class))).then(new Answer<Boolean>() {
          @Override
          public Boolean answer(InvocationOnMock invocation) throws Throwable {
              System.out.println("HIER:"+ invocation.getArguments()[0]);
              Issue issue = (Issue) invocation.getArguments()[0];
              issues.add(issue);
              return Boolean.TRUE;
          }
      });
      rulesProfile = mock(RulesProfile.class);
      profileExporter = mock(DelphiPmdProfileExporter.class);

      String fileName = getClass().getResource("/org/sonar/plugins/delphi/pmd/rules.xml").getPath();
      File rulesFile = new File(fileName);
      String rulesXmlContent;
      try {
          rulesXmlContent = FileUtils.readFileToString(rulesFile);
      } catch (IOException e) {
          throw new RuntimeException(e);
      }

      when(profileExporter.exportProfileToString(rulesProfile)).thenReturn(rulesXmlContent);

      sensor = new DelphiPmdSensor(delphiProjectHelper, perspectives, rulesProfile, profileExporter);

  }

  @After
  public void teardown() {
    if (testFile != null) {
      testFile.delete();
    }
  }

  public String toString(List<Issue> issues) {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for (Issue issue : issues) {
      builder.append(toString(issue)).append(',');
    }
    builder.append("]");
    return builder.toString();
  }

  public String toString(Issue issue) {
    return "Issue [ruleKey=" + issue.ruleKey() + ", message=" + issue.message() + ", line=" + issue.line() + "]";
  }

}
