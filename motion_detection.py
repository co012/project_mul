import cv2
from detection_mask_controller import DetectionMaskController
from debug_controller import DebugController
from input_window import InputWindow


MAIN_WINDOW_NAME = "main_window"
AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi"

inputWindow = InputWindow(AGH_CAM)
inputWindow.show()
if inputWindow.interrupted is True:
    exit(1)


videoSource = inputWindow.videoSource
debugMode = inputWindow.debugMode
minArea = inputWindow.minArea

cv2.namedWindow(MAIN_WINDOW_NAME)
capture = cv2.VideoCapture(videoSource)
wasReadSuccessful, referenceImage = capture.read()
backgroundSubtractor = cv2.bgsegm.createBackgroundSubtractorGSOC()
detectionMaskController = DetectionMaskController(referenceImage, MAIN_WINDOW_NAME)
debugController = DebugController()
if debugMode:
    debugController.active_debug_mode(referenceImage)

while capture.isOpened():
    wasReadSuccessful, image = capture.read()
    if not wasReadSuccessful:
        break

    grayscaleImage = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    reducedNoiseGrayscaleImage = cv2.GaussianBlur(grayscaleImage, (21, 21), 0)
    foregroundMask = backgroundSubtractor.apply(reducedNoiseGrayscaleImage)
    foregroundMaskWithDetectionSpace = detectionMaskController.apply_mask(foregroundMask)
    contours, hierarchy = cv2.findContours(foregroundMaskWithDetectionSpace, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for contour in contours:
        if cv2.contourArea(contour) < minArea:
            continue
        (x, y, w, h) = cv2.boundingRect(contour)
        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0))

    if detectionMaskController.can_rectangle_be_drawn():
        cv2.rectangle(image, *detectionMaskController.points, (0, 0, 255), 2)

    cv2.imshow(MAIN_WINDOW_NAME, image)
    images = [grayscaleImage, reducedNoiseGrayscaleImage, foregroundMask,
              detectionMaskController.mask, foregroundMaskWithDetectionSpace]

    if debugController.debugIsActive:
        debugController.show_debug_window(images)

    key = cv2.waitKey(1)
    if key == ord('d'):
        debugController.toggle_debug(image)
    if key == ord('q') or cv2.getWindowProperty(MAIN_WINDOW_NAME, cv2.WND_PROP_VISIBLE) == 0:
        break


capture.release()
cv2.destroyAllWindows()
