#include <stdio.h>
#include <stdint.h>
#include "rocc.h"
#include "encoding.h"
#include <time.h>

#ifdef __linux
#include <sys/mman.h>
#endif

#define DATA_WIDTH 8

int main() {

	unsigned long start, end;
	time_t start_time, end_time;
	
	#ifdef __linux
  // Ensure all pages are resident to avoid accelerator page faults
  if (mlockall(MCL_CURRENT | MCL_FUTURE)) {
    perror("mlockall");
    return 1;
  }
#endif

  do {
    printf("Start template accelerator  <-->  Rocket test.\n");

    // Setup some test data
    static unsigned char input[DATA_WIDTH]  = "\0" ;
    unsigned char output[DATA_WIDTH];

    printf("Start counting cycles to write\n");
    //start = rdcycle();
    start_time = time(NULL);

    // The "fence" instruction is often used as a memory barrier or memory fence to enforce ordering constraints on memory operations
    printf("asm volatile: fence\n");
    asm volatile ("fence" ::: "memory");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    /* asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); */
    printf("Start writing to Accelerator\n");
    ROCC_INSTRUCTION_SS(2, &input, &output, 0);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    /* asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen)); */
    printf("Start reading from Accelerator\n");
    ROCC_INSTRUCTION_S(2, sizeof(input), 1);
    printf("ASM VOLATILE : FENCE ::: MEMORY");
    asm volatile ("fence" ::: "memory");

    printf("Collect the total time - endcycle");
    //end = rdcycle();
    end_time = time(NULL);

    // Check result
    int i;
    static const unsigned char result[DATA_WIDTH] = "06";
	    
    printf("output:%d ==? result:%d \n",output,result);
    if(output != result) {
      printf("Failed: Outputs don't match!\n");
      printf("RoCC Accelerator execution took %lu cycles\n", end_time - start_time);
      return 1;
    }
    
  } while(0);

  printf("Success!\n");

  printf("RoCC Accelerator execution took %lu cycles\n", end_time - start_time);

  return 0;
}
