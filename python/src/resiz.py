import cv2
import numpy as np
 
cap = cv2.VideoCapture(1)
cap.set(3, 640)
cap.set(4, 480)
 
while True:
    ret, frame = cap.read()
    roi = frame[0: 480, 89: 553]

    cv2.imshow("frame", roi)
 
    key = cv2.waitKey(1)
    if key == 27:
        break
 
 
cap.release()
cv2.destroyAllWindows()