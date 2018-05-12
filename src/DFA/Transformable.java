package src.dfa;

@FunctionalInterface
public interface Transformable {
    boolean transform(Node from, Input input);
}
