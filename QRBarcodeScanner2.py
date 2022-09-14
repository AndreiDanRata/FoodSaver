#General
import numpy as np
import datetime
from datetime import date
import time
import random
from random import choice
import string
import threading

#Computer Vision
import cv2
from imutils.video import VideoStream
from pyzbar import pyzbar
import imutils
#Firebase
import pyrebase
#Others
import argparse
from pathlib import Path
import RPi.GPIO as GPIO

from pydub import AudioSegment
from pydub.playback import play


#For Api
import requests
import json

class MyScript:
    def GetBarcodeApiData(barcodeData):
        #api example: https://api.upcdatabase.org/product/0111222333446?apikey=4368CFAB9E2F2B72DBBDA6EE20DD7877
        BARCODE_API_KEY ="4368CFAB9E2F2B72DBBDA6EE20DD7877"
        BARCODE_BASE_URL = "https://api.upcdatabase.org/product/"
        requestUrl = BARCODE_BASE_URL + str(barcodeData) +"?apikey=" + BARCODE_API_KEY

        response = requests.get(requestUrl)

        if(response.json()['success']==True):
            #text=json.dumps(response.json(),sort_keys=True,indent=4)
            if response.json()['title'] != "":
                print (response.json()['title'])
                return response.json()['title']
                
            elif response.json()['description'] != "":
                print (response.json()['description'])
                return response.json()['description']
        else:
            print("Item not found")
            return 0
    
    def GetRandomKey():
        key=""
        alphabet = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
        for _ in range(10):
             key += choice(alphabet)
        return key
    def loginUser(barcodeData):
        play(soundLoggedIn)
        print("User scanned their QR code")
        userFirebase = barcodeData.replace("userFirebaseUID:","")   #trim the input 
        file = open("userFirebase.txt", "w")
        file.write(userFirebase)
    def addItemToDatabase(barcodeData):
        key =  MyScript.GetRandomKey()
        #if it is a qr code with the requested format
        if("Name/Expiration:" in barcodeData):
            play(soundBeep)
            splitData = barcodeData.split(sep="/+/", maxsplit=-1)
            db.child("foodItems").child(userFirebase).child(key).child("itemName").set(splitData[1])
            db.child("foodItems").child(userFirebase).child(key).child("itemExpirationDate").set(splitData[2])
            db.child("foodItems").child(userFirebase).child(key).child("key").set(key)
            db.child("foodItems").child(userFirebase).child(key).child("toDonate").set(False)
        #if it is a barcode
        elif(barcodeData.isnumeric() == True):
            name = MyScript.GetBarcodeApiData(barcodeData)
            if name != 0:
                play(soundBeep)
                #expirationDate = date.today().strftime("%-d/%-m/%Y")
                expirationDate = "11/11/2000"    
                print(barcodeData)
                #Add items to firebase database
                db.child("foodItems").child(userFirebase).child(key).child("itemName").set(name)
                db.child("foodItems").child(userFirebase).child(key).child("itemExpirationDate").set(expirationDate)
                db.child("foodItems").child(userFirebase).child(key).child("key").set(key)
                db.child("foodItems").child(userFirebase).child(key).child("toDonate").set(False)
                print("successfuly added to database")
                
            else:
                play(soundNotFound) #add sound for item not found in the api
        else: 
            play(soundNotFound)#add sound for item not found in the api
    def removeItemFromDatabase(barcodeData):
        
        if("Name/Expiration:" in barcodeData):
            play(soundBeep)
            splitData = barcodeData.split(sep="/+/", maxsplit=-1)
            items = db.child("foodItems").child(userFirebase).order_by_child("itemName").equal_to(splitData[1]).get()  #gets a response object
            date = splitData[2]
            itemKey = ""
            for item in items.each():
                itemVal=item.val() #the value
                if(date == itemVal['itemExpirationDate']):  #the more recent date has bigger value
                    itemKey = item.key()
                    break
            if(itemKey != ""):
                print("The element (qr code) was removed from the database ")
                db.child("foodItems").child(userFirebase).child(itemKey).remove()
        #if it is a barcode
        elif(barcodeData.isnumeric() == True):
            name = MyScript.GetBarcodeApiData(barcodeData)
            if name != 0:
                items=db.child("foodItems").child(userFirebase).order_by_child("itemName").equal_to(name).get()  #gets a response object
                play(soundBeep)
                shortestExpDateKey = ""
                shortestExpDate = datetime.datetime(2025,5,5)
                for item in items.each():
                    itemVal=item.val() #the value
                    date = datetime.datetime.strptime(itemVal['itemExpirationDate'], '%d/%M/%Y')
                    if(date<shortestExpDate):  #the more recent date has bigger value
                        shortestExpDate = date
                        shortestExpDateKey = item.key()
                if(shortestExpDateKey != ""):
                    print("The element (barcode) was removed from the database ")
                    db.child("foodItems").child(userFirebase).child(shortestExpDateKey).remove()
                #print(db.child("foodItems").child(userFirebase).child(shortestExpDateKey).get().val())
            else:
                play(soundNotFound) #add sound for item not found in the api
        else: 
            play(soundNotFound)#add sound for item not found in the api
    class camThread(threading.Thread):
        def __init__(self, previewName, camID):
            threading.Thread.__init__(self)
            self.previewName = previewName
            self.camID = camID
        def run(self):
            if(self.camID == 0):
                print ("Starting " + self.previewName)
                MyScript.camAddItems(self.previewName, self.camID)
            elif(self.camID == 1):
                print ("Starting " + self.previewName)
                MyScript.camrRemoveItems(self.previewName, self.camID)

    def camAddItems(previewName, camID):
        cv2.namedWindow(previewName)
        cam = cv2.VideoCapture(0, apiPreference=cv2.CAP_V4L2)
        cam.set(cv2.CAP_PROP_FRAME_WIDTH,550)
        cam.set(cv2.CAP_PROP_FRAME_HEIGHT,310)
        cam.set(cv2.CAP_PROP_FPS,15)

        time.sleep(2.0)

        lastBarcodeData = ""
        lastBarcodeDataAdded = ""

        if cam.isOpened():  # try to get the first frame
            rval, frame = cam.read()
        else:
            rval = False

        while rval:
            cv2.imshow(previewName, frame)
            rval, frame = cam.read()

            # width = int(frame.shape[1] * 1)
            # height = int(frame.shape[0] *1)
            # frame = cv2.resize(frame, (width, height))
            
            detections = pyzbar.decode(frame)                     
        
            for barcode in detections:
                #create rectangle around the code
                (x, y, w, h) = barcode.rect
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
                barcodeData = barcode.data.decode("utf-8")      
                #authentificate user
                if ("userFirebaseUID:" in barcodeData) and (barcodeData != lastBarcodeData):
                    MyScript.loginUser(barcodeData)
                #if the last 2 scanned codes are the same then add the item to the database(lowers misidentifications of the barcode)
                elif barcodeData  != lastBarcodeDataAdded and barcodeData != lastBarcodeData:
                    if len(userFirebase) == 0 or len(userFirebase) == 1:
                        play(soundLink)
                    else:
                        MyScript.addItemToDatabase(barcodeData)
                        lastBarcodeDataAdded = barcodeData
                lastBarcodeData = barcodeData

            key = cv2.waitKey(20)
            if key == 27:  # exit on ESC
                break
        print("[INFO] cleaning up...")
        cam.release()
        cv2.destroyWindow(previewName)

    def camrRemoveItems(previewName, camID):
        cv2.namedWindow(previewName)
        cam = cv2.VideoCapture(2, apiPreference=cv2.CAP_V4L2)
        cam.set(cv2.CAP_PROP_FRAME_WIDTH,550)
        cam.set(cv2.CAP_PROP_FRAME_HEIGHT,310)
        cam.set(cv2.CAP_PROP_FPS,15)

        time.sleep(2.0)

        lastBarcodeData = ""
        lastBarcodeDataRemoved = ""

        if cam.isOpened():  # try to get the first frame
            rval, frame = cam.read()
        else:
            rval = False

        while rval:
            cv2.imshow(previewName, frame)
            rval, frame = cam.read()
        
            # width = int(frame.shape[1] * 1)
            # height = int(frame.shape[0] *1)
            # frame = cv2.resize(frame, (width, height))
            



            # #IMPORVE EFFICIENCY
            # grey = cv2.cvtColor(cam, cv2.COLOR_BGR2GRAY)
            # _, thresh =cv2.threshold(grey,120,255,cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)
            
            # kernel = np.ones((4, 4), np.uint8)  
            # thresh = cv2.dilate(thresh, kernel, iterations=1)
            # contours, _ = cv2.findContours(thresh, cv2.RETR_LIST, cv2.CHAIN_APPROX_SIMPLE)
            
            # bboxes = []


            # for cnt in contours:
            #     area = cv2.contourArea(cnt)
            #     xmin, ymin, width, height = cv2.boundingRect(cnt)
            #     extent = area / (width * height)
            #     # filter non-rectangular objects and small objects
            #     #if (extent > np.pi / 5) and (area > 50):
            #     # min(width,height)/max(width,height)) > 0.2  and
            #     if area > 400:
            #         bboxes.append((xmin, ymin, xmin + width, ymin + height))
            
            
            
            # for xmin, ymin, xmax, ymax in bboxes:
                
            #     roi = cam[ymin:ymax, xmin:xmax]

            detections = pyzbar.decode(frame)   #change to roi to use the optimization alg                  
            
            for barcode in detections:
            
                #create rectangle around the code
                (x, y, w, h) = barcode.rect
                cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 0, 255), 2)
                
                barcodeData = barcode.data.decode("utf-8")
                
                #authentificate user
                if ("userFirebaseUID:" in barcodeData) and (barcodeData != lastBarcodeData):
                    MyScript.loginUser(barcodeData)
                #if the last 2 scanned codes are the same then add the item to the database(lowers misidentifications of the barcode)
                elif barcodeData  != lastBarcodeDataRemoved and barcodeData != lastBarcodeData:
                    if len(userFirebase) == 0 or len(userFirebase) == 1:
                        play(soundLink)
                    else:
                        MyScript.removeItemFromDatabase(barcodeData)
                        lastBarcodeDataRemoved = barcodeData
                        print("Item removed")
                lastBarcodeData = barcodeData
        
        
        
            key = cv2.waitKey(20)
            if key == 27:  # exit on ESC
                break
        cam.release()
        cv2.destroyWindow(previewName)
        
        
#firebase
config = {
    "apiKey":"AIzaSyBw2KpLu-3Xm7oFl6jPYhRqe0LQ7LLWWMc",
    "authDomain":"finalyearproject-3d868.firebaseapp.com",
    "databaseURL": "https://finalyearproject-3d868-default-rtdb.europe-west1.firebasedatabase.app",
    "storageBucket": "finalyearproject-3d868.appspot.com"
    }
firebase = pyrebase.initialize_app(config)
db = firebase.database()
#pyrebase bug: to work with order_by_child I have to add this
def noquote(s):
     return s
pyrebase.pyrebase.quote = noquote



# works with vs
# soundLink = AudioSegment.from_mp3("LinkToAccount.mp3")
# soundBeep = AudioSegment.from_mp3("Barcode-scanner-beep-sound.mp3")
# soundLoggedIn = AudioSegment.from_mp3("UserLogged.mp3")
# soundNotFound = AudioSegment.from_mp3("ItemNotFound.mp3")


# works with vs
soundLink = AudioSegment.from_wav("/home/andrei/Downloads/test/LinkToAccount.wav")
soundBeep = AudioSegment.from_wav("/home/andrei/Downloads/test/Barcode-scanner-beep-sound.wav")
soundLoggedIn = AudioSegment.from_wav("/home/andrei/Downloads/test/UserLogged.wav")
soundNotFound = AudioSegment.from_wav("/home/andrei/Downloads/test/ItemNotFound.wav")




#user authentification
fle = Path('userFirebase.txt')
fle.touch(exist_ok=True)  #if file does not exist, then create it.... otherwise do nothing

userFile = open('userFirebase.txt', 'r')
userFirebase = userFile.read()
print(userFirebase)
userFile.close()

if len(userFirebase) == 0 or len(userFirebase) == 1:
    play(soundLink)
    print("nobody is authentificated")  #show red light
else:
    
    print("user is authentificated")      #show green light




# Create two threads as follows
thread1 = MyScript.camThread("Camera Add", 0)
thread2 = MyScript.camThread("Camera Remove", 1)
thread1.start()
thread2.start()
