######################### Alternative 1: VM-Image Preparation #########################

Boot the VM Image, for instance using Oracle's VirtualBox ( https://www.virtualbox.org/ -- we used version 4.3.12). User Credentials are as follows:
	Login: sidup
	Password: sidup
find the prepared artifact in

	> cd ~/SID-UP-artifact

continue at heading #### Execution #### further down this file

######################### Alternative 2: Do-It-Yourself Preparation #########################

To run the provided examples and benchmarks the following software is required:

* JDK7 (java development kit)
	* Oracle (Windows): http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
	* OpenJDK (Linux): http://openjdk.java.net/install/
	* (a JDK8 or JRE7/8 may also work, but this is untested. JDK6 or JRE6 will _not_ work!)
	For instance:

	> sudo apt-get install openjdk-7-jdk

* SBT (simple build tool)
	* http://www.scala-sbt.org/download.html
	* any version of the launcher script should be fine (0.13.1, 0.13.2 and 0.13.5 was tested)
	For instance:
	
	> wget http://repo.scala-sbt.org/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.1/sbt.deb
	> sudo dpkg -i sbt.deb
	> apt-get -f install

* [Optional] GnuPlot: If you want to execute the gnuplot scripts for the 
	benchmark results (which you do not need as an html view is generated 
	anyways), you will also need gnuplot. 

First make sure that you can launch sbt

	> sbt --version
	sbt launcher version 0.13.5

Unzip the artifact file and navigate to the directory. For instance:

	(If needed, install unzip with "> sudo apt-get install unzip")
	> unzip SID-UP-artifact.zip -d SID-UP-artifact
	> cd SID-UP-artifact
	> ls
	Benchmark/ CaseStudies/ Core/ …
	
Now build the project:

	> sbt compile
	
this will download missing dependencies and compile the code.
some warnings related to scala-rx and scala-react are expected.

Finally, prepare benchmark execution:

	> sbt stage

This will create shell scripts to run the benchmarks without involving 
SBT. Otherwise, sbt would be held in the memory of the same JVM 
executing the benchmarks and thus might skew the results. 

	
######################### Executing Examples and Benchmarks #########################

Run SID-UP's Core module unit tests:

	> sbt "project core" test

All tests should pass. The source code for these unit tests can be found
in the folder:

	./Core/src/test/scala/reactive/test/*.scala

Try the minimal UI example:

	> sbt ui/run

This should open a window where you can select a colour. All reactive
behavior in this application is implemented in reactive programming based
on SID-UP. The source for this application can be found in:

	./sidup-ui/src/main/scala/ui/example/ColorList.scala

Note: it is possible that sbt will not terminate if the window is closed
(or just take 10~20 seconds to terminate after the window was closed)
in that case you can safely force termination with ctrl+c.

## Case Study from Paper

Now try the ProfitReact case study from the paper:

	> sbt "project profit" "set mainClass in Compile := Some(\"projections.ProjectionsUI\")" run

This will open four windows, one for each department host. You can enter 
an order size and place new orders in the bottom window. The Sales and 
Purchases departments' windows on the left and right will then update. 
While all these departments run on the same machine, they still are only 
indirectly interconnected using remote reactives, thereby utilizing 
distributed reactive programming. Note that the Sales department is 
slower in recalculating the delivery plan and updates with a slight 
delay. The source code for each department without UI elements can be 
found in the folder:

	./CaseStudies/src/main/scala/projections/reactives

In the top window, management tracks the current profits and shows a log 
of sent manager emails. By placing several orders of size 1 to 4, the 
total profit can be continuously lowered to drop to negative values. 
Whenever the threshold is passed, an note is made in the log. Note that 
when placing regular orders where the total profit never drops below 
zero, SID-UP's glitch freedom successfully prevents erroneous emails 
that would otherwise be caused by the delayed update from the sales 
department. 

ProfitReact has two backends mentioned in the paper and both variants 
are accessible for testing here. Above example used the first one, based 
on SID-UP's remote reactives. The second one, based on Java RMI 
observers, can be started with this command: 

	> sbt "project profit" "set mainClass in Compile := Some(\"projections.ProjectionsUI\")" "run rmi"

With the RMI backend you can disable glitch freedom using the checkbox 
in the bottom window (this does nothing when using the reactive 
backend). Disabling glitch freedom and then placing a huge order will 
cause an email to be recorded in the management log, as the total profit 
is recalculated based only on the quickly propagated spending update 
from the purchases department without waiting for the increased income 
from the slowly updating spending department. The source code for each 
department using RMI observers instead of reactives can be found in the 
folder:

	./CaseStudies/src/main/scala/projections/reactives
	
The UI that is constructed on either of the two variants can be found
in the file:

	./CaseStudies/src/main/scala/projections/ProjectionsUI

## Running Benchmarks

Both benchmarks from the paper are available in the artifact.

# case study benchmark: Remote Reactives vs RMI observers

The first benchmark, comparing the performance of SID-UP's remote 
reactives with Java RMI observers, is however included only as a local 
benchmark. The distributed setup we used in the paper can not be 
reproduced using a single virtual machine. It can be executed through 
the command: 

	> ./Benchmark/target/universal/stage/bin/benchmarks -- -verbose

It is based on the networks described above without the UI added on top.
The Benchmark code can be found in the file:
	
	./CaseStudies/src/main/scala/projections/benchmark/Benchmark.scala

Lines 37 and 40 can be modified to adjust the workload. With the default
workload, execution takes upwards of one hour. Please make sure to
recompile and -package the code after modifying these parameters by
executing the command:

	> sbt stage

# reactive framework comparison with artificial network delays
	
The second Benchmark compares the performance of the different reactive
frameworks with artificial network delays introduced. It can be run with
the command:

	> ./CaseStudies/target/universal/stage/bin/sidup-casestudy-profitreact -- -verbose

The delay is be introduced through the artificial dependency project in
the Folder:

	./SimulateWork

Both Scala.React and Scala.Rx were modified to inject coordinator
communication delay dependent on this project. They can be found in the
folders:

	./scala-react
	./scala-rx

SID-UP and ELM do not require coordinator delay since their propagation
runs without coordinator involvement. They can be found in the folders:

	./Core
	./Elmish
	
Additionally, delay from node-to-node communication applies to 
everyframework. This is however not injected into the frameworks, but 
instead simulated through delaying the computation attached to each node 
that needs to perform network communication. Thus, these delays are 
defined in the benchmark code itself, in the files in folder: 

	./Benchmark/src/main/scala/benchmark/networks

The workload for this benchmark is defined in lines 30 through 39 
(repetitions, iterations, 'size' as in number of nodes in the topology 
modules 'chain' and 'fan', and the artificial delay times in nano-second 
scale busy-waiting and milli-second scale idle-waiting) in this file: 

	./Benchmark/src/main/scala/benchmark/Benchmark.scala

Please make sure to recompile and -package the code after modifying
these parameters by executing the command:

	> sbt stage

# making graphs

Running any of the benchmarks will save results to a 'tmp' folder in the 
current working directory. You can find a visualization of the data in 
'tmp/report/index.html' (requires javascript, no internet connection). 
The raw data we use for further processing is in 'tmp/*.dsv' 

There are two gnuplot scripts which can be used to generate static 
visualizations. Both expect the benchmarks respective 'tmp' output 
directory to be present in the current working directory: 

	./CaseStudies/plotbenchresults.gnuplot
	./Benchmark/plotAll.gnuplot

######################### Writing own programs #########################

Writing own programs can be done easily. You can find precompiled jar
files in the folder:

	./precompiled-dependencies

Add all of these jar files to the build path of a new scala project and 
you can start writing programs using SID-UP. The UI-jar is optional, it 
only provides a few convenience wrappers for integrating SID-UP with a 
Swing-UI. For example usages, refer to the Unit Tests or the ColorList 
example from above. 

