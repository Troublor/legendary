package dfa;

@FunctionalInterface
public interface Transformable {
    boolean transform(Node from, Object input);
}
