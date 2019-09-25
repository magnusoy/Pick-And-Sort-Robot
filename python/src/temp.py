# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
from flask import Flask, render_template, Response, jsonify, request
import cv2


from utils.shape_detector import RemoteShapeDetector

app = Flask(__name__)


# Global video variables
global_frame = None
video_camera = None


def video_stream():
    """Forwards webcame frame with predictions."""
    global global_frame
    global video_camera

    if video_camera == None:
        video_camera = RemoteShapeDetector(
            'localhost', 8089)  # '83.243.219.245'
        video_camera.connect()

    while True:
        frame = video_camera.runEverything()

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


# Running server on localhost:5000
if __name__ == '__main__':
    app.run(host='0.0.0.0',
            port=5000,
            debug=True,
            threaded=True)
