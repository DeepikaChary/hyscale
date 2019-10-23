package io.hyscale.builder.services.handler;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.TailHandler;

public class PushLogHandler implements TailHandler {

	private static String EOF_MARKER = "(.*)digest(.*)size(.*)";

	@Override
	public void handleLine(String line) {
		System.out.println(line);
	}

	@Override
	public boolean handleEOF(String line) {
		if (line == null) {
			return true;
		}
		boolean eof = line.matches(EOF_MARKER);
		if (eof) {
			WorkflowLogger.footer();
		}
		return eof;
	}

}
