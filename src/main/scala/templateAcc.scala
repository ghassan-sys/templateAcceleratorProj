
//authors: Ghassan Shaheen, Anas Mulhem
//
package templateAcc

import Chisel._
import chisel3.util.{HasBlackBoxResource}
import freechips.rocketchip.tile._
import org.chipsalliance.cde.config._
import freechips.rocketchip.diplomacy._
import freechips.rocketchip.rocket.{TLBConfig, HellaCacheReq}

class WrapBundle(nPTWPorts: Int)(implicit p: Parameters) extends Bundle {
  val io = new RoCCIO(nPTWPorts)
  val clock = Clock(INPUT)
  val reset = Input(UInt(1.W))
}

class templateAccBlackBox(implicit p: Parameters) extends 
	BlackBox(Map("DATA_WIDTH" -> IntParam(p.DATA_WIDTH), "ADDR_WIDTH" -> IntParam(p.ADDR_WIDTH), "CFG_REG_WIDTH" -> IntParam(p.CFG_REG_WIDTH), "NUM_OF_CFG_REGS" -> IntParam(p.NUM_OF_CFG_REGS), "MEM_DATA_WIDTH" -> IntParam(p.MEM_DATA_WIDTH), "BUFF_SIZE" -> IntParam(p.BUFF_SIZE), "LATENCY" -> IntParam(p.LATENCY))) 
		with HasBlackBoxResource {
  val io = IO(new WrapBundle(0))

  addResource("/vsrc/accTemplate.v")
}

class templateAcc(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes = opcodes) {
  override lazy val module = new templateAccImp(this)
}

class templateAccImp(outer: templateAcc)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {

    def params : TemplateAccParams
    val templateaccbb = Module(new templateAccBlackBox(params))
    io <> templateaccbb.io.io
    templateaccbb.io.clock := clock
    templateaccbb.io.reset := reset
  }

case class TemplateAccParams(
  DATA_WIDTH: Int = 8,
  ADDR_WIDTH: Int = 10,
  CFG_REG_WIDTH: Int = 32,
  NUM_OF_CFG_REGS: Int = 3,
  MEM_DATA_WIDTH: Int = 8,
  BUFF_SIZE: Int = 32,
  LATANCY: Int = 500,
  MEMORY_BANDWIDTH: Int = 300)
  

class WithTemplateAccBlackBox extends Config((site, here, up) => {
 case BuildRoCC => up(BuildRoCC) ++ Seq(
    (p: Parameters) => {
      val template_acc = LazyModule.apply(new templateAcc(OpcodeSet.custom2)(p))
      template_acc
    }
)	 
})


