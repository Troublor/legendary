package dfa;

public class InvalidTransformationException extends Exception {
    public InvalidTransformationException(){
        super();
    }

    public InvalidTransformationException(String msg) {
        super(msg);
    }
}
