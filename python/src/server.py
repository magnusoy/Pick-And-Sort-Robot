# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
from flask import Flask, render_template, Response, jsonify, request
from object_detection.model import ObjectDetection
import cv2
import sys
import os
from utils.client import Client
import time

# Opens webcamera, changing the resolution
video = cv2.VideoCapture(0)
ret = video.set(3, 640)
ret = video.set(4, 480)

# Change working directory for accessing inference graph and labelmap
os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\resources')

CWD_PATH = os.getcwd()
PATH_TO_CKPT = os.path.join(CWD_PATH, 'model', 'frozen_inference_graph.pb')
PATH_TO_LABELS = os.path.join(CWD_PATH, 'model', 'labelmap.pbtxt')

# Cahge working directory back to source for initializing flask server
os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\python\\src')

app = Flask(__name__)

# Create GUI threads that access application server
object_client = Client("127.0.0.1", 5056, rate=0.1)
state_client = Client("127.0.0.1", 5056, rate=0.2)
command_client = Client("127.0.0.1", 5056, rate=0)
object_client.command = "GET/Objects"
state_client.command = "GET/Status"
object_client.start()
state_client.start()
command_client.connect()

# Global video variables
video_camera = None
global_frame = None


def video_stream():
    """Forwards webcame frame with predictions."""
    global video_camera
    global global_frame

    if video_camera == None:
        video_camera = ObjectDetection(PATH_TO_CKPT, PATH_TO_LABELS)
        video_camera.initialize()

    while True:
        frame = video_camera.run(video)
        if frame != None:
            global_frame = frame
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + frame + b'\r\n\r\n')
        else:
            yield (b'--frame\r\n'
                   b'Content-Type: image/jpeg\r\n\r\n' + global_frame + b'\r\n\r\n')


@app.route('/')
def index():
    """Route to main page."""
    return render_template('index.html')


@app.route('/video_viewer')
def video_viewer():
    """Returns the video stream from webcamera."""
    return Response(video_stream(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


@app.route('/objects')
def objects():
    """Returns object list from the application server."""
    object_list = []
    msg = object_client.content
    if msg is not None:
        msg = msg.split("{")
        object_list = msg
    return render_template('objects.html', objects=object_list)


@app.route('/state')
def state():
    """Returns system states from the application server."""
    state = "S_IDLE"
    return render_template('state.html', state=state)


@app.route('/start')
def start():
    """Sends a start call to the system."""
    command = "POST/Start"
    command_client.write(command)
    return "nothing"


@app.route('/stop')
def stop():
    """Sends a stop call to the system."""
    command = "POST/Stop"
    command_client.write(command)
    return "nothing"


@app.route('/calibrate')
def calibrate():
    """Sends a calibrate call to the system."""
    command = "POST/Calibrate"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.route('/manual')
def manual():
    """Sends a manual override call to the system."""
    command = "POST/Manual"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.route('/automatic')
def automatic():
    """Sends a automatic run call to the system."""
    command = "POST/Automatic"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.errorhandler(404)
def page_not_found(e):
    """Returns 404, because path is not created."""
    return render_template('404.html'), 404

# Running server on localhost:5000
if __name__ == '__main__':
    app.run(host='0.0.0.0',
            port=5000,
            debug=True,
            threaded=True)
