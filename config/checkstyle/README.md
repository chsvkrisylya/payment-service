### Checkstyle summary

Этот проект использует Checkstyle для проверки стиля кода в файлах Java, Properties и XML. В этом файле конфигурации определены различные правила и проверки, которые помогут поддерживать единообразие и качество кода.

## Файлы конфигурации

### `checkstyle.xml`

Этот файл содержит основные настройки для Checkstyle и определяет, какие проверки должны быть применены к вашему коду.

#### Основные элементы конфигурации:

- **Исключение файлов**:
    - Исключает файлы с именем `module-info.java` из проверки с помощью модуля `BeforeExecutionExclusionFileFilter`.

- **Проверки стиля кода**:
    - `FileLength`: Проверяет длину файлов.
    - `TreeWalker`: Основной модуль для проверки кода. Включает:
        - `EmptyLineSeparator`: Проверяет правильное использование пустых строк.
        - `ConstantName`, `LocalVariableName`, `MemberName`, `MethodName`, `PackageName`, `ParameterName`, `StaticVariableName`, `TypeName`: Проверяет соглашения о наименовании.
        - `MethodLength`: Проверяет длину методов (максимум 100 строк).
        - `ParameterNumber`: Проверяет количество параметров методов.
        - `EmptyForIteratorPad`, `GenericWhitespace`, `MethodParamPad`, `NoWhitespaceAfter`, `NoWhitespaceBefore`, `OperatorWrap`, `ParenPad`, `TypecastParenPad`, `WhitespaceAfter`, `WhitespaceAround`: Проверяет пробелы и отступы.
        - `ModifierOrder`, `RedundantModifier`: Проверяет порядок и избыточность модификаторов.
        - `AvoidStarImport`, `RedundantImport`, `UnusedImports`: Проверяет импорты.
        - `AvoidNestedBlocks`, `LeftCurly`, `NeedBraces`, `RightCurly`: Проверяет оформление блоков кода.
        - `EmptyStatement`, `EqualsHashCode`, `IllegalInstantiation`, `InnerAssignment`, `MissingSwitchDefault`, `SimplifyBooleanExpression`, `SimplifyBooleanReturn`: Проверяет общие проблемы кодирования.
        - `InterfaceIsType`: Проверяет, что интерфейсы используются как типы.
        - `ArrayTypeStyle`, `TodoComment`, `UpperEll`, `EmptyBlock`: Прочие проверки.
        - `RegexpSingleline`: Проверяет наличие однострочных комментариев(не затрагивает Javadoc).
        - `RegexpMultiline`: Проверяет наличие многострочных комментариев(не затрагивает Javadoc).
        -  <property name="severity" value="error"/> Чтобы забилдить проект несмотря на синтаксические ошибки нужно поменять значение
<code> <property name="severity" value="error"/> </code> на value="warning" в файле checkstyle.xml

- **Проверка подавления**:
    - `SuppressionFilter`: Позволяет исключить определенные предупреждения на основе файла конфигурации `suppressions.xml`.

## Как использовать

1. **Добавьте файл `checkstyle.xml` в корень вашего проекта**.
2. **Настройте ваш IDE или сборочную систему (например, Maven или Gradle) для использования этой конфигурации Checkstyle**.
3. **Обновите файл `suppressions.xml` при необходимости для подавления конкретных предупреждений**.

## Документация

Для получения более подробной информации о каждой проверке и конфигурационном параметре, вы можете обратиться к [документации Checkstyle](https://checkstyle.sourceforge.io/config.html).

