package com.alphaStS.model;

import com.alphaStS.GameState;

public interface Model {
    NNOutput eval(GameState state);
    void close();
    void startRecordCalls();
    int endRecordCalls();
}
