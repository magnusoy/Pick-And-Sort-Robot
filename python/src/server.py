# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing libraries
from flask import Flask, render_template, Response, jsonify, request
from object_detection.model import ObjectDetection
import cv2
import sys
import os

video = cv2.VideoCapture(0)
ret = video.set(3, 640)
ret = video.set(4, 480)

os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\resources')

CWD_PATH = os.getcwd()
PATH_TO_CKPT = os.path.join(CWD_PATH,'model','frozen_inference_graph.pb')
PATH_TO_LABELS = os.path.join(CWD_PATH,'model','labelmap.pbtxt')

os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\python\\src')

app = Flask(__name__)

video_camera = None
global_frame = None

@app.route('/')
def index():
    """docstring"""
    return render_template('index.html')

def video_stream():
    """doctring"""
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

@app.route('/video_viewer')
def video_viewer():
    """docstring"""
    return Response(video_stream(),
                    mimetype='multipart/x-mixed-replace; boundary=frame')


# Running server
if __name__ == '__main__':
    app.run(host='0.0.0.0',
            port=5000,
            debug=True,
            threaded=True)
