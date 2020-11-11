package eu.jsparrow.maven.adapter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

public class BundleStarterTest {

	private Log log;

	private BundleContext bundleContext;
	private InputStream bundleInputStream;
	private BufferedReader bundleBufferedReader;
	private InputStream resourceInputStream;

	private BundleStarter bundleStarter;

	private boolean isInputStreamNull;

	@Before
	public void setUp() {
		log = mock(Log.class);
		bundleContext = mock(BundleContext.class);
		bundleInputStream = mock(InputStream.class);
		bundleBufferedReader = mock(BufferedReader.class);
		resourceInputStream = mock(InputStream.class);

		bundleStarter = new TestableBundleStarter(log);
	}

	@Test
	public void loadBundles_inputStreamIsNotNull_bundlesInstalledAndAdded() throws Exception {
		Bundle bundle = mock(Bundle.class);

		isInputStreamNull = false;
		String line1 = "line1"; //$NON-NLS-1$

		doAnswer(new Answer<String>() {

			private int counter = 0;

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				if (counter > 0) {
					return null;
				} else {
					counter++;
					return line1;
				}
			}
		}).when(bundleBufferedReader)
			.readLine();

		when(bundleContext.installBundle(anyString(), eq(resourceInputStream))).thenReturn(bundle);

		List<Bundle> bundles = bundleStarter.loadBundles();

		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(bundleContext).installBundle(captor.capture(), eq(resourceInputStream));
		assertTrue(captor.getValue()
			.contains(line1));

		assertEquals(1, bundles.size());
	}

	@Test
	public void loadBundles_inputStreamIsNull_noInteractionWithReaderOrBundleContext() throws Exception {
		isInputStreamNull = true;

		assertThrows(MojoExecutionException.class, () -> bundleStarter.loadBundles());
	}

	@Test
	public void startBundles_shouldStartBundle() throws Exception {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = mock(Dictionary.class);
		List<Bundle> bundles = new ArrayList<>();

		Bundle bundle = mock(Bundle.class);
		bundles.add(bundle);

		when(bundle.getHeaders()).thenReturn(headers);
		when(headers.get(eq(Constants.FRAGMENT_HOST))).thenReturn(null);
		when(bundle.getSymbolicName()).thenReturn(BundleStarter.STANDALONE_BUNDLE_NAME);

		bundleStarter.startBundles(bundles);

		verify(bundle).start();
		assertTrue(bundleStarter.isStandaloneStarted());
	}

	@Test
	public void startBundles_fragmentHostNotNull() throws Exception {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = mock(Dictionary.class);
		List<Bundle> bundles = new ArrayList<>();

		Bundle bundle = mock(Bundle.class);
		bundles.add(bundle);

		when(bundle.getHeaders()).thenReturn(headers);
		when(headers.get(eq(Constants.FRAGMENT_HOST))).thenReturn("someHost"); //$NON-NLS-1$

		bundleStarter.startBundles(bundles);

		verify(bundle).getHeaders();
		verifyNoMoreInteractions(bundle);
		assertFalse(bundleStarter.isStandaloneStarted());
	}

	@Test
	public void startBundles_symbolicNameIsNull() throws Exception {
		@SuppressWarnings("unchecked")
		Dictionary<String, String> headers = mock(Dictionary.class);
		List<Bundle> bundles = new ArrayList<>();

		Bundle bundle = mock(Bundle.class);
		bundles.add(bundle);

		when(bundle.getHeaders()).thenReturn(headers);
		when(headers.get(eq(Constants.FRAGMENT_HOST))).thenReturn(null);
		when(bundle.getSymbolicName()).thenReturn(null);

		bundleStarter.startBundles(bundles);

		verify(bundle).getHeaders();
		verify(bundle).getSymbolicName();
		verifyNoMoreInteractions(bundle);
		assertFalse(bundleStarter.isStandaloneStarted());
	}

	class TestableBundleStarter extends BundleStarter {

		public TestableBundleStarter(Log log) {
			super(log);
		}

		@Override
		protected BundleContext getBundleContext() {
			return bundleContext;
		}

		@Override
		protected InputStream getManifestInputStream() {
			if (isInputStreamNull) {
				return null;
			}
			return bundleInputStream;
		}

		@Override
		protected BufferedReader getBufferedReaderFromInputStream(InputStream is) {
			return bundleBufferedReader;
		}

		@Override
		protected InputStream getBundleResourceInputStream(String resouceName) {
			return resourceInputStream;
		}

	}

}
