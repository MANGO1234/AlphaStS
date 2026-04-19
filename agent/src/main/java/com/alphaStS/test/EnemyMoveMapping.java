package com.alphaStS.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class EnemyMoveMapping {

    public final String alphaStSName;
    /** Maps STS-internal move ID to alphaStS move index. */
    public final HashMap<Integer, Integer> moves;

    private static HashMap<String, EnemyMoveMapping> mapping;

    public EnemyMoveMapping(String alphaStSName, HashMap<Integer, Integer> moves) {
        this.alphaStSName = alphaStSName;
        this.moves = moves;
    }

    /** Returns the mapping keyed by STS internal name, lazily loaded from resources. */
    public static HashMap<String, EnemyMoveMapping> getMapping() {
        if (mapping != null) {
            return mapping;
        }
        try (var stream = EnemyMoveMapping.class.getResourceAsStream("/sts_monster_move_mapping.json")) {
            JsonNode root = new ObjectMapper().readTree(stream);
            mapping = new HashMap<>();
            for (var it = root.fields(); it.hasNext();) {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode data = entry.getValue();
                String stsInternalName = data.path("sts_internal_name").asText();
                String alphaStSName = data.path("alphaStS_name").asText();
                HashMap<Integer, Integer> moves = new HashMap<>();
                for (var mi = data.path("moves").fields(); mi.hasNext();) {
                    Map.Entry<String, JsonNode> m = mi.next();
                    moves.put(Integer.parseInt(m.getKey()), m.getValue().asInt());
                }
                mapping.put(stsInternalName, new EnemyMoveMapping(alphaStSName, moves));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sts_monster_move_mapping.json", e);
        }
        return mapping;
    }
}
