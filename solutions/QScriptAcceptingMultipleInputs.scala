// The namespace in which this class will live
package example.qscripts

// Import the QScript base class
import org.broadinstitute.gatk.queue.QScript

// The class definition of the QScript
class MyAwesomeQSCript extends QScript {

  // A argument that should be passed to the qscript from the command line
  // Will also be used to create commandline documentation of the script
  @Input(doc = "Cool input, right?", shortName = "i", required = true)
  var input: Seq[File] = Nil

  def script() = {

    for(inputFile <- input) {
            
      val base = inputFile.getName().stripSuffix(".fasta")

      val seqCounts = new File(base + "_sequence_counts.txt")
      val totalNumberOfReads = new File(base + "_total_read_nbr.txt")

      add(NaiveSequenceCounter(inputFile, seqCounts))
      add(SumTotalNumberOfRead(seqCounts, totalNumberOfReads))

    }

    val seqCounts = new File("sequence_counts.txt")
    val totalNumberOfReads = new File("total_read_nbr.txt")

    add(NaiveSequenceCounter(input, seqCounts))
    add(SumTotalNumberOfRead(seqCounts, totalNumberOfReads))

  }

  case class NaiveSequenceCounter(@Input fastaFile: File, @Output sequenceCounts: File) extends CommandLineFunction {
    def commandLine = "cat " + fastaFile +
      " | grep -v \"^>\" | sort | uniq -c >" +
      sequenceCounts
  }

  case class SumTotalNumberOfRead(@Input seqCounts: File, @Output totalNumberOfReads: File) extends CommandLineFunction {
    def commandLine = "cat " + seqCounts + " | awk '{sum=sum+$1} END{print sum}' > " + totalNumberOfReads + " && sleep 10"
  }

}
