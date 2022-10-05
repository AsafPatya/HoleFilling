package holeFilling;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.HashSet;
import java.util.Set;

public class ImageManager
{
    private final Set<Point> holePointsSet;
    private final Set<Point> borderPointsSet;

    // with imageFloatMatrix and maskFloatMatrix we will work to do all the calculates
    private final float[][] imageFloatMatrix;
    private final float[][] maskFloatMatrix;

    // just for the beginning, and for load from path
    private final Mat imageMatrix;
    private final Mat maskMatrix;

    private final DefaultWeightFunction weightFunctionObject;
    private final int numberOfRows;             // will be 512
    private final int numberOfColumns;          // will be 512
    private final boolean is4Connected;         // flag

    public final  float PIXEL_THRESHOLD = 0.5f;
    public final  double PIXEL_HIGH_VALUE = 255.0;


    /**
     * Builder for image manager
     *
     * @param path_to_image
     * @param path_to_mask
     * @param is_4_connection_flag
     * @param weightFunctionObject hold the weight function
     */
    public ImageManager(String path_to_image, String path_to_mask, boolean is_4_connection_flag,
                        DefaultWeightFunction weightFunctionObject)
    {
        this.holePointsSet = new HashSet<Point>();
        this.borderPointsSet = new HashSet<Point>();

        this.imageMatrix = Imgcodecs.imread(path_to_image, 0);
        this.maskMatrix = Imgcodecs.imread(path_to_mask, 0);

        this.numberOfRows = imageMatrix.rows();
        this.numberOfColumns = imageMatrix.cols();

        this.imageFloatMatrix = this.MatrixToFloatMatrix(imageMatrix);
        this.maskFloatMatrix = this.MatrixToFloatMatrix(maskMatrix);

        this.weightFunctionObject = weightFunctionObject;

        this.is4Connected = is_4_connection_flag;

    }

    /**
     *  convert the mat to a float matrix with values in [0, 1]
     * @param mat
     * @return
     */
    private float[][] MatrixToFloatMatrix(Mat mat)
    {
        float[][] tempFloatMatrix = new float[numberOfRows][numberOfColumns];

        for (int row = 0; row < this.numberOfRows; row++)
        {
            for (int col = 0; col < this.numberOfColumns; col++)
            {
                // the mat.get return a doubleArray, so we take only the first value
                double[] tempDoubleArray = mat.get(row, col);
                double tempValue = tempDoubleArray[0];
                tempValue = tempValue/PIXEL_HIGH_VALUE;
                tempFloatMatrix[row][col] = (float) tempValue;
            }
        }
        return tempFloatMatrix;
    }

    /**
     *  return the values to range [0, 255]
     */
    public void ImageFloatMatrixToMatrix()
    {

        for (int row=0; row<numberOfRows; row++)
        {
            for (int col = 0; col < numberOfColumns; col++)
            {
                double valToInsert = imageFloatMatrix[row][col];
                valToInsert *= PIXEL_HIGH_VALUE;
                imageMatrix.put(row, col, valToInsert);
            }
        }
    }

    /**
     * We will go over each point in the mask, and check if the value of the point is less than the threshold I set
     * (I chose about 0.5).
     * If so, we will add this point to the hole field of the object.
     *
     * According to the instructions of the question, the points at the end of the mask are border points,
     * so there is no point in going over them
     * @return Set<Points> of hole points
     */
    public void CalculateHoleSet()
    {
        int numberOfRows = this.numberOfRows;
        int numberOfColumns = this.numberOfColumns;

        // we don't want to check the boundaries:
        numberOfRows -= 1;
        numberOfColumns -= 1;


        for (int row = 1; row < numberOfRows; row++)
        {
            for (int col = 1; col < numberOfColumns; col++)
            {
                double tempValue = maskFloatMatrix[row][col];
                if (tempValue < PIXEL_THRESHOLD)
                {
                    Point newPoint = new Point(row, col);
                    holePointsSet.add(newPoint);
                }
            }
        }
    }

    /**
     * In this function we will find all the points that are border points of points that are in a hole,
     * and put you inside the borderPoints.
     *
     * for each point in the hole point, we will check her 4 or 8 neighbors:
     *                                             -1 0 -1
     *                                           -1
     *                                            0   p
     *                                            1
     *  because hole points cant be in the border, no need to check the validity of the indexes in the loop
     */
    public void CalculateBorderSet()
    {

        for (Point holePoint : holePointsSet)
        {
            int x = holePoint.getX();
            int y = holePoint.getY();

            for (int neighborX = -1; neighborX < 2; neighborX++)
            {
                for (int neighborY = -1; neighborY < 2; neighborY++)
                {
                    int currPointX = x + neighborX;
                    int currPointY = y + neighborY;
                    // in this case, this it a Point
                    if (neighborX == 0 && neighborY == 0)
                        continue;

                    // case of (-1, -1) or (-1, 1) or (1, -1) or (1, 1)
                    boolean currPointInCorner = (Math.abs(neighborX) + Math.abs(neighborY) == 2);

                    //it might be point that in the hole, need to check it
                    double currPointPixel = maskFloatMatrix[currPointX][currPointY];
                    boolean isCurrPointInHole = currPointPixel < PIXEL_THRESHOLD;

                    // check if the curr point is Neighbor:  if in the corner and not 4 connected, then it not a Neighbor
                    boolean isCurrPointNeighbor = (!currPointInCorner && is4Connected);

                    if (!isCurrPointInHole && isCurrPointNeighbor)
                    {
                        holeFilling.Point currPoint = new Point(currPointX, currPointY);
                        borderPointsSet.add(currPoint);
                    }
                }
            }
        }
    }

    /**
     *  We will go over the points that are hole points.
     *  For each such point, we will perform the algorithm given to us in the question,
     *  and perform a placement for the new pixel.
     */
    public void FillHolesInImage()
    {

        for (Point holePoint : holePointsSet)
        {
            float Iu_value = CalculateIu(holePoint);
            SetPixelToImageFloatMatrix(holePoint, Iu_value);
        }
    }


    /**
     *
     * @param holePoint - this is the u point from the question
     * @return  image with changes to the hole points
     */
    private float CalculateIu(Point holePoint)
    {
        float numeratorEquation = 0;
        float denominatorEquation = 0;

        for (Point borderPoint : borderPointsSet)
        {
            float weightFunctionValue = weightFunctionObject.weightedFunction(holePoint, borderPoint);
            numeratorEquation += weightFunctionValue * GetPixelFromImageFloatMatrix(borderPoint);
            denominatorEquation += weightFunctionValue;
        }
        return numeratorEquation / denominatorEquation;
    }

    private void SetPixelToImageFloatMatrix(Point holePoint, float Iu_value)
    {
        int xVal = holePoint.getX();
        int yVal = holePoint.getY();
        imageFloatMatrix[xVal][yVal] = Iu_value;
    }

    public float GetPixelFromImageFloatMatrix(Point borderPoint)
    {
        int xVal = borderPoint.getX();
        int yVal = borderPoint.getY();
        return this.imageFloatMatrix[xVal][yVal];
    }

    public Mat GetImageMatrix()
    {
        return imageMatrix;
    }

    /**
     *  Question number 2 in the exercise.
     * What happens is that I will calculate the average of the X rates and the average of the Y rates of the hole points.
     * After here, I will run the function I(u) once to get some value.
     * I will insert this value into the image, and it will be the same value for all the hole points.
     * Hereby, the time complexity will be O(n)
     */
    public void ApproximateFill()
    {

        //Calculate the avg coordinate position
        double XValues=0, XAvg = 0;
        double YValues=0, YAvg = 0;
        double numberOfPointsInHole = holePointsSet.size();
        for (Point hole_point : holePointsSet)
        {
            XValues += hole_point.getX();
            YValues += hole_point.getY();
        }

        XAvg = XValues / numberOfPointsInHole;
        YAvg = YValues / numberOfPointsInHole;

        Point avg_Point = new Point((int)XAvg, (int)YAvg);

        float I_Value = CalculateIu(avg_Point);

        for (Point hole_coordinate : holePointsSet)
        {
            SetPixelToImageFloatMatrix(hole_coordinate, I_Value);
        }
    }
}
