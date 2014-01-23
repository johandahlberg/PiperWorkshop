Queue/Piper workshop [insert date]
==================================

This document contains a small tutorial for the Piper workshop on [insert date]. The idea is to have a small workshop about Queue/Piper, going through the basics of how Queue/Pipere works and getting some hands on experience in writing qscripts.

Prerequisites
-------------
To follow this workshop you need to have the following programs installed:

    git
    sun-java 1.7
    ant
    scala-ide (optional - though it will make Scala coding a lot simpler)
    
If you want to be able to test run things on Uppmax you will also need a uppmax account.

Introductions
-------------
The text below might contain some jargon which is unfamiliar to you. Try not to get stuck, but move on and hopefully all will be clearer once you've started working with it in practice.

**A very brief introduction to Scala**

It will of course be impossible to cover everything in the Scala language here, and there are plenty of resources on the net which you can look through if you are interested. This will be a very brief introduction hopefully containing enough to allow you to start writing qscripts. 

Scala is a object oriented cross-paragdigm language which compiles to java byte code runs on the Java Virtual Machine. To access the full power of Scala one should program in a functional style, but it's absolutely possible to write imperative style code in scala. 

Scala is very similary to Java, so if you've worked with Java before you should be able to jump straight into it.
    
    // Declaring a immutable variable in Scala
    val x = 1
    
    // Declaring a mutable variable (should only be use when it's really needed)
    var y = "a"
    y = "b"

    // Scala is a strongly type language with type inference. That means that the compiler will figure out types for you depending on the context. This means that the following is illegal:  
    y = 1 // y has type String, and 1 has type Int (in this case). This will not compile.

    // Declare a class
    class MyAwesomeClass(x: Int, s: String) { // Parameters are optional. 
        // Class body
    }
    
    // Declase a case class (a special type of class which where natural equals methods are auto generated, and that can be use to do pattern matching.
    case class Point(x: Int, y: Int)
    
    // This means that the following will return true (which it would not do in Java without writing a custom equals method.
    Point(1,1) == Point(1,1)
    
    // As scala is a functional language, so functions are at the hearth of it. Here is how you declare a very simple one:
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
    
    // Scala also has something called traits (similar to interfaces in Java). A class can only inherit one class, but it can implement multiple traits. They can contain implemented fields and functions, or unimplemeted ones (this will force the implementing class to provide a implementation).
    trait Pingable {
        def ping: String = "Pong"
        def pong: String
    }
    
    // Traits are added to classes using the with key-word.
    class TwoDimensionalPoint(color: String, x: Int, y: Int) 
        extends ColoredPoint(color)
        with Pingable {
            def pong: String = "Ping"
        }

**Scala resources**

If you want to learn more about Scala (and read better tutorials than this one) have a look in some of these places:

[INSERT LIST OF LINKS]

**Queue** <br/>
[Queue](http://www.broadinstitute.org/gatk/guide/topic?name=queue) is a framework for writing pipelines developed by the Broad and is relased under a MIT licence. Queue is build in scala and contains a few core concepts which will be covered below. Only the basics will be covered here. For more you material, here are some useful resources:

http://www.broadinstitute.org/gatk/guide/events?id=3391
http://www.broadinstitute.org/gatk/guide/topic?name=queue

Some of the advantages of using Queue are:
* Parallelizes workflows
* Supports reruns
* Possible to create reusable components
* Excellent support for the GATK suite
* Relatively simple to add new components to a pipeline

*Qscripts*</br>
The qscript is at the heart of Queue. It's were you define how your pipeline is going to run.  It's written in Scala with some syntactic sugar. In your qscript you will add Commandline Functions to a dependency graph which will be run by Queue.

Formally a QScript is a class which extends `QScript` and that defines the function `script()`. Furthermore it will typically define (or import) a number of CommandLineFunction and define a have a number of arguments which can be passed to it when running it from the commandline.

Here's a tiny example of what a QScript can look like:

    import org.broadinstitute.sting.queue.QScript
    
    class MyAwesomeQScript extends QScript {
    
        // A argument that should be passed to the QScript from the commandline 
        @Input(doc = "input fasta file", shortName = "i", required = true)
        var input: File = _
    
        // Where you define the pipeline - more about this later
        def script() {
            //...
        }
    }

*CommandLineFunction*</br>
If qscripts are the hearth of Queue, CommandLineFunctions are it's blood. A CommandLineFunction constructs the acutal commands to be run. Each program that is run by Queue is defined as a case class extending the CommandLineFunction class. It defines inputs and outputs which is how Queue knows how to chain the jobs together.

Formally a CommandLineFunction is a class which extends the `CommandLineFunction` class and which defines the `commandline` function. Here's an example which finds runs a simple *nix onliner to find the number of occurences of each sequence in a fasta file.

    case clase NaiveSequenceCounter(@Input fastaFile: File, @Output sequenceCounts: File) extends CommandLineFunction {
        def commandLine = "cat " + fastaFile + 
                          " | grep -v \"^>\" | sort | uniq -c >" +
                          sequenceCounts
    }

The gist is, anything you can run on the commandline you can run with Queue.

*Put it together using add()*</br>
The key to putting the two things above together is using the `add()` function. This is is how you define what jobs are to be added to the dependency graph and run by Queue. An example of this:

    import org.broadinstitute.sting.queue.QScript
    import java.io.File
    import org.broadinstitute.sting.commandline.Argument
    
    class MyAwesomeQScript extends QScript {
    
      // An argument that should be passed to the QScript from the commandline 
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

Note that it does not matter in which order you add the functions. It is the input chain of input and output files that matter, Queue handles the rest.

**Piper** <br/>
Piper is build on top of Queue, and is basically a extension of Queue which allows Queue to be run on Uppmax. Futhermore it contains a number of predefined workflows, for example for exome resequencing, which are simple to deploy provided that your data has been generated by the SNP&SEQ Technology platform.

Advantages of using Piper:
* Simplifies process of deploying Queue on Uppmax
* Has predifined workflows for many common NGS applications (WGS, exome, RNA read counts and differential expression)
* Has predifined build blocks for running some common NGS tasks such as aligments, variant calling, etc.

Writing your first queue script
-------------------------------

Start by getting Piper and installing from github:
    
    git clone https://github.com/johandahlberg/piper.git
    cd piper
    git checkout devel
    ./setup.sh

If you are using Scala-IDE for your development you can create a eclipse project to import by typing:

    sbt/bin/sbt eclipse
    
This will create the files necessary to import the project into the Scala-IDE. 

[More on how to write the QScript]
[Remember that the script needs to extend Uppmaxable to be able to run on Uppmax]

Running it locally
------------------
To run you new QScript locally go into the Piper folder and run the following (with any setup you prefer:

    ./piper -S <path to your script> <all other parameters> --job_runner Shell

This will make a dry run of the script. Showing you the commandlines that will be run. To actually execute the jobs, add `-run`:

    ./piper -S <path to your script> <all other parameters> --job_runner Shell -run

Running it on Uppmax
--------------------
[Something about what changes need to be made to run on Uppmax]
[THIS IS JUST MY NOTES]

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

[Need to login in on a interactive node - need to be able to run Java]

To run a qscript you need to he vthe slurm-drmaa libraries on your library path. To get this run the following:

    export LD_LIBRARY_PATH=/sw/apps/build/slurm-drmaa/1.0.6/lib/:$LD_LIBRARY_PATH

Dry run:

    ./piper -S <path to your script> <all other parameters> --job_runner Shell --job_walltime <time to request from cluster in seconds>


Troubleshooting
---------------