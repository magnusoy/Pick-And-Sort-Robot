# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
import imutils
import cv2
import numpy as np

class ShapeDetector:
	def __init__(self):
		pass

	def detect(self, c):
		shape = "unidentified"
		peri = cv2.arcLength(c, True)
		approx = cv2.approxPolyDP(c, 0.04 * peri, True)

		if len(approx) == 3:
			shape = "triangle"

		elif len(approx) == 4:
			(x, y, w, h) = cv2.boundingRect(approx)
			ar = w / float(h)
			if ar >= 0.95 and ar <= 1.05:
				shape = "square"
			elif ar >= 1.05 and ar <= 1.2:
				shape = "rectangle"
		else:
			shape = "circle"

		return shape


cap = cv2.VideoCapture(0)
# load the image and resize it to a smaller factor so that
# the shapes can be approximated better
while True:

	ret, frame = cap.read()
	resized = imutils.resize(frame, width=300)
	ratio = frame.shape[0] / float(resized.shape[0])

	# convert the resized image to grayscale, blur it slightly,
	# and threshold it
	gray = cv2.cvtColor(resized, cv2.COLOR_BGR2GRAY)
	blurred = cv2.GaussianBlur(gray, (5, 5), 0)
	thresh = cv2.threshold(blurred, 60, 255, cv2.THRESH_BINARY)[1]

	# find contours in the thresholded image and initialize the
	# shape detector
	cnts = cv2.findContours(thresh.copy(), cv2.RETR_EXTERNAL,
		cv2.CHAIN_APPROX_SIMPLE)
	cnts = cnts[0] if imutils.is_cv2() else cnts[1]
	sd = ShapeDetector()
	if len(cnts) > 0:
		# loop over the contours
		for c in cnts:
			# compute the center of the contour, then detect the name of the
			# shape using only the contour

			#c = np.int8(c)
			try:
				M = cv2.moments(c)
				cX = int((M["m10"] / M["m00"]) * ratio)
				cY = int((M["m01"] / M["m00"]) * ratio)
				shape = sd.detect(c)
			except(ZeroDivisionError):
				continue
			# multiply the contour (x, y)-coordinates by the resize ratio,
			# then draw the contours and the name of the shape on the image
			c = c.astype("float")
			c *= ratio
			c = c.astype("int")
			cv2.drawContours(frame, [c], -1, (0, 255, 0), 2)
			cv2.putText(frame, shape, (cX, cY), cv2.FONT_HERSHEY_SIMPLEX,
				0.5, (255, 255, 255), 2)

	# show the output image
	cv2.imshow("Image", frame)
	if cv2.waitKey(1) & 0xFF == ord('q'):
		break

# When everything done, release the capture
cap.release()
cv2.destroyAllWindows()
