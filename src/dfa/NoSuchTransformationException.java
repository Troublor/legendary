package dfa;

public class NoSuchTransformationException extends Exception {
    private Node node;

    private Object input;

    public NoSuchTransformationException(Node node, Object input) {
        super();
        this.node = node;
        this.input = input;
    }

    public NoSuchTransformationException(Node node, Object input, String msg) {
        super(msg);
        this.node = node;
        this.input = input;
    }

    public Object getInput() {
        return input;
    }

    public Node getNode() {
        return node;
    }
}
