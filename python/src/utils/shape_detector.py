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
import json


class VideoCamera:
    """docstring"""

    def __init__(self, source=0, resolution=(640, 480)):
        self.capture = cv2.VideoCapture(source)
        ret = self.capture.set(3, resolution[0])
        ret = self.capture.set(4, resolution[1])

    def run(self):
        """docstring"""
        _, frame = self.capture.read()
        return frame


class RemoteShapeDetector(Thread):
    """docstring"""

    def __init__(self, host="127.0.0.1", port=8089):
        Thread.__init__(self)
        self.addr = (host, port)
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.is_connected = False
        self.terminate = False
        self.content = None
        self.video_camera = VideoCamera(source=1)
        self.frame = None

    def run(self):
        """docstring"""
        self.connect()
        while not self.terminate:
            self.frame = self.video_camera.run()
            data = pickle.dumps(self.frame)
            message_size = struct.pack("L", len(data))
            self.write(message_size + data)

    def connect(self):
        """Establish a secure connection to server."""
        try:
            self.socket.connect(self.addr)
        except OSError:
            pass
        finally:
            self.is_connected = True

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
        return self.frame


# Example of usage
if __name__ == "__main__":
    detector = RemoteShapeDetector('localhost', 8089)
    detector.start()
