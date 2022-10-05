package holeFilling;


/**
 *  This class describes the weight function described in the question.
 *  Epsilon and z are configurable and changeable.
 *  Note that this is an implementation of the interface described in the package,
 *  and it is also easy to change the function and override it.
 *
 */
public class DefaultWeightFunction implements WeightFunction
{

    private final float epsilon = 0.01f;
    private final double z = 3;

    @Override
    public float weightedFunction(Point Point1, Point Point2)
    {
        float diffX = Point1.getX() - Point2.getX();
        float diffY = Point1.getY() - Point2.getY();
        float result = epsilon + (float) (Math.pow(Math.pow(diffX, 2) + Math.pow(diffY, 2), z/2));
        result = 1/result;
        return result;
    }
}
