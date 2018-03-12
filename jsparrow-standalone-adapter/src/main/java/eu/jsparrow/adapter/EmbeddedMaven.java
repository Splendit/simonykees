package eu.jsparrow.adapter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.maven.plugin.logging.Log;
import org.eclipse.osgi.util.NLS;

import eu.jsparrow.adapter.i18n.Messages;

public class EmbeddedMaven {
	
	private static final int BUFFER_SIZE = 4096;
	
	private Log log;
	
	private String mavenHome;
	private String mavenHomeUnzipped = ""; //$NON-NLS-1$
	
	public EmbeddedMaven(Log log, String mavenHome) {
		this.log = log;
		this.mavenHome = mavenHome;
	}
	
	public String prepareMaven(String jsarrowTempPath) {
		String newMavenHome = null;

		if (null != mavenHome && !mavenHome.isEmpty() && !mavenHome.endsWith("EMBEDDED")) { //$NON-NLS-1$
			newMavenHome = mavenHome;
		} else {
			log.debug(Messages.Adapter_embededMavenVersionDetected);

			String tempZipPath = jsarrowTempPath + File.separator + "maven"; //$NON-NLS-1$

			try (InputStream mavenZipInputStream = getClass().getResourceAsStream("/apache-maven-3.5.2-bin.zip")) { //$NON-NLS-1$
				mavenHomeUnzipped += tempZipPath;
				unzip(mavenZipInputStream, tempZipPath);
				newMavenHome = mavenHomeUnzipped;
				setMavenHome(newMavenHome);
			} catch (IOException e) {
				log.debug(e.getMessage(), e);
				log.error(e.getMessage());
			}
		}

		return newMavenHome;
	}
	
	/**
	 * Extracts a zip entry (file entry)
	 * 
	 * @param zipIn
	 * @param filePath
	 * @throws IOException
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
		String loggerInfo = NLS.bind("file unzip: {0}", filePath); //$NON-NLS-1$
		log.debug(loggerInfo);

		try (FileOutputStream fos = new FileOutputStream(filePath);
				BufferedOutputStream bos = new BufferedOutputStream(fos)) {
			byte[] bytesIn = new byte[BUFFER_SIZE];
			int read = 0;
			while ((read = zipIn.read(bytesIn)) != -1) {
				bos.write(bytesIn, 0, read);
			}
		}

		File file = new File(filePath);

		Set<PosixFilePermission> perms = new HashSet<>();
		perms.add(PosixFilePermission.OWNER_READ);
		perms.add(PosixFilePermission.OWNER_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);

		perms.add(PosixFilePermission.OTHERS_READ);
		perms.add(PosixFilePermission.OTHERS_WRITE);
		perms.add(PosixFilePermission.OWNER_EXECUTE);

		perms.add(PosixFilePermission.GROUP_READ);
		perms.add(PosixFilePermission.GROUP_WRITE);
		perms.add(PosixFilePermission.GROUP_EXECUTE);

		Files.setPosixFilePermissions(file.toPath(), perms);
	}
	
	/**
	 * Extracts a zip file from zipInputStream to a directory specified by
	 * destDirectory which is created if does not exists
	 * 
	 * @param zipInputStream
	 * @param destDirectory
	 * @throws IOException
	 */
	public void unzip(InputStream zipInputStream, String destDirectory) throws IOException {
		File destDir = new File(destDirectory);
		if (!destDir.exists()) {
			destDir.mkdir();
		}

		String loggerInfo = NLS.bind("Unzip temporary maven installation to: {0}", destDir.toString()); //$NON-NLS-1$
		log.debug(loggerInfo);

		ZipInputStream zipIn = new ZipInputStream(zipInputStream);
		ZipEntry entry = zipIn.getNextEntry();
		mavenHomeUnzipped += File.separator + entry.getName();

		// iterates over entries in the zip file
		while (entry != null) {
			String filePath = destDirectory + File.separator + entry.getName();
			if (!entry.isDirectory()) {
				// if the entry is a file, extracts it
				extractFile(zipIn, filePath);
			} else {
				// if the entry is a directory, make the directory
				File dir = new File(filePath);
				dir.mkdir();
				log.debug("create dir: {0}" + dir.getAbsoluteFile()); //$NON-NLS-1$
			}
			zipIn.closeEntry();
			entry = zipIn.getNextEntry();
		}
		zipIn.close();
	}
	
	public String getMavenHome() {
		return this.mavenHome;
	}
	
	private void setMavenHome(String mavenHome) {
		this.mavenHome = mavenHome;
	}

}
