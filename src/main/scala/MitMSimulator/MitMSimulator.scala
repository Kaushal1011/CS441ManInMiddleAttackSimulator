package MitMSimulator
import helpers.ComparableNode
import scala.util.Random


object MitMSimulator {

  // attack success when sim over thres, both node ids same and valuable data
  // attack failure when sim over thres, both node ids diff and not valuable data
  // attack not success not fail when sim over thres, node ids diff

  // returns if attacked or not, attack sucessful or not, attack failed or not
  private def attack(currAttr: ComparableNode, attrInOriginal: ComparableNode, successfulAttacks: Long, failedAttacks: Long): (Boolean, Long, Long) = {

    val sim = currAttr.SimRankFromJaccardSimilarity(attrInOriginal)
    // attack based on probability 0.5 is succ = failed =0
    // 0.3 if succ == failed != 0
    // 0.75 if succ > failed
    // 0.2 if succ > failed

    if (currAttr.id == 33){
      println(s"sim 33: $sim")
    }

    if (0.8 < sim) {
      println(s"sim: $sim")
      println("We attacking")
      // Determine attack probability based on attack history
      val attackProb = (successfulAttacks, failedAttacks) match {
        case (0, 0) => 0.9
        case _ if successfulAttacks == failedAttacks => 0.8
        case _ if successfulAttacks > failedAttacks => 0.95
        case _ => 0.1
      }

      // Decide whether to attack or not based on the determined probability
      if (Random.nextDouble() <= attackProb) {
        if (currAttr.id == attrInOriginal.id && currAttr.valuableData) {
          // Attack successful
//          print("Attack successful for 33")
          return (true, successfulAttacks + 1, failedAttacks)
        } else if (currAttr.id != attrInOriginal.id && attrInOriginal.valuableData) {
          // Attack failed
          return (true, successfulAttacks, failedAttacks + 1)
        } else {
          // Attack neither successful nor failed
          return (false, successfulAttacks, failedAttacks)
        }
      }else{
        // we dont attack
        return (false, successfulAttacks, failedAttacks )
      }

    }
    else {
      // Dint attack
      return (false, successfulAttacks, failedAttacks)
    }
  }

  def attackingOriginalGraph(currAttr: ComparableNode, originalGraph: Array[ComparableNode], successfulAttacks: Long, failedAttacks: Long): (Long, Long) = {

    // Initial state
    val initialState = (false, successfulAttacks, failedAttacks) // The first element of the tuple is a flag to indicate if an attack was made

    val (attackMade, finalSuccessful, finalFailed) = originalGraph.foldLeft(initialState) {
      case ((true, successful, failed), _) => // If an attack was already made, just propagate the same state without calling attack function again
        (true, successful, failed)

      case ((false, successful, failed), node) =>
        val (attacked, newSuccessful, newFailed) = attack(currAttr, node, successful, failed)

        if (attacked)
          (true, newSuccessful, newFailed)
        else
          (false, newSuccessful, newFailed)
    }

    (finalSuccessful, finalFailed)
  }

}
