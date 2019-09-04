#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing package
import socket

class Client:
    """Client communicating with Server
    through socket connection."""

    def __init__(self, host="127.0.0.1", port=5056):
        self.addr = (host, port)
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.is_connected = False

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
        self.socket.close()

    def write(self, msg):
        """Write message to server."""
        msg = msg + "\n"
        self.socket.sendall(msg.encode())

    def read(self):
        """Read received data from server."""
        msg = self.socket.recv(4096)
        return msg.decode("latin-1")

import time

# Example of usage
if __name__ == "__main__":
    
    client = Client("127.0.0.1", 5056)
    client.connect()

    while client.is_connected:
        client.write("GET/Objects")
        msg = client.read()
        print(msg)
        time.sleep(0.2)