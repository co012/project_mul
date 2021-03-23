import argparse

AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi"
SOME_VIDEO = "https://static.vecteezy.com/system/resources/" \
             "previews/001/806/954/mp4/two-square-rotation-white-rectangle-animation-free-video.mp4"
DEFAULT_MIN_AREA = 200  # area of smallest possible contour



class ArgumentsClump:

    def __init__(self):
        argumentParser = argparse.ArgumentParser()
        argumentParser.add_argument("-s", "--source", default=AGH_CAM, help="path to video source")
        argumentParser.add_argument("-m", "--mode", help="run script in debug mode", default="normal")
        argumentParser.add_argument("-a", "--min-area", type=int, default=DEFAULT_MIN_AREA, help="area of smallest contour")
        args = vars(argumentParser.parse_args())
        print(args)

        self.videoSource = args.get("source", AGH_CAM)
        self.minArea = args.get("min_area", DEFAULT_MIN_AREA)
        self.mode = args.get("mode", "normal")
