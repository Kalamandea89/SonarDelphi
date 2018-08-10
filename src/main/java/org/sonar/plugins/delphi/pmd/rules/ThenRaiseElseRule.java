package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Created by mahalov on 04.07.2018.
 */
public class ThenRaiseElseRule extends DelphiRule {
    private boolean wasThenRiase = false;

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if ((node.getType() == DelphiLexer.THEN) && node.parent.getChild(node.getChildIndex() + 1).getType() == DelphiLexer.RAISE)
            wasThenRiase = true;
        else if (wasThenRiase && node.getType() == DelphiLexer.ELSE) {
            wasThenRiase = false;
            addViolation(ctx, node);
        } else if (wasThenRiase && (node.getType() == DelphiLexer.END || node.getType() == DelphiLexer.SEMI))
            wasThenRiase = false;
    }

}
