package co.codewizards.cloudstore.core.repo.sync;

import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import co.codewizards.cloudstore.core.AbstractTest;
import co.codewizards.cloudstore.core.progress.LoggerProgressMonitor;
import co.codewizards.cloudstore.core.repo.local.LocalRepoManager;
import co.codewizards.cloudstore.core.util.IOUtil;

public class RepoToRepoSyncTest extends AbstractTest {
	private static Logger logger = LoggerFactory.getLogger(RepoToRepoSyncTest.class);

	private File localRoot;
	private File remoteRoot;

	@Test
	public void syncRemoteRootToLocalRootInitially() throws Exception {
		localRoot = newTestRepositoryLocalRoot();
		assertThat(localRoot).doesNotExist();
		localRoot.mkdirs();
		assertThat(localRoot).isDirectory();

		remoteRoot = newTestRepositoryLocalRoot();
		assertThat(remoteRoot).doesNotExist();
		remoteRoot.mkdirs();
		assertThat(remoteRoot).isDirectory();
		URL remoteRootURL = remoteRoot.toURI().toURL();

		LocalRepoManager localRepoManagerLocal = localRepoManagerFactory.createLocalRepoManagerForNewRepository(localRoot);
		assertThat(localRepoManagerLocal).isNotNull();

		LocalRepoManager localRepoManagerRemote = localRepoManagerFactory.createLocalRepoManagerForNewRepository(remoteRoot);
		assertThat(localRepoManagerRemote).isNotNull();

		localRepoManagerLocal.putRemoteRepository(localRepoManagerRemote.getRepositoryID(), remoteRootURL, localRepoManagerRemote.getPublicKey());
		localRepoManagerRemote.putRemoteRepository(localRepoManagerLocal.getRepositoryID(), null, localRepoManagerLocal.getPublicKey());

		File child_1 = createDirectory(remoteRoot, "1");

		createFileWithRandomContent(child_1, "a");
		createFileWithRandomContent(child_1, "b");
		createFileWithRandomContent(child_1, "c");

		File child_2 = createDirectory(remoteRoot, "2");

		createFileWithRandomContent(child_2, "a");

		File child_2_1 = createDirectory(child_2, "1");
		createFileWithRandomContent(child_2_1, "a");
		createFileWithRandomContent(child_2_1, "b");

		File child_3 = createDirectory(remoteRoot, "3");

		createFileWithRandomContent(child_3, "a");
		createFileWithRandomContent(child_3, "b");
		createFileWithRandomContent(child_3, "c");
		createFileWithRandomContent(child_3, "d");

		localRepoManagerRemote.localSync(new LoggerProgressMonitor(logger));

		assertThatFilesInRepoAreCorrect(remoteRoot);

		logger.info("local repo: {}", localRepoManagerLocal.getRepositoryID());
		logger.info("remote repo: {}", localRepoManagerRemote.getRepositoryID());

		RepoToRepoSync repoToRepoSync = new RepoToRepoSync(localRoot, remoteRootURL);
		repoToRepoSync.sync(new LoggerProgressMonitor(logger));
		repoToRepoSync.close();

		assertThatFilesInRepoAreCorrect(remoteRoot);

		localRepoManagerLocal.close();
		localRepoManagerRemote.close();

		assertThatNoCollisionInRepo(localRoot);
		assertThatNoCollisionInRepo(remoteRoot);

		assertDirectoriesAreEqualRecursively(localRoot, remoteRoot);
	}

	@Test
	public void syncRemoteRootToLocalRootWithAddedFilesAndDirectories() throws Exception {
		syncRemoteRootToLocalRootInitially();

		LocalRepoManager localRepoManagerRemote = localRepoManagerFactory.createLocalRepoManagerForExistingRepository(remoteRoot);
		assertThat(localRepoManagerRemote).isNotNull();

		File child_2 = new File(remoteRoot, "2");
		assertThat(child_2).isDirectory();

		File child_2_1 = new File(child_2, "1");
		assertThat(child_2_1).isDirectory();

		File child_2_1_5 = createDirectory(child_2_1, "5");
		createFileWithRandomContent(child_2_1_5, "aaa");
		createFileWithRandomContent(child_2_1_5, "bbb");

		File child_3 = new File(remoteRoot, "3");
		assertThat(child_3).isDirectory();

		createFileWithRandomContent(child_3, "e");

		localRepoManagerRemote.localSync(new LoggerProgressMonitor(logger));

		assertThatFilesInRepoAreCorrect(remoteRoot);

		RepoToRepoSync repoToRepoSync = new RepoToRepoSync(localRoot, remoteRoot.toURI().toURL());
		repoToRepoSync.sync(new LoggerProgressMonitor(logger));
		repoToRepoSync.close();

		assertThatFilesInRepoAreCorrect(remoteRoot);

		localRepoManagerRemote.close();

		assertThatNoCollisionInRepo(localRoot);
		assertThatNoCollisionInRepo(remoteRoot);

		assertDirectoriesAreEqualRecursively(localRoot, remoteRoot);
	}

	@Test
	public void syncRemoteRootToLocalRootWithModifiedFiles() throws Exception {
		syncRemoteRootToLocalRootInitially();

		LocalRepoManager localRepoManagerRemote = localRepoManagerFactory.createLocalRepoManagerForExistingRepository(remoteRoot);
		assertThat(localRepoManagerRemote).isNotNull();

		File child_2 = new File(remoteRoot, "2");
		assertThat(child_2).isDirectory();

		File child_2_1 = new File(child_2, "1");
		assertThat(child_2_1).isDirectory();

		File child_2_1_a = new File(child_2_1, "a");
		assertThat(child_2_1_a).isFile();

		File child_2_1_b = new File(child_2_1, "b");
		assertThat(child_2_1_b).isFile();

		modifyFileRandomly(child_2_1_a);

		logger.info("file='{}' length={}", child_2_1_b, child_2_1_b.length());

		FileOutputStream out = new FileOutputStream(child_2_1_b);
		out.write(random.nextInt());
		out.close();

		logger.info("file='{}' length={}", child_2_1_b, child_2_1_b.length());

		byte[] child_2_1_a_expected = IOUtil.getBytesFromFile(child_2_1_a);
		byte[] child_2_1_b_expected = IOUtil.getBytesFromFile(child_2_1_b);

		localRepoManagerRemote.localSync(new LoggerProgressMonitor(logger));

		assertThatFilesInRepoAreCorrect(remoteRoot);

		RepoToRepoSync repoToRepoSync = new RepoToRepoSync(localRoot, remoteRoot.toURI().toURL());
		repoToRepoSync.sync(new LoggerProgressMonitor(logger));
		repoToRepoSync.close();

		assertThatFilesInRepoAreCorrect(remoteRoot);

		localRepoManagerRemote.close();

		assertThatNoCollisionInRepo(localRoot);
		assertThatNoCollisionInRepo(remoteRoot);

		assertDirectoriesAreEqualRecursively(localRoot, remoteRoot);

		// ensure that nothing was synced backwards into the wrong direction ;-)
		byte[] child_2_1_a_actual = IOUtil.getBytesFromFile(child_2_1_a);
		byte[] child_2_1_b_actual = IOUtil.getBytesFromFile(child_2_1_b);
		assertThat(child_2_1_a_actual).isEqualTo(child_2_1_a_expected);
		assertThat(child_2_1_b_actual).isEqualTo(child_2_1_b_expected);
	}

	private void modifyFileRandomly(File file) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		try {
			if (file.length() > 0)
				raf.seek(random.nextInt((int)file.length()));

			byte[] buf = new byte[1 + random.nextInt(10)];
			random.nextBytes(buf);

			raf.write(buf);
		} finally {
			raf.close();
		}
	}

	@Test
	public void syncRemoteRootToLocalRootWithDeletedFile() throws Exception {
		syncRemoteRootToLocalRootInitially();

		LocalRepoManager localRepoManagerRemote = localRepoManagerFactory.createLocalRepoManagerForExistingRepository(remoteRoot);
		assertThat(localRepoManagerRemote).isNotNull();

		File child_2 = new File(remoteRoot, "2");
		assertThat(child_2).isDirectory();

		File child_2_1 = new File(child_2, "1");
		assertThat(child_2_1).isDirectory();

		File child_2_1_a = new File(child_2_1, "a");
		assertThat(child_2_1_a).isFile();

		deleteFile(child_2_1_a);

		localRepoManagerRemote.localSync(new LoggerProgressMonitor(logger));

		assertThatFilesInRepoAreCorrect(remoteRoot);

		RepoToRepoSync repoToRepoSync = new RepoToRepoSync(localRoot, remoteRoot.toURI().toURL());
		repoToRepoSync.sync(new LoggerProgressMonitor(logger));
		repoToRepoSync.close();

		assertThatFilesInRepoAreCorrect(remoteRoot);

		localRepoManagerRemote.close();

		assertThatNoCollisionInRepo(localRoot);
		assertThatNoCollisionInRepo(remoteRoot);

		assertDirectoriesAreEqualRecursively(localRoot, remoteRoot);
	}

	@Test
	public void syncRemoteRootToLocalRootWithDeletedDir() throws Exception {
		syncRemoteRootToLocalRootInitially();

		LocalRepoManager localRepoManagerRemote = localRepoManagerFactory.createLocalRepoManagerForExistingRepository(remoteRoot);
		assertThat(localRepoManagerRemote).isNotNull();

		File child_2 = new File(remoteRoot, "2");
		assertThat(child_2).isDirectory();

		File child_2_1 = new File(child_2, "1");
		assertThat(child_2_1).isDirectory();

		for (File child : child_2_1.listFiles()) {
			deleteFile(child);
		}

		for (File child : child_2.listFiles()) {
			deleteFile(child);
		}

		deleteFile(child_2);

		localRepoManagerRemote.localSync(new LoggerProgressMonitor(logger));

		assertThatFilesInRepoAreCorrect(remoteRoot);

		RepoToRepoSync repoToRepoSync = new RepoToRepoSync(localRoot, remoteRoot.toURI().toURL());
		repoToRepoSync.sync(new LoggerProgressMonitor(logger));
		repoToRepoSync.close();

		assertThatFilesInRepoAreCorrect(remoteRoot);

		localRepoManagerRemote.close();

		assertThatNoCollisionInRepo(localRoot);
		assertThatNoCollisionInRepo(remoteRoot);

		assertDirectoriesAreEqualRecursively(localRoot, remoteRoot);
	}

	@Test
	public void syncWithModificationCollision() throws Exception {
		syncRemoteRootToLocalRootInitially();

		File r_child_2 = new File(remoteRoot, "2");
		assertThat(r_child_2).isDirectory();

		File r_child_2_1 = new File(r_child_2, "1");
		assertThat(r_child_2_1).isDirectory();

		File r_child_2_1_a = new File(r_child_2_1, "a");
		assertThat(r_child_2_1_a).isFile();

		File l_child_2 = new File(localRoot, "2");
		assertThat(l_child_2).isDirectory();

		File l_child_2_1 = new File(l_child_2, "1");
		assertThat(l_child_2_1).isDirectory();

		File l_child_2_1_a = new File(l_child_2_1, "a");
		assertThat(l_child_2_1_a).isFile();

		modifyFileRandomly(r_child_2_1_a);
		modifyFileRandomly(l_child_2_1_a);

		for (int i = 0; i < 2; ++i) { // We have to sync twice to make sure the collision file is synced, too (it is created during the first sync).
			RepoToRepoSync repoToRepoSync = new RepoToRepoSync(localRoot, remoteRoot.toURI().toURL());
			repoToRepoSync.sync(new LoggerProgressMonitor(logger));
			repoToRepoSync.close();
		}

		// Expect exactly one collision in remote repo (in directory r_child_2_1).
		File r_collision = null;
		for (File f : r_child_2_1.listFiles()) {
			if (f.getName().contains(IOUtil.COLLISION_FILE_NAME_INFIX)) {
				assertThat(r_collision).isNull();
				r_collision = f;
			}
		}
		assertThat(r_collision).isNotNull();

		// Expect exactly one collision in local repo (in directory l_child_2_1).
		File l_collision = null;
		for (File f : l_child_2_1.listFiles()) {
			if (f.getName().contains(IOUtil.COLLISION_FILE_NAME_INFIX)) {
				assertThat(l_collision).isNull();
				l_collision = f;
			}
		}
		assertThat(l_collision).isNotNull();

		addToFilesInRepo(remoteRoot, r_collision);

		assertThatFilesInRepoAreCorrect(remoteRoot);

		assertDirectoriesAreEqualRecursively(localRoot, remoteRoot);
	}

	private void assertThatNoCollisionInRepo(File localRoot) {
		File[] children = localRoot.listFiles();
		if (children != null) {
			for (File f : children) {
				if (f.getName().contains(IOUtil.COLLISION_FILE_NAME_INFIX))
					Assert.fail("Collision: " + f);

				assertThatNoCollisionInRepo(f);
			}
		}
	}

//	@Test
//	public void syncWithDeletionCollision() throws Exception {
//
//	}
}
