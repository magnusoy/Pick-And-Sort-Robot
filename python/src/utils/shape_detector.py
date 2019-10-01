# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
import cv2
import numpy as np
import socket
import sys
import pickle
import struct
from threading import Thread
import time
import imutils


class VideoCamera:
    """Handles videocamera input."""

    def __init__(self, source=0, resolution=(640, 480)):
        self.capture = cv2.VideoCapture(source)
        ret = self.capture.set(3, resolution[0])
        ret = self.capture.set(4, resolution[1])
        self.kernel = np.ones((5, 5), np.uint8)
        self.lower_cal_color = np.array([30, 37, 106])
        self.upper_cal_color = np.array([64, 133, 197])
        self.calibration_roi = []

    def calibrate(self):
        """Run region of interest calibration."""
        _, frame = self.capture.read()
        hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, self.lower_cal_color, self.upper_cal_color)
        erosion = cv2.erode(mask, self.kernel)
        blurred_frame = cv2.GaussianBlur(erosion, (5, 5), 0)
        canny = cv2.Canny(blurred_frame, 100, 150)
        cnts = cv2.findContours(canny.copy(), cv2.RETR_EXTERNAL,
                                cv2.CHAIN_APPROX_SIMPLE)
        cnts = cnts[0] if imutils.is_cv2() else cnts[1]
        if len(cnts) > 0:
            for c in cnts:
                try:
                    M = cv2.moments(c)
                    center = (int(M["m10"] / M["m00"]),
                              int(M["m01"] / M["m00"]))
                    self.calibration_roi.append(center)
                except(ZeroDivisionError):
                    continue
        #roi = frame[0: 465, 94: 530]
        # return roi

    def run(self):
        """Returns current frame."""
        _, frame = self.capture.read()
        roi = frame[0: 480, 89: 553]
        return roi


class RemoteShapeDetector(Thread):
    """Client that connect and send frames to remote computer."""

    def __init__(self, host="127.0.0.1", port=8089):
        Thread.__init__(self)
        self.addr = (host, port)
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.is_connected = False
        self.terminate = False
        self.content = None
        self.video_camera = VideoCamera(source=0)
        self.frame = None

    def run(self):
        """Connect and send frame in different thread."""
        self.connect()
        while not self.terminate:
            self.frame = self.video_camera.run()
            data = pickle.dumps(self.frame)
            # Change to "L" if windows <-> Windows
            message_size = struct.pack("=L", len(data))
            self.write(message_size + data)

    def send(self):
        """Convert and send frame as bytes."""
        self.frame = self.video_camera.run()
        data = pickle.dumps(self.frame)
        # Change to "L" if windows <-> Windows
        message_size = struct.pack("=L", len(data))
        self.write(message_size + data)
        _, jpeg = cv2.imencode('.jpg', self.frame)
        return jpeg.tobytes()

    def connect(self):
        """Establish a secure connection to server."""
        try:
            self.socket.connect(self.addr)
        except OSError:
            pass
        finally:
            self.is_connected = True
            time.sleep(3)

    def disconnect(self):
        """Close connection."""
        self.terminate = True
        Thread.join(self)
        self.socket.close()

    def write(self, msg: str):
        """Write message to server."""
        self.socket.sendall(msg)

    def read(self) -> str:
        """Read received data from server."""
        msg = self.socket.recv(4096)

        return msg.decode("latin-1")

    def get_frame(self):
        """ Returns current videoframe."""
        _, jpeg = cv2.imencode('.jpg', self.frame)
        return jpeg.tobytes()


"""
# Example of usage
if __name__ == "__main__":
    detector = RemoteShapeDetector('83.243.219.245', 8089)  # 83.243.219.245
    detector.connect()
    while True:
        detector.runEverything()
        cv2.imshow("show", detector.frame)
        cv2.waitKey(1)
"""


# Example of usage
if __name__ == "__main__":
    from visual import FrameDrawer

    vc = VideoCamera()
    drawer = FrameDrawer()
    while True:
        frame = vc.run()
        result = drawer.draw_containers(frame)
        cv2.imshow("show", result)
        cv2.waitKey(1)
