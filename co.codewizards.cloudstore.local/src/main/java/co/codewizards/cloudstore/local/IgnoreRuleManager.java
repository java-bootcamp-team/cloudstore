package co.codewizards.cloudstore.local;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.codewizards.cloudstore.core.oio.File;

public class IgnoreRuleManager {
	private static final Logger logger = LoggerFactory
			.getLogger(IgnoreRuleManager.class);

	private static List<IgnoreRule> exclusionList;

	public IgnoreRuleManager() {

	}

	public List<IgnoreRule> getExclusionList(File localRoot) {
		if (exclusionList == null) {
			exclusionList = new LinkedList<>();
			this.loadExclusionsFromFile(localRoot);
		}

		return exclusionList;
	}

	public void loadExclusionsFromFile(File localRoot) {

		Properties globalProperties = new Properties();
		Properties localProperties = new Properties();
		InputStream input = null;
		OutputStream output = null;

		try {
			Path path = Paths.get(System.getProperty("user.home")
					+ "/.cloudstore/cloudstore.properties");

			input = new FileInputStream(path.toString());

			globalProperties.load(input);
			input.close();

			String regex = globalProperties.getProperty("nameRegex");
			if (globalProperties.getProperty("nameRegex") == null) {
				regex = "";
				globalProperties.setProperty("nameRegex", regex);
				logger.info("[Global properties] Regex pattern for exclusion list: <null>");
			} else
				logger.info("[Global properties] Regex pattern for exclusion list: "
						+ globalProperties.getProperty("nameRegex"));

			input = new FileInputStream(localRoot.getAbsolutePath()
					+ "/.cloudstore-repo/cloudstore-repository.properties");
			localProperties.load(input);
			input.close();

			if (localProperties.getProperty("nameRegex") == null) {

				output = new FileOutputStream(localRoot.getAbsolutePath()
						+ "/.cloudstore-repo/cloudstore-repository.properties");

				localProperties.setProperty("nameRegex", regex);
				localProperties.store(output, null);
				logger.info("[Local properties] Regex pattern for exclusion list: <null>");
				output.close();
			} else
				logger.info("[Local properties] Regex pattern for exclusion list: "
						+ localProperties.getProperty("nameRegex"));

			input = new FileInputStream(localRoot.getAbsolutePath()
					+ "/.cloudstore-repo/cloudstore-repository.properties");
			localProperties.load(input);

			exclusionList.add(new IgnoreRule(localProperties
					.getProperty("nameRegex")));

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	public boolean isExcluded(File file, File localRoot) {

		if (exclusionList == null)
			this.getExclusionList(localRoot);

		for (int i = 0; i < exclusionList.size(); i++) {

			String regexPattern = (exclusionList.get(i)).getRegex();

			if ((file.getName()).matches(regexPattern))

				return true;
		}

		return false;
	}
}
