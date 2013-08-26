#!/usr/bin/python
from Adafruit_PWM_Servo_Driver import PWM
import sys

#print 'Number of arguments:', len(sys.argv), 'arguments.'
#print 'Argument List:', str(sys.argv)

pwm = PWM(0x40, debug=True)

chanel = int(sys.argv[1])
rotation = float(sys.argv[2])
print 'chanel: ', chanel
print 'rotation: ', rotation

servoMin = 100  # Min pulse length out of 4096
servoMax = 600  # Max pulse length out of 4096

pulse = int((rotation / float(180)) * float((servoMax - servoMin)) + servoMin)
print 'pulse: ', pulse

def setServoPulse(channel, pulse):
  pulseLength = 1000000                   # 1,000,000 us per second
  pulseLength /= 60                       # 60 Hz
#  print "%d us per period" % pulseLength
  pulseLength /= 4096                     # 12 bits of resolution
#  print "%d us per bit" % pulseLength
  pulse *= 1000
  pulse /= pulseLength
  pwm.setPWM(channel, 0, pulse)

pwm.setPWMFreq(60)                       
pwm.setPWM(chanel, 0, pulse)
