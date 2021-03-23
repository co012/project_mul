import PySimpleGUI as sg
AGH_CAM = "http://live.uci.agh.edu.pl/video/stream1.cgi"


class InputWindow:

    layout = [[sg.Text('Movement detector', size=(40, 1), justification='center', font='Helvetica 20')],
              [sg.Text("Link to a video :")],
              [sg.Input(default_text=AGH_CAM)],
              [sg.Button('Show', size=(10, 1), font='Helvetica 14'), sg.Button('Exit', size=(10, 1), font='Helvetica 14') ]]

    def __init__(self):
        self.videoSource = AGH_CAM
        self.interrupted = False

    def show(self):
        window = sg.Window('Movement detector', self.layout, location=(800, 400))

        while True:
            event, values = window.read()
            if event == 'Exit' or event == sg.WIN_CLOSED:
                self.interrupted = True
                break
            if event == "Show":
                self.videoSource = values[0]
                break

        window.close()

