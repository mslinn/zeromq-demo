Working version (eventually) of [Akka 2.0 ZeroMQ sample code](http://doc.akka.io/docs/akka/2.0/scala/zeromq.html).

Before the program can run, ZeroMQ needs to be built and installed. On Windows 64 bit, this is painful.
Happily, there are two prebuilt packages to download:
[ZeroMQ Win64](http://miru.hk/archive/ZeroMQ-2.1.10-win64.exe) &bull;
[JZQMQ Win64](http://miru.hk/archive/JZMQ-2.1.10-win64.exe)

On Ubuntu, it is easy to build:

````
sudo apt-get install libtool autoconf automake uuid-dev e2fsprogs
git clone git://github.com/zeromq/libzmq.git
cd libzmq
#less README
#less INSTALL
./autogen.sh && ./configure && make && sudo make install
sudo cp src/.libs/libzmq.so /usr/lib
sudo ldconfig -v
````

On Mac it is also easy:

````
brew install zeromq
...
To install the zmq gem on 10.6 with the system Ruby on a 64-bit machine, you may need to do:

ARCHFLAGS="-arch x86_64" gem install zmq -- --with-zmq-dir=/usr/local

If you want to build the Java bindings from https://github.com/zeromq/jzmq
you will need the Java Developer Package from http://connect.apple.com/
````

If running from IntelliJ or Eclipse, launch the programs from <tt>target/scala-2.9.1-1/classes</tt> so <tt>application.conf</tt> and <tt>common.conf</tt> are found.
To run from sbt:

````
sbt run
````
