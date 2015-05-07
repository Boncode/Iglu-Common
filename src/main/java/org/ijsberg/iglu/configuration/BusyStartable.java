package org.ijsberg.iglu.configuration;

/**
 */
public interface BusyStartable extends Startable {

	void pause() throws BusyException;

	void resume();

	boolean isBusy();

	void forceStop();
}
