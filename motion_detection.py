import cv2
import argparse

AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi?start=1543408695"
DEFAULT_MIN_AREA = 200  # area of smallest possible contour

argumentParser = argparse.ArgumentParser()
argumentParser.add_argument("-s", "--source", default=AGH_CAM, help="path to video source")
argumentParser.add_argument("-D", "-DEBUG", help="run script in debug mode")
argumentParser.add_argument("-a", "--min-area", type=int, default=DEFAULT_MIN_AREA, help="area of smallest contour")
args = vars(argumentParser.parse_args())

videoSource = args.get("source", AGH_CAM)
print("source = ", videoSource)

minArea = args.get("min-area", DEFAULT_MIN_AREA)
print("area of smallest contour = ", minArea)

cv2.namedWindow("main_window")
capture = cv2.VideoCapture(videoSource)
backgroundSubtractor = cv2.bgsegm.createBackgroundSubtractorGSOC()

while capture.isOpened():
    wasReadSuccessful, image = capture.read()
    if not wasReadSuccessful:
        continue

    grayscaleImage = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
    reducedNoiseGrayscaleImage = cv2.GaussianBlur(grayscaleImage, (21, 21), 0)
    foregroundMask = backgroundSubtractor.apply(reducedNoiseGrayscaleImage)
    contours, hierarchy = cv2.findContours(foregroundMask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

    for contour in contours:
        if cv2.contourArea(contour) < minArea:
            continue
        (x, y, w, h) = cv2.boundingRect(contour)
        cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0))

    cv2.imshow("main_window", image)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

capture.release()
cv2.destroyAllWindows()
