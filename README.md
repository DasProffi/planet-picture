# Planet-Picture
A procedurally generated 2D image of a Planet and Stars with an export to file function.

The current and original version can be found in [build](build/)

**NOTE the performance of the current version is bad and might take 10+ seconds to generate**!

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

## Impressions
![planet_picture_24809381](https://github.com/DasProffi/planet-picture/assets/67233923/844f6db3-0e08-4f94-b5fd-c913c5bcd8fa)
![planet_picture_7735549](https://github.com/DasProffi/planet-picture/assets/67233923/7803b802-0834-46ab-b54b-27db1ecf51b7)
![planet_picture_23687714](https://github.com/DasProffi/planet-picture/assets/67233923/c86293a2-7257-42b2-8962-96f28a34db0d)
![planet_picture_26434890](https://github.com/DasProffi/planet-picture/assets/67233923/7a08deaf-ac51-4d23-bcec-036f987c9233)
![planet_picture_38269089](https://github.com/DasProffi/planet-picture/assets/67233923/336599d7-8217-4e2b-bb7a-6729778e9963)
![planet_picture_40871442](https://github.com/DasProffi/planet-picture/assets/67233923/c820f2ee-bd32-4c06-b040-fcbe3444cf3e)

