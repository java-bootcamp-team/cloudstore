package co.codewizards.cloudstore.local;

import static co.codewizards.cloudstore.core.oio.OioFileFactory.createFile;
import static co.codewizards.cloudstore.core.repo.local.LocalRepoManager.REPOSITORY_PROPERTIES_FILE_NAME;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
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
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {
			System.out.println(localRoot.getAbsolutePath());

			Writer output;
			output = new BufferedWriter(new FileWriter(localRoot.getAbsolutePath() 
					+ "/.cloudstore-repo/cloudstore-repository.properties"));  //clears file every time
			output.append("nameRegex=^~.*\\$$|^.*test.*$|^.*aaa.*$");
			
			output.close();
			
			input = new FileInputStream(localRoot.getAbsolutePath() 
					+ "/.cloudstore-repo/cloudstore-repository.properties");
			prop.load(input);
			
			exclusionList.add(new IgnoreRule(prop.getProperty("nameRegex")));

		} catch (IOException ex) {
			ex.printStackTrace();
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
		
		for (int i=0; i < exclusionList.size(); i++) {
			
			String regexPattern = (exclusionList.get(i)).getRegex();
			
			if ((file.getName()).matches(regexPattern))
				
				return true;
		}

		return false;
	}		
}
