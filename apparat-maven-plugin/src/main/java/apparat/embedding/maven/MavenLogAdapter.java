package apparat.embedding.maven;

import apparat.log.*;

/**
 * @author Joa Ebert
 */
public final class MavenLogAdapter implements LogOutput {
	/**
	 * Maps the level of a given Maven log to a corresponding
	 * Apparat log level.
	 *
	 * @param log The current Maven log.
	 * @return The corresponding Apparat log level.
	 */
	public static LogLevel mapLevelOf(final org.apache.maven.plugin.logging.Log log) {
		//
		// We return an instance of a Scala case object here.
		//

		if(log.isDebugEnabled()) {
			return Debug$.MODULE$;
		} else if(log.isInfoEnabled()) {
			return Info$.MODULE$;
		} else if(log.isWarnEnabled()) {
			return Warning$.MODULE$;
		} else if(log.isErrorEnabled()) {
			return Error$.MODULE$;
		} else {
			return Off$.MODULE$;
		}
	}

	private final org.apache.maven.plugin.logging.Log mavenLog;

	public MavenLogAdapter(final org.apache.maven.plugin.logging.Log log) {
		mavenLog = log;
	}

	/**
	 * {@inheritDoc}
	 */
	public void log(final LogLevel level, final String message) {
		//
		// Do not be afraid of IDE errors. This code is the correct way to
		// call a method on a Scala case object.
		//
		
		if(Debug$.MODULE$.matches(level)) {
			mavenLog.debug(message);
		} else if(Info$.MODULE$.matches(level)) {
			mavenLog.info(message);
		} else if(Warning$.MODULE$.matches(level)) {
			mavenLog.warn(message);
		} else if(Error$.MODULE$.matches(level)) {
			mavenLog.error(message);
		} else {
			mavenLog.error("Unexpected log level: "+level);
		}
	}

	public LogLevel getLevel() {
		return mapLevelOf(mavenLog);
	}
}
