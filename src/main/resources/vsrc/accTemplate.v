/*
* Accelerator module (generic template).
* Fits both types - MMIO and RoCC accelerators.
* Counts latency.
*/


module AcceleratorTemplate#
(
// Define module parameters (the accelerator's traits)
  parameter int DATA_WIDTH 	   = 8,    // Data width in bits.
  parameter int ADDR_WIDTH 	   = 10,   // Address width in bits.
  parameter int CFG_REG_WIDTH    = 32,   // number of register bits.
  parameter int NUM_OF_CFG_REGS  = 3,    // depends on many variables?
  parameter int MEM_DATA_WIDTH   = 8,    // number of bits used to transfer data between a MMIO accelerator and the system memory. This parameter defines the size of the data bus used for memory transactions.
  parameter int BUFF_SIZE	   	= 32,   // Buffer size in bytes. helps our memory access.
  parameter int LATENCY   	   	= 500,  // later, this should be per instruction - opcode. Latency is the time required for the accelerator to complete a single operation.
  parameter int MEMORY_BANDWIDTH = 300   // measured in bps (bytes per second), this should be a unique accelerator trait. affects performance.
//parameter int pipeline
)

(
//define the inputs/outputs
	input 	  clock,
	input 	  reset,

// RoCC interface
// CPU -> Accelerator. recieve a command from the CPU.
	input 	  io_cmd_valid,
	output 	  io_cmd_ready,
	input  [6:0]  io_cmd_bits_inst_funct,
	input  [4:0]  io_cmd_bits_inst_rs2,
	input  [4:0]  io_cmd_bits_inst_rs1,  // This is the register number. there are 32 registers.
	input  [4:0]  io_cmd_bits_inst_rd,
	input  [6:0]  io_cmd_bits_inst_opcode,
	input  [63:0] io_cmd_bits_rs1,  
	input  [63:0] io_cmd_bits_rs2,

// RoCC interface. 
// Accelerator -> CPU (Rocket/BOOM), give the CPU the calculated data
	input         io_resp_ready,
	output        io_resp_valid,
	output [4:0]  io_resp_bits_rd,
	output [63:0] io_resp_bits_data

// MMIO interface. 
// Accelerator --> Memory
	//input         io_mem_req_ready,
	//output        io_mem_req_valid,
	//output [39:0] io_mem_req_bits_addr,
	//output [7:0]  io_mem_req_bits_tag,
	//output [4:0]  io_mem_req_bits_cmd,
	//output [1:0]  io_mem_req_bits_size,
	//output        io_mem_req_bits_signed,
	//output        io_mem_req_bits_no_alloc,
	//output        io_mem_req_bits_no_xcpt,
	//output [63:0] io_mem_req_bits_data,
	//output [7:0]  io_mem_req_bits_mask,

// MMIO interface. 
// Memory --> Accelerator
	//input         io_mem_resp_valid,
	//input  [39:0] io_mem_resp_bits_addr,
	//input  [7:0]  io_mem_resp_bits_tag,
	//input  [4:0]  io_mem_resp_bits_cmd,
	//input  [1:0]  io_mem_resp_bits_size,
	//input         io_mem_resp_bits_signed,
	//input  [1:0]  io_mem_resp_bits_dprv,
	//input         io_mem_resp_bits_dv,
	//input  [63:0] io_mem_resp_bits_data
);


// define the registers array.

logic [CFG_REG_WIDTH - 1 : 0] reg_array [NUM_OF_CFG_REGS - 1 : 0];   

int counter;
logic flag;

genvar i;

generate
	for (i = 0; i < NUM_OF_CFG_REGS; i++) begin : init_loop
      initial reg_array[i] = 0;
	end
endgenerate
  

always_ff@(posedge clock, negedge reset) begin

	if(reset) begin
		counter <= 0;
		flag <= 0;
	end
	else 
	begin
	
		if(io_cmd_valid) //recive command.
		begin
			counter <= 0;
			flag <= 1;
		
		end
		
		if(flag)
		begin
			
			counter <= counter + 1;
			if(counter == LATENCY) // finish command count
			begin
				
				flag <= 0;
				counter <= 0;
				io_resp_valid <= 1;
			end
			
		end // falg
		
		if(io_resp_valid & io_resp_ready) // give back the data to the cpu.
		begin
		
			io_resp_bits_rd   <= 5;
			io_resp_bits_data <= 8'h06; 
		end
		
		
// 		if(io_mem_req_ready) //recive command.
// 		begin
// 			counter <= 0;
// 			flag <= 1;
		
// 		end
		
// 		if(flag)
// 		begin
			
// 			counter <= counter + 1;
// 			if(counter == LATENCY) // finish command count
// 			begin
				
// 				flag <= 0;
// 				counter <= 0;
// 				io_mem_req_valid <= 1;
// 			end
			
// 		end // falg
		
// 		if(io_mem_req_valid & io_resp_ready) // give back the data to the cpu.
// 		begin
		
// 			io_resp_bits_rd   <= 5;
// 			io_resp_bits_data <= 111111; 
// 		end
		
		
		
	end // reset

end


endmodule
