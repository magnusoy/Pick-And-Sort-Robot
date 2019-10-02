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


class VideoCamera:
    """Handles videocamera input."""

    def __init__(self, source=0, resolution=(640, 480)):
        self.capture = cv2.VideoCapture(source)
        ret = self.capture.set(3, resolution[0])
        ret = self.capture.set(4, resolution[1])

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
        # Create a region of interest that excludes the sorted zone
        roi = self.frame[100: 480, 0: 460]
        data = pickle.dumps(roi)
        # Change to "L" if windows <-> Windows
        message_size = struct.pack("=L", len(data))
        self.write(message_size + data)
        _, jpeg = cv2.imencode('.jpg', roi)
        return jpeg.tobytes()

    def convert_to_jpeg(self, frame):
        _, jpeg = cv2.imencode('.jpg', frame)
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
    from formater import JsonConverter

    vc = VideoCamera()
    drawer = FrameDrawer()
    converter = JsonConverter()
    obj = '["{"object": 0, "type": "triangle", "x": 203, "y": 256, "probability": "99"}, {"object": 1, "type": "rectangle", "x": 105, "y": 78, "probability": "91"}, "]'

    while True:
        frame = vc.run()
        data = converter.convert_to_json(obj)
        result1 = drawer.draw_containers(frame)
        result2 = drawer.draw_circles(result1, data)
        cv2.imshow("show", result2)
        cv2.waitKey(1)
