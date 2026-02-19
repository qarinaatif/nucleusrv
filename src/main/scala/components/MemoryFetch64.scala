package nucleusrv.components
import chisel3._
import chisel3.util._ 

class MemoryFetch64(TRACE: Boolean) extends Module {
  val io = IO(new Bundle {
    val aluResultIn: UInt = Input(UInt(32.W))
    val writeData: UInt = Input(UInt(32.W))
    val writeEnable: Bool = Input(Bool())
    val readEnable: Bool = Input(Bool())
    val readData: UInt = Output(UInt(32.W))
    val stall: Bool = Output(Bool())
    val dccmReq = Decoupled(new MemRequestIO)
    val dccmRsp = Flipped(Decoupled(new MemResponseIO))
  })

  io.dccmRsp.ready := true.B

  // Write word logic
  when(io.writeEnable) {
    io.dccmReq.bits.dataRequest := io.writeData
    io.dccmReq.bits.addrRequest := Cat("b00".U, (io.aluResultIn & "h3FFFFFFF".U)(31, 2))
    io.dccmReq.bits.isWrite := true.B
    io.dccmReq.valid := true.B
  }.otherwise {
    io.dccmReq.bits.dataRequest := 0.U
    io.dccmReq.bits.addrRequest := 0.U
    io.dccmReq.bits.isWrite := false.B
    io.dccmReq.valid := io.readEnable
  }

  io.stall := ((io.writeEnable || io.readEnable) && !io.dccmRsp.valid)

  // Load word logic
  when(io.readEnable) {
    val funct3 = io.aluResultIn(14,12) // Example: extract funct3 from aluResultIn
    when(funct3 === "b010".U) { // LW
      io.readData := Mux(io.dccmRsp.valid, io.dccmRsp.bits.dataResponse, 0.U)
    }.otherwise {
      io.readData := 0.U
    }
  }
}