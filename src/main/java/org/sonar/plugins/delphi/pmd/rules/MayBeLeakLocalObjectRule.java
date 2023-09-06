package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

import java.util.*;

public class MayBeLeakLocalObjectRule extends DelphiRule {

    private boolean isImplementation;
    private int methodLevel = 0; // уровень метода. для отслеживания переменных методов и модуля и вложенных методов
    private int beginLevel = 0;  // для отслеживания конца метода, процедуры, функции.
    boolean isProcessed; //
    //список уровней метода. На каждом уровне мапа Переменная = Место где был создан объект
    private List<Map<String, DelphiPMDNode>> declaredVars = new ArrayList<>(30);
    public static Set<String> analyzedClass = new HashSet<String>(){{
        add("TList".toUpperCase());
        add("TStringList".toUpperCase());
        add("TStaticSet".toUpperCase());
        add("TDelphiStream".toUpperCase());
        add("TTransporter".toUpperCase());
        add("TStringStream".toUpperCase());
        add("TAxStream".toUpperCase());
        add("TRegistry".toUpperCase());
        add("TSafeStringList".toUpperCase());
        add("TEnumStore".toUpperCase());
        add("TTableStore".toUpperCase());
        add("TBitmap".toUpperCase());
        add("TTIFFBitMap".toUpperCase());
        add("TJPEGImage".toUpperCase());
        add("TPNGObject".toUpperCase());
        add("TImage".toUpperCase());
        add("TErrorLog".toUpperCase());
        add("TObjectList".toUpperCase());
        add("TMemoryStream".toUpperCase());
        add("TInterfaceList".toUpperCase());
        add("TParams".toUpperCase());
        add("TObjectList".toUpperCase());
        add("TOleStream".toUpperCase());
        add("TIniFile".toUpperCase());
        add("TFileStream".toUpperCase());
    }};

    public int getBeginLevel() {
        return beginLevel;
    }

    @Override
    protected void init() {
        isImplementation = false;
        methodLevel = 0;
        beginLevel = 0;
        declaredVars.clear();
    }

    //TODO: forward directive in implementation section break method level counting
    // operator 'with' can make false result

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
        calcLevel(node, ctx);
        if (beginLevel == 0)
            return;
        // отслеживаем создание; определяем переменную.
        // ищем освобождение, и присваивание. Передачу параметром здесь не учитываем.
        isProcessed = checkCreateAndFree(node);
    }

    void calcLevel(DelphiPMDNode node, RuleContext ctx){
        if ((node.getType() == DelphiLexer.FUNCTION ||
                node.getType() == DelphiLexer.PROCEDURE ||
                node.getType() == DelphiLexer.CONSTRUCTOR ||
                node.getType() == DelphiLexer.DESTRUCTOR) &&
                node.getParent().getType() != DelphiLexer.TkClass) // exclude methods in class declaration
            methodLevel++;
        else if ((methodLevel >= 0) && (node.getType() == DelphiLexer.TkVariableType) &&
                (node.getParent().getType() != DelphiLexer.TkFunctionArgs)) {
            if ((node.getChildCount() > 0) && analyzedClass.contains(node.getChild(0).getText().toUpperCase()))
                addVars(node);
        } else if ((node.getType() == DelphiLexer.BEGIN) ||
                (node.getType() == DelphiLexer.TRY) ||
                (node.getType() == DelphiLexer.CASE)
        )
            beginLevel++;
        else if ((node.getType() == DelphiLexer.END) && (beginLevel > 0)) {
            beginLevel--;
            if ((beginLevel == 0) && methodLevel >= 0) { // end of procedure or subprocedure
                if (declaredVars.size() > methodLevel) {
                    for (Map.Entry<String, DelphiPMDNode> entry : declaredVars.get(methodLevel).entrySet())
                        if (entry.getValue() != null)
                            addViolation(ctx, entry.getValue());
                    declaredVars.get(methodLevel).clear();
                }
                methodLevel--;
            }
        }
    }

    boolean checkCreateAndFree(DelphiPMDNode node){
        if ((node.getType() == DelphiLexer.TkIdentifier) && // check on create
                analyzedClass.contains(node.getText().toUpperCase()) &&
                (node.getParent().getChildCount() > (node.getChildIndex() + 2)) &&
                (node.getParent().getChild(node.getChildIndex() + 1).getType() == DelphiLexer.DOT) &&
                (node.getParent().getChild(node.getChildIndex() + 2).getText().equalsIgnoreCase("CREATE"))
        ) {
            if ((node.getChildIndex() > 1) && // в переди есть переменная_0 :=_1
                    (node.getParent().getChild(node.getChildIndex() - 2)).getType() == DelphiLexer.TkIdentifier) {
                DelphiPMDNode varNode = (DelphiPMDNode) node.getParent().getChild(node.getChildIndex() - 2);
                addCreate(varNode);
            }
        } else if (("Free".equalsIgnoreCase(node.getText()) || "Destroy".equalsIgnoreCase(node.getText()))&& // check destroy and process
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
                !((node.getParent().getChild(node.childIndex + 2).getType() == DelphiLexer.DOT ) ||
                        (node.getParent().getChild(node.childIndex + 2).getType() == DelphiLexer.LBRACK)
                ))
            processFree(node.getParent().getChild(node.childIndex + 1));
        else
            return false;
        return true;
    }

    void processFree(Tree node){
        String varName = node.getText().toUpperCase();
        for(int i = methodLevel; i >= 0; i-- ) {
            if (declaredVars.size() > i &&(declaredVars.get(i) != null) && (declaredVars.get(i).containsKey(varName))) {
                declaredVars.get(i).put(varName, null);
                return;
            }
        }
    }

    private void addVars(DelphiPMDNode node){
        for (int i = 0; i <= methodLevel; i++)
            declaredVars.add(new HashMap<>());
        Tree listVars = node.getParent().getChild(node.getChildIndex() - 1);
        for (int i = 0; i < listVars.getChildCount(); i++) {
            declaredVars.get(methodLevel).put(listVars.getChild(i).getText().toUpperCase(), null);
        }
    }

    private void addCreate(DelphiPMDNode varNode){
        for(int i = methodLevel; i >= 0; i-- ) {
            if (declaredVars.size() > i &&
                    (declaredVars.get(i) != null) &&
                    (declaredVars.get(i).containsKey(varNode.getText().toUpperCase()))
            ) {
                declaredVars.get(i).put(varNode.getText().toUpperCase(), varNode);
                return;
            }
        }
    }
}
