package co.codewizards.cloudstore.rest.client.request;

import static co.codewizards.cloudstore.core.util.Util.*;

import javax.ws.rs.core.Response;

import co.codewizards.cloudstore.core.util.AssertUtil;

public class EndSyncFromRepository extends VoidRequest {

	private final String repositoryName;

	public EndSyncFromRepository(final String repositoryName) {
		this.repositoryName = AssertUtil.assertNotNull("repositoryName", repositoryName);
	}

	@Override
	public Response _execute() {
		return assignCredentials(
				createWebTarget("_endSyncFromRepository", urlEncode(repositoryName))
				.request()).post(null);
	}

}
