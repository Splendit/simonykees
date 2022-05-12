/*******************************************************************************
* Copyright (c) 2019 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package eu.jsparrow.jdt.ls.core.internal.corrections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.manipulation.CleanUpContextCore;
import org.eclipse.jdt.core.manipulation.CleanUpOptionsCore;
import org.eclipse.jdt.core.manipulation.CleanUpRequirementsCore;
import org.eclipse.jdt.core.manipulation.ICleanUpFixCore;
import org.eclipse.jdt.internal.corext.dom.ASTNodes;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.fix.CleanUpConstants;
import org.eclipse.jdt.internal.corext.fix.ConvertLoopFixCore;
import org.eclipse.jdt.internal.corext.fix.ICleanUpCore;
import org.eclipse.jdt.internal.corext.fix.IProposableFix;
import org.eclipse.jdt.internal.corext.fix.LambdaExpressionsFixCore;
import org.eclipse.jdt.internal.corext.fix.LinkedProposalModelCore;
import org.eclipse.jdt.internal.corext.refactoring.code.ConvertAnonymousToNestedRefactoring;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.fix.AbstractCleanUpCore;
import org.eclipse.jdt.internal.ui.fix.LambdaExpressionsCleanUpCore;
import org.eclipse.jdt.internal.ui.fix.MultiFixMessages;
import org.eclipse.jdt.internal.ui.text.correction.IProblemLocationCore;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.text.edits.TextEdit;

import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.ASTRewriteCorrectionProposal;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.CUCorrectionProposal;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.ChangeCorrectionProposal;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.FixCorrectionProposal;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.IProposalRelevance;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.RefactoringCorrectionProposal;
import eu.jsparrow.jdt.ls.core.internal.corrections.proposals.TypeChangeCorrectionProposal;
import eu.jsparrow.jdt.ls.core.internal.preferences.PreferenceManager;
import eu.jsparrow.jdt.ls.core.internal.text.correction.RefactorProposalUtility;
import eu.jsparrow.jdt.ls.core.internal.text.correction.RefactoringCorrectionCommandProposal;

/**
 * RefactorProcessor
 */
public class RefactorProcessor {
	public static final String CONVERT_ANONYMOUS_CLASS_TO_NESTED_COMMAND = "convertAnonymousClassToNestedCommand";

	private PreferenceManager preferenceManager;

	public RefactorProcessor(PreferenceManager preferenceManager) {
		this.preferenceManager = preferenceManager;
	}

	public List<ChangeCorrectionProposal> getProposals(CodeActionParams params, IInvocationContext context, IProblemLocationCore[] locations) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode != null) {
			ArrayList<ChangeCorrectionProposal> proposals = new ArrayList<>();

			InvertBooleanUtility.getInverseConditionProposals(params, context, coveringNode, proposals);
			getInverseLocalVariableProposals(params, context, coveringNode, proposals);

			getMoveRefactoringProposals(params, context, coveringNode, proposals);

			boolean noErrorsAtLocation = noErrorsAtLocation(locations, coveringNode);
			if (noErrorsAtLocation) {

				getConvertAnonymousClassCreationsToLambdaProposals(context, coveringNode, proposals);
				getConvertLambdaToAnonymousClassCreationsProposals(context, coveringNode, proposals);

				getConvertResolvedTypeToVarTypeProposal(context, coveringNode, proposals);

				getConvertForLoopProposal(context, coveringNode, proposals);

			}
			return proposals;
		}
		return Collections.emptyList();
	}

	

	private boolean getInverseLocalVariableProposals(CodeActionParams params, IInvocationContext context, ASTNode covering, Collection<ChangeCorrectionProposal> proposals) {
		if (proposals == null) {
			return false;
		}

		ChangeCorrectionProposal proposal = null;
		if (this.preferenceManager.getClientPreferences().isAdvancedExtractRefactoringSupported()) {
			proposal = InvertBooleanUtility.getInvertVariableProposal(params, context, covering, true /*returnAsCommand*/);
		} else {
			proposal = InvertBooleanUtility.getInvertVariableProposal(params, context, covering, false /*returnAsCommand*/);
		}

		if (proposal == null) {
			return false;
		}

		proposals.add(proposal);
		return true;
	}

	private boolean getMoveRefactoringProposals(CodeActionParams params, IInvocationContext context, ASTNode coveringNode, ArrayList<ChangeCorrectionProposal> resultingCollections) {
		if (resultingCollections == null) {
			return false;
		}

		if (this.preferenceManager.getClientPreferences().isMoveRefactoringSupported()) {
			List<CUCorrectionProposal> newProposals = RefactorProposalUtility.getMoveRefactoringProposals(params, context);
			if (newProposals != null && !newProposals.isEmpty()) {
				resultingCollections.addAll(newProposals);
				return true;
			}
		}

		return false;

	}

	static boolean noErrorsAtLocation(IProblemLocationCore[] locations, ASTNode coveringNode) {
		if (locations != null) {
			int start = coveringNode.getStartPosition();
			int length = coveringNode.getLength();
			for (int i = 0; i < locations.length; i++) {
				IProblemLocationCore location = locations[i];
				if (location.getOffset() > start + length || (location.getOffset() + location.getLength()) < start) {
					continue;
				}
				if (location.isError()) {
					if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(location.getMarkerType()) && JavaCore.getOptionForConfigurableSeverity(location.getProblemId()) != null) {
						// continue (only drop out for severe (non-optional) errors)
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}


	public static RefactoringCorrectionProposal getConvertAnonymousToNestedProposal(CodeActionParams params, IInvocationContext context, final ASTNode node, boolean returnAsCommand) throws CoreException {
		String label = CorrectionMessages.QuickAssistProcessor_convert_anonym_to_nested;
		ClassInstanceCreation cic = getClassInstanceCreation(node);
		if (cic == null) {
			return null;
		}
		final AnonymousClassDeclaration anonymTypeDecl = cic.getAnonymousClassDeclaration();
		if (anonymTypeDecl == null || anonymTypeDecl.resolveBinding() == null) {
			return null;
		}

		final ConvertAnonymousToNestedRefactoring refactoring = new ConvertAnonymousToNestedRefactoring(anonymTypeDecl);
		if (!refactoring.checkInitialConditions(new NullProgressMonitor()).isOK()) {
			return null;
		}

		if (returnAsCommand) {
			return new RefactoringCorrectionCommandProposal(label, CodeActionKind.Refactor, context.getCompilationUnit(), IProposalRelevance.CONVERT_ANONYMOUS_TO_NESTED, RefactorProposalUtility.APPLY_REFACTORING_COMMAND_ID,
					Arrays.asList(CONVERT_ANONYMOUS_CLASS_TO_NESTED_COMMAND, params));
		}

		String extTypeName = ASTNodes.getTypeName(cic.getType());
		ITypeBinding anonymTypeBinding = anonymTypeDecl.resolveBinding();
		String className;
		if (anonymTypeBinding.getInterfaces().length == 0) {
			className = Messages.format(CorrectionMessages.QuickAssistProcessor_name_extension_from_interface, extTypeName);
		} else {
			className = Messages.format(CorrectionMessages.QuickAssistProcessor_name_extension_from_class, extTypeName);
		}
		String[][] existingTypes = ((IType) anonymTypeBinding.getJavaElement()).resolveType(className);
		int i = 1;
		while (existingTypes != null) {
			i++;
			existingTypes = ((IType) anonymTypeBinding.getJavaElement()).resolveType(className + i);
		}
		refactoring.setClassName(i == 1 ? className : className + i);

		LinkedProposalModelCore linkedProposalModel = new LinkedProposalModelCore();
		refactoring.setLinkedProposalModel(linkedProposalModel);

		final ICompilationUnit cu = context.getCompilationUnit();
		RefactoringCorrectionProposal proposal = new RefactoringCorrectionProposal(label, CodeActionKind.Refactor, cu, refactoring, IProposalRelevance.CONVERT_ANONYMOUS_TO_NESTED);
		proposal.setLinkedProposalModel(linkedProposalModel);
		return proposal;
	}

	private static ClassInstanceCreation getClassInstanceCreation(ASTNode node) {
		while (node instanceof Name || node instanceof Type || node instanceof Dimension || node.getParent() instanceof MethodDeclaration
				|| node.getLocationInParent() == AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY) {
			node = node.getParent();
		}

		if (node instanceof ClassInstanceCreation) {
			return (ClassInstanceCreation) node;
		} else if (node.getLocationInParent() == ClassInstanceCreation.ANONYMOUS_CLASS_DECLARATION_PROPERTY) {
			return (ClassInstanceCreation) node.getParent();
		} else {
			return null;
		}
	}

	private static boolean getConvertAnonymousClassCreationsToLambdaProposals(IInvocationContext context, ASTNode covering, Collection<ChangeCorrectionProposal> resultingCollections) {
		ClassInstanceCreation cic = getClassInstanceCreation(covering);
		if (cic == null) {
			return false;
		}

		/*
		 * TODO: 
		 * 1. Replace the LambdaExpressionsFixCore with a jSparrow Resolver Instance.
		 * 2. How to create a FixCorrectionProposal with from the results of a resolver.  
		 * 		-> Go through all the methods defined in FixCorrectionProposals and its parents. 
		 * 		-> study all the methods that are related to textEdits, document changes, positions, etc,
		 * 		-> Is it more convenient for us to use ASTRewriteCorrectionProposal? 
		 * 
		 */
		FunctionalInterfaceResolver r = new FunctionalInterfaceResolver(node -> true);
		CompilationUnit root = context.getASTRoot();
		ASTRewrite rewrite = ASTRewrite.create(root.getAST());
		r.setASTRewrite(rewrite);
		cic.accept(r);
		
		try {
			TextEdit edits = rewrite.rewriteAST();
			if(!edits.hasChildren()) { // FIXME: find a better way to check whether there was a finding or not. 
				return false;
			}
		} catch (JavaModelException | IllegalArgumentException e) {
			e.printStackTrace();
		}
		
		ASTRewriteCorrectionProposal rewriteProposal = new ASTRewriteCorrectionProposal(
				r.getDescription().getName(), 
				CodeActionKind.Refactor, 
				context.getCompilationUnit(), 
				rewrite, 
				IProposalRelevance.CONVERT_TO_LAMBDA_EXPRESSION);

		if (resultingCollections == null) {
			return true;
		}

		resultingCollections.add(rewriteProposal);
		return true;
	}

	private static boolean getConvertLambdaToAnonymousClassCreationsProposals(IInvocationContext context, ASTNode covering, Collection<ChangeCorrectionProposal> resultingCollections) {
		if (resultingCollections == null) {
			return true;
		}

		LambdaExpression lambda;
		if (covering instanceof LambdaExpression) {
			lambda = (LambdaExpression) covering;
		} else if (covering.getLocationInParent() == LambdaExpression.BODY_PROPERTY) {
			lambda = (LambdaExpression) covering.getParent();
		} else {
			return false;
		}

		IProposableFix fix = LambdaExpressionsFixCore.createConvertToAnonymousClassCreationsFix(lambda);
		if (fix == null) {
			return false;
		}

		// add correction proposal
		Map<String, String> options = new HashMap<>();
		options.put(CleanUpConstants.CONVERT_FUNCTIONAL_INTERFACES, CleanUpOptionsCore.TRUE);
		options.put(CleanUpConstants.USE_ANONYMOUS_CLASS_CREATION, CleanUpOptionsCore.TRUE);
		FixCorrectionProposal proposal = new FixCorrectionProposal(fix, new LambdaExpressionsCleanUpCore(options), IProposalRelevance.CONVERT_TO_ANONYMOUS_CLASS_CREATION, context, CodeActionKind.Refactor);
		resultingCollections.add(proposal);
		return true;
	}

	
	private static SimpleName getSimpleNameForVariable(ASTNode node) {
		if (!(node instanceof SimpleName)) {
			return null;
		}
		SimpleName name = (SimpleName) node;
		if (!name.isDeclaration()) {
			while (node instanceof Name || node instanceof Type) {
				node = node.getParent();
			}
			if (node instanceof VariableDeclarationStatement) {
				List<VariableDeclarationFragment> fragments = ((VariableDeclarationStatement) node).fragments();
				if (fragments.size() > 0) {
					// var is not allowed in a compound declaration
					name = fragments.get(0).getName();
				}
			}
		}
		return name;
	}

	private static boolean getConvertResolvedTypeToVarTypeProposal(IInvocationContext context, ASTNode node, Collection<ChangeCorrectionProposal> proposals) {
		CompilationUnit astRoot = context.getASTRoot();
		IJavaElement root = astRoot.getJavaElement();
		if (root == null) {
			return false;
		}
		IJavaProject javaProject = root.getJavaProject();
		if (javaProject == null) {
			return false;
		}
		if (!JavaModelUtil.is10OrHigher(javaProject)) {
			return false;
		}

		SimpleName name = getSimpleNameForVariable(node);
		if (name == null) {
			return false;
		}

		IBinding binding = name.resolveBinding();
		if (!(binding instanceof IVariableBinding)) {
			return false;
		}
		IVariableBinding varBinding = (IVariableBinding) binding;
		if (varBinding.isField() || varBinding.isParameter()) {
			return false;
		}

		ASTNode varDeclaration = astRoot.findDeclaringNode(varBinding);
		if (varDeclaration == null) {
			return false;
		}

		Type type = null;
		Expression expression = null;

		ITypeBinding typeBinding = varBinding.getType();
		if (typeBinding == null) {
			return false;
		}
		ITypeBinding expressionTypeBinding = null;

		if (varDeclaration instanceof SingleVariableDeclaration) {
			SingleVariableDeclaration svDecl = (SingleVariableDeclaration) varDeclaration;
			type = svDecl.getType();
			expression = svDecl.getInitializer();
			if (expression != null) {
				expressionTypeBinding = expression.resolveTypeBinding();
			} else {
				ASTNode parent = svDecl.getParent();
				if (parent instanceof EnhancedForStatement) {
					EnhancedForStatement efStmt = (EnhancedForStatement) parent;
					expression = efStmt.getExpression();
					if (expression != null) {
						ITypeBinding expBinding = expression.resolveTypeBinding();
						if (expBinding != null) {
							if (expBinding.isArray()) {
								expressionTypeBinding = expBinding.getElementType();
							} else {
								ITypeBinding iterable = Bindings.findTypeInHierarchy(expBinding, "java.lang.Iterable"); //$NON-NLS-1$
								if (iterable != null) {
									ITypeBinding[] typeArguments = iterable.getTypeArguments();
									if (typeArguments.length == 1) {
										expressionTypeBinding = typeArguments[0];
										expressionTypeBinding = Bindings.normalizeForDeclarationUse(expressionTypeBinding, context.getASTRoot().getAST());
									}
								}
							}
						}
					}
				}
			}
		} else if (varDeclaration instanceof VariableDeclarationFragment) {
			ASTNode parent = varDeclaration.getParent();
			expression = ((VariableDeclarationFragment) varDeclaration).getInitializer();
			if (expression != null) {
				expressionTypeBinding = expression.resolveTypeBinding();
			}
			if (parent instanceof VariableDeclarationStatement) {
				type = ((VariableDeclarationStatement) parent).getType();
			} else if (parent instanceof VariableDeclarationExpression) {
				VariableDeclarationExpression varDecl = (VariableDeclarationExpression) parent;
				// cannot convert a VariableDeclarationExpression with multiple fragments to var.
				if (varDecl.fragments().size() > 1) {
					return false;
				}
				type = varDecl.getType();
			}
		}

		if (type == null || type.isVar()) {
			return false;
		}
		if (expression == null || expression instanceof ArrayInitializer || expression instanceof LambdaExpression || expression instanceof MethodReference) {
			return false;
		}
		if (expressionTypeBinding == null || !expressionTypeBinding.isEqualTo(typeBinding)) {
			return false;
		}

		TypeChangeCorrectionProposal proposal = new TypeChangeCorrectionProposal(context.getCompilationUnit(), varBinding, astRoot, typeBinding, IProposalRelevance.CHANGE_VARIABLE);
		proposal.setKind(CodeActionKind.Refactor);
		proposals.add(proposal);
		return true;
	}

	private static boolean getConvertForLoopProposal(IInvocationContext context, ASTNode node, Collection<ChangeCorrectionProposal> resultingCollections) {
		ForStatement forStatement = getEnclosingForStatementHeader(node);
		if (forStatement == null) {
			return false;
		}
		if (resultingCollections == null) {
			return true;
		}
		IProposableFix fix = ConvertLoopFixCore.createConvertForLoopToEnhancedFix(context.getASTRoot(), forStatement);
		if (fix == null) {
			return false;
		}
		Map<String, String> options = new HashMap<>();
		options.put(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED, CleanUpOptionsCore.TRUE);
		ICleanUpCore cleanUp = new AbstractCleanUpCore(options) {
			@Override
			public CleanUpRequirementsCore getRequirementsCore() {
				return new CleanUpRequirementsCore(isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED), false, false, null);
			}

			@Override
			public ICleanUpFixCore createFixCore(CleanUpContextCore context) throws CoreException {
				CompilationUnit compilationUnit = context.getAST();
				if (compilationUnit == null) {
					return null;
				}
				boolean convertForLoops = isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED);
				boolean checkIfLoopVarUsed = isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_ONLY_IF_LOOP_VAR_USED);
				return ConvertLoopFixCore.createCleanUp(compilationUnit, convertForLoops, convertForLoops,
						isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL) && isEnabled(CleanUpConstants.VARIABLE_DECLARATIONS_USE_FINAL_LOCAL_VARIABLES), checkIfLoopVarUsed);
			}

			@Override
			public String[] getStepDescriptions() {
				List<String> result = new ArrayList<>();
				if (isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED)) {
					result.add(MultiFixMessages.Java50CleanUp_ConvertToEnhancedForLoop_description);
					if (isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_ONLY_IF_LOOP_VAR_USED)) {
						result.add(MultiFixMessages.Java50CleanUp_ConvertLoopOnlyIfLoopVarUsed_description);
					}
				}
				return result.toArray(new String[result.size()]);
			}

			@Override
			public String getPreview() {
				StringBuilder buf = new StringBuilder();
				if (isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED)) {
					buf.append("for (int element : ids) {\n"); //$NON-NLS-1$
					buf.append("    double value= element / 2; \n"); //$NON-NLS-1$
					buf.append("    System.out.println(value);\n"); //$NON-NLS-1$
					buf.append("}\n"); //$NON-NLS-1$
				} else {
					buf.append("for (int i = 0; i < ids.length; i++) {\n"); //$NON-NLS-1$
					buf.append("    double value= ids[i] / 2; \n"); //$NON-NLS-1$
					buf.append("    System.out.println(value);\n"); //$NON-NLS-1$
					buf.append("}\n"); //$NON-NLS-1$
				}
				if (isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_TO_ENHANCED) && !isEnabled(CleanUpConstants.CONTROL_STATMENTS_CONVERT_FOR_LOOP_ONLY_IF_LOOP_VAR_USED)) {
					buf.append("for (int id : ids) {\n"); //$NON-NLS-1$
					buf.append("    System.out.println(\"here\");\n"); //$NON-NLS-1$
					buf.append("}\n"); //$NON-NLS-1$
				} else {
					buf.append("for (int i = 0; i < ids.length; i++) {\n"); //$NON-NLS-1$
					buf.append("    System.out.println(\"here\");\n"); //$NON-NLS-1$
					buf.append("}\n"); //$NON-NLS-1$
				}
				return buf.toString();
			}
		};
		FixCorrectionProposal proposal = new FixCorrectionProposal(fix, cleanUp, IProposalRelevance.CONVERT_FOR_LOOP_TO_ENHANCED, context, CodeActionKind.Refactor);
		resultingCollections.add(proposal);
		return true;
	}

	private static ForStatement getEnclosingForStatementHeader(ASTNode node) {
		return getEnclosingHeader(node, ForStatement.class, ForStatement.INITIALIZERS_PROPERTY, ForStatement.EXPRESSION_PROPERTY, ForStatement.UPDATERS_PROPERTY);
	}

	private static <T extends ASTNode> T getEnclosingHeader(ASTNode node, Class<T> headerType, StructuralPropertyDescriptor... headerProperties) {
		if (headerType.isInstance(node)) {
			return headerType.cast(node);
		}

		while (node != null) {
			ASTNode parent = node.getParent();
			if (headerType.isInstance(parent)) {
				StructuralPropertyDescriptor locationInParent = node.getLocationInParent();
				for (StructuralPropertyDescriptor property : headerProperties) {
					if (locationInParent == property) {
						return headerType.cast(parent);
					}
				}
				return null;
			}
			node = parent;
		}
		return null;
	}
}
