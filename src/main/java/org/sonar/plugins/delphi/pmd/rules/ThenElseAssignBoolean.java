package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.CommonTree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Created by mahalov on 06.07.2018.
 */
public class ThenElseAssignBoolean extends DelphiRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if (node.getType() == DelphiLexer.THEN) {
            CommonTree parent = node.parent;
            int index = node.getChildIndex();
            if (parent.getChild(index + 1).getType() == DelphiLexer.TkIdentifier && // variable
                    parent.getChild(index + 2).getType() == DelphiLexer.ASSIGN &&   // :=
                    (parent.getChild(index + 3).getType() == DelphiLexer.TRUE ||    // true
                            parent.getChild(index + 3).getType() == DelphiLexer.FALSE) &&  // or false
                    parent.getChild(index + 4).getType() == DelphiLexer.ELSE &&     // else
                    parent.getChild(index + 5).getType() == DelphiLexer.TkIdentifier && // that variable
                    parent.getChild(index + 6).getType() == DelphiLexer.ASSIGN &&   // :=
                    (parent.getChild(index + 7).getType() == DelphiLexer.TRUE ||    // true
                            parent.getChild(index + 7).getType() == DelphiLexer.FALSE) && // or false
                    parent.getChild(index + 1).getText().equalsIgnoreCase(parent.getChild(index + 5).getText()))  // it's one variable
                addViolation(ctx, node);
        }


    }

}
