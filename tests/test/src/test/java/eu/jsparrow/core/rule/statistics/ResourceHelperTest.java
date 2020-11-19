package eu.jsparrow.core.rule.statistics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.core.statistic.RuleDocumentationURLGeneratorUtil;


public class ResourceHelperTest {

    public static Stream<Arguments> urls() throws Throwable
    {
        return Stream.of(Arguments.of("ArithmethicAssignment","https://jsparrow.github.io/rules/arithmethic-assignment.html"),
        Arguments.of("BracketsToControl","https://jsparrow.github.io/rules/brackets-to-control.html"),
        Arguments.of("CodeFormatter","https://jsparrow.github.io/rules/code-formatter.html"),
        Arguments.of("DateDeprecated","https://jsparrow.github.io/rules/date-deprecated.html"),
        Arguments.of("DiamondOperator","https://jsparrow.github.io/rules/diamond-operator.html"),
        Arguments.of("EnhancedForLoopToStreamAnyMatch","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-any-match.html"),
        Arguments.of("EnhancedForLoopToStreamFindFirst","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-find-first.html"),
        Arguments.of("EnhancedForLoopToStreamForEach","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-for-each.html"),
        Arguments.of("EnhancedForLoopToStreamSum","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-sum.html"),
        Arguments.of("EnumsWithoutEquals","https://jsparrow.github.io/rules/enums-without-equals.html"),
        Arguments.of("FieldRenaming","https://jsparrow.github.io/rules/field-renaming.html"),
        Arguments.of("FlatMapInsteadOfNestedLoops","https://jsparrow.github.io/rules/flat-map-instead-of-nested-loops.html"),
        Arguments.of("ForToForEach","https://jsparrow.github.io/rules/for-to-for-each.html"),
        Arguments.of("FunctionalInterface","https://jsparrow.github.io/rules/functional-interface.html"),
        Arguments.of("GuardCondition","https://jsparrow.github.io/rules/guard-condition.html"),
        Arguments.of("ImmutableStaticFinalCollections","https://jsparrow.github.io/rules/immutable-static-final-collections.html"),
        Arguments.of("IndexOfToContains","https://jsparrow.github.io/rules/index-of-to-contains.html"),
        Arguments.of("InefficientConstructor","https://jsparrow.github.io/rules/inefficient-constructor.html"),
        Arguments.of("LambdaForEachCollect","https://jsparrow.github.io/rules/lambda-for-each-collect.html"),
        Arguments.of("LambdaForEachIfWrapperToFilter","https://jsparrow.github.io/rules/lambda-for-each-if-wrapper-to-filter.html"),
        Arguments.of("LambdaForEachMap","https://jsparrow.github.io/rules/lambda-for-each-map.html"),
        Arguments.of("LambdaToMethodReference","https://jsparrow.github.io/rules/lambda-to-method-reference.html"),
        Arguments.of("MultiCatch","https://jsparrow.github.io/rules/multi-catch.html"),
        Arguments.of("MultiVariableDeclarationLine","https://jsparrow.github.io/rules/multi-variable-declaration-line.html"),
        Arguments.of("OptionalIfPresent","https://jsparrow.github.io/rules/optional-if-present.html"),
        Arguments.of("OverrideAnnotation","https://jsparrow.github.io/rules/override-annotation.html"),
        Arguments.of("PrimitiveBoxedForString","https://jsparrow.github.io/rules/primitive-boxed-for-string.html"),
        Arguments.of("PrimitiveObjectUseEquals","https://jsparrow.github.io/rules/primitive-object-use-equals.html"),
        Arguments.of("PutIfAbsent","https://jsparrow.github.io/rules/put-if-absent.html"),
        Arguments.of("RearrangeClassMembers","https://jsparrow.github.io/rules/rearrange-class-members.html"),
        Arguments.of("ReImplementingInterface","https://jsparrow.github.io/rules/re-implementing-interface.html"),
        Arguments.of("RemoveEmptyStatement","https://jsparrow.github.io/rules/remove-empty-statement.html"),
        Arguments.of("RemoveExplicitCallToSuper","https://jsparrow.github.io/rules/remove-explicit-call-to-super.html"),
        Arguments.of("RemoveNewStringConstructor","https://jsparrow.github.io/rules/remove-new-string-constructor.html"),
        Arguments.of("RemoveToStringOnString","https://jsparrow.github.io/rules/remove-to-string-on-string.html"),
        Arguments.of("RemoveUnnecessaryThrows","https://jsparrow.github.io/rules/remove-unnecessary-throws.html"),
        Arguments.of("StandardLogger","https://jsparrow.github.io/rules/standard-logger.html"),
        Arguments.of("StatementLambdaToExpression","https://jsparrow.github.io/rules/statement-lambda-to-expression.html"),
        Arguments.of("StringBufferToBuilder","https://jsparrow.github.io/rules/string-buffer-to-builder.html"),
        Arguments.of("StringBuildingLoop","https://jsparrow.github.io/rules/string-building-loop.html"),
        Arguments.of("StringConcatToPlus","https://jsparrow.github.io/rules/string-concat-to-plus.html"),
        Arguments.of("StringFormatLineSeparator","https://jsparrow.github.io/rules/string-format-line-separator.html"),
        Arguments.of("StringLiteralEqualityCheck","https://jsparrow.github.io/rules/string-literal-equality-check.html"),
        Arguments.of("StringUtils","https://jsparrow.github.io/rules/string-utils.html"),
        Arguments.of("TryWithResource","https://jsparrow.github.io/rules/try-with-resource.html"),
        Arguments.of("UseIsEmptyOnCollections","https://jsparrow.github.io/rules/use-is-empty-on-collections.html"),
        Arguments.of("UseStringBuilderAppend","https://jsparrow.github.io/rules/use-string-builder-append.html"),
        Arguments.of("WhileToForEach","https://jsparrow.github.io/rules/while-to-for-each.html"),
        Arguments.of("CollectionRemoveAll","https://jsparrow.github.io/rules/collection-remove-all.html"),
        Arguments.of("LocalVariableTypeInference","https://jsparrow.github.io/rules/local-variable-type-inference.html"),
        Arguments.of("OrganiseImports","https://jsparrow.github.io/rules/organise-imports.html"),
        Arguments.of("RemoveDoubleNegationRule","https://jsparrow.github.io/rules/remove-double-negation-rule.html"),
        Arguments.of("SerialVersionUid","https://jsparrow.github.io/rules/serial-version-uid.html")
        );
    }

	@ParameterizedTest(name = "Run {index}: RuleId={0}, Result={1}")
	@MethodSource("urls")
	public void testTransformation(String ruleId, String expected) {
		String result = RuleDocumentationURLGeneratorUtil.generateLinkToDocumentation(ruleId);
		assertEquals(expected, result);
	}
}
