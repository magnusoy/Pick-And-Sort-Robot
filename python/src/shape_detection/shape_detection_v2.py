# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# import packages
import imutils
import cv2
import numpy as np


class ShapeDetector:
    """docstring"""

    def __init__(self):
        pass

    def detect(self, c):
        """docstring"""
        shape = "unidentified"
        peri = cv2.arcLength(c, True)
        approx = cv2.approxPolyDP(c, 0.02 * peri, True)

        if len(approx) > 3 and len(approx) <= 7:
            shape = "triangle"

        elif len(approx) == 4:
            (x, y, w, h) = cv2.boundingRect(approx)
            ar = w / float(h)
            if ar >= 0.85 and ar <= 1.15:
                shape = "square"
            elif ar > 1.15:
                shape = "rectangle"

        elif len(approx) > 7:
            shape = "circle"

        return shape


class ShapeDetection:
    """docstring"""

    def __init__(self):
        self.lower = np.array([0, 214, 98])
        self.upper = np.array([179, 255, 253])
        self.kernel = np.ones((5, 5), np.uint(8))
        self.sd = ShapeDetector()
        self.capture = cv2.VideoCapture(0)
        ret = self.capture.set(3, 640)
        ret = self.capture.set(4, 480)

    def run(self, debug=False):
        """docstring"""
        _, frame = self.capture.read()
        ratio = frame.shape[0] / float(frame.shape[0])
        hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
        mask = cv2.inRange(hsv, self.lower, self.upper)
        opening = cv2.morphologyEx(
            mask, cv2.MORPH_OPEN, self.kernel, iterations=2)
        cnts = cv2.findContours(opening.copy(), cv2.RETR_EXTERNAL,
                                cv2.CHAIN_APPROX_SIMPLE)
        cnts = cnts[0] if imutils.is_cv2() else cnts[1]

        if len(cnts) > 0:
            # loop over the contours
            for c in cnts:
                try:
                    M = cv2.moments(c)
                    cX = int((M["m10"] / M["m00"]) * ratio)
                    cY = int((M["m01"] / M["m00"]) * ratio)
                    shape = self.sd.detect(c)
                except(ZeroDivisionError):
                    continue
                c = c.astype("float")
                c *= ratio
                c = c.astype("int")
                cv2.drawContours(frame, [c], -1, (0, 255, 0), 2)
                cv2.putText(frame, shape, (cX, cY), cv2.FONT_HERSHEY_SIMPLEX,
                            0.5, (255, 255, 255), 2)
        if debug:
            cv2.imshow("Frame", frame)

        _, jpeg = cv2.imencode('.jpg', frame)
        return jpeg.tobytes()

    def close(self):
        """Closes the camerafeed."""
        self.capture.release()
        cv2.destroyAllWindows()


# Example of usage
if __name__ == "__main__":
	shape_detector = ShapeDetection()
	while True:
		shape_detector.run(debug=True)