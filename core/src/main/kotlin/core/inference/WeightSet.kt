package core.inference

abstract class WeightSet {

    abstract val neighborhoodExtra: Int

    abstract val closedFour: Int
    abstract val openThree: Int

    abstract val blockThree: Int
    abstract val openFour: Int
    abstract val five: Int

    abstract val blockFourExtra: Int
    abstract val treatBlockThreeFork: Int

    abstract val threeSideTrap: Int
    abstract val fourSideTrap: Int
    abstract val treatThreeSideTrapFork: Int

    abstract val doubleThreeFork: Int
    abstract val threeFourFork: Int
    abstract val doubleFourFork: Int

}
