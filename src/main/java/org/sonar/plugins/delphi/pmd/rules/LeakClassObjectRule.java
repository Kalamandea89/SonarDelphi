package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.*;

import static org.sonar.plugins.delphi.pmd.rules.MayBeLeakLocalObjectRule.analyzedClass;

public class LeakClassObjectRule extends DelphiRule {
    // Информация о месте создания и освобождении объекта. в коде класса может идти в любом порядке.
    // Созданий и освобождений может быть несколько. Запоминаем любое.
    class CreateFreeInfo {
        DelphiPMDNode create;
        Tree destroy;
    }

    private boolean isImplementation;
    private int methodLevel = 0; // уровень метода. для отслеживания переменных методов и модуля и вложенных методов
    private int beginLevel = 0;  // для отслеживания конца метода, процедуры, функции.
    private String inClassName;
    // мапа класс = мапа переменная = место создания объекта
    private Map<String, Map<String, CreateFreeInfo>> declaredFields = new HashMap<>(30);

    @Override
    protected void init() {
        isImplementation = false;
        methodLevel = 0;
        beginLevel = 0;
        declaredFields.clear();
    }

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
            // вышли из секции реализации. Публикуем проблемы по тем объектам, что были созданы и не были освобождены
            for (String className : declaredFields.keySet()) {
                for (Map.Entry<String, CreateFreeInfo> variable : declaredFields.get(className).entrySet()) {
                    if (variable.getValue().create != null && variable.getValue().destroy == null)
                        addViolation(ctx, variable.getValue().create);
                }
            }
            // выходить можем и два раза. что бы не публиковать проблемы два раза чистим их после первой публикации
            declaredFields.clear();
        } else if (node.getType() == DelphiLexer.TkClassField) {
            parseClassFields(node); // обрабатываем поля класса в декларациях и реализации
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

        if (methodLevel == 0)
            inClassName = null;

        if (beginLevel == 0)
            return;

        // отслеживаем создание; определяем переменную.
        // ищем освобождение, и присваивание. Передачу параметром здесь не учитываем.
        if ((node.getType() == DelphiLexer.TkIdentifier) && // check on create
                analyzedClass.contains(node.getText().toUpperCase()) &&
                (node.getParent().getChildCount() > (node.getChildIndex() + 2)) &&
                (node.getParent().getChild(node.getChildIndex() + 1).getType() == DelphiLexer.DOT) &&
                (node.getParent().getChild(node.getChildIndex() + 2).getText().equalsIgnoreCase("CREATE"))
        ) {
            // обработаем случай когда СтатикСет создаётся с овнером
            if (node.getText().equalsIgnoreCase("TStaticSet") &&
                    node.getParent().getChildCount() > node.getChildIndex() + 5 &&
                    node.getParent().getChild(node.getChildIndex() + 3).getType() == DelphiLexer.LPAREN &&
                    !node.getParent().getChild(node.getChildIndex() + 4).getText().equalsIgnoreCase("nil"))
                return;
            if ((node.getChildIndex() > 1) && // в переди есть переменная_0 :=_1
                    (node.getParent().getChild(node.getChildIndex() - 2)).getType() == DelphiLexer.TkIdentifier) {
                DelphiPMDNode varNode = (DelphiPMDNode) node.getParent().getChild(node.getChildIndex() - 2);
                addCreate(varNode);
            }
        } else if (("Free".equalsIgnoreCase(node.getText()) || "Destroy".equalsIgnoreCase(node.getText())) && // check destroy and process
                (node.getChildIndex() > 1) && // exist varName_0 and ._1
                (node.getParent().getChild(node.getChildIndex() - 1).getType() == DelphiLexer.DOT) &&
                (node.getParent().getChild(node.getChildIndex() - 2).getType() == DelphiLexer.TkIdentifier)
        ) {
            processFree(node.getParent().getChild(node.childIndex - 2));
        } else if ("FreeAndNil".equalsIgnoreCase(node.getText()) &&
                (node.getParent().getChildCount() > node.childIndex + 3) &&
                (node.getParent().getChild(node.childIndex + 1).getType() == DelphiLexer.LPAREN) &&
                (node.getParent().getChild(node.childIndex + 2).getType() == DelphiLexer.TkIdentifier)
        )
            processFree(node.getParent().getChild(node.getChildIndex() + 2));
        else if ((node.getType() == DelphiLexer.ASSIGN) && // check assign result := obj; and etc.
                !((node.getParent().getChild(node.childIndex + 2).getType() == DelphiLexer.DOT) ||
                        (node.getParent().getChild(node.childIndex + 2).getType() == DelphiLexer.LBRACK)
                ))
            processFree(node.getParent().getChild(node.childIndex + 1));


    }

    private void parseClassFields(DelphiPMDNode node) {
        if (node.getParent().getType() != DelphiLexer.TkClass && node.getParent().getType() != DelphiLexer.TkObject)
            throw new RuntimeException("parent is not TkClass and TkObject");

        String className = node.getParent().getParent().getText().toUpperCase();

        if (node.getChildCount() != 2)
            throw new RuntimeException(" class field node has not 2 child");

        if (node.getChild(1).getType() != DelphiLexer.TkVariableType)
            throw new RuntimeException(" child 2 of class field node has not type TKVariableType");

        if (analyzedClass.contains(node.getChild(1).getChild(0).getText().toUpperCase())) {
            Map<String, CreateFreeInfo> fields = declaredFields.get(className);
            if (fields == null) {
                fields = new HashMap<>();
                declaredFields.put(className, fields);
            }
            Tree field = node.getChild(0).getChild(0);
            fields.put(field.getText().toUpperCase(), new CreateFreeInfo());
            // перечисленные через запятую идут потомками к первому полю.
            for (int i = 0; i < field.getChildCount(); i++) {
                fields.put(field.getChild(i).getText().toUpperCase(), new CreateFreeInfo());
            }
        }

    }

    private void processFree(Tree node) {
        if (inClassName == null)
            return; // освобождение в свободной процедуре или теле dpr файла

        String varName = node.getText().toUpperCase();
        if (declaredFields.get(inClassName) != null && declaredFields.get(inClassName).get(varName) != null)
            declaredFields.get(inClassName).get(varName).destroy = node;
    }

    private void addCreate(DelphiPMDNode varNode) {
        if (inClassName == null)
            return; //создание в свободной процедуре или теле dpr файла

        if (!declaredFields.containsKey(inClassName))
            return; // в классе не было не одного поля подходящего класса throw new RuntimeException("Class set has not class " + inClassName);

        String varName = varNode.getText().toUpperCase();
        if (!declaredFields.get(inClassName).containsKey(varName))
            return; //was not such class field. it local variable create throw new RuntimeException("Fields set has not field: " + varName);

        declaredFields.get(inClassName).get(varName).create = varNode;
    }

}
