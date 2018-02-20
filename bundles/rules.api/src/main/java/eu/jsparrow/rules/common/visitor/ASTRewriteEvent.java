package eu.jsparrow.rules.common.visitor;

public class ASTRewriteEvent {

	private String compilationUnitHandle;

	public ASTRewriteEvent(String compilationUnitName) {
		this.compilationUnitHandle = compilationUnitName;
	}

	public String getCompilationUnit() {
		return compilationUnitHandle;
	}

}
