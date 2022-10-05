package holeFilling;

import org.opencv.core.Core;
import org.opencv.imgcodecs.Imgcodecs;

public class Main {
    static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}

    public static void main(String[] args){
        // read the image, the mask and the 4-or8 flag
        String path_to_image = args[0];
        String path_to_mask = args[1];
        boolean is_4_connection_flag = (Integer.parseInt(args[2]) == 4);
        DefaultWeightFunction weightFunctionObject = new DefaultWeightFunction();

        ImageManager Image_Manager = new ImageManager(path_to_image,path_to_mask, is_4_connection_flag,
                weightFunctionObject);
        Image_Manager.CalculateHoleSet();
        Image_Manager.CalculateBorderSet();
        Image_Manager.FillHolesInImage();
        Image_Manager.ImageFloatMatrixToMatrix();
        Imgcodecs.imwrite("out/ModifiedImages/Fixed.png", Image_Manager.GetImageMatrix());

        ImageManager Image_Manager2 = new ImageManager(path_to_image,path_to_mask, is_4_connection_flag,
                weightFunctionObject);
        Image_Manager2.CalculateHoleSet();
        Image_Manager2.CalculateBorderSet();
        Image_Manager2.ApproximateFill();
        Image_Manager2.ImageFloatMatrixToMatrix();
        Imgcodecs.imwrite("out/ModifiedImages/LazyFix.png", Image_Manager2.GetImageMatrix());
    }
}