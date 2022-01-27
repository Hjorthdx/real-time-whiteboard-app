package com.whiteboardapp.core.pipeline;


import com.whiteboardapp.common.AppUtils;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;
import org.opencv.core.Size;

public class ChangeDetector {
    private Mat prevImgChanges;

    // Detects changes
    // NB: Mats' should be binary images ie. 1 channel
    public Mat detectChanges(Mat img1, Mat img2) {

        Mat currentImgChanges = new Mat();
        Core.absdiff(img1, img2, currentImgChanges);

        Mat imgPersistentChanges = getPersistentChanges(prevImgChanges, currentImgChanges, img1.size());

        prevImgChanges = currentImgChanges;

        return imgPersistentChanges;
    }

    //Returns image with persistent changes if any. Result image will be black background with white changes.
    private Mat getPersistentChanges(Mat prevImgChanges, Mat currentImgChanges, Size size) {

        Mat imgPersistentChanges = Mat.zeros(size, CvType.CV_8UC1);

        if (prevImgChanges != null) {
            // Find changes that survived from previous round and set changes to white.
            // Make persistent changes white on black background.

//            for (int i = 0; i < prevImgChanges.rows(); i++) {
//                for (int j = 0; j < prevImgChanges.cols(); j++) {
//                    if (prevImgChanges.get(i, j)[0] == 255 && currentImgChanges.get(i, j)[0] == 255) {
//                        imgPersistentChanges.put(i, j, 255);
//                    }
//                }
//            }

            byte[] bufferPrevChanges = AppUtils.getBuffer(prevImgChanges);
            byte[] bufferCurrentChanges = AppUtils.getBuffer(currentImgChanges);
            byte[] bufferPersistentChanges = AppUtils.getBuffer(imgPersistentChanges);

            for (int i = 0; i < bufferPrevChanges.length; i++) {
                if (bufferPrevChanges[i] == -1 && bufferCurrentChanges[i] == -1) {
                    bufferPersistentChanges[i] = -1;
                }
            }
            imgPersistentChanges.put(0, 0, bufferPersistentChanges);
        }

        return imgPersistentChanges;
    }
}
