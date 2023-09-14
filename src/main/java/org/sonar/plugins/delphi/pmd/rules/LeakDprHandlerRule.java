package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class LeakDprHandlerRule extends LeakDprObjectRule {

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
        calcLevel(node, ctx, LeakClassHandlerRule.analyzedClass);
        isProcessed = checkCreateAndFree(node);
    }

    boolean checkCreateAndFree(DelphiPMDNode node){
        // отслеживаем создание; определяем переменную.
        // ищем освобождение, и присваивание. Передачу параметром здесь не учитываем.
        if ((node.getType() == DelphiLexer.TkIdentifier) && LeakClassHandlerRule.analyzedMethod.contains(node.getText().toUpperCase()) &&
                (node.getChildIndex() > 1) && // в переди есть переменная_0 :=_1
                (node.getParent().getChild(node.getChildIndex() - 2)).getType() == DelphiLexer.TkIdentifier
        ) {
            DelphiPMDNode varNode = (DelphiPMDNode) node.getParent().getChild(node.getChildIndex() - 2);
            addCreate(varNode);
        } else if ("CloseHandle".equalsIgnoreCase(node.getText())  && // check closeparseClassFields and process
                (node.getParent().getChildIndex() > 2) && // exist varName_0 and ._1
                (node.getParent().getChildCount() > (node.getChildIndex() + 1)) &&
                (node.getParent().getChild(node.getChildIndex() + 2).getType() == DelphiLexer.TkIdentifier)
        ) {
            processFree(node.getParent().getChild(node.childIndex + 2));
        } else
            return false;
        return true;
    }
}
