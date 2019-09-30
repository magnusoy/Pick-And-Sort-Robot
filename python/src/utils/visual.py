# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing packages
import cv2
import json


class FrameDrawer():
    """Used to draw shapes and boundries on frame"""

    def __init__(self):
        self.radius = 20
        self.shapecolor = (255, 0, 0)
        self.circlethickness = 2
        self.linethickness = 2
        self.linebegin = (0, 100)  # x1, y1
        self.lineend = (640, 100)  # x2, , y2
        self.linecolor = (0, 255, 0)
        self.font = cv2.FONT_HERSHEY_SIMPLEX
        self.fontscale = 1
        self.fontcolor = (255, 255, 255)

    def draw_circles(self, frame, shapes):
        """Draws circles around shapes on frame."""
        result = frame
        for shape in shapes:
            obj = json.loads(shape)
            center_coordinates = (obj['x'], obj['y'])
            result = cv2.circle(result, center_coordinates,
                                self.radius, self.shapecolor, self.circlethickness)
        return result

    def draw_containers(self, frame):
        """Draw object container on frame for referance."""
        result = frame
        cv2.line(result, self.linebegin, self.lineend,
                 self.linecolor, self.linethickness)
        cv2.line(result, (640*0.25, 100), (640*0.25, 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (640*0.50, 100), (640*0.50, 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (640*0.75, 100), (640*0.75, 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (640, 100), (640, 0),
                 self.linecolor, self.linethickness)
        cv2.putText(result, "Rectangle", (100, 100), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        cv2.putText(result, "Circle", (100, 100), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        cv2.putText(result, "Triangle", (100, 100), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        cv2.putText(result, "Square", (100, 100), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        return result
