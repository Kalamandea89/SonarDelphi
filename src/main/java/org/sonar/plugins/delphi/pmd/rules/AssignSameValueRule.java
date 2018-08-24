package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class AssignSameValueRule extends DelphiRule {
    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        // ищем if переменнная then 
        if ((node.getType() == DelphiLexer.IF) &&
                (node.parent.getChild(node.getChildIndex() + 1).getType() == DelphiLexer.TkIdentifier) &&
                node.parent.getChild(node.getChildIndex() + 2).getType() == DelphiLexer.THEN) {

                // тут обрабатываем что внутри begin ей присвоили опять true
                if (node.parent.getChild(node.getChildIndex() + 3).getType() == DelphiLexer.BEGIN) {
                    Tree begin = node.parent.getChild(node.getChildIndex() + 3);
                    for (int i = 0; i < begin.getChildCount(); i++) {
                        if ((begin.getChild(i).getType() == DelphiLexer.TkIdentifier) &&
                                begin.getChild(i).getText().equalsIgnoreCase(node.parent.getChild(node.getChildIndex() + 1).getText()) &&
                                begin.getChild(i + 1).getType() == DelphiLexer.ASSIGN &&
                                begin.getChild(i + 2).getType() == DelphiLexer.TRUE)
                            addViolation(ctx, node);
                    }
                }
                // а здесь что сразу после then ей опять присвоиили true
            if (node.parent.getChild(node.getChildIndex() + 3).getText().equalsIgnoreCase((node.parent.getChild(node.getChildIndex() + 1).getText())) &&
                node.parent.getChild(node.getChildIndex() + 4).getType() == DelphiLexer.ASSIGN &&
                node.parent.getChild(node.getChildIndex() + 5).getType() == DelphiLexer.TRUE)
            addViolation(ctx, node);
        }

        // !!! ЗДЕСЬ ПРОВЕРКА на false

        if ((node.getType() == DelphiLexer.IF) &&
                (node.parent.getChild(node.getChildIndex() + 1).getType() == DelphiLexer.NOT) &&
                (node.parent.getChild(node.getChildIndex() + 2).getType() == DelphiLexer.TkIdentifier) &&
                node.parent.getChild(node.getChildIndex() + 3).getType() == DelphiLexer.THEN ){

            // здесь проверка на вложеный begin/end;
            if (node.parent.getChild(node.getChildIndex() + 4).getType() == DelphiLexer.BEGIN) {
                Tree begin2 = node.parent.getChild(node.getChildIndex() + 4);
                for (int i = 0; i < begin2.getChildCount(); i++) {
                    if ((begin2.getChild(i).getType() == DelphiLexer.TkIdentifier) &&
                            begin2.getChild(i).getText().equalsIgnoreCase(node.parent.getChild(node.getChildIndex() + 2).getText()) &&
                            begin2.getChild(i + 1).getType() == DelphiLexer.ASSIGN &&
                            begin2.getChild(i + 2).getType() == DelphiLexer.FALSE)
                        addViolation(ctx, node);
                }
            }
                // далей той же самой переменнной прпосто присвоили false
            if (node.parent.getChild(node.getChildIndex() + 4).getText().equalsIgnoreCase((node.parent.getChild(node.getChildIndex() + 2).getText())) &&
                node.parent.getChild(node.getChildIndex() + 5).getType() == DelphiLexer.ASSIGN &&
                node.parent.getChild(node.getChildIndex() + 6).getType() == DelphiLexer.FALSE)
                addViolation(ctx, node);

               // или же где-то внутрри begin end сделали это.


        }

    }



}
