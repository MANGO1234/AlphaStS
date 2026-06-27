package com.alphaStS;

import com.alphaStS.gameAction.GameActionCtx;
import com.alphaStS.utils.ScenarioStats;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class MatchSessionWriter {
    private final static boolean LOG_GAME_USING_LINES_FORMAT = true;

    private final String modelDir;
    private Writer matchLogWriter;
    private int matchLogCount;

    public MatchSessionWriter(String modelDir) {
        this.modelDir = modelDir;
    }

    public void setMatchLogFile(String fileName) {
        try {
            File file = new File(modelDir + "/" + fileName);
            file.delete();
            matchLogWriter = new OutputStreamWriter(new GzipCompressorOutputStream(new FileOutputStream(modelDir + "/" + fileName, true)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void writeMatch(int matchNumber, MatchSession.GameResult result, List<GameStep> steps, Map<Integer, GameStateRandomization.Info> combinedInfoMap, int r) throws IOException {
        if (matchLogWriter != null && shouldWriteMatch(result.game())) {
            matchLogWriter.write("*** Match " + matchNumber + " ***\n");
            writeGameRecord(matchLogWriter, result, steps, combinedInfoMap, r);
            matchLogCount++;
        }
    }

    public void writeRemoteMatch(int matchNumber, String remoteGameRecord) throws IOException {
        if (matchLogWriter != null && remoteGameRecord != null && matchLogCount < Configuration.WRITE_MATCHES_MAX_COUNT) {
            matchLogWriter.write("*** Match " + matchNumber + " (Remote) ***\n");
            matchLogWriter.write(remoteGameRecord);
            matchLogCount++;
        }
    }

    public String getRemoteGameRecord(MatchSession.GameResult result, List<GameStep> steps, Map<Integer, GameStateRandomization.Info> combinedInfoMap, int r) throws IOException {
        if (!shouldWriteMatch(result.game())) {
            return null;
        }
        var gameRecordWriter = new StringWriter();
        writeGameRecord(gameRecordWriter, result, steps, combinedInfoMap, r);
        matchLogCount++;
        return gameRecordWriter.toString();
    }

    public void flushAndClose() {
        try {
            if (matchLogWriter != null) {
                matchLogWriter.flush();
                matchLogWriter.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean shouldWriteMatch(MatchSession.Game game) {
        return matchLogCount < Configuration.WRITE_MATCHES_MAX_COUNT && (Configuration.WRITE_MATCHES_FILTER == null || Configuration.WRITE_MATCHES_FILTER.test(game));
    }

    private void writeGameRecord(Writer writer, MatchSession.GameResult result, List<GameStep> steps, Map<Integer, GameStateRandomization.Info> combinedInfoMap, int r) throws IOException {
        var state = steps.get(steps.size() - 1).state();
        int damageTaken = state.getPlayerForRead().getOrigHealth() - state.getPlayerForRead().getHealth();
        if (state.properties.randomization != null) {
            if (combinedInfoMap.size() > 1) {
                writer.write("Scenario: " + combinedInfoMap.get(r).desc() + "\n");
            }
        }
        writer.write("Result: " + (state.isTerminal() == 1 ? "Win" : "Loss") + "\n");
        writer.write("Damage Taken: " + damageTaken + "\n");
        writer.write("Seed: " + result.seed() + "\n");
        boolean usingLine = steps.stream().anyMatch((s) -> s.lines != null);
        if (usingLine && LOG_GAME_USING_LINES_FORMAT) {
            for (GameStep step : steps) {
                if (step.state().actionCtx == GameActionCtx.BEGIN_TURN) continue;
                if (step.lines != null) {
                    writer.write(step.state().toString() + "\n");
                    for (int i = 0; i < Math.min(step.lines.size(), 5); i++) {
                        writer.write("  " + (i + 1) + ". " + step.lines.get(i) + "\n");
                    }
                }
            }
        } else {
            MatchSession.printGame(writer, steps);
        }
        writer.write("\n");
        writer.write("\n");
    }
}
