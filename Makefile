# Variables
SBT_PROJECT = templateAcceleratorProj
VERILOG_SRC = src/main/vsrc/accTemplate.v
SCALA_SRC = src/main/scala/templateAcc.scala
CONFIG_SRC = config.scala

# Default target
all: build run

# Build target
build:
#	sbt "project $(SBT_PROJECT)" "runMain $(SBT_PROJECT).BuildVerilog --verilogSrc $(VERILOG_SRC)"
sbt "project (templateAcceleratorProj)" "runMain (templateAcceleratorProj).BuildVerilog --verilogSrc (src/main/vsrc/accTemplate.v)"
# Run target
run:
	sbt "project $(SBT_PROJECT)" "runMain $(SBT_PROJECT).RunEnvironment --scalaSrc $(SCALA_SRC) --configSrc $(CONFIG_SRC)"

# Clean target
clean:
	sbt "project $(SBT_PROJECT)" clean

# Additional targets and rules can be added as needed
