package org.ijsberg.iglu.configuration;

/**
 */
public class BusyException extends Exception {
	public BusyException() {
	}

	public BusyException(String message) {
		super(message);
	}

	public BusyException(String message, Throwable cause) {
		super(message, cause);
	}

	public BusyException(Throwable cause) {
		super(cause);
	}

	public BusyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
