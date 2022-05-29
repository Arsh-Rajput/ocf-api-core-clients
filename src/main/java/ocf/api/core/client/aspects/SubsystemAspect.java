package ocf.api.core.client.aspects;

import java.text.MessageFormat;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;

import ocf.api.core.client.exception.SubsystemDataException;
import ocf.api.core.client.exception.SubsystemUnavailableException;

@Aspect
@Component
@EnableAspectJAutoProxy
public class SubsystemAspect {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Pointcut("execution(* *.handleSubSystemException(..))")
	public void subSystemExceptionPointcut() {

	}

	@AfterReturning(value ="subSystemExceptionPointcut()" ,returning ="exception")
	public void logsubSystemException(JoinPoint joinPoint,RuntimeException exception) {
		Object[] arguments = joinPoint.getArgs();
		if (arguments.length == 3) {
			logger.error(MessageFormat.format("Exception occured: {0} , URL: {1}, HttpStatus: {2}, ErrorResponse: {3}",
					Boolean.TRUE.equals((Boolean) arguments[2]) ? SubsystemUnavailableException.class.getSimpleName()
							: SubsystemDataException.class.getSimpleName(),
					(String) arguments[0], ((ClientResponse) arguments[1]).statusCode(),
					((ClientResponse) arguments[1]).bodyToMono(String.class).block()));
		} else {
			logger.error(MessageFormat.format("Unhandled Exception occured while hitting URL: {0}, Reason: {1} ",
					(String) arguments[0], ((Throwable) arguments[1]).getMessage()));
		}
	}

}
