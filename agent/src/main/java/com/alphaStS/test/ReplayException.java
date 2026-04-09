package com.alphaStS.test;

import com.alphaStS.GameState;

public class ReplayException extends Exception {

    public final GameState state;
    public final String logLine;

    public ReplayException(String message, GameState state, String logLine) {
        super(buildMessage(message, state, logLine));
        this.state = state;
        this.logLine = logLine;
    }

    private static String buildMessage(String message, GameState state, String logLine) {
        StringBuilder sb = new StringBuilder(message);
        if (logLine != null) {
            sb.append("\n  log line: ").append(logLine);
        }
        if (state != null) {
            sb.append("\n  game state: ").append(state);
        }
        return sb.toString();
    }
}
