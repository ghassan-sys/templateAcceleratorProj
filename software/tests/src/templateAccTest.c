#include <stdio.h>
#include <stdint.h>
//#include "include/rocc.h"
#include "include/encoding.h"
//#include "compiler.h"
#include "rocc-software/src/xcustom.h"
#include <time.h>


#ifdef __linux
#include <sys/mman.h>
#endif

#ifndef __aligned
#define __aligned(x) __attribute__ ((aligned (x)))
#endif


#define DATA_WIDTH 8

int main() {

	unsigned long start_time, end_time, mid_time;
	

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
    int rd, rs1, rs2;
    
 
    // The "fence" instruction is often used as a memory barrier or memory fence to enforce ordering constraints on memory operations
    printf("asm volatile: fence\n");
    asm volatile ("fence" ::: "memory");
    // Invoke the acclerator and check responses

    // setup accelerator with addresses of input and output
    //              opcode rd rs1          rs2          funct
    // asm volatile ("custom2 x0, %[msg_addr], %[hash_addr], 0" : : [msg_addr]"r"(&input), [hash_addr]"r"(&output)); 
    //printf("Start writing to Accelerator \n");
   // ROCC_INSTRUCTION_SS(2, &input, &output, 0);

    printf("executing the CUSTOM command\n");
    start_time = rdcycle();
    ROCC_INSTRUCTION(2, rd, rs1, rs2, 0);
    mid_time = rdcycle();
    printf("CUSTOM command SUCECESS!!\n");
    printf("got data in rd = %d\n", rd);

    printf("time took to write from CPU to accelerator is %d cycles \n", mid_time - start_time);

    // Set length and compute hash
    //              opcode rd rs1      rs2 funct
    // asm volatile ("custom2 x0, %[length], x0, 1" : : [length]"r"(ilen));
    //printf("Start reading from Accelerator\n");
    //ROCC_INSTRUCTION_S(2, sizeof(input), 1);
    printf("ASM VOLATILE : FENCE ::: MEMORY\n");
    asm volatile ("fence" ::: "memory");

    //printf("Collect the total time - endcycle\n");
    end_time = rdcycle();
    //end_time = time(NULL);

    // Check result
    static const unsigned char result[] = {'0', '6', '\0'};
    int res = 6;
	    
    //printf("output:\"%s\" ==? result:=\"%s\" \n",output,result);
    printf("expected = %d vs output = %d\n", res, rd);
    if(rd != res) {
      printf("Failed: Outputs don't match!\n");
      printf("RoCC Accelerator execution took %lu cycles\n", mid_time - start_time);
      return 1;
    }
  } while(0);

  //printf("Success!\n");

  printf("RoCC Accelerator SUCCESS! execution took %lu cycles\n", mid_time - start_time);

  return 0;
}
