import PySimpleGUI as sg


class InputWindow:

    def __init__(self, default_src):
        self.videoSource = default_src
        self.layout = [[sg.Text('Movement detector', size=(40, 1), justification='center', font='Helvetica 20')],
                       [sg.Text("Link to a video :")],
                       [sg.Input(default_text=default_src, key="-SOURCE-"), sg.FileBrowse()],
                       [sg.Checkbox('Debug mode', key="-DEBUG-MODE-")],
                       # [sg.Text("Min area :")],
                       # [sg.Input(key="-MIN-AREA-")],
                       [sg.Spin([i for i in range(1, 2000, 50)], initial_value=200, key="-MIN-AREA-"),
                        sg.Text('Min area level')],
                       [sg.Button('Show', size=(10, 1), font='Helvetica 14'), sg.Button('Exit', size=(10, 1), font='Helvetica 14')]]
        self.interrupted = False
        self.debugMode = False
        self.minArea = 200

    def show(self):
        window = sg.Window('Movement detector', self.layout, location=(800, 400))

        while True:
            event, values = window.read()
            if event == 'Exit' or event == sg.WIN_CLOSED:
                self.interrupted = True
                break
            if event == "Show":
                self.videoSource = values["-SOURCE-"]
                try:
                    self.videoSource = int(self.videoSource)
                except ValueError:
                    pass

                self.minArea = int(values["-MIN-AREA-"])
                self.debugMode = values["-DEBUG-MODE-"]
                break

        window.close()
