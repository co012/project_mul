import PySimpleGUI as sg


class InputWindow:

    def __init__(self, default_src):
        self.videoSource = default_src
        self.layout = [[sg.Text('Movement detector', size=(40, 1), justification='center', font='Helvetica 20')],
                       [sg.Text("Link to a video :")],
                       [sg.Input(default_text=default_src), sg.FileBrowse()],
                       [sg.Button('Show', size=(10, 1), font='Helvetica 14'), sg.Button('Exit', size=(10, 1), font='Helvetica 14')]]
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
