package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LeakClassHandlerRule extends LeakClassObjectRule {

    private static Set<String> analyzedMethod = new HashSet<String>(){{
        add("CreateMutex".toUpperCase());
        add("CreateEvent".toUpperCase());
        add("CreateSemaphore".toUpperCase());
        add("FindWindow".toUpperCase());
        add("CreateFile".toUpperCase());
    }};
    private static Set<String> analyzedClass = new HashSet<String>(){{
        add("THANDLE");
    }};

    @Override
    public void visit(DelphiPMDNode node, RuleContext ctx) {
        if (node.getType() == DelphiLexer.IMPLEMENTATION) {
            isImplementation = true;
            return;
        } else if ((node.getType() == DelphiLexer.INITIALIZATION) ||
                (node.getType() == DelphiLexer.FINALIZATION) ||
                ((node.getType() == DelphiLexer.END) && // end.
                        (node.getParent().getChildCount() > node.childIndex + 1) &&
                        (node.getParent().getChild(node.childIndex + 1).getType() == DelphiLexer.DOT)
                )) {// dpr так же заканчивается end.
            isImplementation = false;
            // вышли из секции реализации. Публикуем проблемы по тем Handle, что были созданы и не были освобождены
            for (String className : declaredFields.keySet()) {
                for (Map.Entry<String, CreateFreeInfo> variable : declaredFields.get(className).entrySet()) {
                    if (variable.getValue().create != null && variable.getValue().destroy == null)
                        addViolation(ctx, variable.getValue().create);
                }
            }
            // выходить можем и два раза. что бы не публиковать проблемы два раза чистим их после первой публикации
            declaredFields.clear();
        } else if (node.getType() == DelphiLexer.TkClassField) {
            parseClassFields(node, analyzedClass); // обрабатываем поля класса в декларациях и реализации
            return;
        }

        if (!isImplementation)
            return;

        // здесь мы в реализации. Смотрим внутри метода какого класса. Отслеживаем создание и освобождение.
        if ((node.getType() == DelphiLexer.FUNCTION ||
                node.getType() == DelphiLexer.PROCEDURE ||
                node.getType() == DelphiLexer.CONSTRUCTOR ||
                node.getType() == DelphiLexer.DESTRUCTOR) &&
                node.getParent().getType() != DelphiLexer.TkClass) {// exclude methods in class declaration
            methodLevel++;
            if (node.getChildCount() > 0 && node.getChild(0).getChildCount() == 3)
                inClassName = node.getChild(0).getChild(0).getText().toUpperCase();
        } else if ((node.getType() == DelphiLexer.BEGIN) ||
                (node.getType() == DelphiLexer.TRY) ||
                (node.getType() == DelphiLexer.CASE)
        )
            beginLevel++;
        else if ((node.getType() == DelphiLexer.END) &&
                (beginLevel > 0)) {
            beginLevel--;
            if ((beginLevel == 0) && // end of procedure or sub procedure
                    methodLevel > 0) {
                methodLevel--;
            }
        }

        if (methodLevel == 0){
            inClassName = null;
            return;
        }

        // отслеживаем создание; определяем переменную.
        // ищем освобождение, и присваивание. Передачу параметром здесь не учитываем.
        if ((node.getType() == DelphiLexer.TkIdentifier) && analyzedMethod.contains(node.getText().toUpperCase()) &&
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
        }
    }
}
