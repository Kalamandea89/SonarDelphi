package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class LeakLocalObjectRule extends MayBeLeakLocalObjectRule {

    //TODO: forward directive in implementation section break method level counting
    // operator 'with' can make false result
    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        super.visit(node, ctx);

        if (isProcessed)
            return;

        // в дополнению к проверкам предка проверяем передачу параметром.
        if ((node.getType() == DelphiLexer.TkIdentifier) && // check pass first parameter
                (node.getChildIndex() > 4) &&
                (node.getParent().getChild(node.childIndex - 1).getType() == DelphiLexer.LPAREN) &&
                ((node.getParent().getChild(node.childIndex + 1).getType() == DelphiLexer.RPAREN) ||
                        (node.getParent().getChild(node.childIndex + 1).getType() == DelphiLexer.COMMA))
                )
            processFree(node);
        else if ((node.getType() == DelphiLexer.COMMA) && // check pass as last parameter
                (node.getParent().getChildCount() > node.childIndex + 2) &&
                (node.getParent().getChild(node.childIndex + 2).getType() == DelphiLexer.RPAREN))
            processFree(node.getParent().getChild(node.childIndex + 1));
        else if ((node.getType() == DelphiLexer.COMMA) && // check pass as middle parameter
                (node.getParent().getChildCount() > node.childIndex + 2) &&
                (node.getParent().getChild(node.getChildIndex() + 2).getType() == DelphiLexer.COMMA))
            processFree(node.getParent().getChild(node.childIndex + 1));
    }

}
