package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.rules.common.util.ClassRelationUtil.isContentOfType;
import static eu.jsparrow.rules.common.util.ClassRelationUtil.isInheritingContentOfTypes;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.markers.common.RemoveUnnecessaryThrownExceptionsEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Removes the following thrown exceptions from the method signature:
 * 
 * <ul>
 * <li>Exceptions that are subtypes of already thrown exceptions</li>
 * <li>Exceptions that are thrown more than once</li>
 * <li>Exceptions that are inheriting from {@link RuntimeException}</li>
 * </ul>
 * 
 * @since 2.7.0
 *
 */
public class RemoveUnnecessaryThrownExceptionsASTVisitor extends AbstractASTRewriteASTVisitor implements RemoveUnnecessaryThrownExceptionsEvent {

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {

		List<Type> thrownTypes = ASTNodeUtil.convertToTypedList(methodDeclaration.thrownExceptionTypes(), Type.class);
		if (thrownTypes.isEmpty()) {
			return true;
		}

		// This is to avoid resolving type bindings more than once
		Map<Type, ITypeBinding> typeBindingsMap = computeTypeBindingMaps(thrownTypes);
		if (typeBindingsMap.isEmpty()) {
			return true;
		}

		List<Type> toBeRemoved = new ArrayList<>();

		List<Type> runtimeExceptions = findRuntimeExceptions(typeBindingsMap);
		toBeRemoved.addAll(runtimeExceptions);

		List<Type> duplications = findDuplications(typeBindingsMap);
		toBeRemoved.addAll(duplications);

		List<Type> thrownSubtypes = findThrownSubtypes(typeBindingsMap);
		toBeRemoved.addAll(thrownSubtypes);

		for (Type type : toBeRemoved) {
			astRewrite.remove(type, null);
			onRewrite();
			addMarkerEvent(type);
		}

		return true;
	}

	private List<Type> findRuntimeExceptions(Map<Type, ITypeBinding> typeBindingsMap) {
		return typeBindingsMap.entrySet()
			.stream()
			.filter(entry -> isRuntimeException(entry.getValue()))
			.map(Map.Entry::getKey)
			.collect(Collectors.toList());
	}

	private boolean isRuntimeException(ITypeBinding typeBinding) {
		String runtimeExceptionName = java.lang.RuntimeException.class.getName();
		return isContentOfType(typeBinding, runtimeExceptionName)
				|| isInheritingContentOfTypes(typeBinding, singletonList(runtimeExceptionName));
	}

	private List<Type> findThrownSubtypes(Map<Type, ITypeBinding> typeBindingsMap) {
		List<Type> toBeRemoved = new ArrayList<>();
		for (Map.Entry<Type, ITypeBinding> entry : typeBindingsMap.entrySet()) {
			Type type = entry.getKey();
			ITypeBinding typeBinding = entry.getValue();

			if (!toBeRemoved.contains(type)) {
				typeBindingsMap.entrySet()
					.stream()
					.filter(e -> type != e.getKey())
					.filter(e -> isInheritingContentOfTypes(typeBinding, singletonList(e.getValue()
						.getQualifiedName())))
					.findFirst()
					.map(e -> type)
					.ifPresent(toBeRemoved::add);
			}
		}
		return toBeRemoved;
	}

	private List<Type> findDuplications(Map<Type, ITypeBinding> typeBindingsMap) {
		List<Type> duplications = new ArrayList<>();
		for (Map.Entry<Type, ITypeBinding> type : typeBindingsMap.entrySet()) {
			if (!duplications.contains(type.getKey())) {
				List<Type> equivalentOccurrences = findEquivalentOccurrences(type.getKey(), type.getValue(), typeBindingsMap);
				duplications.addAll(equivalentOccurrences);
			}
		}
		return duplications;
	}

	private List<Type> findEquivalentOccurrences(Type type, ITypeBinding typeBinding, Map<Type, ITypeBinding> typeBindingsMap) {
		List<Type> equivalentTypes = new ArrayList<>();
		for (Map.Entry<Type, ITypeBinding> entry : typeBindingsMap.entrySet()) {
			Type key = entry.getKey();
			if (key != type) {
				ITypeBinding keyBinding = entry.getValue();
				if (isContentOfType(typeBinding, keyBinding.getQualifiedName())) {
					equivalentTypes.add(key);
				}
			}
		}
		return equivalentTypes;
	}

	private Map<Type, ITypeBinding> computeTypeBindingMaps(List<Type> thrownTypes) {
		Map<Type, ITypeBinding> typeBindingMap = new LinkedHashMap<>();
		for (Type type : thrownTypes) {
			ITypeBinding binding = type.resolveBinding();
			if (binding == null) {
				return emptyMap();
			}
			typeBindingMap.put(type, binding);
		}
		return typeBindingMap;
	}
}
