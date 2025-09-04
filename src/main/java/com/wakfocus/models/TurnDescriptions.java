package com.wakfocus.models;

public class TurnDescriptions {
    private String characterName;
    private boolean isPlayerTurn;

    public TurnDescriptions(String characterName, boolean isPlayerTurn) {
        this.characterName = characterName;
        this.isPlayerTurn = isPlayerTurn;
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
}
