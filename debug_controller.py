import cv2
DEBUG_WINDOW_NAME = "debug"


class DebugController:

    def __init__(self):
        self.debugIsActive = False
        self.debugWindowName = DEBUG_WINDOW_NAME

    def active_debug_mode(self, image):
        self.debugIsActive = True
        cv2.namedWindow(self.debugWindowName)
        cv2.moveWindow("debug", int(2.1 * image.shape[0]), 110)

    def deactivate_debug_mode(self):
        self.debugIsActive = False
        cv2.destroyWindow(self.debugWindowName)

    def toggle_debug(self, img):
        if self.debugIsActive:
            self.deactivate_debug_mode()
        else:
            self.active_debug_mode(img)

    def show_debug_window(self, images):
        if cv2.getWindowProperty(self.debugWindowName, cv2.WND_PROP_VISIBLE) == 0:
            self.deactivate_debug_mode()
            return
        image = images[0]
        height = int(image.shape[0] / len(images))
        width = int(images[0].shape[1] * height / images[0].shape[0])
        images_resized = [cv2.resize(im, (width, height), interpolation=cv2.INTER_CUBIC) for im in images]
        debug_window = cv2.vconcat(images_resized)
        cv2.imshow(self.debugWindowName, debug_window)

