LairPlay v0.1
==============
Mike Schultz
January 15, 2016

What it is
----------
This builds on bencall's RPlay
It emulates an Airport Express for the purpose of streaming music from iTunes and compatible iPods.
It implements a server for the Apple RAOP protocol.
It allows you to control a multizone amplifier controller (specifically the Russound MCA-C5)
   directly through Airplay without a controller middleware app.

Installation
------------
Double clicking on RPlay.jar in the DIST folder should be enough...

Thanks
------
Big thanks to David Hammerton for releasing an ALAC decoder and to soiaf for porting it to Java (https://github.com/soiaf/Java-Apple-Lossless-decoder).
Thanks to Jame Laird for his C implementation (shairport - https://github.com/albertz/shairport)
Thanks to anyone involved in one of the libraries i used for creating this software.

Libraries & References
----------------------
These libraries are included in RPlay:
* http://www.bouncycastle.org/latest_releases.html
* http://commons.apache.org/
* https://github.com/albertz/shairport
* https://github.com/soiaf/Java-Apple-Lossless-decoder
* http://jmdns.sourceforge.net

Contributors
------------
* [David Hammerton]
* [James Laird]
* [soiaf]
* [adeward] (https://github.com/adeward)
* [jblezoray] (https://github.com/jblezoray)
* [Maik Schulz] for Mac OS X bundle (now obsolete)
* [csholmq]
* Everyone who has helped with shairport, the alac decoder (or the java port of it), apache commons lib or bouncycastle lib (see their README)

