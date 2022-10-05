package holeFilling;

public class Point
{
    int xVal;
    int yVal;

    public Point(int x, int y)
    {
        this.xVal = x;
        this.yVal = y;
    }

    public int getX() {
        return xVal;
    }

    public int getY() {
        return yVal;
    }


//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//        Points that = (Points) o;
//        return getX() == that.getX() && getY() == that.getY();
//    }
//
//    /**
//     * Overides the hash function of Coordinate to identify it by (X,Y) and not by the reference address
//     * @return hash value of (X,Y)
//     */
//    @Override
//    public int hashCode() {
//        return Objects.hash(getX(), getY());
//    }
}
