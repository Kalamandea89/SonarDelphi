package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class DestroyRule extends DelphiRule  {
    private int beginCount;

    @Override
    protected void init() {
        beginCount = 0;
    }

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if ((node.getType() == DelphiLexer.BEGIN) || ( beginCount > 0 && (
                (node.getType() == DelphiLexer.TRY) ||
                (node.getType() == DelphiLexer.CASE)))
            )
            beginCount++;
        else if ((beginCount > 0) && (node.getType() == DelphiLexer.END))
            beginCount--;
        else if ((beginCount > 0) && (node.getType() == DelphiLexer.TkIdentifier) &&
                (node.getParent().getChildCount() > node.getChildIndex() + 2) &&
                (node.getParent().getChild(node.childIndex + 1).getType() == DelphiLexer.DOT) &&
                (node.getParent().getChild(node.getChildIndex() + 2).getText().equalsIgnoreCase("Destroy")))
            addViolation(ctx, node);
    }

}
