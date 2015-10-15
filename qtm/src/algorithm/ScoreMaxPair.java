package algorithm;

import structure.Vertex;

public class ScoreMaxPair<V extends Comparable<V>> {
    private Vertex<V> _bestParent;
    private int _scoreMax;

    protected ScoreMaxPair(Vertex<V> bestParent, int scoreMax) {
        _bestParent = bestParent;
        _scoreMax = scoreMax;
    }

    public Vertex<V> getBestParent() {
        return _bestParent;
    }

    public int getScoreMax() {
        return _scoreMax;
    }
}
