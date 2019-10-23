# #!/usr/bin/env python3
# -*- coding: utf-8 -*-

# Importing OpenCV
import cv2


class FrameDrawer():
    """Used to draw shapes and boundries on frame"""

    def __init__(self):
        self.radius = 20
        self.shapecolor = (0, 165, 255)
        self.circlethickness = 2
        self.linethickness = 2
        self.linebegin = (5, 100)  # x1, y1
        self.lineend = (460, 100)  # x2, , y2
        self.linecolor = (0, 255, 0)
        self.font = cv2.FONT_HERSHEY_SIMPLEX
        self.fontscale = 0.5
        self.fontcolor = (255, 255, 255)

    def draw_circles(self, frame, shapes):
        """Draws circles around shapes on frame."""
        result = frame
        for shape in shapes:
            center_coordinates = (shape['x'], shape['y'])
            result = cv2.circle(result, center_coordinates,
                                self.radius, self.shapecolor, self.circlethickness)
            cv2.putText(result, shape['type'], center_coordinates,
                        self.font, self.fontscale*0.8, self.fontcolor, 1)
        return result

    def draw_containers(self, frame):
        """Draw object container on frame for referance."""
        result = frame
        resolution = 285
        cv2.line(result, self.linebegin, self.lineend,
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*0.01), 150), (int(resolution*0.01), 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*0.29), 150), (int(resolution*0.29), 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*0.57), 150), (int(resolution*0.57), 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*0.82), 150), (int(resolution*0.82), 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*1.06), 150), (int(resolution*1.06), 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*1.31), 150), (int(resolution*1.31), 0),
                 self.linecolor, self.linethickness)
        cv2.line(result, (int(resolution*1.62), 150), (int(resolution*1.62), 0),
                 self.linecolor, self.linethickness)
        cv2.putText(result, "Rectangle", (84, 20), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        cv2.putText(result, "Circle", (164, 20), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        cv2.putText(result, "Triangle", (234, 20), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        cv2.putText(result, "Square", (304, 20), self.font,
                    self.fontscale, self.fontcolor, self.linethickness)
        return result
