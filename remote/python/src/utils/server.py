# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Import packages
import pickle
import socket
import struct
import cv2


class RemoteShapeDetectorServer:
    """docstring"""

    def __init__(self, host="localhost", port=8089):
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.connection = None
        self.addr = None
        self.initialize()
        self.data = b''
        self.payload_size = struct.calcsize("L")

    def initialize(self, host="localhost", port=8089):
        """docstring"""
        addr = (host, port)
        self.socket.bind(addr)
        self.socket.listen(10)
        self.connection, self.addr = self.socket.accept()

    def get_frame(self):
        """docstring"""
        while len(self.data) < self.payload_size:
            self.data += self.connection.recv(4096)

        packed_msg_size = self.data[:self.payload_size]
        self.data = self.data[self.payload_size:]
        msg_size = struct.unpack("L", packed_msg_size)[0]

        while len(self.data) < msg_size:
            self.data += self.connection.recv(4096)

        frame_data = self.data[:msg_size]
        self.data = self.data[msg_size:]

        frame = pickle.loads(frame_data)
        return frame


# Example of usage
if __name__ == "__main__":
    rsds = RemoteShapeDetectorServer(host="localhost", port=8089)

    while True:
        frame = rsds.get_frame()

        cv2.imshow('frame', frame)
        cv2.waitKey(1)
