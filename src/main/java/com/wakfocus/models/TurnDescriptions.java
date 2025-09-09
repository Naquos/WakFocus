package com.wakfocus.models;

public class TurnDescriptions {
    private String characterName;
    private boolean isPlayerTurn;
    private boolean isEndTurn;

    public TurnDescriptions(String characterName, boolean isPlayerTurn, boolean isEndTurn) {
        this.characterName = characterName;
        this.isPlayerTurn = isPlayerTurn;
        this.isEndTurn = false;
    }

    public String getCharacterName() {
        return characterName;
    }

    public void setCharacterName(String characterName) {
        this.characterName = characterName;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public void setPlayerTurn(boolean isPlayerTurn) {
        this.isPlayerTurn = isPlayerTurn;
    }
    
    public boolean isEndTurn() {
        return isEndTurn;
    }

    public void setEndTurn(boolean isEndTurn) {
        this.isEndTurn = isEndTurn;
    }
}
