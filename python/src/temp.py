# Just for testing pruposes

from object_detection.model import ObjectDetection
import cv2
import sys
import os

os.chdir('C:\\Users\\Magnus\\Documents\\Pick-And-Sort-Robot\\resources')

CWD_PATH = os.getcwd()
PATH_TO_CKPT = os.path.join(CWD_PATH,'model','frozen_inference_graph.pb')
PATH_TO_LABELS = os.path.join(CWD_PATH,'model','labelmap.pbtxt')


video_camera = ObjectDetection(PATH_TO_CKPT, PATH_TO_LABELS)
video_camera.initialize()

video = cv2.VideoCapture(0)
ret = video.set(3, 640)
ret = video.set(4, 480)


while True:
    video_camera.run(video, debug=True)

    # Press 'q' to quit
    if cv2.waitKey(1) == ord('q'):
        break

# Clean up
video.release()
cv2.destroyAllWindows()