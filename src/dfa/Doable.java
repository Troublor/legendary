package dfa;

@FunctionalInterface
public interface Doable {
    void doIt(Node destNode, Object input);
}
