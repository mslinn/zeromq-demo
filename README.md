Working version (eventually) of [Akka 2.0 ZeroMQ sample code](http://doc.akka.io/docs/akka/2.0/scala/zeromq.html).

Before the program can run, ZeroMQ needs to be built and installed. On Windows 64 bit, this is painful. On Ubuntu and Mac, it is easy:

````
git clone git://github.com/zeromq/libzmq.git
cd libzmq
sudo apt-get install libtool autoconf automake uuid-dev e2fsprogs
#less README
#less INSTALL
./autogen.sh && ./configure && make && sudo make install
sudo mv src/.libs/libzmq.so /usr/lib
sudo ldconfig -v
````

If running from IntelliJ or Eclipse, launch the programs from <tt>target/scala-2.9.1-1/classes</tt> so <tt>application.conf</tt> and <tt>common.conf</tt> are found.
To run from sbt:

````
sbt run
````
