Queue/Piper workshop at eInfraMPS2015 
=====================================

This document contains the material for the Queue/Piper workshop given at e-Infrastructure for [Massively Parallel Sequencing 2015](http://www.uppnex.se/events/eInfraMPS2015). Completing this tutorial should take approximatly one hour. The aim is that once you've completed the tutorial you should:

* Understand some basic Scala syntax
* Understand the basic underlying concepts of GATK Queue
* Be able to write a simple QScript
* Run this locally
* Run it in distributed mode

**What is Queue?** <br/>
[Queue](http://www.broadinstitute.org/gatk/guide/topic?name=queue) is a framework for writing pipelines developed by the GATK group at the Broad Institure and is released under a MIT licence. Queue is built in Scala and contains a few core concepts which will be covered below. Only the basics will be covered here. For more material, here are some useful resources:

* http://www.broadinstitute.org/gatk/guide/events?id=3391
* http://www.broadinstitute.org/gatk/guide/topic?name=queue

Some of the advantages of using Queue are:
* Parallelize workflows
* Supports reruns
* Possible to create reusable components
* Excellent support for the GATK suite
* Relatively simple to add new components to a pipeline

**What is Piper?** <br/>
Piper is a collection of common NGS workflows implemented on-top of Queue and to run on the Uppmax high-performance computing cluster. It supports things like variant calling in whole genome and targeted DNA-seq, and transcript quantifications for RNA-seq data. This workshop will focus on Queue since this of more general interest. 

**Finding help** <br/>
If you get stuck (and your at the actual workshop) don't hesitate to ask me. If your doing this on your own you can find examples covering everything in this workshop in the `solutions` directory in this repository.

Getting started
---------------

The simplest way to get going with this tutorial is to download the following VM and import into e.g. VirtualBox: https://drive.google.com/file/d/0BwX8h-A0AgGIMlhnMHhnZFZLTnM/view?usp=sharing (md5sum: 12610cf6aac8cc74a709a12a4c8511f5). Spin it up and login with:

    username: piper
    password: workshop

This has all pre-requisites required to run this tutorial installed, including a slurm-installation which allows you to simulate running in a distributed environment. If you want to install Piper for yourself follow the install instructions in the main repo: https://github.com/NationalGenomicsInfrastructure/piper

To make sure you have the latest version of this tutorial available in the VM do this:

    cd ~/Desktop/PiperWorkshop
    git pull origin

To keep things simple `vim` has been installed on the VM. Additionally ScalaIDE has been installed (/home/piper/Bin/eclipse/eclipse), though that might be somewhat sluggish on a limited resources VM. Want to use another editor, feel free to install it (you do have sudo privileges on the VM).

Part 0: A super quick intro to Scala
------------------------------------

The text below might contain some jargon which is unfamiliar to you. Try not to get stuck, but move on and hopefully it all will be clearer once you've started working with it in practice.

**A very brief introduction to Scala**<br/>
Queue is build in Scala, therefore it's useful to have a basic understanding of the language. It will of course be impossible to cover everything in the Scala language here, and there are plenty of resources on the net which you can look through if you are interested. This will be a very brief introduction hopefully containing enough to allow you to start writing qscripts. 

Scala is a object oriented cross-paradigm language which compiles to java byte code and runs on the Java Virtual Machine. To access the full power of Scala one should program in a functional style, but it's absolutely possible to write imperative style code in scala. Scala is very similary to Java, so if you've worked with Java before you should be able to jump straight into it.

*Exercise 1*<br/>
Start by fireing up `sbt` (Scalas build tool) from the Piper directory using:

    # Assumes you have cd:ed into the workshop dir: ~/Desktop/PiperWorkshop
    piper/sbt/bin/sbt
    
When the console opens type `console` to initiate the Scala REPL. You can then type all the commands below and see them evaluate. Play around with it to familiarize yourself with the syntax.
    
    // Declaring an immutable variable in Scala
    val x = 1
    
    // Declaring a mutable variable
    var y = "a"
    y = "b"
    
    // Scala is a strongly type language with type inference.
    // That means that the compiler will figure out types for you 
    // depending on the context. This means that the following is illegal:
    y = 1 // y has type String, 
          // and 1 has type Int (in this case).
          // This will not compile.
    
    // Declare a class
    class MyAwesomeClass(x: Int, s: String) { // Parameters are optional.
    // Class body
    }
    
    // Declare a case class (a special type of class which where natural
    // equals methods are auto generated, and that can be use to do 
    // pattern matching.
    case class Point(x: Int, y: Int)
    
    // This means that the following will return true (which it would 
    // not do in Java without writing a custom equals method).
    Point(1, 1) == Point(1, 1)
    
    // As Scala is a functional language, so functions are at the 
    // heart of it. Here is how you declare a very simple one:
    def square(x: Int): Int = x * x
    
    // A slightly more complex example is
    // Note the use of higher order functions in "map" and "reduce"
    def squareThanSum(x: List[Int]): Int = {
        val squared = x.map(y => y * y) // Square each element in the list
        val summed = squared.reduce((y, z) => y + z) // And sum the elements
        summed // The last line in the function is returned
               // no need for a return keyword
    }
    
    // Extending a class:
    class ColoredPoint(color: String)
    class TwoDimensionalPoint(
      color: String,
      x: Int, y: Int) extends ColoredPoint(color)
    
    // Scala also has something called traits (similar to interfaces in Java).
    // A class can only inherit one class, but it can implement multiple traits.
    // They can contain implemented fields and functions, or unimplemented ones
    // (this will force the implementing class to provide a implementation).
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
    val newSequence: Seq[Int] =
        for (elem <- sequence) yield elem + 1


**Scala resources** <br/>
If you want to learn more about Scala (and read better tutorials than this one) have a look in some of these places:

* http://www.scala-lang.org
* http://twitter.github.io/scala_school/
* http://stackoverflow.com/tags/scala/info
* http://aperiodic.net/phil/scala/s-99/
* http://www.scala-lang.org/docu/files/ScalaByExample.pdf

Part 1: The basic concepts
--------------------------

**Qscripts**<br/>
The qscript is at the heart of Queue. It's were you define how your pipeline is going to run. The basic concept is that programs are chained together based on their inputs and outputs to create a dependency graph. Queue the uses this to determine the order in which the programs can be run and execute the accordingly. Formally a QScript is a class which extends `QScript` and that defines the function `script()`.

Here's a tiny example of what a QScript can look like:

    // The namespace in which this QScript exits
    package example.qscripts
    
    // Imports of other classes
    import org.broadinstitute.gatk.queue.QScript
   
    // The class definition of the QScript
    class MyAwesomeQScript extends QScript {
    
        // A argument that should be passed to the qscript from the command line
        // Will also be used to create commandline documentation of the script
        @Input(doc = "input fasta file", shortName = "i", required = true)
        var input: File = _
    
        // Where you define the pipeline - more about this later
        def script() {
            //...
        }

        // Start adding your CommandLineFunction classes here
    }

*Exercise 2*<br/>
Create a file called `MyAwesomeQScript.scala` and insert the code above into it. Then try: `piper -S MyAwesomeQScript.scala --help`. This will show you all the general options for Queue (there are lots of them) and the once specific to this QScript.

Try removing the `--help` part. What happens?

**CommandLineFunction**<br/>
If qscripts are the heart of Queue, CommandLineFunctions are its blood. A CommandLineFunction constructs the actual commands to be run. Each program that is run by Queue is defined as a case class extending the CommandLineFunction class. It defines inputs and outputs, which is how Queue knows how to chain the jobs together into a dependency graph.

Formally a CommandLineFunction is a class which extends the `CommandLineFunction` class and which defines the `commandline` function. Here's an example which runs a simple *nix oneliner to find the number of occurrences of each sequence in a fasta file.

    case class NaiveSequenceCounter(@Input fastaFile: File, @Output sequenceCounts: File) extends CommandLineFunction {
        def commandLine = "cat " + fastaFile + 
                          " | grep -v \"^>\" | sort | uniq -c >" +
                          sequenceCounts
    }

The gist is, anything you can run on the commandline you can run with Queue.

*Exercise 3* <br/>
Add the code example code above to `MyAwesomeQScript.scala` and add another one command line program with the following specification:

    Input: the output of the previous example (a file with counts in 
           the first column and sequences in the second column).
    Output: a file containing the total number reads in the file
    Commandline suggestion: cat <input file> | awk '{sum=sum+$1} END{print sum}' > <output_file>

And then try: `piper -S MyAwesomeQScript.scala --help`. See if you can get it to run without any compile errors.

**Put it together using add()**<br/>
So far we've not actually had this create any dependency graph. The key to creating the dependency graph is the `add()` function.

*Exercise 4* <br/>
It's time to get into your `script()` function. Add the output files you want to create and then use the `add()` function to add your jobs to the dependency graph.

    def script() = {
    
        // Defining names of output files
        val seqCounts = new File("sequence_counts.txt")
        val totalNumberOfReads = new File("total_read_nbr.txt")
    
        // Add jobs to dependency graph
        add(NaiveSequenceCounter(input, seqCounts))
        //add(YOUR OWN SEQUENCE COUNTER, YOU CREATED IN EXERCISE 3)
    }

Note that it does not matter in which order you `add()` the functions. It is the chain of input and output files that matter, Queue handles the rest.

Part 2: Time to run this!
--------------------------

Up until this point we actually haven't actually run any jobs. Time to fix that!

*Exercise 5*<br/>
By now you should have a functioning qscript. So lets try dry running it. Run the qscript like this:

    # Assumes you are in the ~/Desktop/PiperWorkshop directory
    piper -S MyAwesomeQScript.scala -i test_data/test1.fasta 

You should now see Queue output indicating that the QScript was dry run. Well dry running isn't as fun as running it for real, so lets try that:

    # Assumes you are in the ~/Desktop/PiperWorkshop directory
    piper -S MyAwesomeQScript.scala -i test_data/test1.fasta -run

What happens?

You should see a your output files created in the output directory you specified. In addition to this a file for each job with a `.out` extension is create. This will contain the log for the program that created the file. 

Try `ls -la` and you should see that each job has a corresponding hidden file which the file extention `.done` this is how Queue knows what jobs have finished sucessfully and or not.

What happens if you run `piper -S MyAwesomeQScript.scala -i test_data/test1.fasta -run` again? Is the pipeline re-run? Remove one of the `.done` files and run it again. What's the difference?

Part 3: Make it distributed
---------------------------

Well running your jobs is cool, but running something in distributed mode is way cooler. The default "jobrunner" in Queue is the Shell jobrunner. By switching job runners you can make Queue run your jobs in a distributed fashion. The VM has a simulated Slurm environment setup for this purpose.

*Exercise 6*<br/>

    # The -startFromScratch option will make sure the pipeline 
    # is run even if all files are present
    piper -S MyAwesomeQScript.scala -i test_data/test1.fasta -run -startFromScratch -jobRunner Drmaa

You should now see that the DrmaaJobRunner submits jobs. Use the `squeue` command to see the job queue. (You might have to add a `sleep 10` to you commandlines to actually catch them in the Slurm queue, since the example files are so small these jobs will run very fast).

Please note that depending on your specific compute environment adding the `-jobRunner` argument might not be enough. Since the cluster environment might require additional information about the job such as project number to bill hours to etc. Using the `-jobNative` flag can be useful in such circumstances to to feed additional arguments to the cluster environment. Piper provides a number of utility classes to do this on the specific setup of the Uppmax Slurm cluster, and that might work in other environments as well.  

Part 4: Additional cool things (if time allows)
-----------------------------------------------

**Running on multiple files**<br/>
In the example above we have so far only processed one file - in real life that's seldom the case. One way to allow multiple input files of the same type is to import:

    import org.broadinstitute.sting.queue.util.QScriptUtils
    
And use a construct like this:    

    val fastaFiles: Seq[File] = QScriptUtils.createSeqFromFile(input)
    for(fastaFile <- fastaFiles) {
        // Do something with the files
    }

This allows you to input a file with a list of files (one per line) to run.

Alternatively you can change your input argument to:

    var input: Seq[File] = Nil

Then you can specify `-i` on the commandline multiple times. 

*Exercise 6*<br/>
See if you can use either of these methods to run your CommandLineFunctions on all the fasta files in the test data directory. A tip is that since Queue uses file names to build the dependency graph you need to make sure each output file gets a unique file name.

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

      writer.println("List of the 10 most common sequences:")
      val sequenceCounts = Source.fromFile(sequenceCountsFile).getLines.take(10)
      writer.println(sequenceCounts.mkString("\n"))

      writer.close()


*Exercise 7*<br/>
Create the class, including a definition of the `run()` function and see if you can add it to your workflow.

**Putting it all together**<br/>
Armed with the tools you've got so far - can you create a script which will count the number of occurences of base base (A, C, T, G) in the sequence parts of the fasta files, add this as another commandline tool, and add that information to the report? Use any tools you see fit to do the base counting.

Congratulations, you've now finished this tutorial!


