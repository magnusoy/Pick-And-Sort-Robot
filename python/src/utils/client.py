#!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing package
import socket
from threading import Thread
import time

class Client(Thread):
    """Client communicating with Server
    through socket connection."""

    def __init__(self, host="127.0.0.1", port=5056):
        Thread.__init__(self)
        self.addr = (host, port)
        self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.is_connected = False
        self.command = "help"
        self.content = None
    
    def run(self):
        self.connect()
        while True:
            self.write(self.command)
            self.content = self.read()
            time.sleep(0.5)
        
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


# Example of usage
if __name__ == "__main__":
    
    object_client = Client("127.0.0.1", 5056)
    help_client = Client("127.0.0.1", 5056)
    object_client.command = "GET/Objects"
    help_client.command = "help"
    object_client.start()
    help_client.start()

    while True:
        msg = object_client.content
        if msg is not None:
            print(msg.split(","))
