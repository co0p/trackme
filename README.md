trackme
=======

publish your location to THE internet

WARNING: USE AT YOUR OWN RISK: YOU ARE PUBLISHING YOUR LOCATION USING THIS APP.

Installation
---------------

1. copy server files to your server
2. Edit the file TrackMe.java file on line 40 and add your server url
3. create android app
4. publish your gps location to your server and tell your friends about it



Client
---------
This is an android (gingerbread) application which publishes your gps location to a server every n seconds. You can even add tell the world what is happening at your current location.

![The client view](https://raw.github.com/co0p/trackme/master/images/client.png)

Server
------------

The server consists of a couple of crude php scripts collecting the locations sent by the client. It displays the history in google maps.

![The server view](https://raw.github.com/co0p/trackme/master/images/server.png)
