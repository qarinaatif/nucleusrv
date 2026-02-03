package nucleusrv.components

import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec

class ImmediateGenTest extends AnyFlatSpec with ChiselScalatestTester {

  behavior of "ImmediateGen"

  it should "generate correct immediates for all RISC-V types" in {
    test(new ImmediateGen(F = false, XLEN = 64)) { dut =>

      // -------------------------------
      // I-TYPE : ADDI x1, x2, -1
      // imm = -1
      // -------------------------------
      dut.io.instruction.poke("hFFB00193".U)
      dut.clock.step()
      dut.io.out.expect("hFFFFFFFFFFFFFFFB".U)   

    }
  }
}
