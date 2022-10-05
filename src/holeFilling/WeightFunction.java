package holeFilling;

/**
 * interface that describes the signature of the weight function given in the question
 */

public interface WeightFunction {

    float weightedFunction(Point Point1, Point Point2);
}