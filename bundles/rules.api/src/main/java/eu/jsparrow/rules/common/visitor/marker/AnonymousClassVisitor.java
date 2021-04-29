package eu.jsparrow.rules.common.visitor.marker;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.MarkerEvent;

public class AnonymousClassVisitor extends ASTVisitor {
	
	private List<MarkerEvent> events = new ArrayList<>();
	private IJavaElement iJavaElement;
	private static final String MESSAGE = "Use Lambda Expressions"; //$NON-NLS-1$
	
	@Override
	public boolean visit(CompilationUnit cu) {
		this.iJavaElement = cu.getJavaElement();
		return true;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration anonymosClass) {
		
		int length = anonymosClass.getLength();
		int startPosition = anonymosClass.getStartPosition();
		
		MarkerEvent event = new MarkerEvent() {
			@Override
			public int getOffset() {
				return startPosition;
			}
			
			@Override
			public String getMessage() {
				return MESSAGE;
			}
			
			@Override
			public int getLength() {
				return length;
			}
			
			@Override
			public IJavaElement getJavaElement() {
				return iJavaElement;
			}
			
			@Override
			public int getIndex() {
				return events.size();
			}
		};
		events.add(event);
		return true;
	}

	public List<MarkerEvent> getEvents() {
		return this.events;
	}
	

}
