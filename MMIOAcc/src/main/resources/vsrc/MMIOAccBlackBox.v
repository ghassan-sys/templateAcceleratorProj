/*
* Accelerator module (generic template).
* Fits both types - MMIO and RoCC accelerators.
* Counts latency.
*/


module MMIOAccBlackBox#
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

NUM_OF_CFG_REGS = RoCC Input + Rocc output + NUM_OF_CFG_REGS (Rocc)

(
//define the inputs/outputs
	input 	  	       clock,
	input 	  	       reset,

	output    	       input_ready,
    	input     	       input_valid,
    	input                  output_ready,
    	output                 output_valid,
    	output                 busy

    	output reg [63:0] data_out,
	input [NUM_OF_CFG_REGS - 2: 0][CFG_REG_WIDTH - 1: 0] _cfg_regs_,
	input [NUM_INPUT - 1:0]
	input [NUM_outPUT - 1:0
	INPUT FUNCT
	//need to map all in reg map
);

reg [63:0] io_resp_data_reg;
reg io_resp_valid_reg;
reg io_busy_reg;
reg io_cmd_ready_reg;


// define the registers array.
logic [CFG_REG_WIDTH - 1 : 0] reg_array [NUM_OF_CFG_REGS - 1 : 0];   

int counter;
int target_latency;
logic flag;
logic flag2;

genvar i;

generate
	for (i = 0; i < NUM_OF_CFG_REGS; i++) begin : init_loop
      initial reg_array[i] = 0;
	end
endgenerate
  

always_ff@(posedge clock, negedge reset) begin

	if(reset) begin
		counter 	     	      <= 0;
		flag                 	      <= 0;
		flag2			      <= 0;
		io_resp_data_reg     	      <= 0;
		io_resp_valid_reg    	      <= 0;
		io_busy_reg          	      <= 0;
		io_cmd_ready_reg     	      <= 0;
		target_latency		      <= 0;
	end
	else 
	begin
		
		io_cmd_ready_reg     <= 0;

		if(io_cmd_bits_inst_funct == 2 && input_valid == 1 && flag == 0) begin //COMPUTE
			target_latency <= LATENCY;
			$display("in compute Time = %0t", $time);

		end
		else if(io_cmd_bits_inst_funct == 1 && io_cmd_valid == 1 && flag == 0) begin //CONFIG
			target_latency <= 4;
	          			
			$display("in config Time = %0t", $time);
	
		end

		if(input_valid && target_latency != 0) //recive command.
		begin
			counter <= 0;
			flag    <= 1;
			io_cmd_ready_reg <= 1;
			io_busy_reg <= 1;
		
		end
		
		if(flag)
		begin
			
			counter <= counter + 1;
			if(counter == target_latency) // finish command count
			begin
				
				flag 		  <= 0;
				counter 	  <= 0;
				io_resp_valid_reg <= 1;
				target_latency    <= 0;
			end
			
		end // falg
		
		if(io_resp_valid_reg & output_ready) // give back the data to the cpu.
		begin
		
			io_resp_data_reg  <= 8'd06;
		        io_resp_valid_reg <= 1;	
			flag2             <= 1;
		end

		if(flag2)
		begin
			io_resp_valid_reg <= 0;
			flag2             <= 0;
			io_busy_reg       <= 0;
			io_cmd_ready_reg  <= 1;
		end

		if (io_cmd_bits_inst_funct == 1) begin
			reg_array[io_cmd_bits_inst_rs1] <= io_cmd_bits_inst_rs1;	
		end
		
	end // reset

end


assign data_out		 = io_resp_data_reg;
assign output_valid 	 = io_resp_valid_reg;
assign busy 		 = io_busy_reg;
assign input_ready	 = io_cmd_ready_reg;

endmodule


endmodule
