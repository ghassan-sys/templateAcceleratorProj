package chipyard.example

// scala collections

import chisel3._
import chisel3.util._
import chisel3.experimental.{IntParam, BaseModule}
import freechips.rocketchip.amba.axi4._
import freechips.rocketchip.subsystem.BaseSubsystem
import org.chipsalliance.cde.config.{Parameters, Field, Config}
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.regmapper.{HasRegMap, RegField}
import freechips.rocketchip.tilelink._
import freechips.rocketchip.util.UIntIsOneOf

// DOC include start: MMIO params
case class MMIOParams(
  address: BigInt = 0x1000,
  DATA_WIDTH: Int = 8,
  ADDR_WIDTH: Int = 10,
  CFG_REG_WIDTH: Int = 32,
  NUM_OF_CFG_REGS: Int = 3,
  MEM_DATA_WIDTH: Int = 8,
  BUFF_SIZE: Int = 32,
  LATENCY: Int = 500,
  MEMORY_BANDWIDTH: Int = 300)
// DOC include end: MMIO params


// FIXME: to be deleted
// DOC include start: GCD key
// case object GCDKey extends Field[Option[GCDParams]](None)
// DOC include end: GCD key

class MMIO_IO() extends Bundle {
  val clock = Input(Clock())
  val reset = Input(Bool())
  val input_ready = Output(Bool())
  val input_valid = Input(Bool())
  val output_ready = Input(Bool())
  val output_valid = Output(Bool())
  val busy = Output(Bool())
  val data_out = Output(UInt(64.W))
  
// watch out! might not be true
  val common_cfg_reg_ = Output(UInt(NUM_OF_CFG_REG*CFG_REG_WIDTH.W))
  val input_cfg_reg_ = Output(UInt(NUM_OF_CFG_REG*CFG_REG_WIDTH.W))
  val output_cfg_reg_ = Output(UInt(NUM_OF_CFG_REG*CFG_REG_WIDTH.W))
  val funct_cfg_reg_ = Output(UInt(CFG_REG_WIDTH.W))
}


//FIXME: do we need this?
trait MMIOTopIO extends Bundle {
  val mmio_busy = Output(Bool())
}

trait HasMMIO_IO extends BaseModule {
//  val w: Int
  val io = IO(new MMIO_IO())
}



// DOC include start: MMIO blackbox
class MMIOAccBlackBox(tap: MMIOParams) (implicit p: Parameters) extends BlackBox(Map("address" -> IntParam(tap.address), "DATA_WIDTH" -> IntParam(tap.DATA_WIDTH), "ADDR_WIDTH" -> IntParam(tap.ADDR_WIDTH), "CFG_REG_WIDTH" -> IntParam(tap.CFG_REG_WIDTH), "NUM_OF_CFG_REGS" -> IntParam(tap.NUM_OF_CFG_REGS), "MEM_DATA_WIDTH" -> IntParam(tap.MEM_DATA_WIDTH), "BUFF_SIZE" -> IntParam(tap.BUFF_SIZE), "LATENCY" -> IntParam(tap.LATENCY))) with HasBlackBoxResource
  with HasMMIO_IO
{
  addResource("/vsrc/MMIOAccBlackBox.v")
}
// DOC include end: MMIO blackbox


// DOC include start: GCD instance regmap

trait MMIOModule extends HasRegMap {
  val io: MMIOTopIO

  implicit val p: Parameters
  def params: MMIOParams = {MMIOParams()}
  val clock: Clock
  val reset: Reset
  val impl = Module(new MMIOAccBlackBox(params))
  val y = Wire(new DecoupledIO(UInt(1.W)))
  val gcd = Wire(new DecoupledIO(UInt(64.W)))
  val splitCfgReg = Vec(UInt(params.CFG_REG_WIDTH.W), UInt(params.NUM_OF_CFG_REGS.W))

  impl.io.clock        := clock
  impl.io.reset        := reset.asBool
  impl.io.input_valid  := y.valid
  y.ready 	       := impl.io.input_ready
  gcd.bits 	       := impl.io.data_out
  gcd.valid 	       := impl.io.output_valid
  impl.io.output_ready := gcd.ready
  io.mmio_busy 	       := impl.io.busy

  for (i <- 0 until params.NUM_OF_CFG_REGS) {
    splitCfgReg(i) := VecInit(Seq.tabulate(4)(j => _cfg_reg_(i * 4 + j)))
  }
  cfg_reg_mapping = splitCfgReg.zip(range(8, 64, 4)).map{case (x, y) => (y -> Seq(x))}

  regmap(
    0x00 -> Seq(
      RegField.w(1, y)), // write-only, y.valid is set on write
    0x04 -> Seq(
      RegField.r(64, gcd))) // read-only, gcd.ready is set on read
}
// DOC include end: MMIO instance regmap


// DOC include start: MMIO router
class MMIOTL(params: MMIOParams, beatBytes: Int)(implicit p: Parameters)
  extends TLRegisterRouter(
    params.address, "mmio", Seq("ucbbar,mmio"),
    beatBytes = beatBytes)(
      new TLRegBundle(params, _) with MMIOTopIO)(
      new TLRegModule(params, _, _) with MMIOModule)



// DOC include start: MMIO lazy trait
trait CanHavePeripheryMMIO { this: BaseSubsystem =>
  private val portName = "mmio"
  val mmio = LazyModule(new MMIOTL(params, pbus.beatBytes)(p))
  pbus.coupleTo(portName) { mmio.node := TLFragmenter(pbus.beatBytes, pbus.blockBytes) := _ }
  Some(mmio)
}
// DOC include end: MMIO lazy trait



// DOC include start: MMIO imp trait
trait CanHavePeripheryMMIOModuleImp extends LazyModuleImp {
  val outer: CanHavePeripheryMMIO
  val mmio_busy = outer.mmio match {
    case Some(mmio) => {
      val busy = IO(Output(Bool()))
      busy := mmio.module.io.mmio_busy
      Some(busy)
    }
    case None => None
  }
}

// DOC include end: MMIO imp trait


// DOC include start: MMIO config fragment
class WithMMIO() extends Config((site, here, up) => {})
// DOC include end: MMIO config fragment
