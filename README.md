# Current Product Goal
Introduce colors to the current implementation while enhancing performance to be real time (~30 fps).

# Real Time Whiteboard App

Android app that digitizes whiteboards and papers in real time developed as part of our bachelor project at AAU (BSc Software Engineering).

<img src="./doc/demo.gif" width="600" />

## About

This app is an implementation of our paper **Digitizing whiteboard and papers in real time using a mobile device**.

The system combines several techniques within
the computer vision area in order to develop a complete system
for capturing a whiteboard or paper and make it presentable
for an audience in real time. Our system is implemented
on a regular smartphone, thus enabling users to do remote
presentations without requiring any extra tools. We divide
our method into five major steps that take care of a specific
part of the problem.  

The five major steps are:
* **Corner detection** by detecting the largest edge in the picture image. This is assumed to be the whiteboard as the whiteboard should be the largest shape in the image. For each corner of the image we find the closest point in the edge and assume this is the whiteboard corner.
* **Perspective transformation** corrects the angle of the image as the camera may not always be placed directly in front of the whiteboard. As such we obtain a birds eye view of the whiteboard.
* **Segmentation** detects the part of the presenter that is in front of the whiteboard. This is done using a pre-trained Deeplab v3 model.
* **Binarization** binarizes the image using an adaptive threshold, thereby ending up with a simple black and white image, where pen strokes are enhanced and noise are reduced. 
* **Change detection** compares the binarized image with a stored model of the current binarized whiteboard. As the semgmentated area contains the presenter, changes in this area are ignored. Remaining changes are registered into the current binarized whiteboard model.

## Dependencies
- TensorFlow Lite Task Library v0.1.0 
- OpenCV Android library v3.4.14 downloaded manually from https://opencv.org/releases/ and included in project folder `openCVLibrary3414` 
- Other dependencies are listed in `app/build.gradle`

## Technical details
Tested with physical device:
- Huawei Honor 7 Lite, Android OS 6.0 (API 23), 1920x1080 display

Developed using emulator with settings:
- Pixel 2, Android OS 11.0 (API 30), 1.5 GB RAM (default), 5 GB internal storage, 1920x1080 display

## Running the app
Open the project in Android Studio and run the app in an emulator (e.g. the one above) with API version >= 23

## Authors
- Christian Damsgaard
- Simon Holst
- Søren Hjorth Boelskifte


  


