//see LICENSE for license
// The following is a RISC-V program to test the functionality of the
// templateAccTest RoCC accelerator.
// Compile with riscv-gcc sha3-rocc.c
// Run with spike --extension=sha3 pk a.out

#include <stdio.h>
#include <stdint.h>
#include "templateAccTest_h.h"

#ifdef __linux
#include <sys/mman.h>
#endif

#define DATA_WIDTH = 8

int main() 
{

	unsigned long start, end;

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
    static unsigned char input[DATA_WIDTH]  = '\0' ;
    unsigned char output[DATA_WIDTH];

    start = rdcycle();

    // Compute hash with accelerator
    asm volatile ("fence");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    /* asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); */
    ROCC_INSTRUCTION_SS(2, &input, &output, 0);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    /* asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen)); */
    ROCC_INSTRUCTION_S(2, sizeof(input), 1);
    asm volatile ("fence" ::: "memory");

    end = rdcycle();

    // Check result
    int i;
    static const unsigned char result[DATA_WIDTH] = '06';
	    
    printf("output:%d ==? result:%d \n",output,result);
    if(output != result) {
      printf("Failed: Outputs don't match!\n");
      printf("SHA execution took %lu cycles\n", end - start);
      return 1;
    }
    
  } while(0);

  printf("Success!\n");

  printf("SHA execution took %lu cycles\n", end - start);

  return 0;
}