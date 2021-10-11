package eu.jsparrow.rules.java16.javarecords;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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

		List<MethodDeclaration> constructorDeclarations = methodDeclarations.stream()
			.filter(MethodDeclaration::isConstructor)
			.collect(Collectors.toList());

		MethodDeclaration canonicConstructor = constructorDeclarations.stream()
			.filter(constructorDeclaration -> isCanonicConstructor(constructorDeclaration,
					recordComponentDataList))
			.findFirst()
			.orElse(null);

		if (canonicConstructor == null) {
			return Optional.empty();
		}

		if (!validateConstructorDeclarations(constructorDeclarations, canonicConstructor)) {
			return Optional.empty();
		}

		List<MethodDeclaration> methodDeclarationsToKeep = methodDeclarations.stream()
			.filter(methodDeclaration -> canonicConstructor != methodDeclaration &&
					!isRecordGetterToRemove(methodDeclaration))
			.collect(Collectors.toList());

		return Optional
			.of(new SupportedBodyDeclarations(recordComponentDataList, staticFields,
					methodDeclarationsToKeep));
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
	 * TODO: implement analysis whether the given constructor declaration
	 * fulfills the requirements on a canonic constructor declaration of a
	 * record.
	 */
	private boolean isCanonicConstructor(MethodDeclaration constructorDeclaration,
			List<RecordComponentData> recordComponentDataList) {
		return true;
	}

	/**
	 * TODO: implement validation, each non canonic constructor must call the
	 * canonic constructor of a record.
	 */
	private boolean validateConstructorDeclarations(List<MethodDeclaration> constructorDeclarations,
			MethodDeclaration canonicConstructor) {
		return true;
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
		return true;
	}
}
