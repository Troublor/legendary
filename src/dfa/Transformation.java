package src.dfa;

public class Transformation {
    private Node destination;
    private Doable action;
    private Transformable transform;

    public Node getDestination() {
        return destination;
    }

    public Doable getAction() {
        return action;
    }

    public Transformable getTransform() {
        return transform;
    }

    public Transformation(Node destination, Transformable transform, Doable action) {
        this.destination = destination;
        this.action = action;
        this.transform = transform;

    }
}
