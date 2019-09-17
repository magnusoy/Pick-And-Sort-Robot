
# import the necessary packages
import imutils
import cv2
import numpy as np

class ShapeDetector:
	def __init__(self):
		pass

	def detect(self, c):
		# initialize the shape name and approximate the contour
		shape = "unidentified"
		peri = cv2.arcLength(c, True)
		approx = cv2.approxPolyDP(c, 0.02 * peri, True)

		# if the shape is a triangle, it will have 3 vertices
		if len(approx) > 3 and len(approx) <= 7:
			shape = "triangle"
		
		# if the shape has 4 vertices, it is either a square or
		# a rectangle
		elif len(approx) == 4:
			# compute the bounding box of the contour and use the
			# bounding box to compute the aspect ratio
			(x, y, w, h) = cv2.boundingRect(approx)
			ar = w / float(h)

			# a square will have an aspect ratio that is approximately
			# equal to one, otherwise, the shape is a rectangle
			if ar >= 0.85 and ar <= 1.15:
				shape = "square"
			elif ar > 1.15:
				shape = "rectangle"
	
		# otherwise, we assume the shape is a circle
		elif len(approx) > 7:
			shape = "circle"
		# return the name of the shape
		return shape


cap = cv2.VideoCapture(0)
kernel = np.ones((5, 5), np.uint(8))
lower = np.array([0, 214, 98])
upper = np.array([179, 255, 253])
# load the image and resize it to a smaller factor so that
# the shapes can be approximated better
while True:

	ret, frame = cap.read()
	#resized = imutils.resize(frame, width=300)
	ratio = frame.shape[0] / float(frame.shape[0])
	hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
	mask = cv2.inRange(hsv, lower, upper)
	opening = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=2)
	
	# find contours in the thresholded image and initialize the
	# shape detector
	cnts = cv2.findContours(opening.copy(), cv2.RETR_EXTERNAL,
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
