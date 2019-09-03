#!/usr/bin/env python
 
import socket
 
HOST = "localhost"
PORT = 5056
 
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.connect((HOST, PORT))
 
sock.sendall("GET/Status\n".encode())
data = sock.recv(1024)
print(data.decode())

while True:
    msg = input("type: ")
    msg = msg + "\n"
    sock.sendall(msg.encode())
    data = sock.recv(1024)
    print(data.decode())
sock.close()

