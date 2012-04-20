Working version (eventually) of [Akka 2.0 ZeroMQ sample code](http://doc.akka.io/docs/akka/2.0/scala/zeromq.html).

Before the program can run, ZeroMQ and its (Java bindings)[http://www.zeromq.org/bindings:java] need to be built and installed. 

## Windows
On Windows 64 bit, this is painful. Happily, there are two prebuilt packages to download:
[ZeroMQ Win64](http://miru.hk/archive/ZeroMQ-2.1.10-win64.exe) &bull;
[JZQMQ Win64](http://miru.hk/archive/JZMQ-2.1.10-win64.exe)

## Ubuntu
On Ubuntu, it is easy to build from source.

Unfortunately, the [Java bindings repository](https://launchpad.net/~tuomjarv/+archive/jzmq) has not been updated in over a year, so it is not up to date.
You should build from source instead.
Here is how to add the ZeroMQ repository, but I don't think you should use it because you should build the Java bindings anyway, and the versions should match.

````
sudo add-apt-repository ppa:chris-lea/zeromq
sudo aptitude install libzmq-dev
````

### Building from source
First build ZeroMQ.

````
sudo apt-get install libtool autoconf automake uuid-dev e2fsprogs
git clone git://github.com/zeromq/libzmq.git
cd libzmq
#less README
#less INSTALL
./autogen.sh && ./configure && make && sudo make install && echo ":: ALL OK ::"
sudo cp src/.libs/libzmq.so /usr/lib
sudo ldconfig
ls -al /usr/local/lib/libzmq.*
cd ..
````

Now build the Java bindings.

````
# Verify that JAVA_HOME environment variable is correctly set
echo $JAVA_HOME/bin/java
# Clone the github repository for the ZeroMQ Java bindings and build the project
git clone https://github.com/zeromq/jzmq.git
cd jzmq
./autogen.sh && ./configure && make && sudo make install && echo ":: ALL OK ::"
ls -al /usr/local/lib/*jzmq* /usr/local/share/java/*zmq*
````

Ideally, typesafe or Sonatype shold host zmq.jar in their repository. Until then, you could host in your repo, or 
just copy the jar with the Java bindings to your sbt project's <tt>lib/</tt> directory, where unmanaged dependencies live:

````
cd $mySbtProject
mkdir lib
cp /usr/local/share/java/zmq.jar lib
````

## Mac
On Mac it is also easy to build from source:

````
brew install zeromq
...
This message appears:
To install the zmq gem on 10.6 with the system Ruby on a 64-bit machine, you may need to do:

ARCHFLAGS="-arch x86_64" gem install zmq -- --with-zmq-dir=/usr/local

If you want to build the Java bindings from https://github.com/zeromq/jzmq
you will need the Java Developer Package from http://connect.apple.com/

Other (simpler) instructions are here: https://github.com/zeromq/jzmq/issues/29
````

If running from IntelliJ or Eclipse, launch the programs from <tt>target/scala-2.9.1-1/classes</tt> so <tt>application.conf</tt> and <tt>common.conf</tt> are found.
To run from sbt:

````
sbt run
````
