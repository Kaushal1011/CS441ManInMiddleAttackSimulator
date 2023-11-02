package MitMSimulator
import helpers.ComparableNode
import scala.util.Random
import com.typesafe.config.ConfigFactory
import Utilz.ConfigReader

object MitMSimulator {

  // attack success when sim over thres, both node ids same and valuable data
  // attack failure when sim over thres, both node ids diff and not valuable data
  // attack not success not fail when sim over thres, node ids diff

  // returns if attacked or not, attack sucessful or not, attack failed or not

  val config = ConfigFactory.load()
  val mitmSimConfig = ConfigReader.getMitMSimConfig(config)


  private def attack(currAttr: ComparableNode, attrInOriginal: ComparableNode, successfulAttacks: Long, failedAttacks: Long, misIdentified:Long, uneventful:Long): (Boolean, Long, Long, Long, Long) = {

    val sim = currAttr.SimRankFromJaccardSimilarity(attrInOriginal)
    // attack based on probability 0.5 is succ = failed =0
    // 0.3 if succ == failed != 0
    // 0.75 if succ > failed
    // 0.2 if succ > failed
    // The above are example probabilities, we can change them using the configuration file

    if (mitmSimConfig.mitmConfig.similarityThreshold < sim) {
      // Determine attack probability based on attack history
      val attackProb = (successfulAttacks, failedAttacks) match {
        case (0, 0) => mitmSimConfig.mitmConfig.initCase
        case _ if successfulAttacks == failedAttacks => mitmSimConfig.mitmConfig.sucEqFail
        case _ if successfulAttacks > failedAttacks => mitmSimConfig.mitmConfig.sucGtFail
        case _ => mitmSimConfig.mitmConfig.sucLtFail
      }

      // Decide whether to attack or not based on the determined probability
      if (Random.nextDouble() <= attackProb) {
        if (currAttr.id == attrInOriginal.id && currAttr.valuableData) {
          // Attack successful
//          print("Attack successful for 33")
          return (true, successfulAttacks + 1, failedAttacks, misIdentified, uneventful)
        } else if (currAttr.id != attrInOriginal.id && attrInOriginal.valuableData) {
          // Attack failed
          return (true, successfulAttacks, failedAttacks + 1,misIdentified, uneventful)
        } else if (currAttr.id != attrInOriginal.id){
          // Attack neither successful nor failed
          return (false, successfulAttacks, failedAttacks,misIdentified + 1, uneventful)
        }
        else {
          // Attack neither successful nor failed
          return (false, successfulAttacks, failedAttacks, misIdentified , uneventful + 1)
        }
      }else{
        // we dont attack
        return (false, successfulAttacks, failedAttacks, misIdentified, uneventful)
      }

    }
    else {
      // Dint attack
      return (false, successfulAttacks, failedAttacks, misIdentified, uneventful)
    }
  }

  def attackingOriginalGraph(currAttr: ComparableNode, originalGraph: Array[ComparableNode], successfulAttacks: Long, failedAttacks: Long, misIdentifiedAttacks: Long, uneventfulattacks: Long): (Long, Long, Long, Long) = {

    // Initial state
    val initialState = (false, successfulAttacks, failedAttacks, misIdentifiedAttacks, uneventfulattacks) // The first element of the tuple is a flag to indicate if an attack was made

    val (attackMade, finalSuccessful, finalFailed, finalMisIdentified, finalUneventful) = originalGraph.foldLeft(initialState) {
      case ((true, successful, failed, miss, unevent), _) => // If an attack was already made, just propagate the same state without calling attack function again
        (true, successful, failed, miss, unevent)

      case ((false, successful, failed, miss, unevent), node) =>
        val (attacked, newSuccessful, newFailed, newMiss, newUnevent) = attack(currAttr, node, successful, failed, miss, unevent)

        if (attacked)
          (true, newSuccessful, newFailed, newMiss, newUnevent)
        else
          (false, newSuccessful, newFailed, newMiss, newUnevent)
    }

    (finalSuccessful, finalFailed, finalMisIdentified, finalUneventful)
  }

}
