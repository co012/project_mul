import cv2
import argparse
from detection_mask_controller import DetectionMaskController
from debug_controller import DebugController
from input_window import InputWindow

AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi"
SOME_VIDEO = "https://static.vecteezy.com/system/resources/" \
             "previews/001/806/954/mp4/two-square-rotation-white-rectangle-animation-free-video.mp4"
DEFAULT_MIN_AREA = 200  # area of smallest possible contour
MAIN_WINDOW_NAME = "main_window"

argumentParser = argparse.ArgumentParser()
argumentParser.add_argument("-s", "--source", default=AGH_CAM, help="path to video source")
argumentParser.add_argument("-m", "--mode", help="run script in debug mode", default="normal")
argumentParser.add_argument("-a", "--min-area", type=int, default=DEFAULT_MIN_AREA, help="area of smallest contour")
args = vars(argumentParser.parse_args())
print(args)

videoSource = args.get("source", AGH_CAM)
print("source = ", videoSource)

minArea = args.get("min_area", DEFAULT_MIN_AREA)
print("area of smallest contour = ", minArea)

mode = args.get("mode", "normal")
print("mode = ", mode)

inputWindow = InputWindow()
inputWindow.show()
if inputWindow.interrupted is True:
    exit(1)

cv2.namedWindow(MAIN_WINDOW_NAME)
capture = cv2.VideoCapture(AGH_CAM)
wasReadSuccessful, referenceImage = capture.read()
backgroundSubtractor = cv2.bgsegm.createBackgroundSubtractorGSOC()
detectionMaskController = DetectionMaskController(referenceImage, MAIN_WINDOW_NAME)
debugController = DebugController()
if mode == "Debug":
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
    if key == ord('q') or cv2.getWindowProperty(MAIN_WINDOW_NAME, cv2.WND_PROP_VISIBLE == 0):
        break


capture.release()
cv2.destroyAllWindows()
