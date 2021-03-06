package co.codewizards.cloudstore.updater;

import static co.codewizards.cloudstore.core.oio.OioFileFactory.*;
import static co.codewizards.cloudstore.core.util.IOUtil.*;
import static co.codewizards.cloudstore.core.util.StringUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.junit.After;
import org.junit.Before;

import co.codewizards.cloudstore.core.oio.File;

public abstract class AbstractTestWithTempDir {

	protected File tempDir;

	@Before
	public void before() throws Exception {
		tempDir = createTempDirectory("cloudstore-test-");
	}

	@After
	public void after() throws Exception {
		File td = tempDir;
		tempDir = null;

		if (td != null)
			td.deleteRecursively();
	}

	protected File downloadFileToTempDir(String urlStr) throws IOException {
		long startTimestamp = System.currentTimeMillis();
		URL url = new URL(urlStr);

		String fileName = url.getPath();
		int lastSlash = fileName.lastIndexOf('/');
		if (lastSlash < 0)
			throw new IllegalArgumentException("urlStr's path does not contain a '/': " + urlStr);

		fileName = fileName.substring(lastSlash + 1);
		if (isEmpty(fileName))
			throw new IllegalArgumentException("urlStr's path ends on '/': " + urlStr);

		File file = tempDir.createFile(fileName);
		try (InputStream in = url.openStream();) {
			try (OutputStream out = file.createOutputStream();) {
				transferStreamData(in, out);
			}
		}
		System.out.println("Download took " + (System.currentTimeMillis() - startTimestamp) + " ms: " + urlStr);
		return file;
	}

}
