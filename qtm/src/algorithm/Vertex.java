package algorithm;

import java.util.ArrayList;

public class Vertex<T extends Comparable<T>> implements Comparable<Vertex<T>> {
    private T _id;
    private int _degree;

    private int _depth = -1;
    private Vertex<T> _parent;
    private ArrayList<Vertex<T>> _children;
    private int _nextChildIndex = 0;

    public Vertex(T id) {
        this(id, -1);
    }

    public Vertex(T id, int degree) {
        this(id, degree, null, -1);
    }

    public Vertex(T id, int degree, Vertex<T> parent, int depth) {
        _id = id;
        _degree = degree;
        _parent = parent;
        _depth = depth;
        _children = new ArrayList<>();
    }

    public T getId() {
        return _id;
    }

    public Integer getDegree() {
        return _degree;
    }

    public void setDegree(Integer degree) {
        _degree = degree;
    }

    public Vertex<T> getParent() {
        return _parent;
    }

    public void setParent(Vertex<T> parent) {
        _parent = parent;
    }

    public void addChild(Vertex<T> child) {
        _children.add(child);
    }

    public void removeChild(Vertex<T> child) {
        _children.remove(child);
    }

    public void setChildren(ArrayList<Vertex<T>> children) {
        _children = children;
    }

    public ArrayList<Vertex<T>> getChildren() {
        return _children;
    }

    public int getDepth() {
        return _depth;
    }

    public void setDepth(int depth) {
        _depth = depth;
    }

    public void setNextChildIndex(int i) {
        _nextChildIndex = i;
    }

    public int getNextChildIndex() {
        return _nextChildIndex++;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Vertex) {
            Vertex vertex = (Vertex) other;
            return _id == vertex.getId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return _id.hashCode();
    }

    @Override
    public int compareTo(Vertex<T> o) {
        return _id.compareTo(o.getId());
    }

    @Override
    public String toString() {
        return _id.toString();
    }
}
