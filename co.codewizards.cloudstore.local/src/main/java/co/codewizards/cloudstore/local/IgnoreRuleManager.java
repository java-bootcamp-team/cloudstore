package co.codewizards.cloudstore.local;

import static co.codewizards.cloudstore.core.oio.OioFileFactory.createFile;
import static co.codewizards.cloudstore.core.repo.local.LocalRepoManager.REPOSITORY_PROPERTIES_FILE_NAME;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.codewizards.cloudstore.core.oio.File;
import co.codewizards.cloudstore.core.oio.IoFile;
import co.codewizards.cloudstore.core.oio.IoFileFactory;
import co.codewizards.cloudstore.core.oio.OioFileFactory;
import co.codewizards.cloudstore.local.persistence.RepoFile;

public class IgnoreRuleManager {
	private static final Logger logger = LoggerFactory.getLogger(IgnoreRuleManager.class);
	
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

		Properties prop1 = new Properties();
		Properties prop2 = new Properties();
		Properties prop3 = new Properties();
		InputStream input1 = null;
		InputStream input2 = null;
		OutputStream output = null;

		try {
			System.out.println(localRoot.getAbsolutePath());

			Path path = Paths.get(System.getProperty("user.home") + "/.cloudstore/cloudstore.properties");

			input1 = new FileInputStream(path.toString());

			prop1.load(input1);
			input1.close();

			String regex = prop1.getProperty("nameRegex");

			input2 = new FileInputStream(localRoot.getAbsolutePath() 
					+ "/.cloudstore-repo/cloudstore-repository.properties");
			prop2.load(input2);
			input2.close();

			if (prop2.getProperty("nameRegex") == null) {

				output = new FileOutputStream(localRoot.getAbsolutePath() 
						+ "/.cloudstore-repo/cloudstore-repository.properties");

				prop3.setProperty("nameRegex", regex);
				prop3.store(output, null);

				output.close();
			}

			input2 = new FileInputStream(localRoot.getAbsolutePath() 
					+ "/.cloudstore-repo/cloudstore-repository.properties");
			prop2.load(input2);

			exclusionList.add(new IgnoreRule(prop2.getProperty("nameRegex")));

		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (input2 != null) {
				try {
					input2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

	public boolean isExcluded(File file, File localRoot) {
		
		if (exclusionList == null)
			this.getExclusionList(localRoot);
		
		for (int i=0; i < exclusionList.size(); i++) {
			
			String regexPattern = (exclusionList.get(i)).getRegex();
			
			if ((file.getName()).matches(regexPattern))
				
				return true;
		}

		return false;
	}		
}
