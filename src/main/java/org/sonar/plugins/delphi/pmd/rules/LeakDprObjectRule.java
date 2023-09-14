package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class LeakDprObjectRule extends MayBeLeakLocalObjectRule {

    int check;

    @Override
    public void init() {
        super.init();
        // needs to check at new file
        check = -1;
    }

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if (check == -1) {
            if (node.getASTTree().getFileName().endsWith(".dpr") || node.getASTTree().getFileName().endsWith(".dpk")) {
                check = 1;
            } else {
                check = 0;
            }
        }
        if (check != 1) {
            // not a .dpr/.dpk file
            return;
        }
        isProcessed = true;
        calcLevel(node, ctx, analyzedClass);
        isProcessed = checkCreateAndFree(node);
    }
}
