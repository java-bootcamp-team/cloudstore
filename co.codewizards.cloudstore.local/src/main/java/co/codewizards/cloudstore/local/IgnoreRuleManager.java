package co.codewizards.cloudstore.local;

import static co.codewizards.cloudstore.core.oio.OioFileFactory.createFile;
import static co.codewizards.cloudstore.core.repo.local.LocalRepoManager.REPOSITORY_PROPERTIES_FILE_NAME;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private List<IgnoreRule> exclusionList;

	public IgnoreRuleManager() {
	
	}
	
	public List<IgnoreRule> getExclusionList(String filePath) {
		if (exclusionList == null) {
			exclusionList = new LinkedList<>();
			this.loadExclusionsFromFile(exclusionList, filePath);
		}
		
		return exclusionList;
	}
	
	public void loadExclusionsFromFile(List<IgnoreRule> list, String filePath) {
		
		Properties prop = new Properties();
		InputStream input = null;
		
		try {

			input = new FileInputStream("/home/student/Documents/client/clientrepository/.cloudstore-repo/cloudstore-repository.properties");

			prop.load(input);
			
			list.add(new IgnoreRule(prop.getProperty("nameRegex")));

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

	public boolean isExcluded(RepoFile file) {
		String filePath = file.getPath();
		//String fileName = "/" + file.getName();
		
		//filePath.replaceAll(fileName, "");
		
		if (exclusionList == null)
			this.getExclusionList(filePath);
		
		for (int i=0; i < exclusionList.size(); i++) {
			
			String regexPattern = (exclusionList.get(i)).getRegex();
			
			if ((file.getName()).matches(regexPattern))
				//logger.warn("AAAAAAAAAAAAA file excluded:" + file.getAbsolutePath());
				return true;
		}
		//logger.warn("AAAAAAAAAAAAA file not excluded:" + file.getAbsolutePath());

		return false;
	}

	//public static void main(String[] args) {
	//	File file = OioFileFactory.createFile("~dgdstd$.txt");
	//	IgnoreRuleManager mng = new IgnoreRuleManager();
	//	System.out.println(mng.isExcluded(file));
	//}
		
}
