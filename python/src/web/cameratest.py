
import cv2
import numpy as np
 
cams_test = 1000
for i in range(0, cams_test):
    cap = cv2.VideoCapture(i)
    test, frame = cap.read()
    if test:

        print("i : "+str(i)+" /// result: "+str(test))