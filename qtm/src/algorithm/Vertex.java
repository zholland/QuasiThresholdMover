package algorithm;

public class Vertex<T> {
    T _id;
    Integer _degree;
    Vertex _parent;

    public Vertex(T id, int degree) {
        this(id, degree, null);
    }

    public Vertex(T id, int degree, Vertex<T> parent) {
        _id = id;
        _degree = degree;
        _parent = parent;
    }

    public T getId() {
        return _id;
    }

    public Integer getDegree() {
        return _degree;
    }

    public Vertex getParent() {
        return _parent;
    }

    public void setParent(Vertex parent) {
        _parent = parent;
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
}
