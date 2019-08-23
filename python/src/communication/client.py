import socket
import sys

class Client:

    def __init__(self, host, port):
        self.host = host
        self.port = port
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.connect((host, port))
        except expression as identifier:
            sys.exit(1)
        self.socket.setblocking(False)
    
    def send(self, msg):
        self.socket.send(msg.encode('UTF-8'))
    
    def receive(self):
        return self.socket.recv(1024).decode('UTF-8')

if __name__ == "__main__":
    client = Client("localhost", 5056)

    while True:

        msg = input("Message:")
        client.send(msg)

        try:
            while True:
                    
                response = client.receive()
                print(response)

        except IOError as e:
            # We just did not receive anything
            continue

        except Exception as e:
            # Any other exception - something happened, exit
            print('Reading error: '.format(str(e)))
            sys.exit()