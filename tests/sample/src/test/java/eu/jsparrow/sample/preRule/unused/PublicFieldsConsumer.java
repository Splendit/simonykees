package eu.jsparrow.sample.preRule.unused;

public class PublicFieldsConsumer {

	void consumingPublicFields() {
		UnusedFields unusedFields = new UnusedFields();
		System.out.println(unusedFields.publicFieldUsedExternally);
		System.out.println(unusedFields.publicFieldUsedInternallyAndExternally);
		unusedFields.publicFieldReassignedExternally = "";
	}
}
