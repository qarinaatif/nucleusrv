package nucleusrv.components

import chisel3._
import chisel3.util._

import nucleusrv.components.ALUOps._

class ALU(XLEN: Int) extends Module {
  val io = IO(new Bundle {
    val input1: UInt = Input(UInt(XLEN.W))
    val input2: UInt = Input(UInt(XLEN.W))
    val aluCtl: UInt = Input(UInt(4.W))

    val result: UInt = Output(UInt(XLEN.W))
  })
  
  val shamt     = io.input2(log2Ceil(XLEN)-1, 0)
  val input1_32 = io.input1(31,0).asSInt
  val input2_32 = io.input2(31,0).asSInt

  val sllw_tmp = (io.input1(31,0) << io.input2(4, 0))(31,0)
  val srlw_tmp = (io.input1(31,0) >> io.input2(4, 0))(31,0)
  val sraw_tmp = (input1_32 >> io.input2(4, 0)).asUInt

  io.result := MuxCase(
    0.U,
    Array(
      (io.aluCtl === AND) -> (io.input1 & io.input2),
      (io.aluCtl === OR) -> (io.input1 | io.input2),
      (io.aluCtl === ADD) -> (io.input1 + io.input2),
      (io.aluCtl === SUB) -> (io.input1 - io.input2),
      (io.aluCtl === SLT) -> (io.input1.asSInt < io.input2.asSInt).asUInt,
      (io.aluCtl === SLTU) -> (io.input1 < io.input2),
      (io.aluCtl === SLL) -> (io.input1 << shamt),
      (io.aluCtl === SRL) -> (io.input1 >> shamt),
      (io.aluCtl === SRA) -> (io.input1.asSInt >> shamt).asUInt,
      (io.aluCtl === XOR) -> (io.input1 ^ io.input2)
    ) ++ (if (XLEN > 32) Array(
      (io.aluCtl === ADDW) -> Cat(Fill(XLEN-32, (input1_32 + input2_32)(31)), (input1_32 + input2_32)).asUInt,
      (io.aluCtl === SUBW) -> Cat(Fill(XLEN-32, (input1_32 - input2_32)(31)), (input1_32 - input2_32)).asUInt,
      (io.aluCtl === SLLW) -> Cat(Fill(XLEN-32, sllw_tmp(31)), sllw_tmp).asUInt,
      (io.aluCtl === SRLW) -> Cat(Fill(XLEN-32, srlw_tmp(31)), srlw_tmp).asUInt,
      (io.aluCtl === SRAW) -> Cat(Fill(XLEN-32, sraw_tmp(31)), sraw_tmp).asUInt
    ) else Array[(Bool, UInt)]())
  )
}
