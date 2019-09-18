# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
from flask import Flask, render_template, Response, jsonify, request
import cv2

# Importing utility and shape detection
from shape_detection.shape_detection_v2 import ShapeDetection
from utils.client import Client


app = Flask(__name__)

# Create GUI threads that access application server
object_client = Client("10.10.10.219", 5056, rate=0.1)
state_client = Client("10.10.10.219", 5056, rate=0.2)
command_client = Client("10.10.10.219", 5056, rate=0)
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
        video_camera = ShapeDetection()
    while True:
        frame = video_camera.run(debug=False)
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
