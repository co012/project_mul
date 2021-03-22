import cv2
import argparse
import numpy as np

AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi?start=1543408695"
DEFAULT_MIN_AREA = 200  # area of smallest possible contour


def make_mask(img, pnt1, pnt2):
    m = np.zeros(img.shape[:2], dtype="uint8")
    cv2.rectangle(m, pnt1, pnt2, 255, -1)
    return m


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

cv2.namedWindow("main_window")
capture = cv2.VideoCapture(videoSource)
wasReadSuccessful, image = capture.read()
backgroundSubtractor = cv2.bgsegm.createBackgroundSubtractorGSOC()
mask = make_mask(image, (0, 0), image.shape[:2])

points = []
drawing = False

while capture.isOpened():
    wasReadSuccessful, image = capture.read()
    if not wasReadSuccessful:
        continue

    # # -------------- Zosia
    #
    # clone = image.copy()
    #
    # def draw(event, x, y, flags, parameters):
    #     global points, drawing
    #
    #     if event == cv2.EVENT_LBUTTONDOWN:
    #         points = [(x, y)]
    #         drawing = True
    #     elif event == cv2.EVENT_LBUTTONUP:
    #         points.append((x, y))
    #         drawing = False
    #
    #         cv2.rectangle(clone, points[0], points[1], (0, 0, 255), 2)
    #         cv2.imshow("image", clone)
    #
    #
    # cv2.setMouseCallback('main_window', draw)
    #
    # # Nie reaguje na przyciski do konca
    # if cv2.waitKey(1) & 0xFF == ord('y') or cv2.waitKey(1) & 0xFF == ord('n'):
    #     cv2.destroyWindow('image')
    #
    # # -------------- Zosia

    grayscaleImage = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    grayscaleImage_2 = cv2.cvtColor(grayscaleImage,cv2.COLOR_GRAY2BGR)
    reducedNoiseGrayscaleImage = cv2.GaussianBlur(grayscaleImage_2, (21, 21), 0)
    foregroundMask = backgroundSubtractor.apply(reducedNoiseGrayscaleImage)
    foregroundMask_2 = cv2.cvtColor(foregroundMask,cv2.COLOR_GRAY2BGR)
    foregroundMaskWithDetectionSpace = cv2.bitwise_and(foregroundMask, foregroundMask, mask=mask)
    foregroundMaskWithDetectionSpace_2 = cv2.cvtColor(foregroundMaskWithDetectionSpace,cv2.COLOR_GRAY2BGR)
    contours, hierarchy = cv2.findContours(foregroundMaskWithDetectionSpace, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    images = [grayscaleImage_2, reducedNoiseGrayscaleImage, foregroundMask_2, foregroundMaskWithDetectionSpace_2]
    height = int(image.shape[0] * 0.25)
    width = int(images[0].shape[1] * height / images[0].shape[0])
    imagesResized = [cv2.resize(im, (width,height), interpolation=cv2.INTER_CUBIC) for im in images]
    debugWindow = cv2.vconcat(imagesResized)

    mainHeight = len(images)*height
    mainWidth = int(mainHeight/image.shape[0] * image.shape[1])
    image = cv2.resize(image, (mainWidth, mainHeight),interpolation=cv2.INTER_CUBIC)

    for contour in contours:
        if cv2.contourArea(contour) < minArea:
            continue
        (x, y, w, h) = cv2.boundingRect(contour)
        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0))

    final = cv2.hconcat([debugWindow, image])

    cv2.imshow("main_window", final)
    #cv2.imshow("debug",debug_window)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

capture.release()
cv2.destroyAllWindows()
