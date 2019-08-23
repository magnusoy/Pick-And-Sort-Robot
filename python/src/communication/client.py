import socket
import sys

class Client:

    def __init__(self, host, port):
        self.host = host
        self.port = port
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((host, port))
            self.socket.setblocking(False)
        except expression as identifier:
            sys.exit(1)
        
    
    def __str__(self):
        return self.socket

    def send(self, msg):
        try:

            self.socket.sendall(msg.encode('UTF-8'))
        finally:
            return True
        
    
    def receive(self):
        return self.socket.recv(1024).decode('UTF-8')

if __name__ == "__main__":
    client = Client("localhost", 5056)
    client.send("Hello")