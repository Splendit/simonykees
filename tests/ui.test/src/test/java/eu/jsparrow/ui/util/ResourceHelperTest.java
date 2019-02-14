package eu.jsparrow.ui.util;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ResourceHelperTest {

	String ruleId, expected;

	public ResourceHelperTest(String ruleId, String expected) {
		this.ruleId = ruleId;
		this.expected = expected;
	}

@SuppressWarnings("nls")
@Parameters(name = "Run {index}: RuleId={0}, Result={1}")
    public static Iterable<Object[]> data() throws Throwable
    {
        return Arrays.asList(new Object[][] {
        {"ArithmethicAssignment","https://jsparrow.github.io/rules/arithmethic-assignment.html"},
        {"BracketsToControl","https://jsparrow.github.io/rules/brackets-to-control.html"},
        {"CodeFormatter","https://jsparrow.github.io/rules/code-formatter.html"},
        {"DateDeprecated","https://jsparrow.github.io/rules/date-deprecated.html"},
        {"DiamondOperator","https://jsparrow.github.io/rules/diamond-operator.html"},
        {"EnhancedForLoopToStreamAnyMatch","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-any-match.html"},
        {"EnhancedForLoopToStreamFindFirst","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-find-first.html"},
        {"EnhancedForLoopToStreamForEach","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-for-each.html"},
        {"EnhancedForLoopToStreamSum","https://jsparrow.github.io/rules/enhanced-for-loop-to-stream-sum.html"},
        {"EnumsWithoutEquals","https://jsparrow.github.io/rules/enums-without-equals.html"},
        {"FieldRenaming","https://jsparrow.github.io/rules/field-renaming.html"},
        {"FlatMapInsteadOfNestedLoops","https://jsparrow.github.io/rules/flat-map-instead-of-nested-loops.html"},
        {"ForToForEach","https://jsparrow.github.io/rules/for-to-for-each.html"},
        {"FunctionalInterface","https://jsparrow.github.io/rules/functional-interface.html"},
        {"GuardCondition","https://jsparrow.github.io/rules/guard-condition.html"},
        {"ImmutableStaticFinalCollections","https://jsparrow.github.io/rules/immutable-static-final-collections.html"},
        {"IndexOfToContains","https://jsparrow.github.io/rules/index-of-to-contains.html"},
        {"InefficientConstructor","https://jsparrow.github.io/rules/inefficient-constructor.html"},
        {"LambdaForEachCollect","https://jsparrow.github.io/rules/lambda-for-each-collect.html"},
        {"LambdaForEachIfWrapperToFilter","https://jsparrow.github.io/rules/lambda-for-each-if-wrapper-to-filter.html"},
        {"LambdaForEachMap","https://jsparrow.github.io/rules/lambda-for-each-map.html"},
        {"LambdaToMethodReference","https://jsparrow.github.io/rules/lambda-to-method-reference.html"},
        {"MultiCatch","https://jsparrow.github.io/rules/multi-catch.html"},
        {"MultiVariableDeclarationLine","https://jsparrow.github.io/rules/multi-variable-declaration-line.html"},
        {"OptionalIfPresent","https://jsparrow.github.io/rules/optional-if-present.html"},
        {"OverrideAnnotation","https://jsparrow.github.io/rules/override-annotation.html"},
        {"PrimitiveBoxedForString","https://jsparrow.github.io/rules/primitive-boxed-for-string.html"},
        {"PrimitiveObjectUseEquals","https://jsparrow.github.io/rules/primitive-object-use-equals.html"},
        {"PutIfAbsent","https://jsparrow.github.io/rules/put-if-absent.html"},
        {"RearrangeClassMembers","https://jsparrow.github.io/rules/rearrange-class-members.html"},
        {"ReImplementingInterface","https://jsparrow.github.io/rules/re-implementing-interface.html"},
        {"RemoveEmptyStatement","https://jsparrow.github.io/rules/remove-empty-statement.html"},
        {"RemoveExplicitCallToSuper","https://jsparrow.github.io/rules/remove-explicit-call-to-super.html"},
        {"RemoveNewStringConstructor","https://jsparrow.github.io/rules/remove-new-string-constructor.html"},
        {"RemoveToStringOnString","https://jsparrow.github.io/rules/remove-to-string-on-string.html"},
        {"RemoveUnnecessaryThrows","https://jsparrow.github.io/rules/remove-unnecessary-throws.html"},
        {"StandardLogger","https://jsparrow.github.io/rules/standard-logger.html"},
        {"StatementLambdaToExpression","https://jsparrow.github.io/rules/statement-lambda-to-expression.html"},
        {"StringBufferToBuilder","https://jsparrow.github.io/rules/string-buffer-to-builder.html"},
        {"StringBuildingLoop","https://jsparrow.github.io/rules/string-building-loop.html"},
        {"StringConcatToPlus","https://jsparrow.github.io/rules/string-concat-to-plus.html"},
        {"StringFormatLineSeparator","https://jsparrow.github.io/rules/string-format-line-separator.html"},
        {"StringLiteralEqualityCheck","https://jsparrow.github.io/rules/string-literal-equality-check.html"},
        {"StringUtils","https://jsparrow.github.io/rules/string-utils.html"},
        {"TryWithResource","https://jsparrow.github.io/rules/try-with-resource.html"},
        {"UseIsEmptyOnCollections","https://jsparrow.github.io/rules/use-is-empty-on-collections.html"},
        {"UseStringBuilderAppend","https://jsparrow.github.io/rules/use-string-builder-append.html"},
        {"WhileToForEach","https://jsparrow.github.io/rules/while-to-for-each.html"},
        {"CollectionRemoveAll","https://jsparrow.github.io/rules/collection-remove-all.html"},
        {"LocalVariableTypeInference","https://jsparrow.github.io/rules/local-variable-type-inference.html"},
        {"OrganiseImports","https://jsparrow.github.io/rules/organise-imports.html"},
        {"RemoveDoubleNegationRule","https://jsparrow.github.io/rules/remove-double-negation-rule.html"},
        {"SerialVersionUid","https://jsparrow.github.io/rules/serial-version-uid.html"}
        });
    }

	@Test
	public void testTransformation() {
		String result = ResourceHelper.generateLinkToDocumentation("https://jsparrow.github.io/rules/", ruleId);
		Assert.assertEquals(expected, result);
	}

}
