# Planet-Picture
A procedurally generated 2D image of a Planet and Stars with an export to file function.

## Used Ideas
- Pseudorandom number generation (XOR-shift)
- Bilinear-Interpolation
- Smoothstep-Function
- 2D Noise
- Layering of Noise
- Basic Java UI
- Int as Color with Bit shifting operations

## Build
```
cd src/
javac --release 8 ./*.java
jar cfe ../build/current.jar GUI ./*.class
```
- the second lines argument is necessary to be backwards compatible (command only works on java >=9)

**NOTE the performance of the current version is bad and might take 10+ seconds to generate**