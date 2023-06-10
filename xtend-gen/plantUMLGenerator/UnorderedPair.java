package plantUMLGenerator;

public class UnorderedPair<T> {
    private T first;
    private T second;

    public UnorderedPair(T first, T second) {
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnorderedPair<?> pair = (UnorderedPair<?>) o;
        return (first.equals(pair.first) && second.equals(pair.second)) ||
                (first.equals(pair.second) && second.equals(pair.first));
    }

    @Override
    public int hashCode() {
        return first.hashCode() + second.hashCode();
    }
    
    public boolean has(T e) {
    	return e.equals(first) || e.equals(second);
    }

    @Override
    public String toString() {
        return "(" + first + ", " + second + ")";
    }
}