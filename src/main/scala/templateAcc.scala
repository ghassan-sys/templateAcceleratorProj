
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
	BlackBox(Map("DATA_WIDTH" -> IntParam(w), "ADDR_WIDTH" -> IntParam(w), "CFG_REG_WIDTH" -> IntParam(w), "NUM_OF_CFG_REGS" -> IntParam(w), "MEM_DATA_WIDTH" -> IntParam(w), "BUFF_SIZE" -> IntParam(w), "LATENCY" -> IntParam(w))) 
		with HasBlackBoxResource {
  val io = IO(new WrapBundle(0))

  addResource("/vsrc/accTemplate.v")
}

class templateAcc(opcodes: OpcodeSet)(implicit p: Parameters) extends LazyRoCC(opcodes = opcodes) {
  override lazy val module = new templateAccImp(this)
}

class templateAccImp(outer: templateAcc)(implicit p: Parameters) extends LazyRoCCModuleImp(outer) {

    val templateaccbb = Module(new templateAccBlackBox)
    io <> templateaccbb.io.io
    templateaccbb.io.clock := clock
    templateaccbb.io.reset := reset
  }
  

class WithTemplateAccBlackBox extends Config((site, here, up) => {})


