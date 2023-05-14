import org.chipsalliance.cde.config.{Config}
import freechips.rocketchip.diplomacy.{AsynchronousCrossing}

// ------------------------------
// Configs with RoCC Accelerators
// ------------------------------

class templateAccConfig extends Config(
  new freechips.rocketchip.subsystem.WithNBigCores(1) ++
  new chipyard.templateAcc.WithTemplateAccBlackBox() ++
  new chipyard.config.AbstractConfig)
