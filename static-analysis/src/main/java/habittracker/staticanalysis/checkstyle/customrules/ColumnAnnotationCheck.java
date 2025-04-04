package habittracker.staticanalysis.checkstyle.customrules;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

public class ColumnAnnotationCheck extends AbstractCheck {

    // Сообщение об ошибке, если аннотация @Column отсутствует
    private static final String MSG_MISSING_COLUMN = "Поле должно быть аннотировано @Column с указанием атрибута.";

    // Аннотации, которые исключают проверку на @Column
    private static final String[] EXCLUDED_ANNOTATIONS = {
            "Id",          // @Id
            "JoinColumn",  // @JoinColumn
            "OneToMany",   // @OneToMany
            "ManyToOne",   // @ManyToOne
            "OneToOne",    // @OneToOne
            "ManyToMany",  // @ManyToMany
            "Transient",   // @Transient
            "Embedded",    // @Embedded
            "EmbeddedId"   // @EmbeddedId
    };

    @Override
    public int[] getDefaultTokens() {
        // Проверяем только поля класса
        return new int[]{TokenTypes.VARIABLE_DEF};
    }

    @Override
    public void visitToken(DetailAST ast) {
        // Проверяем, что токен является полем класса, а не локальной переменной
        if (!isField(ast)) {
            return; // Пропускаем локальные переменные
        }

        // Получаем родительский класс поля
        DetailAST classDef = getClassDef(ast);

        // Проверяем, что класс аннотирован @Entity
        if (classDef != null && isEntityClass(classDef)) {
            // Проверяем, есть ли у поля исключающие аннотации
            if (hasExcludedAnnotations(ast)) {
                // Если есть исключающие аннотации, пропускаем проверку
                return;
            }

            // Проверяем, есть ли у поля аннотация @Column
            if (!AnnotationUtil.containsAnnotation(ast, "Column")) {
                log(ast.getLineNo(), MSG_MISSING_COLUMN);
            } else {
                // Проверяем, что у аннотации @Column есть хотя бы один атрибут
                DetailAST annotationAST = AnnotationUtil.getAnnotation(ast, "Column");
                if (annotationAST != null && !hasAttributes(annotationAST)) {
                    log(ast.getLineNo(), MSG_MISSING_COLUMN);
                }
            }
        }
    }

    /**
     * Проверяет, является ли токен полем класса, а не локальной переменной.
     * ast Токен, представляющий переменную.
     * return true, если это поле класса, иначе false.
     */
    private boolean isField(DetailAST ast) {
        DetailAST parent = ast.getParent();
        while (parent != null) {
            if (parent.getType() == TokenTypes.OBJBLOCK) {
                // Если родительский узел - OBJBLOCK, это поле класса
                return true;
            }
            if (parent.getType() == TokenTypes.SLIST) {
                // Если родительский узел - SLIST, это локальная переменная
                return false;
            }
            parent = parent.getParent();
        }
        return false;
    }

    /**
     * Проверяет, есть ли у поля аннотации, исключающие проверку на @Column.
     * параметр ast Токен, представляющий поле.
     * return true, если у поля есть исключающие аннотации, иначе false.
     */
    private boolean hasExcludedAnnotations(DetailAST ast) {
        for (String annotation : EXCLUDED_ANNOTATIONS) {
            if (AnnotationUtil.containsAnnotation(ast, annotation)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Получает AST класса, к которому принадлежит поле.
     * параметр ast Токен, представляющий поле.
     * return AST класса или null, если класс не найден.
     */
    private DetailAST getClassDef(DetailAST ast) {
        DetailAST classDef = ast;
        while (classDef != null && classDef.getType() != TokenTypes.CLASS_DEF) {
            classDef = classDef.getParent();
        }
        return classDef;
    }

    /**
     * Проверяет, что класс аннотирован @Entity.
     * параметр classDef AST класса.
     * return true, если класс аннотирован @Entity, иначе false.
     */
    private boolean isEntityClass(DetailAST classDef) {
        return AnnotationUtil.containsAnnotation(classDef, "Entity");
    }

    /**
     * Проверяет, есть ли у аннотации атрибуты.
     * параметр annotationAST AST аннотации.
     * return true, если у аннотации есть атрибуты, иначе false.
     */
    private boolean hasAttributes(DetailAST annotationAST) {
        DetailAST annotationParameters = annotationAST.findFirstToken(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
        return annotationParameters != null;
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }
}