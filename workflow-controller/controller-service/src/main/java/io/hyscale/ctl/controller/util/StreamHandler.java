package io.hyscale.ctl.controller.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * Handler input stream through specific consumer
 *
 */
public class StreamHandler implements Runnable {
	private InputStream inputStream;
	private Consumer<String> consumer;

	public StreamHandler(InputStream inputStream, Consumer<String> consumer) {
		this.inputStream = inputStream;
		this.consumer = consumer;
	}

	@Override
	public void run() {
		new BufferedReader(new InputStreamReader(inputStream)).lines().forEach(consumer);
	}

}
