# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Import packages
import cv2
import sys
import os

from object_detection.model import ObjectDetector
from utils.server import RemoteShapeDetectorServer

os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\resources')

CWD_PATH = os.getcwd()
PATH_TO_CKPT = os.path.join(CWD_PATH, 'model', 'frozen_inference_graph.pb')
PATH_TO_LABELS = os.path.join(CWD_PATH, 'model', 'labelmap.pbtxt')

# Change working directory back to source
os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\remote\\python\\src')

object_detector = ObjectDetector(PATH_TO_CKPT, PATH_TO_LABELS)
object_detector.initialize()

rsds = RemoteShapeDetectorServer(host="localhost", port=8089)

while True:
    frame = rsds.get_frame()
    if len(frame) == 240:
        object_detector.run(frame, debug=False)
    cv2.waitKey(1)
