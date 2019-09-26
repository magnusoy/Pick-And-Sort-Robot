# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Import packages
import cv2
import sys
import os

# Importing utility and model
from server import RemoteShapeDetectorServer
from client import DetectionDataSender

# Creates a TCP Server
rsds = RemoteShapeDetectorServer(host="0.0.0.0", port=8089)

# Create a thread reading from file and sends it to Jetson
#sender = DetectionDataSender("localhost", 5056) # TODO: Fix up address to match Jetson
#sender.start()

# Collects frames recieved from client on server
# Computes the Object detection and sends back
# JSON of the results.
while True:
    frame = rsds.get_frame()
    print("Receiving frame")
