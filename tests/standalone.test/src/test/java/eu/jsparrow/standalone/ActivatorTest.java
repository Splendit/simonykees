package eu.jsparrow.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;

/**
 * test class for {@link Activator}
 * 
 * @author Matthias Webhofer, Hans-Jörg Schrödl
 * @since 2.5.0
 */
public class ActivatorTest {

	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	private static final String LIST_RULES_SELECTED_ID_KEY = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$

	protected static Map<String, String> configuration;

	private Activator activator;

	BundleContext context;

	private RefactoringInvoker refactoringInvoker;

	private ListRulesUtil listRulesUtil;

	private StandaloneLicenseUtilService licenseService;

	@BeforeAll
	public static void setUpClass() {

		configuration = new HashMap<>();
		configuration.put(STANDALONE_MODE_KEY, StandaloneMode.TEST.name());
	}

	@BeforeEach
	public void setUp() {
		context = mock(BundleContext.class);

		refactoringInvoker = mock(RefactoringInvoker.class);

		listRulesUtil = mock(ListRulesUtil.class);

		licenseService = mock(StandaloneLicenseUtilService.class);

		activator = new TestableActivator();
	}

	@Test
	public void start_withListRules_invokesListRules() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LIST_RULES"); //$NON-NLS-1$

		activator.start(context);

		verify(listRulesUtil).listRules();
	}

	@Test
	public void start_withListRulesShort_invokesListRulesShort() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LIST_RULES_SHORT"); //$NON-NLS-1$

		activator.start(context);

		verify(listRulesUtil).listRulesShort();
	}

	@Test
	public void start_withLicenseInfo_invokesLicenseInfo() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LICENSE_INFO"); //$NON-NLS-1$

		activator.start(context);

		verify(licenseService).licenseInfo(anyString(), anyString());
	}

	@Test
	public void start_withListRulesWithSelectedId_invokesListSelectedId() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("LIST_RULES"); //$NON-NLS-1$
		String ruleId = "doesntMatter"; //$NON-NLS-1$
		when(context.getProperty(LIST_RULES_SELECTED_ID_KEY)).thenReturn(ruleId);

		activator.start(context);

		verify(listRulesUtil).listRules(ruleId);
	}

	@Test
	public void start_withRefactorAndValidLicense_invokesRefactoringInvoker() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("REFACTOR"); //$NON-NLS-1$
		when(licenseService.validate(anyString(), anyString())).thenReturn(true);

		activator.start(context);

		verify(refactoringInvoker).startRefactoring(any());
	}

	@Test
	public void start_withRefactorAndInvalidLicense_refactoringInvokerNotInvoked() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("REFACTOR"); //$NON-NLS-1$
		when(licenseService.validate(anyString(), anyString())).thenReturn(false);

		activator.start(context);

		verify(refactoringInvoker, never()).startRefactoring(any());
	}

	@Test
	public void start_withReportAndValidLicense_invokesRunInDemoMode() throws Exception {
		when(context.getProperty(STANDALONE_MODE_KEY)).thenReturn("REPORT"); //$NON-NLS-1$

		activator.start(context);
		verify(licenseService, never()).validate(anyString(), anyString());
		verify(refactoringInvoker).runInReportMode(any());
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
		}

		@Override
		StandaloneLicenseUtilService getStandaloneLicenseUtilService() {
			return ActivatorTest.this.licenseService;
		}
	}
}
