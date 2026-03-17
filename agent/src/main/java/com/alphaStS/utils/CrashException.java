package com.alphaStS.utils;

import com.alphaStS.GameState;

public class CrashException extends RuntimeException {
    private String message;

    private CrashException() {
        super();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private GameState state;
        private GameState childState;
        private Integer action;

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withState(GameState state) {
            this.state = state;
            return this;
        }

        public Builder withChildState(GameState childState) {
            this.childState = childState;
            return this;
        }

        public Builder withAction(int action) {
            this.action = action;
            return this;
        }

        public CrashException build() {
            CrashException ex = new CrashException();
            ex.message = buildMessage();
            return ex;
        }

        private String buildMessage() {
            StringBuilder sb = new StringBuilder(message == null ? "Crash" : message);
            if (action != null) {
                appendAction(sb);
            }
            appendState(sb, "State", state);
            if (childState != null) {
                appendState(sb, "Child state", childState);
            }
            return sb.toString();
        }

        private void appendAction(StringBuilder sb) {
            sb.append("\nAction: ").append(action);
            if (state != null) {
                sb.append(" ctx=").append(state.getActionCtx());
                try {
                    sb.append(" desc=").append(state.getActionString(action));
                } catch (Exception ignored) {
                    sb.append(" desc=<unavailable>");
                }
            }
        }

        private void appendState(StringBuilder sb, String label, GameState value) {
            sb.append("\n").append(label).append(": ");
            if (value == null) {
                sb.append("<null>");
            } else {
                sb.append(value);
            }
        }
    }
}
