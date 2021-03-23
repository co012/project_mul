import cv2
import argparse
import numpy as np

AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi?start=1543408695"
SOME_VIDEO = "https://static.vecteezy.com/system/resources/previews/001/806/954/mp4/two-square-rotation-white-rectangle-animation-free-video.mp4"
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

mode = args.get("mode", "normal")
print("mode = ", mode)

cv2.namedWindow("main_window")
capture = cv2.VideoCapture(videoSource)
wasReadSuccessful, image = capture.read()
backgroundSubtractor = cv2.bgsegm.createBackgroundSubtractorGSOC()
mask = make_mask(image, (0, 0), (image.shape[1], image.shape[0]))

points = []
drawing = False

while capture.isOpened():
    wasReadSuccessful, image = capture.read()
    if not wasReadSuccessful:
        break

    # -------------- Zosia

    def draw(event, x, y, flags, parameters):
        global points, drawing, mask

        if event == cv2.EVENT_LBUTTONDOWN:
            points = [(x, y)]
            drawing = True
        elif event == cv2.EVENT_LBUTTONUP:
            points.append((x, y))
            drawing = False
            mask = make_mask(mask, *points)


    cv2.setMouseCallback('main_window', draw)

    # Nie reaguje na przyciski do konca
    if cv2.waitKey(1) & 0xFF == ord('y') or cv2.waitKey(1) & 0xFF == ord('n'):
        cv2.destroyWindow('image')

    # -------------- Zosia

    grayscaleImage = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    reducedNoiseGrayscaleImage = cv2.GaussianBlur(grayscaleImage, (21, 21), 0)
    foregroundMask = backgroundSubtractor.apply(reducedNoiseGrayscaleImage)
    foregroundMaskWithDetectionSpace = cv2.bitwise_and(foregroundMask, foregroundMask, mask=mask)
    contours, hierarchy = cv2.findContours(foregroundMaskWithDetectionSpace, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for contour in contours:
        if cv2.contourArea(contour) < minArea:
            continue
        (x, y, w, h) = cv2.boundingRect(contour)
        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0))

    images = [grayscaleImage, reducedNoiseGrayscaleImage, foregroundMask, foregroundMaskWithDetectionSpace]
    height = int(image.shape[0] * 0.25)
    width = int(images[0].shape[1] * height / images[0].shape[0])
    imagesResized = [cv2.resize(im, (width,height), interpolation=cv2.INTER_CUBIC) for im in images]
    debugWindow = cv2.vconcat(imagesResized)

    cv2.imshow("main_window", image)
    #cv2.imshow("mask", mask)
    cv2.imshow("debug",debugWindow)
    cv2.moveWindow("debug", int(2.1* image.shape[0]), 110)

    if cv2.waitKey(1) & 0xFF == ord('q'):
     break

capture.release()
cv2.destroyAllWindows()
