Working version of [Akka 2.0 ZeroMQ sample code](http://doc.akka.io/docs/akka/2.0/scala/zeromq.html).
I added a bunch of prinlns so progress when running could be apparent.

Before the program can run, ZeroMQ and its (Java bindings)[http://www.zeromq.org/bindings:java] need to be built and installed. 

## Windows
Building ZeroMQ on Windows 64 bit is painful. Happily, there are two prebuilt packages to download:
[ZeroMQ Win64](http://miru.hk/archive/ZeroMQ-2.1.10-win64.exe) &bull;
[JZQMQ Win64](http://miru.hk/archive/JZMQ-2.1.10-win64.exe)

## Ubuntu
On Ubuntu, it is easy to build both packages from source, if you follow the proper recipe (below).
You should build the [Java bindings repository](https://launchpad.net/~tuomjarv/+archive/jzmq) from source because the
package archive has not been updated in over a year, so it is not up to date.

FYI, here is how to add the old ZeroMQ repository.
I don't think you should use it because you should build the Java bindings anyway, and the versions of the two packages should match.

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
./autogen.sh && ./configure && make && sudo make install && echo ":: ALL OK ::"
sudo cp src/.libs/libzmq.so /usr/lib
sudo ldconfig
ls -al /usr/local/lib/libzmq.*
cd ..
````

Now build <tt>zmq.jar</tt>, which will contain the Java bindings, or use <tt>lib/zmq.jar</tt> from this project.

````
# Verify that JAVA_HOME environment variable is correctly set
echo $JAVA_HOME/bin/java
# Clone the github repository for the ZeroMQ Java bindings and build the project
git clone https://github.com/zeromq/jzmq.git
cd jzmq
./autogen.sh && ./configure && make && sudo make install && echo ":: ALL OK ::"
ls -al /usr/local/lib/*jzmq* /usr/local/share/java/*zmq*
````

Ideally, Sonatype would one day host <tt>zmq.jar</tt> their repository.
Until then, you could host <tt>zmq.jar</tt> in your own repository, or you could just copy <tt>zmq.jar</tt>
to your sbt project's <tt>lib/</tt> directory, where unmanaged dependencies live.
For now, the Java bindings are checked into this project as <tt>lib/zmq.jar</tt>.

## Mac
On Mac it is really easy to build ZeroMQ from source.
You will also need <tt>zmq.jar</tt>; you could get it from this project.

````
brew install zeromq
````

# Running the Akka ZeroMQ Demo
If running from IntelliJ or Eclipse, launch the programs from <tt>target/scala-2.9.1-1/classes</tt>
so <tt>application.conf</tt> and <tt>common.conf</tt> are found.
You will also need to add an OS-specific definion to VM Options in your Run/Debug configuration.
For Linux, the magic incantation is <tt>-Djava.library.path=/usr/local/lib</tt>

If you prefer to launch from <tt>sbt</tt>, modify your <tt>sbt</tt> script so as to add a parameter called <tt>$JAVA_OPTS</tt>.
Here is my <tt>sbt</tt> script, which will work on any OS:

````
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1536m -Xmx512M -Xss2M $JAVA_OPTS -jar `dirname $0`/sbt-launch.jar "$@"
````

To run the program, first set <tt>java.library.path</tt>; this is an OS-specific setting.
Here is the proper setting for Linux:

````
export JAVA_OPTS=-Djava.library.path=/usr/local/lib
sbt run
````

You could also hard-code the setting for <tt>java.library.path</tt> into <tt>sbt</tt>, which would mean that your <tt>sbt</tt> script would be OS specific.
Here is a script with the proper setting for Linux:

````
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1536m -Xmx512M -Xss2M -Djava.library.path=/usr/local/lib -jar `dirname $0`/sbt-launch.jar "$@"
````

### Go, Baby!
````
$ sbt run
[info] Loading global plugins from /home/mslinn/.sbt/plugins
[info] Loading project definition from /home/mslinn/work/zeromq-demo/project
[info] Set current project to zeroMQDemo (in build file:/home/mslinn/work/zeromq-demo/)
[info] Running Main
Logger about to subscribe to health
HeapAlerter about to subscribe to health.heap
HeapAlerter got a Connecting
Entered HealthProbe preStart()
Logger got a Connecting
HealthProbe got a Tick
HealthProbe about to publish health.heap
HeapAlerter got a ZMQMessage for health.heap
Logger got a ZMQmessage for health.heap
[INFO] [04/20/2012 00:28:27.23] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Used heap 9697856 bytes, at 00:28:27.011
HealthProbe about to publish health.load
Logger got a ZMQMessage health.load
[INFO] [04/20/2012 00:28:27.28] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Load average 1.11, at 00:28:27.011
HealthProbe got a Tick
HealthProbe about to publish health.heap
HeapAlerter got a ZMQMessage for health.heap
Logger got a ZMQmessage for health.heap
[INFO] [04/20/2012 00:28:28.112] [default-akka.actor.default-dispatcher-5] [akka://default/user/logger] Used heap 10106240 bytes, at 00:28:28.110
HealthProbe about to publish health.load
Logger got a ZMQMessage health.load
[INFO] [04/20/2012 00:28:28.113] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Load average 1.02, at 00:28:28.110
````
