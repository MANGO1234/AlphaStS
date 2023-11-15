package com.alphaStS;

public record CardCount(Card card, int count) {
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        CardCount cardCount = (CardCount) o;
        return card.cardName.equals(cardCount.card.cardName);
    }

    @Override
    public int hashCode() {
        return card.cardName.hashCode();
    }
}
