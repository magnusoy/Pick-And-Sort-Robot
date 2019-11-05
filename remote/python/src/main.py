# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Import packages
import cv2
import os

# Importing model and utility
from object_detection.model import ObjectDetector
from utils.server import RemoteShapeDetectorServer
from utils.client import DetectionDataSender

# Change working directory to get the inference graph and labelmap
os.chdir('C:\\Users\\Petter\\Documents\\Pick-And-Sort-Robot\\resources')

# Get file paths to frozen inference graph and labelmap
CWD_PATH = os.getcwd()
PATH_TO_CKPT = os.path.join(CWD_PATH, 'model', 'frozen_inference_graph.pb')
PATH_TO_LABELS = os.path.join(CWD_PATH, 'model', 'labelmap.pbtxt')

# Change working directory back to source
os.chdir('C:\\Users\\Petter\\Documents\\Pick-And-Sort-Robot\\remote\\python\\src')

# Initialize object detector model
object_detector = ObjectDetector(PATH_TO_CKPT, PATH_TO_LABELS)
object_detector.initialize()

# Creates a TCP Server
rsds = RemoteShapeDetectorServer(host="0.0.0.0", port=8089)


# Collects frames recieved from client on server
# Computes the Object detection
# and stores them in file.
if __name__ == "__main__":
    while True:
        frame = rsds.get_frame()

        if len(frame) > 280:  # Check if resolution match
            object_detector.run(frame, debug=False)
        cv2.waitKey(1)
