
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

class ShapeDetection:
	def __init__(self):
		self.lower = np.array([0, 214, 98])
		self.upper = np.array([179, 255, 253])
		self.kernel = np.ones((5, 5), np.uint(8))
	
	def run(self, frame, debug=False):
		ret, frame = cap.read()
		ratio = frame.shape[0] / float(frame.shape[0])
		hsv = cv2.cvtColor(frame, cv2.COLOR_BGR2HSV)
		mask = cv2.inRange(hsv, lower, upper)
		opening = cv2.morphologyEx(mask, cv2.MORPH_OPEN, kernel, iterations=2)
		cnts = cv2.findContours(opening.copy(), cv2.RETR_EXTERNAL,
			cv2.CHAIN_APPROX_SIMPLE)
		cnts = cnts[0] if imutils.is_cv2() else cnts[1]
		sd = ShapeDetector()
		if len(cnts) > 0:
			# loop over the contours
			for c in cnts:
				try:
					M = cv2.moments(c)
					cX = int((M["m10"] / M["m00"]) * ratio)
					cY = int((M["m01"] / M["m00"]) * ratio)
					shape = sd.detect(c)
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
		
		if cv2.waitKey(1) & 0xFF == ord('q'):
			self.close()

		_ , jpeg = cv2.imencode('.jpg', frame)
		return jpeg.tobytes()
	
	def close(self):
		cap.release()
		cv2.destroyAllWindows()
