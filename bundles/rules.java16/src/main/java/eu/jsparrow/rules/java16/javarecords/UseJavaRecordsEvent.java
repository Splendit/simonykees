package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.ASTNode;

public interface UseJavaRecordsEvent {

	default void addMarkerEvent(ASTNode original, ASTNode newNdoe){}
}
