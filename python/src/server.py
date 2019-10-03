# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
from flask import Flask, render_template, Response
import logging
import cv2

# Importing utils
from utils.client import Client
from utils.shape_detector import RemoteShapeDetector
from utils.visual import FrameDrawer
from utils.formater import JsonConverter


# Define Flask server
app = Flask(__name__)

# Removes logging in terminal
log = logging.getLogger('werkzeug')
log.setLevel(logging.ERROR)

# Create GUI threads that access application server
object_client = Client("10.10.10.219", 5056, rate=0.5)
state_client = Client("10.10.10.219", 5056, rate=0.2)
command_client = Client("10.10.10.219", 5056, rate=0)
object_client.command = "GET/Objects"
state_client.command = "GET/Status"
object_client.start()
state_client.start()
command_client.connect()

# Converts string to json
converter = JsonConverter()

# Global video variables
global_frame = None
video_camera = None
objects_received = None


def video_stream():
    """Forwards webcam frame with predictions."""
    global global_frame
    global video_camera
    global objects_received

    if video_camera == None:
        drawer = FrameDrawer()
        video_camera = RemoteShapeDetector(
            '83.243.251.62', 8089)  # '83.243.251.62'
        video_camera.connect()

    while True:
        frame = video_camera.send()

        if frame != None:
            result = drawer.draw_containers(video_camera.frame)
            result = drawer.draw_circles(result, objects_received)
            frame = video_camera.convert_to_jpeg(result)
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
    global objects_received
    objects = []
    msg = object_client.content
    if msg is not None:
        new = msg.split("{")
        objects = new
        objects_received = converter.convert_to_json(msg)
    return render_template('objects.html', objects=objects)


@app.route('/state')
def state():
    """Returns system states from the application server."""
    state = state_client.content
    return render_template('state.html', state=state)


@app.route('/start')
def start():
    """Sends a start call to the system."""
    command = "POST/Start"
    command_client.write(command)
    return "nothing"


@app.route('/reset')
def reset():
    """Sends a reset call to the system."""
    command = "POST/Reset"
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


@app.route('/configure')
def configure():
    """Sends a configure call to the system."""
    command = "POST/Configure"
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


@app.route('/all')
def all():
    """Sends a pick all objects call to the system."""
    command = "POST/All"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.route('/squares')
def squares():
    """Sends a pick all squares call to the system."""
    command = "POST/Square"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.route('/triangles')
def triangles():
    """Sends a pick all triangle call to the system."""
    command = "POST/Triangle"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.route('/circles')
def circles():
    """Sends a pick all circle call to the system."""
    command = "POST/Circle"
    command_client.write(command)
    content = command_client.read()
    return "nothing"


@app.route('/rectangles')
def rectangles():
    """Sends a pick all circle call to the system."""
    command = "POST/Rectangle"
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
