import cv2
import numpy as np
def getRedArea(img):
    lower_red = np.array([160, 60, 60])
    upper_red = np.array([180, 255, 255])
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    mask_r = cv2.inRange(hsv, lower_red, upper_red)
    # binaryr = cv2.blur(mask_r, (3, 3))
    binaryr = cv2.erode(mask_r, None, iterations=2)
    binaryr = cv2.dilate(binaryr, None, iterations=2)
    # cv2.imshow('binaryr', binaryr)
    contours, hierarchy = cv2.findContours(binaryr, cv2.RETR_EXTERNAL,
                                           cv2.CHAIN_APPROX_SIMPLE)
    if len(contours) <= 0:
        return 0
    c = max(contours, key=cv2.contourArea)
    areas = [cv2.contourArea(b) for b in contours]
    index = np.argmax(areas)
    # print(cv2.contourArea(c))
    ((x, y), radius) = cv2.minEnclosingCircle(c)
    drawing = np.zeros_like(binaryr, np.uint8)  # create a black image
    cv2.drawContours(drawing, contours, index, 255, thickness=-1)
    # cv2.circle(drawing, (int(x), int(y)), int(radius), 255, -1)  # thickness=-1
    # cv2.imshow('drawing', drawing)
    # print(cv2.FILLED)
    return cv2.contourArea(c)


def getresult(picpath):
    image = cv2.imread(picpath)
    a1 = getRedArea(image)
    return a1

def add(a, b):
    return a + b