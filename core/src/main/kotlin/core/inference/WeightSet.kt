package core.inference

interface WeightSet {

    val neighborhoodExtra: Int

    val closedFour: Int
    val openThree: Int

    val blockThree: Int
    val openFour: Int
    val five: Int

    val blockFourExtra: Int
    val treatBlockThreeFork: Int

    val threeSideTrap: Int
    val fourSideTrap: Int
    val treatThreeSideTrapFork: Int

    val doubleThreeFork: Int
    val threeFourFork: Int
    val doubleFourFork: Int

}
