import cv2
import numpy as np


class DetectionMaskController:

    def __init__(self, img, window_name):
        self.points = [(0, 0), (img.shape[1], img.shape[0])]
        self.mask = self.make_mask(img, *self.points)
        cv2.setMouseCallback(window_name, lambda event, x, y, flags, parameters: self.on_mouse_click(event, x, y))

    @staticmethod
    def make_mask(img, pnt1, pnt2):
        m = np.zeros(img.shape[:2], dtype="uint8")
        cv2.rectangle(m, pnt1, pnt2, 255, -1)
        return m

    def on_mouse_click(self, event, x, y):
        if event == cv2.EVENT_LBUTTONDOWN:
            self.points = [(x, y)]
        elif event == cv2.EVENT_LBUTTONUP:
            self.points.append((x, y))
            self.mask = self.make_mask(self.mask, *self.points[:2])

    def can_rectangle_be_drawn(self):
        return len(self.points) == 2

    def apply_mask(self, img):
        return cv2.bitwise_and(img, img, mask=self.mask)

