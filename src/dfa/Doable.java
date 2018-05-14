package dfa;

@FunctionalInterface
public interface Doable {
    void doIt(dfa.Node destNode, Object input);
}
