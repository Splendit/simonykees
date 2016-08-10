package at.splendit.simonykees.core.util;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.SimplePropertyDescriptor;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class ASTPrinter {

	public static void print(ASTNode node, int level) {

		printString(level, "[" + node.getClass() + "]");
		List properties = node.structuralPropertiesForType();

		for (Object desciptor : properties) {

			if (desciptor instanceof SimplePropertyDescriptor) {
				SimplePropertyDescriptor simple = (SimplePropertyDescriptor) desciptor;
				Object value = node.getStructuralProperty(simple);
				printString(level, simple.getId() + " (" + value.toString() + ")");
			} else if (desciptor instanceof ChildPropertyDescriptor) {
				ChildPropertyDescriptor child = (ChildPropertyDescriptor) desciptor;
				ASTNode childNode = (ASTNode) node.getStructuralProperty(child);
				if (childNode != null) {
					printString(level, "Child (" + child.getId() + ") {");
					print(childNode, level + 1);
					printString(level, "}");
				}
			} else {
				ChildListPropertyDescriptor list = (ChildListPropertyDescriptor) desciptor;
				printString(level, "List (" + list.getId() + "){");
				print((List<ASTNode>) node.getStructuralProperty(list), level + 1);
				printString(level, "}");
			}
		}
	}

	public static void print(List<ASTNode> nodes, int level) {
		for (ASTNode astNode : nodes) {
			print(astNode, level);
		}
	}

	private static void printString(int level, String toPrint) {
		System.out.println(generateTab(level) + toPrint);
	}

	private static String generateTab(int level) {
		String tab = "\t";
		String result = "";
		for (int i = 1; i <= level; i++) {
			result += tab;
		}
		return result + "<" + level + "> ";
	}

}
