Working version of [Akka 2.0 ZeroMQ sample code](http://doc.akka.io/docs/akka/2.0/scala/zeromq.html),
implemented as three programs: a publisher and two subscribers.
I added debug-level log statements so progress when running could be apparent.

Before the program can run, ZeroMQ and its (Java bindings)[http://www.zeromq.org/bindings:java] need to be built and installed. 

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

## Windows
Building ZeroMQ and the Java bindings on Windows 7 64 bit requires the non-free version of Visual C++.
Here are instructions for building [ZeroMQ](http://www.zeromq.org/docs:windows-installations)
and [Java bindings](http://www.zeromq.org/bindings:java).

Alternatively, there are two prebuilt packages to download, but I could not get them to work:
[ZeroMQ Win64](http://miru.hk/archive/ZeroMQ-2.1.10-win64.exe) &bull;
[JZQMQ Win64](http://miru.hk/archive/JZMQ-2.1.10-win64.exe)

# Running the Akka ZeroMQ Demo
You will need to add an OS-specific definion to VM Options in your Run/Debug configuration.
For Linux, the magic incantation is <tt>-Djava.library.path=/usr/local/lib</tt>

If you prefer to launch from <tt>sbt</tt>, modify your <tt>sbt</tt> script so as to add a parameter called <tt>$JAVA_OPTS</tt>.
Here is my <tt>sbt</tt> script, which will work on any OS that supports <tt>bash</tt>:

````
#!/bin/bash
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1536m -Xmx512M -Xss2M $JAVA_OPTS -jar `dirname $0`/sbt-launch.jar "$@"
````

To run each of the programs, first set <tt>java.library.path</tt>; this is an OS-specific setting.
The proper setting for Linux is <tt>-Djava.library.path=/usr/local/lib</tt>.

Launch the programs in three console sessions. For Linux, do the following:

````
export JAVA_OPTS=-Djava.library.path=/usr/local/lib
sbt 'run-main com.micronautics.zeromq.benchmark.HealthPublisher'
````

````
export JAVA_OPTS=-Djava.library.path=/usr/local/lib
sbt 'run-main com.micronautics.zeromq.benchmark.HeapSubscriber'
````

````
export JAVA_OPTS=-Djava.library.path=/usr/local/lib
sbt 'run-main com.micronautics.zeromq.benchmark.LogSubscriber'
````

You could also hard-code the setting for <tt>java.library.path</tt> into <tt>sbt</tt>, which would mean that your <tt>sbt</tt> script would be OS-specific.
Here is a script with the proper setting for Linux:

````
#!/bin/bash
java -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1536m -Xmx512M -Xss2M -Djava.library.path=/usr/local/lib -jar `dirname $0`/sbt-launch.jar "$@"
````

### Go, Baby, Go!
After launching <tt>HealthPublisher</tt>, output might look something like the following.

````
$ sbt 'run-main com.micronautics.zeromq.benchmark.HealthPublisher'
[info] Loading global plugins from /home/mslinn/.sbt/plugins
[info] Loading project definition from /home/mslinn/work/zeromq-demo/project
[info] Set current project to zeroMQBenchmark (in build file:/home/mslinn/work/zeromq-demo/)
[info] Running com.micronautics.zeromq.benchmark.HealthPublisher
...
````

Launch <tt>LogSubscriber</tt> next. Output might look something like the following.

````
$ sbt 'run-main com.micronautics.zeromq.benchmark.LogSubscriber'
[info] Loading global plugins from /home/mslinn/.sbt/plugins
[info] Loading project definition from /home/mslinn/work/zeromq-demo/project
[info] Set current project to zeroMQBenchmark (in build file:/home/mslinn/work/zeromq-demo/)
[info] Running com.micronautics.zeromq.benchmark.LogSubscriber
Logger got a Connecting
[INFO] [04/20/2012 20:18:57.491] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Used heap 8726728 bytes, at 20:18:57.482
[INFO] [04/20/2012 20:18:57.494] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Load average 1.53, at 20:18:57.482
[INFO] [04/20/2012 20:18:58.583] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Used heap 8726792 bytes, at 20:18:58.582
[INFO] [04/20/2012 20:18:58.583] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Load average 1.53, at 20:18:58.582
[INFO] [04/20/2012 20:18:59.683] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Used heap 8726856 bytes, at 20:18:59.682
[INFO] [04/20/2012 20:18:59.683] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Load average 1.53, at 20:18:59.682
[INFO] [04/20/2012 20:19:00.783] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Used heap 8777944 bytes, at 20:19:00.782
[INFO] [04/20/2012 20:19:00.783] [default-akka.actor.default-dispatcher-2] [akka://default/user/logger] Load average 1.53, at 20:19:00.782
...
````

<tt>HeapSubscriber</tt> won't generate output unless there is a problem, or the log level is increased to debug.
[<tt>src/main/resources/application.conf-</tt>](https://github.com/mslinn/zeromq-demo/blob/master/src/main/resources/application.conf-) has been provided to make this easy.
Simply rename that file and start the apps that you want to run with debug logging enabled, as follows:

````
$ mv src/main/resources/application.conf{-,}
$ export JAVA_OPTS=-Djava.library.path=/usr/local/lib
$ sbt 'run-main com.micronautics.zeromq.benchmark.HeapSubscriber'
[info] Loading global plugins from /home/mslinn/.sbt/plugins
[info] Loading project definition from /home/mslinn/work/zeromq-demo/project
[info] Set current project to zeroMQBenchmark (in build file:/home/mslinn/work/zeromq-demo/)
[info] Running com.micronautics.zeromq.benchmark.HeapSubscriber
[DEBUG] [04/20/2012 20:42:58.493] [run-main] [EventStream(akka://default)] logger log1-Logging$DefaultLogger started
[DEBUG] [04/20/2012 20:42:58.596] [run-main] [EventStream(akka://default)] Default Loggers started
[DEBUG] [04/20/2012 20:42:58.596] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber about to subscribe to health.heap
[DEBUG] [04/20/2012 20:42:58.813] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a Connecting
[DEBUG] [04/20/2012 20:42:59.763] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a ZMQMessage for health.heap
[DEBUG] [04/20/2012 20:42:59.764] [default-akka.actor.default-dispatcher-5] [akka.serialization.Serialization(akka://default)] Using serializer[akka.serialization.JavaSerializer] for message [com.micronautics.zeromq.Heap]
[DEBUG] [04/20/2012 20:43:00.857] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a ZMQMessage for health.heap
[DEBUG] [04/20/2012 20:43:01.957] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a ZMQMessage for health.heap
[DEBUG] [04/20/2012 20:43:03.57] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a ZMQMessage for health.heap
[DEBUG] [04/20/2012 20:43:04.157] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a ZMQMessage for health.heap
[DEBUG] [04/20/2012 20:43:05.257] [default-akka.actor.default-dispatcher-5] [akka://default/user/alerter] HeapSubscriber got a ZMQMessage for health.heap
...
````

If you want run the programs quietly, modify <tt>application.conf</tt> to contain <tt>loglevel = "ERROR"</tt>.