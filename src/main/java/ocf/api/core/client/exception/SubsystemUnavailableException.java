package ocf.api.core.client.exception;

import org.springframework.http.HttpStatus;

public class SubsystemUnavailableException extends RuntimeException{
	private HttpStatus httpStatus;
	private String url;
	private String responseBody;
	public SubsystemUnavailableException(HttpStatus httpStatus, String url, String responseBody) {
		super();
		this.httpStatus = httpStatus;
		this.url = url;
		this.responseBody = responseBody;
	}

}
