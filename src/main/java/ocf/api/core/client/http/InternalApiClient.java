package ocf.api.core.client.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Primary
@Scope("prototype")
public class InternalApiClient {

	@Autowired
	private RestClient restClient;

	public <T> T get(String url, Map<String, String> headerMap, Class<T> responseType) {
		return restClient.get(url, headerMap, responseType);
	}

	public <T, U> U post(String url, T request, Map<String, String> headerMap, Class<U> responseType) {
		return restClient.post(url, request, headerMap, responseType);
	}

}
