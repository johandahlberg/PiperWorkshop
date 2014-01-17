Queue/Piper workshop 22/1 2014
==============================

This document contains a small tutorial for the Piper workshop on 22/1 2014. The idea is to have a small workshop about Queue/Piper, going through the basics of how Queue/Pipere works and getting some hands on experience in writing QScripts.

Prerequisites
-------------
To follow this workshop you need to have the following programs installed:

    git
    sun-java 1.7
    scala-ide (optional - though it will make Scala coding alot simpler)

Introductions
-------------
The text below might contain some jargon which is unfamiliar to you. Try not to get stuck, but move on and hopefully all will be clearer once you've started working with it in practice.

**A very brief introduction to Scala**


**Queue** <br/>
[Queue](http://www.broadinstitute.org/gatk/guide/topic?name=queue) is a framework for writing pipelines developed by the Broad and is relased under a MIT licence. Queue is build in scala and contains a few core concepts which will be covered below. Only the basics will be covered here. For more you material, here are some useful resources:

http://www.broadinstitute.org/gatk/guide/events?id=3391
http://www.broadinstitute.org/gatk/guide/topic?name=queue

*QScripts*</br>
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
If qscripts are the heath of Queue, CommandLineFunctions are it's blood. A CommandLineFunction constructs the acutal commands to be run. Each program that is run by Queue is defined as a case class extending the CommandLineFunction class. It defines inputs and outputs which is how Queue knows how to chain the jobs together.

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

Writing your first queue script
-------------------------------

Running it locally
------------------

Dry run / real run

Running it on Uppmax
--------------------

Dry run / real run
