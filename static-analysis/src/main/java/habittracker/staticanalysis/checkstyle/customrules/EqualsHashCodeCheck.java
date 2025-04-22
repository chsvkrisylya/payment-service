package habittracker.staticanalysis.checkstyle.customrules;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Проверяет обязательное наличие методов equals() и hashCode()
 * в классах без Lombok-аннотаций.
 */
public class EqualsHashCodeCheck extends AbstractCheck {

    private static final String MSG_KEY = "Класс должен содержать методы equals() и hashCode().";
    private final List<String> lombokAnnotations = Arrays.asList(
            "Data",
            "EqualsAndHashCode",
            "Value",
            "lombok.EqualsAndHashCode",
            "lombok.Data",
            "lombok.Value"
    );

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.CLASS_DEF};
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void visitToken(DetailAST ast) {
        if (!isClassDefinition(ast)) {
            return;
        }

        if (hasLombokAnnotations(ast)) {
            return;
        }

        MethodPresenceStatus status = analyzeClassMethods(ast);

        if (status.missingRequirements()) {
            log(ast.getLineNo(), MSG_KEY);
        }
    }

    private boolean isClassDefinition(DetailAST ast) {
        return ast != null && ast.getType() == TokenTypes.CLASS_DEF;
    }

    private boolean hasLombokAnnotations(DetailAST classAst) {
        return checkLombokAnnotations(classAst);
    }

    private MethodPresenceStatus analyzeClassMethods(DetailAST classAst) {
        MethodPresenceStatus status = new MethodPresenceStatus();
        DetailAST objBlock = classAst.findFirstToken(TokenTypes.OBJBLOCK);

        if (objBlock != null) {
            List<DetailAST> methods = extractMethods(objBlock);
            methods.forEach(method -> processMethod(method, status));
        }
        return status;
    }

    private List<DetailAST> extractMethods(DetailAST objBlock) {
        List<DetailAST> methods = new ArrayList<>();
        DetailAST method = objBlock.getFirstChild();

        while (method != null) {
            if (method.getType() == TokenTypes.METHOD_DEF) {
                methods.add(method);
            }
            method = method.getNextSibling();
        }
        return methods;
    }

    private void processMethod(DetailAST method, MethodPresenceStatus status) {
        DetailAST ident = method.findFirstToken(TokenTypes.IDENT);
        if (ident == null) {
            return;
        }

        String methodName = ident.getText();

        if (methodName.equals("equals") && hasOverrideAnnotation(method)) {
            status.setHasEquals(true);
        } else if (methodName.equals("hashCode") && hasOverrideAnnotation(method)) {
            status.setHasHashCode(true);
        }
    }

    private boolean hasOverrideAnnotation(DetailAST method) {
        DetailAST modifiers = method.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }

        for (DetailAST annotation = modifiers.getFirstChild();
             annotation != null;
             annotation = annotation.getNextSibling()) {

            if (annotation.getType() == TokenTypes.ANNOTATION
                    && "Override".equals(getAnnotationName(annotation))) {
                return true;
            }
        }
        return false;
    }

    private String getTypeName(DetailAST typeNode) {
        if (typeNode == null) {
            return "";
        }

        // Обработка узла TYPE
        if (typeNode.getType() == TokenTypes.TYPE) {
            DetailAST childType = typeNode.getFirstChild();
            return getTypeName(childType);
        }

        return switch (typeNode.getType()) {
            case TokenTypes.IDENT -> typeNode.getText();
            case TokenTypes.DOT -> getFullAnnotationName(typeNode);
            case TokenTypes.ARRAY_DECLARATOR -> getTypeName(typeNode.getFirstChild()) + "[]";
            default -> "";
        };
    }

    private String getAnnotationName(DetailAST annotation) {
        DetailAST ident = annotation.findFirstToken(TokenTypes.IDENT);
        if (ident != null) {
            return ident.getText();
        }

        DetailAST dot = annotation.findFirstToken(TokenTypes.DOT);
        return dot != null ? getFullAnnotationName(dot) : "";
    }

    private String getFullAnnotationName(DetailAST dot) {
        if (dot.getType() != TokenTypes.DOT) {
            return "";
        }

        DetailAST left = dot.getFirstChild();
        DetailAST right = left.getNextSibling();

        String leftPart = left.getType() == TokenTypes.DOT
                ? getFullAnnotationName(left)
                : getComponentName(left);
        String rightPart = getComponentName(right);

        return leftPart + "." + rightPart;
    }

    private String getComponentName(DetailAST node) {
        if (node == null) {
            return "";
        }
        return switch (node.getType()) {
            case TokenTypes.IDENT -> node.getText();
            case TokenTypes.DOT -> getFullAnnotationName(node);
            default -> "";
        };
    }

    private boolean checkLombokAnnotations(DetailAST classDef) {
        DetailAST modifiers = classDef.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }

        for (DetailAST annotation = modifiers.getFirstChild();
             annotation != null;
             annotation = annotation.getNextSibling()) {

            if (annotation.getType() == TokenTypes.ANNOTATION) {
                String annotationName = getAnnotationName(annotation);
                if (lombokAnnotations.contains(annotationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class MethodPresenceStatus {
        private boolean hasEquals;
        private boolean hasHashCode;

        boolean missingRequirements() {
            return !hasEquals || !hasHashCode;
        }

        void setHasEquals(boolean state) {
            hasEquals = state;
        }

        void setHasHashCode(boolean state) {
            hasHashCode = state;
        }
    }
}