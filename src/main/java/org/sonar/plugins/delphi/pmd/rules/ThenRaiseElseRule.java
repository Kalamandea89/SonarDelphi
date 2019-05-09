package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Created by mahalov on 04.07.2018.
 */
public class ThenRaiseElseRule extends DelphiRule {
    private boolean wasGoodBefore = false;
    private boolean wasThenRiase = false;

    /* before can be (begin, try, repeat, ;) and not can't be (then, else, do, :) if ... then raise

     */

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if ((node.getType() == DelphiLexer.BEGIN) || (node.getType() == DelphiLexer.TRY) || (node.getType() == DelphiLexer.REPEAT)
                || (node.getType() == DelphiLexer.SEMI))
            wasGoodBefore = true;
        else if ((node.getType() == DelphiLexer.DO) || (node.getType() == DelphiLexer.COLON))
            wasGoodBefore = false;

        if (wasGoodBefore && (node.getType() == DelphiLexer.THEN)) {
            if (node.parent.getChild(node.getChildIndex() + 1).getType() == DelphiLexer.RAISE)
                wasThenRiase = true;
            else
                wasGoodBefore = false;
        }
        else if (wasThenRiase && node.getType() == DelphiLexer.ELSE) {
            wasGoodBefore = false;
            wasThenRiase = false;
            addViolation(ctx, node);
        } else if (wasThenRiase && (node.getType() == DelphiLexer.END || node.getType() == DelphiLexer.SEMI))
            wasThenRiase = false;
        else if (node.getType() == DelphiLexer.ELSE)
            wasGoodBefore = false;
    }

}
