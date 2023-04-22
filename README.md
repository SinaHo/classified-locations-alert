
# Classifed Locations Alert

This application is used to alert user whenever they move to a location with different class.

Locations are going to be exported images of geological and surveying applications and every class gets a unique color.
For example let's suppose we have an image of france flag as our classes source. We assign the upper right and bottom left vertices to real GPS locations. So then by calculating the ratios of the distance of user's current location to these two points and having the image's width and height we can can determine corresponding pixel. The color of that pixel will determine that location's class and if it has changed comparing to the last one it will show an appropriate alert. So if the user moves from white section to the blue one they will get a notification. This source image plus user's current location as a yellow marker wil be shown on the main activity.
