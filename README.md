# Photobooth project from GPhoto2 Server V2.8

## GPhoto2 Server v2.8
This is a Jetty-powered WebApp that serves as a web-interface for GPhoto2 program.
It can be put on any machine that has libgphoto2 and Java 1.5 or above. The calls to GPhoto2 are done using JNA.

Included dependencies:
- gphoto2-jna
- jlibgphoto2

Build dependencies:
- LibGPhoto2 - http://www.gphoto.org
- JDK 8+
- Maven 3.3+

Runtime dependencies:
- LibGPhoto2 - http://www.gphoto.org
- Java 8+ (use Java5 compatibility builds if you need to run on earlier java versions - see in releases).

## Add GPIO and Printer
Like this project in python the aim of this fork is to do a java controller for my photobooth based on : 
- ESP32 CAM to take picture
	- https://fr.aliexpress.com/af/ESP-CAM.htm
	- https://fr.aliexpress.com/item/1005001579306340.html
	- https://fr.aliexpress.com/item/32826006482.htm
- Ring Light to have enough light
	- https://fr.aliexpress.com/af/Adaptateur-d%2527alimentation-12V-3A.html
	- https://fr.aliexpress.com/af/strip-led-2835%252C-12V%252C-5m-60LED%252Fm-cold-white.html 
- Another ESP to control LEDs and button with MQTT	
	- https://fr.aliexpress.com/af/wemos-d1-mini.html
	- Some LEDs + Buttons 

---

Notes:
- See also GPhoto2 for Raspberry Pi - https://github.com/gonzalo/gphoto2-updater
- See also for more informations about original projects: 
	- https://github.com/mvmn/gp2srv
	- https://github.com/mvmn/jlibgphoto2 
	- https://github.com/mvmn/gphoto2-jna
	- https://www.instructables.com/id/Raspberry-Pi-photo-booth-controller/
	- https://github.com/sojojo/RPi_photobooth

