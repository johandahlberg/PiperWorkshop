Queue/Piper workshop 31/1 2014
==================================

This document contains a small tutorial for the Queue/Piper workshop on 31/1 2014. The idea is to have a small workshop about Queue/Piper, going through the basics of how Queue/Piper works and getting some hands on experience in writing qscripts.

Prerequisites
-------------
To follow this workshop you need to have the following programs installed:

* [git](http://git-scm.com/)
* [oracle jdk 1.7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [ant](http://ant.apache.org/) (you need a version from before 1.9.3, since this has a bug in it)
* [scala-ide](http://scala-ide.org/index.html) (optional - though it will make Scala coding a lot simpler)
    
If you want to be able to test run things on Uppmax you will also need a uppmax account.

Running the examples below
--------------------------
To be able to test run the things presented below you need to install Piper:

    git clone https://github.com/johandahlberg/piper.git
    cd piper
    git checkout devel
    ./setup.sh

Introductions
------------------
The text below might contain some jargon which is unfamiliar to you. Try not to get stuck, but move on and hopefully it all will be clearer once you've started working with it in practice.

**A very brief introduction to Scala**<br/>
It will of course be impossible to cover everything in the Scala language here, and there are plenty of resources on the net which you can look through if you are interested. This will be a very brief introduction hopefully containing enough to allow you to start writing qscripts. 

Scala is a object oriented cross-paradigm language which compiles to java byte code and runs on the Java Virtual Machine. To access the full power of Scala one should program in a functional style, but it's absolutely possible to write imperative style code in scala. 

Scala is very similary to Java, so if you've worked with Java before you should be able to jump straight into it.

If you want to try out the examples below the simplest way to do so (provided that you have downloaded and setup Piper) is to run `sbt` from the Piper directory using:

    sbt/bin/sbt
    
When the console opens type `console` to initiate the Scala REPL. You can then type all the commands below and see them evaluate:

    
    // Declaring an immutable variable in Scala
    val x = 1
    
    // Declaring a mutable variable (should only be used when it's really needed)
    var y = "a"
    y = "b"

    // Scala is a strongly type language with type inference. That means that the compiler will figure out types for you depending on the context. This means that the following is illegal:  
    y = 1 // y has type String, and 1 has type Int (in this case). This will not compile.

    // Declare a class
    class MyAwesomeClass(x: Int, s: String) { // Parameters are optional. 
        // Class body
    }
    
    // Declare a case class (a special type of class which where natural equals methods are auto generated, and that can be use to do pattern matching.
    case class Point(x: Int, y: Int)
    
    // This means that the following will return true (which it would not do in Java without writing a custom equals method.
    Point(1,1) == Point(1,1)
    
    // As Scala is a functional language, so functions are at the hearth of it. Here is how you declare a very simple one:
    def square(x: Int): Int = x * x
    
    // A slightly more complex example is:
    def squareThanSum(x: List[Int]): Int = {
        val squared = x.map(y => y * y) // Square each element in the list
        val summed = squared.reduce((y,z) => y + z) // And sum the elements
        summed // The last line in the function is returned
    }
    
    // Extending a class:
    class ColoredPoint(color: String)
    class TwoDimensionalPoint(color: String, x: Int, y: Int) extends ColoredPoint(color)
    
    // Scala also has something called traits (similar to interfaces in Java). A class can only inherit one class, but it can implement multiple traits. They can contain implemented fields and functions, or unimplemented ones (this will force the implementing class to provide a implementation).
    trait Pingable {
        def ping: String = "Pong"
        def pong: String
    }
    
    // Traits are added to classes using the "with" key-word.
    class TwoDimensionalPoint(color: String, x: Int, y: Int) 
        extends ColoredPoint(color)
        with Pingable {
            def pong: String = "Ping"
        }

    // Using for-constructs in scala
    val sequence = Seq(1, 2, 3, 4)
    
    // Classical iteration
    for (elem <- sequence) {
        println("elem: " + elem)
    }
    
    // Using a for-comprehension to generate a new sequence
    val newSequence = 
        for (elem <- sequence) yield {
            elem + 1
        }
    

**Scala resources** <br/>
If you want to learn more about Scala (and read better tutorials than this one) have a look in some of these places:

* http://www.scala-lang.org
* http://twitter.github.io/scala_school/
* http://stackoverflow.com/tags/scala/info
* http://aperiodic.net/phil/scala/s-99/
* http://programming-scala.labs.oreilly.com
* http://www.scala-lang.org/docu/files/ScalaByExample.pdf
* http://devcheatsheet.com/tag/scala/
* http://davetron5000.github.com/scala-style/index.html

**Queue** <br/>
[Queue](http://www.broadinstitute.org/gatk/guide/topic?name=queue) is a framework for writing pipelines developed by the Broad and is released under a MIT licence. Queue is built in Scala and contains a few core concepts which will be covered below. Only the basics will be covered here. For more you material, here are some useful resources:

* http://www.broadinstitute.org/gatk/guide/events?id=3391
* http://www.broadinstitute.org/gatk/guide/topic?name=queue

Some of the advantages of using Queue are:
* Parallelize workflows
* Supports reruns
* Possible to create reusable components
* Excellent support for the GATK suite
* Relatively simple to add new components to a pipeline

**Qscripts**<br/>
The qscript is at the heart of Queue. It's were you define how your pipeline is going to run.  It's written in Scala with some syntactic sugar. In your qscript you will add command line functions to a dependency graph which will be run by Queue.

Formally a QScript is a class which extends `QScript` and that defines the function `script()`. Furthermore it will typically define (or import) a number of CommandLineFunction and a have a number of arguments which can be passed to it when running it from the commandline.

Here's a tiny example of what a QScript can look like:

    package molmed.qscripts
    
    import org.broadinstitute.sting.queue.QScript
    
    class MyAwesomeQScript extends QScript {
    
        // A argument that should be passed to the qscript from the command line 
        @Input(doc = "input fasta file", shortName = "i", required = true)
        var input: File = _
    
        // Where you define the pipeline - more about this later
        def script() {
            //...
        }
    }

**CommandLineFunction**<br/>
If qscripts are the heart of Queue, CommandLineFunctions are it's blood. A CommandLineFunction constructs the actual commands to be run. Each program that is run by Queue is defined as a case class extending the CommandLineFunction class. It defines inputs and outputs, which is how Queue knows how to chain the jobs together into a dependency graph.

Formally a CommandLineFunction is a class which extends the `CommandLineFunction` class and which defines the `commandline` function. Here's an example which runs a simple *nix oneliner to find the number of occurrences of each sequence in a fasta file.

    case class NaiveSequenceCounter(@Input fastaFile: File, @Output sequenceCounts: File) extends CommandLineFunction {
        def commandLine = "cat " + fastaFile + 
                          " | grep -v \"^>\" | sort | uniq -c >" +
                          sequenceCounts
    }

The gist is, anything you can run on the commandline you can run with Queue.

**Put it together using add()**<br/>
The key to putting the two things above together is using the `add()` function. This is is how you define what jobs are to be added to the dependency graph and run by Queue. An example of this:

    package molmed.qscripts

    import org.broadinstitute.sting.queue.QScript
    import java.io.File
    import org.broadinstitute.sting.commandline.Argument
    
    class MyAwesomeQScript extends QScript {
    
      // An argument that should be passed to the qscript from the commandline 
      @Input(doc = "input fasta file", shortName = "i", required = true)
      var input: File = _
    
      // Where you define the pipeline
      def script() {
    
        // Defining names of output files
        val seqCounts = new File("sequence_counts.txt")
        val totalNumberOfReads = new File("total_read_nbr.txt")
    
        // Add jobs to dependency graph
        add(NaiveSequenceCounter(input, seqCounts))
        add(SumTotalNumberOfReads(seqCounts, totalNumberOfReads))
    
      }
    
      case class SumTotalNumberOfReads(@Input seqCounts: File, @Output totalNumberOfReads: File) extends CommandLineFunction {
        // Another way to define the commandline
        def commandLine = required("cat")  + 
                		  required(seqCounts) +
        				  required(" |  awk \'{sum=sum+$1} END{print sum}\' >  ", escape = false) +
        				  required(totalNumberOfReads)
      }
    
      case class NaiveSequenceCounter(@Input fastaFile: File, @Output sequenceCounts: File) extends CommandLineFunction {
    	// Simplest possible way to define commandline 
        def commandLine = "cat " + fastaFile +
          " | grep -v \"^>\" | sort | uniq -c >" +
          sequenceCounts
      }
    }

Note that it does not matter in which order you `add()`  the functions. It is the chain of input and output files that matter, Queue handles the rest.

**Piper** <br/>
Piper is build on top of Queue, and is basically a extension of Queue which allows Queue to be run on Uppmax. Furthermore it contains a number of predefined workflows, for example for exome resequencing, which are simple to deploy provided that your data has been generated by the SNP&SEQ Technology platform.

Advantages of using Piper:
* Simplifies process of deploying Queue on Uppmax
* Has predefined workflows for many common NGS applications (WGS, exome, RNA read counts and differential expression)
* Has predefined building blocks for running some common NGS tasks such as alignments, variant calling, etc.

Running it locally
------------------
To run your new QScript locally go into the Piper folder and run the following (with any setup you prefer):

    ./piper -S <path to your script> <all other parameters> --job_runner Shell

This will make a dry run of the script. Showing you the command lines that will be run. To actually execute the jobs, add `-run`:

    ./piper -S <path to your script> <all other parameters> --job_runner Shell -run

Running it on Uppmax
--------------------
To run qscripts on Uppmax there are somethings that you need to do. Piper communicates with SLURM using the DRMAA APIs. This means that you need to feed the cluster the required information (such as project id and walltime limit) somehow. The arguments which are required to be set are collected in the `Uppmaxable` trait in Piper, extending you qscript with this will automatically bring those to your script.

Furthermore you need to specify the resource usage, this is done by wrapping you `CommandLineFunction` case classes in a class which extends the `UppmaxJob`, passing a `UppmaxConfig` instance as a argument. Each `CommandLineFunction` can then specify it's resource usage by extending classes called `OneCoreJob`, `TwoCoreJob`, etc. Sounds difficult? It's not that bad, look at the example below and see for yourself:

    package molmed.qscripts
    
    import org.broadinstitute.sting.queue.QScript
    import java.io.File
    import org.broadinstitute.sting.commandline.Argument
    import molmed.utils.Uppmaxable
    import molmed.utils.UppmaxConfig
    import molmed.utils.UppmaxJob
    
    // Uppmaxable is a trait holding the necessary uppmax arguments
    // to bring in.
    class MyAwesomeQScript extends QScript with Uppmaxable {
    
      // An argument that should be passed to the QScript from the commandline 
      @Input(doc = "input fasta file", shortName = "i", required = true)
      var input: File = _
    
      // Where you define the pipeline
      def script() {
    
        // Load the uppmax config (projId and uppmaxQosFlag both live in the Uppmaxable
        // trait and can therefore be used here.
        val uppmaxConfig = new UppmaxConfig(this.projId, this.uppmaxQoSFlag)
        val uppmaxBase = new UppmaxBase(uppmaxConfig)
    
        // Defining names of output files
        val seqCounts = new File("sequence_counts.txt")
        val totalNumberOfReads = new File("total_read_nbr.txt")
    
        // Add jobs to dependency graph
        add(uppmaxBase.NaiveSequenceCounter(input, seqCounts))
        add(uppmaxBase.SumTotalNumberOfReads(seqCounts, totalNumberOfReads))
    
      }
    
      // Now our two classes are wrapped in a Uppmax base class which extends the
      // UppmaxJob class. This holds the utility classes to specify resource
      // usage.
      class UppmaxBase(uppmaxConfig: UppmaxConfig) extends UppmaxJob(uppmaxConfig) {
    
        // Note the "extends OneCoreJob" part, which specifies that this job should request one core from the cluster.
        // Other 
        case class SumTotalNumberOfReads(@Input seqCounts: File, @Output totalNumberOfReads: File) extends OneCoreJob {
          // Another way to define the commandline
          def commandLine = required("cat") +
            required(seqCounts) +
            required(" |  awk \'{sum=sum+$1} END{print sum}\' >  ", escape = false) +
            required(totalNumberOfReads)
        }
    
        case class NaiveSequenceCounter(@Input fastaFile: File, @Output sequenceCounts: File) extends OneCoreJob {
          // Simplest possible way to define commandline 
          def commandLine = "cat " + fastaFile +
            " | grep -v \"^>\" | sort | uniq -c >" +
            sequenceCounts
        }
      }
    
    }

This qscript is now ready to go, and be run on Uppmax. Log in to a node where you're allowed to run java (e.g. a interactive node).

To run a qscript you need to add the the slurm-drmaa libraries on your library path. To get this run the following:

    export LD_LIBRARY_PATH=/sw/apps/build/slurm-drmaa/1.0.6/lib/:$LD_LIBRARY_PATH

You can now dry run the qscript using something like this:

    ./piper -S <path to your script> <all other parameters> --job_runner Drmaa --job_walltime <time to request from cluster in seconds> --project_id <your uppmax project number>

Just as before, the script will not run, only create the dependency graph and make sure everything looks okey. Add `--run` to the command line to make it run the jobs, sending them to the cluster.

Writing your first queue script
-------------------------------

Start by getting Piper and installing from Github (if you did this in the begining, you can skip it now):
    
    git clone https://github.com/johandahlberg/piper.git
    cd piper
    git checkout devel
    ./setup.sh

If you are using Scala-IDE for your development you can create a eclipse project to import by typing:

    sbt/bin/sbt eclipse
    
This will create the files necessary to import the project into the Scala-IDE. 

**Add a command line function** <br/>
Starting from the example script above, we are now going to add an example command line which will count the number of occurrences of each base in the fasta file. The original command line will look something like this:

    cat  [your fasta file] | grep -v "^>" | awk 'BEGIN{a=0; c=0; g=0; t=0;} {a+=gsub("A",""); c+=gsub("C",""); g+=gsub("G",""); t+=gsub("T","");} END{print a"\t"c"\t"g"\t"t}' > [some output file]

Create a new file called `MyAwesomeQScript.scala`, and open it in Scala-IDE. Copy the qscript above into the editor and see if you can add the new `CommandLineFunction` to the qscript. When you've done so, try to run it, as above.

**Process more than one file**<br/>
In the example above we have so far only processed one file - in real life that's seldom the case. One way to allow multiple input files of the same type is to use import:

    import org.broadinstitute.sting.queue.util.QScriptUtils
    
And use a construct like this:    

    val fastaFiles = QScriptUtils.createSeqFromFile(input)
    for(fastaFile <- fastaFiles) {
        // Do something with the files
    }

See if you can use this method to run your CommandLineFunctions on all the fasta files in the test data directory. A tip is that since Queue uses file names to build the dependency graph you need to make sure each output file gets a unique file name.


**Adding a InProcessFunction**<br/>
A `InProcessFunction` is a function which will not run as a command line, but Queue runs it as it would run any other Scala function, but based on the inputs and output it will know when in the workflow to run it.

Creating a `InProcessFunction` is done by extending the `InProcessFunction` trait, and implementing a function called `run()` within the new class, containing the code you want to run.

Now, lets create a new case class called `CreateReport`, which has the following inputs:

* totalNumberOfReadsFile: File
* sequenceCountsFile: File
* baseCountsFile: File

And outputs:

* report: File

This should run the following code to compile a very simplistic report:

      val writer = new PrintWriter(report)

      val totalNumberOfReads = Source.fromFile(totalNumberOfReadsFile).getLines.mkString
      writer.println("Total number of reads: " + totalNumberOfReads)

      writer.println(List("A", "C", "G", "T").mkString("\t"))
      val baseCounts = Source.fromFile(baseCountsFile).mkString
      writer.println(baseCounts)

      writer.println("List of the 10 most common sequences:")
      val sequenceCounts = Source.fromFile(sequenceCountsFile).getLines.take(10)
      writer.println(sequenceCounts.mkString("\n"))

      writer.close()

Create the class, including a definition of the `run()` function and see if you can add it to your workflow.

If there is time
-----------------
* See if you can implement some workflow that you normally use in Queue/Piper.
* Explore the existing qscripts to see some more advanced examples. `DNABestPracticeVariantCalling` might be a good place to start since it implements a well recognized workflow of "bwa" -> "GATK best practice data processing" -> "variant calling".


Troubleshooting
---------------
* Need an import but don't know it's namespace? `ctrl + space` in Scala-IDE will give you a list of suggestions. Selecting one will auto-complete and import the necessary class.
* If you have `ant 1.9.3` installed (which is the default version in e.g. Ubuntu 13.10) you need to revert to an earlier version. I used the .deb file found here: https://launchpad.net/ubuntu/+source/ant
