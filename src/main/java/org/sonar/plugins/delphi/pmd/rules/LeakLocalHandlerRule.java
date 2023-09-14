package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class LeakLocalHandlerRule extends MayBeLeakLocalObjectRule {

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        isProcessed = true;
        if (node.getType() == DelphiLexer.IMPLEMENTATION) {// нужно обработать и структуру dpr файлов
            isImplementation = true;
            return;
        } else if ((node.getType() == DelphiLexer.INITIALIZATION) ||
                (node.getType() == DelphiLexer.FINALIZATION) ||
                ((node.getType() == DelphiLexer.END) && // end.
                        (node.getParent().getChildCount() > node.childIndex + 1) &&
                        (node.getParent().getChild(node.childIndex + 1).getType() == DelphiLexer.DOT)
                )) // dpr так же заканчивается end.
            isImplementation = false;

        if (!isImplementation)
            return;
        calcLevel(node, ctx, LeakClassHandlerRule.analyzedClass);
        if (beginLevel == 0)
            return;
        // отслеживаем создание; определяем переменную.
        // ищем освобождение, и присваивание. Передачу параметром здесь не учитываем.
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
                (node.getParent().getChildIndex() >= 2) && // exist varName_0 and ._1
                (node.getParent().getChildCount() > (node.getChildIndex() + 1)) &&
                (node.getParent().getChild(node.getChildIndex() + 2).getType() == DelphiLexer.TkIdentifier)
        ) {
            processFree(node.getParent().getChild(node.childIndex + 2));
        } else
            return false;
        return true;
    }
}
