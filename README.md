# Getting Started

To run the provided examples and benchmarks the following software is required:

* JDK7 (java development kit)
	* Oracle (Windows): http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html
	* OpenJDK (Linux): http://openjdk.java.net/install/
	* (a JDK6/8 or JRE6/7/8 may also work, but this is untested)

* SBT (simple build tool)
	* http://www.scala-sbt.org/download.html
	* any version of the launcher script should be fine (0.13.5 was tested)


First make sure that you can launch sbt

	> sbt --version
	sbt launcher version 0.13.5

then navigate to the root of the unpacked archive

	> ls
	Benchmark/ CaseStudies/ Core/ …

now build the project

	> sbt compile
	…

this will download missing dependencies and compile the code.
some warnings related to scala-rx and scala-react are expected.

now try the minimal UI example

	> sbt ui/run

this should open a window where you can select a colour.
Note: it is possible that sbt will not terminate if the window is closed
(or just take 10~20 seconds to terminate after the window was closed)
in that case you can safely force termination with ctrl+c

now try the ProfitReact case study

	> sbt "project profit" "set mainClass in Compile := Some(\"projections.ProjectionsUI\")" run


# Detailed Instructions

## Folder overview

the archive contains several sub projects, each in its own folder.
each `src` sub folder contains the source code for the project.
compiled code is saved in the `target` sub folder of each project.

sub projects:

* Benchmark
	* contains the main comparison between the different reactive frameworks
* CaseStudies
	* the ProfitReact case study
* Core
	* the main SID-UP library
* Elmish
	* a version of the SID-UP library that tries to emulate Elms (http://elm-lang.org/) propagation algorithm
* log2dot
	* this was used to visualize reactive networks from logfiles (currently unsupported)
* project
	* this is not a sub project, but contains build definitions
* scala-react
	* a clone of scala-react, with some hacks for benchmarking (https://github.com/ingoem/scala-react)
* scala-rx
	* a clone of scala-rx (https://github.com/lihaoyi/scala.rx) (this clone is somewhat out of date by now)
* SimulateWork
	* some common features to simulate network latency for the reactive frameworks
* Tests
	* tests for the core SID-UP library
* Ui
	* reactive swing wrappers for SID-UP


## The ProfitReact case study

ProfitReact has two backends, first based on SID-UP remote reactives

	> sbt "project profit" "set mainClass in Compile := Some(\"projections.ProjectionsUI\")" run

and a second one based on java rmi observers

	> sbt "project profit" "set mainClass in Compile := Some(\"projections.ProjectionsUI\")" "run rmi"

in both version you can enter an order size an place new orders.
the purchases department will have to pay `5 + orderSize` but after some time the sales department will earn `2 * orderSize`.
the manager will panic if the profit falls below 0.

with the rmi backend you can disable glitch freedom with the checkbox (this does nothing with the reactive backend).
try to disable this and then enter a huge order size.
also note that the rmi backend will become inconsistent if you place many orders in a short amount of time.

## Running Benchmarks

we provide two benchmarks the first is a version of the ProfitReact case study which compares SID-UP with rmi observers.
the other benchmark is the one in `Benchmark` which compares the different reactive frameworks (or rather their propagation algorithms)
with simulated network latency.

there are two ways to run the benchmarks, the easy way:

	> sbt benchmark/run
	> sbt profit/run

but this will execute the benchmarks inside of sbt (they share the same jvm) which can skew the results.
the easiest way around this is to produce standalone versions of the benchmarks

	> sbt stage

this will create bash and bat scripts to run the benchmarks from the console

	> cd Benchmark
	> ./target/universal/stage/bin/benchmarks
	> cd ../CaseStudies
	> ./target/universal/stage/bin/sidup-casestudy-profitreact

be aware that running the first benchmark takes about an hour.

running any of the benchmarks will save results to `tmp` in the current working dir
(which is why it is recommended to change the directory before running the scripts)
you can find a visualization of the data in `tmp/report/index.html` (requires javascript, no internet connection)
the raw data we use for further processing is in `tmp/*.dsv`

there are two gnuplot script which can be used to generate static visualizations.
the script `Benchmark/plotAll.gnuplot` expects the `tmp` directory created above in `./results/tmp` (relative to working dir)
where the scrip for ProfitReact `CaseStudies/plotbenchresults.gnuplot` expects the `tmp` dir in the current working directory.

using the `report/index.html` is the recommended way when you just want to check the data. for obvious reasons. (no, not because js is fancy).
