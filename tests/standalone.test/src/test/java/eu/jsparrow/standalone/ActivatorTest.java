package eu.jsparrow.standalone;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import eu.jsparrow.core.refactorer.RefactoringPipeline;
import eu.jsparrow.license.api.LicenseValidationService;

/**
 * test class for {@link Activator}
 * 
 * @author Matthias Webhofer, Hans-Jörg Schrödl
 * @since 2.5.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivatorTest {

	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String LIST_RULES_SELECTED_ID_KEY = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$

	protected static Map<String, String> configuration;

	private Activator activator;

	@Mock
	BundleContext context;

	@Mock
	private RefactoringInvoker refactoringInvoker;

	@Mock
	private ListRulesUtil listRulesUtil;

	@Mock
	private LicenseValidationService licenseService;

	@BeforeClass
	public static void setUpClass() {
		configuration = new HashMap<>();
		configuration.put(STANDALONE_MODE_KEY, StandaloneMode.TEST.name());
	}

	@Before
	public void setUp() {
		activator = new TestableActivator();
	}

	@Test
	public void start_withListRules_invokesListRules() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LIST_RULES"); //$NON-NLS-1$
		when(context.getBundles()).thenReturn(new Bundle[] {});

		activator.start(context);

		verify(listRulesUtil).listRules();
	}

	@Test
	public void start_withListRulesShort_invokesListRulesShort() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LIST_RULES_SHORT"); //$NON-NLS-1$
		when(context.getBundles()).thenReturn(new Bundle[] {});

		activator.start(context);

		verify(listRulesUtil).listRulesShort();
	}

	@Test
	public void start_withListRulesWithSelectedId_invokesListSelectedId() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LIST_RULES"); //$NON-NLS-1$
		String ruleId = "doesntMatter"; //$NON-NLS-1$
		when(context.getProperty(LIST_RULES_SELECTED_ID_KEY)).thenReturn(ruleId);
		when(context.getBundles()).thenReturn(new Bundle[] {});

		activator.start(context);

		verify(listRulesUtil).listRules(ruleId);
	}

	@Test
	public void start_withRefactorAndValidLicense_invokesRefactoringInvoker() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("REFACTOR"); //$NON-NLS-1$
		when(context.getBundles()).thenReturn(new Bundle[] {});
		when(licenseService.isFullValidLicense()).thenReturn(true);

		activator.start(context);

		verify(licenseService).startValidation();
		verify(refactoringInvoker).startRefactoring(any(), any(RefactoringPipeline.class));
	}

	@Test
	public void start_withRefactorAndInvalidLicense_refactoringInvokerNotInvoked() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("REFACTOR"); //$NON-NLS-1$
		when(context.getBundles()).thenReturn(new Bundle[] {});
		when(licenseService.isFullValidLicense()).thenReturn(false);

		activator.start(context);

		verify(licenseService).startValidation();
		verify(refactoringInvoker, never()).startRefactoring(any(), any(RefactoringPipeline.class));
	}

	@Test
	public void setExitErrorMessage_shouldReturnErrorMessage() {
		String key = "eu.jsparrow.standalone.exit.message"; //$NON-NLS-1$
		String testMessage = "Test"; //$NON-NLS-1$

		activator.setExitErrorMessage(context, testMessage);

		String result = System.getProperty(key);

		assertEquals(testMessage, result);
	}

	class TestableActivator extends Activator {

		public TestableActivator() {
			super(ActivatorTest.this.refactoringInvoker, ActivatorTest.this.listRulesUtil);
			this.licenseService = ActivatorTest.this.licenseService;
		}

		@Override
		void injectDependencies(BundleContext context) {
			return;
		}
	}
}
