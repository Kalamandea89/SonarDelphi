/*
 * Sonar Delphi Plugin
 * Author: Apshay 15.11.2018
 *
 */
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class TypeRealRule extends DelphiRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if (node.token.getText().equalsIgnoreCase("Real"))
            addViolation(ctx, node);
    }
}
