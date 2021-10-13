package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * @since 4.4.0
 */
public class UseJavaRecordsASTVisitor extends AbstractASTRewriteASTVisitor {

	/**
	 * Prototype with incomplete validation
	 */
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {

		SupportedBodyDeclarations supportedBodyDeclarations = collectSupportedBodyDeclarations(typeDeclaration)
			.orElse(null);

		if (supportedBodyDeclarations != null) {
			transform(typeDeclaration, supportedBodyDeclarations);
		}

		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void transform(TypeDeclaration typeDeclarationToReplace,
			SupportedBodyDeclarations supportedBodyDeclarations) {
		AST ast = astRewrite.getAST();
		RecordDeclaration recordDeclaration = ast.newRecordDeclaration();
		SimpleName recordName = (SimpleName) astRewrite.createCopyTarget(typeDeclarationToReplace.getName());
		recordDeclaration.setName(recordName);
		List recordComponents = recordDeclaration.recordComponents();
		supportedBodyDeclarations.getRecordComponentDataList()
			.forEach(recordComponentData -> {
				SingleVariableDeclaration component = ast.newSingleVariableDeclaration();
				Type componentType = (Type) astRewrite.createCopyTarget(recordComponentData.getType());
				SimpleName componentName = (SimpleName) astRewrite.createCopyTarget(recordComponentData.getName());
				component.setType(componentType);
				component.setName(componentName);
				recordComponents.add(component);
			});

		List recordBodyDeclarations = recordDeclaration.bodyDeclarations();
		supportedBodyDeclarations.getRecordBodyDeclarations()
			.forEach(bodyDeclaration -> {
				recordBodyDeclarations.add(astRewrite.createCopyTarget(bodyDeclaration));
			});
		astRewrite.replace(typeDeclarationToReplace, recordDeclaration, null);
		onRewrite();
	}

	private Optional<SupportedBodyDeclarations> collectSupportedBodyDeclarations(TypeDeclaration typeDeclaration) {

		List<FieldDeclaration> fieldDeclarations = ASTNodeUtil.convertToTypedList(typeDeclaration.bodyDeclarations(),
				FieldDeclaration.class);

		List<FieldDeclaration> privateFinalInstanceFields = fieldDeclarations.stream()
			.filter(this::isPrivateFinalInstanceField)
			.collect(Collectors.toList());

		List<FieldDeclaration> staticFields = fieldDeclarations.stream()
			.filter(fieldDeclaration -> Modifier.isStatic(fieldDeclaration.getModifiers()))
			.collect(Collectors.toList());

		List<MethodDeclaration> methodDeclarations = ASTNodeUtil.convertToTypedList(typeDeclaration.bodyDeclarations(),
				MethodDeclaration.class);

		int allBodyDeclarationsSize = typeDeclaration.bodyDeclarations()
			.size();

		if (allBodyDeclarationsSize > privateFinalInstanceFields.size() + staticFields.size()
				+ methodDeclarations.size()) {
			return Optional.empty();
		}

		List<RecordComponentData> recordComponentDataList = collectRecordComponentData(privateFinalInstanceFields);

		ConstructorDeclarationsAnalyzer constructorDeclarationsAnalyzer = new ConstructorDeclarationsAnalyzer(
				typeDeclaration, recordComponentDataList);

		if (!constructorDeclarationsAnalyzer.analyzeConstructors()) {
			return Optional.empty();
		}

		List<BodyDeclaration> recordBodyDeclarations = new ArrayList<>();
		recordBodyDeclarations.addAll(staticFields);
		recordBodyDeclarations.addAll(methodDeclarations);

		constructorDeclarationsAnalyzer.getCanonicalConstructorToRemove()
			.ifPresent(recordBodyDeclarations::remove);
		methodDeclarations
			.stream()
			.filter(this::isRecordGetterToRemove)
			.forEach(recordBodyDeclarations::remove);

		return Optional
			.of(new SupportedBodyDeclarations(recordComponentDataList, recordBodyDeclarations));
	}

	private List<RecordComponentData> collectRecordComponentData(List<FieldDeclaration> privateFinalInstanceFields) {
		List<RecordComponentData> recordComponentDataList = new ArrayList<>();
		privateFinalInstanceFields.forEach(fieldDeclaration -> {
			ASTNodeUtil.convertToTypedList(fieldDeclaration.fragments(), VariableDeclarationFragment.class)
				.stream()
				.forEach(variableDeclarationFragment -> recordComponentDataList
					.add(new RecordComponentData(fieldDeclaration, variableDeclarationFragment)));
		});
		return recordComponentDataList;
	}

	private boolean isPrivateFinalInstanceField(FieldDeclaration fieldDeclaration) {
		int modifiers = fieldDeclaration.getModifiers();
		return !Modifier.isStatic(modifiers) && Modifier.isPrivate(modifiers) && Modifier.isFinal(modifiers);
	}

	/**
	 * TODO: implement analysis whether MethodDeclaration is a record getter
	 * which can be removed like for example:
	 * 
	 * <pre>
	 * public int x() {
	 * 	return x;
	 * }	 *
	 * </pre>
	 * 
	 * @param methodDeclaration
	 * @return
	 */
	private boolean isRecordGetterToRemove(MethodDeclaration methodDeclaration) {
		return !methodDeclaration.isConstructor();
	}
}
