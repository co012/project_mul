import PySimpleGUI as sg
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


points = []
drawing = False
hasRectangle = False
mask = None


def main():
    global hasRectangle, drawing, points, mask
    #sg.theme('LightGrey')

    layout = [[sg.Text('OpenCV Demo', size=(40, 1), justification='center', font='Helvetica 20')],
              [sg.Text("Link to a video :")],
              [sg.Input()],
              [sg.Button('Ok')],
              [sg.Button('Show', size=(10, 1), font='Helvetica 14'),
               sg.Button('Exit', size=(10, 1), font='Helvetica 14'), ]]

    window = sg.Window('Demo Application - OpenCV Integration', layout, location=(800, 400))

    argumentParser = argparse.ArgumentParser()
    argumentParser.add_argument("-s", "--source", default=AGH_CAM, help="path to video source")
    argumentParser.add_argument("-m", "--mode", help="run script in debug mode", default="normal")
    argumentParser.add_argument("-a", "--min-area", type=int, default=DEFAULT_MIN_AREA, help="area of smallest contour")
    args = vars(argumentParser.parse_args())
    print(args)

    event = None
    values = []
    while event != 'Ok':
        event, values = window.read()
        if event == 'Exit' or event == sg.WIN_CLOSED:
            return

    videoSource = values[0]
    print("source = ", videoSource)

    minArea = args.get("min_area", DEFAULT_MIN_AREA)
    print("area of smallest contour = ", minArea)

    mode = args.get("mode", "normal")
    print("mode = ", mode)
    
    capture = cv2.VideoCapture(videoSource)
    recording = False

    while event != 'Show':
        event, values = window.read(timeout=20)
        if event == 'Exit' or event == sg.WIN_CLOSED:
            return

        elif event == 'Show':
            recording = True

    if recording:
        cv2.namedWindow("main_window")
        capture = cv2.VideoCapture(videoSource)
        wasReadSuccessful, image = capture.read()
        backgroundSubtractor = cv2.bgsegm.createBackgroundSubtractorGSOC()
        mask = make_mask(image, (0, 0), (image.shape[1], image.shape[0]))

        points = []
        drawing = False
        hasRectangle = False

        while capture.isOpened():
            wasReadSuccessful, image = capture.read()
            if not wasReadSuccessful:
                break

            def draw(event, x, y, flags, parameters):
                global points, drawing, mask, hasRectangle

                if event == cv2.EVENT_LBUTTONDOWN:
                    points = [(x, y)]
                    drawing = True
                elif event == cv2.EVENT_LBUTTONUP:
                    points.append((x, y))
                    drawing = False
                    mask = make_mask(mask, *points)
                    hasRectangle = True

            cv2.setMouseCallback('main_window', draw)

            grayscaleImage = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)
            reducedNoiseGrayscaleImage = cv2.GaussianBlur(grayscaleImage, (21, 21), 0)
            foregroundMask = backgroundSubtractor.apply(reducedNoiseGrayscaleImage)
            foregroundMaskWithDetectionSpace = cv2.bitwise_and(foregroundMask, foregroundMask, mask=mask)
            contours, hierarchy = cv2.findContours(foregroundMaskWithDetectionSpace, cv2.RETR_EXTERNAL,
                                                   cv2.CHAIN_APPROX_SIMPLE)

            for contour in contours:
                if cv2.contourArea(contour) < minArea:
                    continue
                (x, y, w, h) = cv2.boundingRect(contour)
                cv2.rectangle(image, (x, y), (x + w, y + h), (255, 0, 0))

            images = [grayscaleImage, reducedNoiseGrayscaleImage, foregroundMask, foregroundMaskWithDetectionSpace]
            height = int(image.shape[0] * 0.25)
            width = int(images[0].shape[1] * height / images[0].shape[0])
            imagesResized = [cv2.resize(im, (width, height), interpolation=cv2.INTER_CUBIC) for im in images]
            debugWindow = cv2.vconcat(imagesResized)

            if hasRectangle:
                cv2.rectangle(image, points[0], points[1], (0, 0, 255), 2)
            cv2.imshow("main_window", image)
            # cv2.imshow("mask", mask)
            cv2.imshow("debug", debugWindow)
            cv2.moveWindow("debug", int(2.1 * image.shape[0]), 110)

            if cv2.waitKey(1) & 0xFF == ord('q'):
                break

    capture.release()
    cv2.destroyAllWindows()


main()