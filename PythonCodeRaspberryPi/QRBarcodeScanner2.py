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

#Audio
from pydub import AudioSegment
from pydub.playback import play

#For Api
import requests
import json


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

# defining sounds
soundLink = AudioSegment.from_wav("/home/andrei/Downloads/test/LinkToAccount.wav")
soundBeep = AudioSegment.from_wav("/home/andrei/Downloads/test/Barcode-scanner-beep-sound.wav")
soundLoggedIn = AudioSegment.from_wav("/home/andrei/Downloads/test/UserLogged.wav")
soundNotFound = AudioSegment.from_wav("/home/andrei/Downloads/test/ItemNotFound.wav")

#user authentification
fle = Path('/home/andrei/Downloads/test/userFirebase.txt')
fle.touch(exist_ok=True)  #if file does not exist, then create it.... otherwise do nothing
userFile = open('/home/andrei/Downloads/test/userFirebase.txt', 'r')
userFirebase = userFile.read()
print(userFirebase)
userFile.close()

if len(userFirebase) == 0 or len(userFirebase) == 1:
    play(soundLink)
    print("nobody is authentificated") 
else:
    print("user is authentificated")


class MyScript:
    #Gets data from barcode API
    def GetBarcodeApiData(barcodeData):
        #api example: https://api.upcdatabase.org/product/0111222333446?apikey=4368CFAB9E2F2B72DBBDA6EE20DD7877
        BARCODE_API_KEY ="4368CFAB9E2F2B72DBBDA6EE20DD7877"
        BARCODE_BASE_URL = "https://api.upcdatabase.org/product/"
        requestUrl = BARCODE_BASE_URL + str(barcodeData) +"?apikey=" + BARCODE_API_KEY

        response = requests.get(requestUrl)

        if(response.json()['success']==True):
            if response.json()['title'] != "":
                print (response.json()['title'])
                return response.json()['title']
            elif response.json()['description'] != "":
                print (response.json()['description'])
                return response.json()['description']
        else:
            print("Item not found")
            return 0
    
    #Generates a random string of 10 chars
    def GetRandomKey():
        key=""
        alphabet = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890"
        for _ in range(10):
             key += choice(alphabet)
        return key

    #Logins the user into the Firebase Account
    def loginUser(barcodeData):
        play(soundLoggedIn)
        print("User scanned their QR code")
        global userFirebase 
        userFirebase = barcodeData.replace("userFirebaseUID:","")   
        file = open("/home/andrei/Downloads/test/userFirebase.txt", "w")
        file.write(userFirebase)
        file.close()
        userFile = open('/home/andrei/Downloads/test/userFirebase.txt', 'r')
        userFirebase = userFile.read()
        userFile.close()

    #After a barcode is scanned this adds the data to the firebase database
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
                expirationDate = "11/11/2000"    
                print(barcodeData)

                #Add items to firebase database
                db.child("foodItems").child(userFirebase).child(key).child("itemName").set(name)
                db.child("foodItems").child(userFirebase).child(key).child("itemExpirationDate").set(expirationDate)
                db.child("foodItems").child(userFirebase).child(key).child("key").set(key)
                db.child("foodItems").child(userFirebase).child(key).child("toDonate").set(False)
                print("successfuly added to database")
            else:
                play(soundNotFound)
        else: 
            play(soundNotFound)
    
    #After a barcode is scanned this removes the data to the firebase database
    def removeItemFromDatabase(barcodeData):
        #if it is a qr code with the requested format
        if("Name/Expiration:" in barcodeData):
            play(soundBeep)
            splitData = barcodeData.split(sep="/+/", maxsplit=-1)
            items = db.child("foodItems").child(userFirebase).order_by_child("itemName").equal_to(splitData[1]).get()  #gets a response object
            date = splitData[2]
            itemKey = ""
            for item in items.each():
                itemVal=item.val()
                if(date == itemVal['itemExpirationDate']):
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
            else:
                play(soundNotFound) 
        else: 
            play(soundNotFound)

    #starts running the 2 camers on different threads to run in parrallel        
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

    #First camera adds items. Processing each frame happens here
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

    #Second camera removes items. Processing each frame happens here
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
                elif barcodeData  != lastBarcodeDataRemoved and barcodeData != lastBarcodeData:
                    print("item scanned:"+userFirebase)
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
        
        

# Create two threads as follows 
thread1 = MyScript.camThread("Camera Add", 0)
thread2 = MyScript.camThread("Camera Remove", 1)
thread1.start()
thread2.start()
