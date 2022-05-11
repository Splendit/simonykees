package eu.jsparrow.jdt.ls.core.internal.lsp;

import java.util.concurrent.CompletableFuture;

import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.services.JsonRequest;
import org.eclipse.lsp4j.jsonrpc.services.JsonSegment;

import eu.jsparrow.jdt.ls.core.internal.handlers.GenerateToStringHandler.GenerateToStringParams;
import eu.jsparrow.jdt.ls.core.internal.handlers.HashCodeEqualsHandler.CheckHashCodeEqualsResponse;
import eu.jsparrow.jdt.ls.core.internal.handlers.OverrideMethodsHandler.AddOverridableMethodParams;
import eu.jsparrow.jdt.ls.core.internal.handlers.OverrideMethodsHandler.OverridableMethodsResponse;

@JsonSegment("java")
public interface JavaProtocolExtensions {

	@JsonRequest
	CompletableFuture<String> classFileContents(TextDocumentIdentifier documentUri);



	@JsonRequest
	CompletableFuture<OverridableMethodsResponse> listOverridableMethods(CodeActionParams params);

	@JsonRequest
	CompletableFuture<WorkspaceEdit> addOverridableMethods(AddOverridableMethodParams params);

	@JsonRequest
	CompletableFuture<CheckHashCodeEqualsResponse> checkHashCodeEqualsStatus(CodeActionParams params);


	@JsonRequest
	CompletableFuture<WorkspaceEdit> organizeImports(CodeActionParams params);



	@JsonRequest
	CompletableFuture<WorkspaceEdit> generateToString(GenerateToStringParams params);




}
