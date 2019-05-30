package org.sonar.plugins.delphi.pmd.rules;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class InheritedInDestroyFromTComObjectRule extends DelphiRule {
    private static final int MAX_LOOK_AHEAD = 3;

    // потомки от них перекрывая destroy должны вызывать inherited
    private Set<String> parents = new HashSet<String>(){{
        add("TComObject".toUpperCase());
        add("TTypedComObject".toUpperCase());
        add("TAutoObject".toUpperCase());
        add("TAutoObjectModule".toUpperCase());
        add("TModule".toUpperCase());
        add("TComponentComObject".toUpperCase());
    }};

    private Set<String> descendants = new HashSet<>();

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if (node.getType() == DelphiLexer.TkClassParents) {
            if (node.getChild(0) != null &&
                    parents.contains(node.getChild(0).getText().toUpperCase())) {
                descendants.add(node.getParent().getParent().getText().toUpperCase());
                parents.add(node.getParent().getParent().getText().toUpperCase());
            }
        } else if (node.getType() == DelphiLexer.DESTRUCTOR) {
            if (node.getChild(0).getType() == DelphiLexer.TkFunctionName) {
                Tree funcName = node.getChild(0);
                if (funcName.getChildCount() == 3 &&
                        funcName.getChild(1).getType() == DelphiLexer.DOT &&
                        descendants.contains(funcName.getChild(0).getText().toUpperCase())) {
                    lookInherited(node, ctx);
                }
            }
        }

    }

    private void lookInherited(DelphiPMDNode node, RuleContext ctx){
        Tree beginNode = null;
        for (int i = node.getChildIndex() + 1; i < node.getChildIndex() + MAX_LOOK_AHEAD
                && i < node.getParent().getChildCount(); ++i) {
            if (node.getParent().getChild(i).getType() == DelphiLexer.BEGIN) {
                beginNode = node.getParent().getChild(i);
                break;
            }
        }
        if (beginNode != null) {
            boolean wasInherited = false;
            for (int c = 0; c < beginNode.getChildCount(); c++) {
                if (beginNode.getChild(c).getType() == DelphiLexer.INHERITED) {
                    wasInherited = true;
                    break;
                }
            }

            if (!wasInherited)
                addViolation(ctx, node);
        }
    }
}
