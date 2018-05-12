package src.dfa;

public class NoSuchTransformationException extends Exception {
    private Node node;

    private Input input;

    public NoSuchTransformationException(Node node, Input input) {
        super();
        this.node = node;
        this.input = input;
    }

    public NoSuchTransformationException(Node node, Input input, String msg) {
        super(msg);
        this.node = node;
        this.input = input;
    }
}
