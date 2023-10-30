package Utilz

import com.typesafe.config.Config

object ConfigReader {

  case class MitMConfig(
                         similarityThreshold: Double,
                         initCase: Double,
                         sucEqFail: Double,
                         sucGtFail: Double,
                         sucLtFail: Double
                       )

  case class SimConfig(
                        initNodesPercentage: Double,
                        simIterations: Int
                      )

  case class MitMSimConfig(
                            mitmConfig: MitMConfig,
                            simConfig: SimConfig
                          )

  def getMitMSimConfig(config: Config): MitMSimConfig = {
    val mitmConfig = config.getConfig("MitMSim.mitmConfig")
    val simConfig = config.getConfig("MitMSim.simConfig")

    MitMSimConfig(
      MitMConfig(
        mitmConfig.getDouble("similarityThreshold"),
        mitmConfig.getDouble("initCase"),
        mitmConfig.getDouble("sucEqFail"),
        mitmConfig.getDouble("sucGtFail"),
        mitmConfig.getDouble("sucLtFail")
      ),
      SimConfig(
        simConfig.getDouble("initNodesPercentage"),
        simConfig.getInt("simIterations")
      )
    )
  }
}
