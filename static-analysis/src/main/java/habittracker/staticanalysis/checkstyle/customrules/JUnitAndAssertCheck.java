package habittracker.staticanalysis.checkstyle.customrules;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Arrays;
import java.util.List;

public class JUnitAndAssertCheck extends AbstractCheck {

    private static final String MSG_KEY = "Test should use AssertJ (assertThat()) for assertions.";

    // Список JUnit аннотаций
    private static final List<String> JUNIT_ANNOTATIONS = Arrays.asList(
            "Test", "Before", "After", "BeforeClass", "AfterClass", "BeforeEach", "AfterEach",
            "BeforeAll", "AfterAll", "ParameterizedTest"
    );

    //Список аннотаций для интеграционных тестов (пример)
    private static final List<String> INTEGRATION_TEST_ANNOTATIONS = Arrays.asList(
            "SpringBootTest", "WebMvcTest", "DataJpaTest", "TestConfiguration"
    );

    @Override
    public int[] getDefaultTokens() {
        return new int[]{TokenTypes.METHOD_CALL, TokenTypes.CLASS_DEF};
    }

    //    служит для указания тех токенов, которые правило должно обработать.
    @Override
    public int[] getAcceptableTokens() {
        return new int[]{TokenTypes.METHOD_CALL, TokenTypes.CLASS_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {

        if (ast.getType() == TokenTypes.METHOD_CALL) {
            checkAssertMethod(ast);
        }

        // Проверка класса
        if (ast.getType() == TokenTypes.CLASS_DEF) {
            if (!isTestClass(ast)) {
                return; // Пропускаем проверки для модульных тестов
            }
            if (isIntegrationTest(ast)) {
                return; // Пропускаем проверки для интеграционных тестов
            }
            checkJUnitAnnotations(ast);
        }
    }

    // Проверка методов на использование AssertJ
    private void checkAssertMethod(DetailAST ast) {
        DetailAST child = ast.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.IDENT) {
                String methodName = child.getText();
                if (methodName.startsWith("assert") && !methodName.startsWith("assertThat")) {
                    log(ast.getLineNo(), "⚠️" + MSG_KEY + methodName);
                }
            }
            child = child.getNextSibling();
        }
    }

    // Проверка наличия JUnit аннотаций в классе
    private void checkJUnitAnnotations(DetailAST classAst) {
        if (!hasJUnitAnnotationsInClass(classAst) && !hasJUnitAnnotationsInMethods(classAst)) {
            log(classAst.getLineNo(), "⚠️ This class does not contain JUnit annotations."
                    + " Please add appropriate JUnit annotations.");
        }
    }

    private boolean hasJUnitAnnotationsInClass(DetailAST classAst) {
        return checkAnnotationsInModifiers(classAst, JUNIT_ANNOTATIONS);
    }

    private boolean hasJUnitAnnotationsInMethods(DetailAST classAst) {
        DetailAST objBlock = classAst.findFirstToken(TokenTypes.OBJBLOCK);
        if (objBlock == null) {
            return false;
        }

        DetailAST child = objBlock.getFirstChild();
        while (child != null) {
            if (child.getType() == TokenTypes.METHOD_DEF && hasJUnitAnnotationsInMethod(child)) {
                    return true;
                }

            child = child.getNextSibling();
        }
        return false;
    }

    private boolean hasJUnitAnnotationsInMethod(DetailAST methodAst) {
        DetailAST modifiers = methodAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }

        for (DetailAST annotation = modifiers.getFirstChild(); annotation != null;
             annotation = annotation.getNextSibling()) {
            if (annotation.getType() == TokenTypes.ANNOTATION) {
                String annotationName = getAnnotationName(annotation);
                if (JUNIT_ANNOTATIONS.contains(annotationName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkAnnotationsInModifiers(DetailAST classAst, List<String> annotations) {
        DetailAST modifiers = classAst.findFirstToken(TokenTypes.MODIFIERS);
        if (modifiers == null) {
            return false;
        }

        for (DetailAST annotation = modifiers.getFirstChild(); annotation != null;
             annotation = annotation.getNextSibling()) {
            if (annotation.getType() == TokenTypes.ANNOTATION) {
                String annotationName = getAnnotationName(annotation);
                if (annotations.contains(annotationName)) {
                    return true; // Если найдена хотя бы одна из нужных аннотаций
                }
            }
        }
        return false;
    }

    // Метод для извлечения имени аннотации
    private String getAnnotationName(DetailAST annotation) {
        DetailAST ident = annotation.findFirstToken(TokenTypes.IDENT);
        if (ident != null) {
            return ident.getText();
        }
        return "";
    }

    //Метод для определения, является ли класс интеграционным тестом
    private boolean isIntegrationTest(DetailAST classAst) {
        // Проверка на наличие аннотаций интеграционного теста
        return hasIntegrationTestAnnotations(classAst);
    }

    // Проверка наличия аннотаций для интеграционных тестов в классе
    private boolean hasIntegrationTestAnnotations(DetailAST classAst) {
        return checkAnnotationsInModifiers(classAst, INTEGRATION_TEST_ANNOTATIONS);
    }

    private boolean isTestClass(DetailAST classAst) {
        // Проверка наличия аннотаций тестов в классе
        return hasJUnitAnnotationsInClass(classAst) || hasJUnitAnnotationsInMethods(classAst);
    }

    // Этот метод определяет токены, которые должны быть обязательно обработаны, чтобы правило работало.
    @Override
    public int[] getRequiredTokens() {
        return new int[0];
    }

}
