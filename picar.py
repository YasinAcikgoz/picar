import socket
import RPi.GPIO as GPIO
import time
import signal
import sys
import os
import pygame
import threading

r1 = 11
r2 = 12

l1 = 15
l2 = 16

leftSignal = 40
rightSignal = 7

temperature_treshold = 65

GPIO.setmode(GPIO.BOARD) 
GPIO.setwarnings(False)
GPIO.setup(r1, GPIO.OUT)
GPIO.setup(r2, GPIO.OUT)
GPIO.setup(l1, GPIO.OUT)
GPIO.setup(l2, GPIO.OUT)

GPIO.setup(rightSignal, GPIO.OUT)
GPIO.setup(leftSignal, GPIO.OUT)

pR1 = GPIO.PWM(r1, 100)
pR2 = GPIO.PWM(r2, 100)
pL1 = GPIO.PWM(l1, 100)
pL2 = GPIO.PWM(l2, 100)



def getCPUtemperature():  
    res = os.popen('vcgencmd measure_temp').readline()
    return(res.replace("temp=","").replace("'C\n",""))  

def blink(pin):
    GPIO.output(pin, True)
        
def cleanPin(pin):
    GPIO.output(pin, False)

def stop(r_pin1, r_pin2, l_pin1, l_pin2):
    GPIO.output(r_pin1, False)
    GPIO.output(r_pin2, False)
    GPIO.output(l_pin1, False)
    GPIO.output(l_pin2, False)
    return

def makeMove(direction, angle):
    pR1.start(0)
    pR2.start(0)
    pL1.start(0)
    pL2.start(0)
    
    if direction == 'w':
        if angle ==0:
            print 'duz ileri git'
            cleanPin(leftSignal)
            cleanPin(rightSignal)
            pR1.ChangeDutyCycle(100)
            pL1.ChangeDutyCycle(100)
        elif angle >=5:
            print 'sola ileri git'
            blink(leftSignal)
            cleanPin(rightSignal)
            pR1.ChangeDutyCycle(abs(10-angle)*20)
            pL1.ChangeDutyCycle(100)
            
        elif angle < 5:
            print 'saga ileri git'
            blink(rightSignal)
            cleanPin(leftSignal)
            pR1.ChangeDutyCycle(100)
            pL1.ChangeDutyCycle(abs(5-angle)*20)
           
    elif direction == 's':
        if angle == 0:
            print 'duz geri git'
            cleanPin(leftSignal)
            cleanPin(rightSignal)
            pR2.ChangeDutyCycle(100)
            pL2.ChangeDutyCycle(100)
        elif angle >=5:
            print 'sola geri git'
            blink(leftSignal)
            cleanPin(rightSignal)
            pR2.ChangeDutyCycle(abs(10-angle)*20)
            pL2.ChangeDutyCycle(100)
            
        elif angle < 5:
            print 'saga geri git'
            blink(rightSignal)
            cleanPin(leftSignal)
            pR2.ChangeDutyCycle(100)
            pL2.ChangeDutyCycle((5-angle)*20)
            
    elif direction == 'x':
        print 'dur'
        cleanPin(leftSignal)
        cleanPin(rightSignal)
        pR1.ChangeDutyCycle(0)
        pL1.ChangeDutyCycle(0)
        pR2.ChangeDutyCycle(0)
        pL2.ChangeDutyCycle(0)
    return

try:

    
    s = socket.socket()         # Create a socket object

    s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    # s.bind(('192.168.43.247',  5001))        # Bind to the port
    s.bind(('192.168.2.166',  4568))        # Bind to the port


    s.listen(1)                 # Now wait for client connection.

    c, addr = s.accept()     # Establish connection with client.
    print 'Got connection from', addr
        
    i = 0
    while True:
        data = c.recv(1024)
        #print data
       # print len(data)
        if len(data) == 3:
            #print 'yon: ', data[0]
            #print 'aci: ', data[2]
            #print 'len: ', len(data)
            makeMove (data[0], int(data[2]))
            i+=1
            #   print i
            if i % 10 == 0:     #her 10 komutta 1 defa cpu sicakligini kontrol et.
                temperature = float(getCPUtemperature())
                #print temperature
                if temperature > temperature_treshold:
                    print 'CPU cok isindi. Program kapatiliyor.'
                    stop(r1, r2, l1, l2)
                    GPIO.cleanup()
                    c.close()        
                    sys.exit()
            #time.sleep(0.2)
        elif data=='x':
            #print 'XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX'
            makeMove (data[0], 0)


        elif data=='e':
            print "exit",addr
            stop(r1, r2, l1, l2)
            GPIO.cleanup()
            c.close()
            sys.exit()
            
    
except KeyboardInterrupt:
        print('You pressed Ctrl+C!')
        stop(r1, r2, l1, l2)
        GPIO.cleanup()
        c.close()
