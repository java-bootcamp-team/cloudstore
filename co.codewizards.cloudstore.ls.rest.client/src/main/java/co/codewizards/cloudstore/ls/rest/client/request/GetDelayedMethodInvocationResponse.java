package co.codewizards.cloudstore.ls.rest.client.request;

import static co.codewizards.cloudstore.core.util.AssertUtil.*;

import javax.ws.rs.client.WebTarget;

import co.codewizards.cloudstore.core.dto.Uid;
import co.codewizards.cloudstore.ls.core.invoke.MethodInvocationResponse;
import co.codewizards.cloudstore.ls.core.provider.MediaTypeConst;

public class GetDelayedMethodInvocationResponse extends AbstractRequest<MethodInvocationResponse> {

	private final Uid delayedResponseId;

	public GetDelayedMethodInvocationResponse(final Uid delayedResponseId) {
		this.delayedResponseId = assertNotNull("delayedResponseId", delayedResponseId);
	}

	@Override
	public MethodInvocationResponse execute() {
		final WebTarget webTarget = createWebTarget("InvokeMethod", delayedResponseId.toString());
		final MethodInvocationResponse repoInfoResponseDto = assignCredentials(webTarget.request(MediaTypeConst.APPLICATION_JAVA_NATIVE_WITH_OBJECT_REF_TYPE))
				.get(MethodInvocationResponse.class);
		return repoInfoResponseDto;
	}

	@Override
	public boolean isResultNullable() {
		return false;
	}
}
