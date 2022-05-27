package ocf.api.core.client.http;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import ocf.api.core.client.exception.SubsystemDataException;
import ocf.api.core.client.exception.SubsystemUnavailableException;
import reactor.core.publisher.Mono;

@Service
@Primary
@Scope("prototype")
public class RestClient {

	@Autowired
	private WebClient webClient;

	public <T, U> U post(String url, T request, Map<String, String> headersMap, Class<U> responseType) {
		return internalPost(url, request, convertHeaders(headersMap), responseType);

	}

	public <T> T get(String url, Map<String, String> headersMap, Class<T> responseType) {
		return internalGet(url, convertHeaders(headersMap), responseType);

	}

	private <T, U> U internalPost(String url, T request, HttpHeaders headers, Class<U> responseType) {
		return webClient.post().uri(url).headers(item -> item.addAll(headers)).bodyValue(request).retrieve()
				.onStatus(HttpStatus::is4xxClientError,
						response -> handleSubSystemException(url, response, Boolean.FALSE))
				.onStatus(HttpStatus::is5xxServerError,
						response -> handleSubSystemException(url, response, Boolean.TRUE))
				.bodyToMono(responseType).doOnError(throwable -> handleSubSystemException(url, throwable)).block();
	}

	private <T> T internalGet(String url, HttpHeaders headers, Class<T> responseType) {
		return webClient.get().uri(url).headers(item -> item.addAll(headers)).retrieve()
				.onStatus(HttpStatus::is4xxClientError,
						response -> handleSubSystemException(url, response, Boolean.FALSE))
				.onStatus(HttpStatus::is5xxServerError,
						response -> handleSubSystemException(url, response, Boolean.TRUE))
				.bodyToMono(responseType).doOnError(throwable -> handleSubSystemException(url, throwable)).block();
	}

	private RuntimeException handleSubSystemException(String url, Throwable throwable) {

		if (throwable instanceof SubsystemDataException)
			return (SubsystemDataException) throwable;
		else if (throwable instanceof SubsystemUnavailableException)
			return (SubsystemUnavailableException) throwable;
		else
			return new SubsystemUnavailableException(HttpStatus.INTERNAL_SERVER_ERROR, url, throwable.getMessage());
	}

	private Mono<? extends Throwable> handleSubSystemException(String url, ClientResponse response,
			Boolean isServerError) {

		return response.bodyToMono(String.class).map(body ->

		{
			if (Boolean.TRUE.equals(isServerError)) {
				return new SubsystemUnavailableException(response.statusCode(), url, body);
			} else {
				return new SubsystemDataException(response.statusCode(), url, body);
			}
		});
	}

	private HttpHeaders convertHeaders(Map<String, String> headersMap) {
		final HttpHeaders headers = new HttpHeaders();
		if (!CollectionUtils.isEmpty(headersMap)) {
			headersMap.forEach((k, v) -> headers.add(k, v));
		}
		return headers;
	}

}
