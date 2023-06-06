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
![planet_picture_71550187](https://github.com/DasProffi/planet-picture/assets/67233923/932d34bf-437f-4716-bef1-4664769f5ea3)
![planet_picture_89017285](https://github.com/DasProffi/planet-picture/assets/67233923/613dcbd3-6aa7-4dda-8c7a-4aa5a9e65d6f)
![planet_picture_5552198](https://github.com/DasProffi/planet-picture/assets/67233923/dd9610e3-5d03-416c-9dac-9fabf12f3bdc)
![planet_picture_38709903](https://github.com/DasProffi/planet-picture/assets/67233923/e3749bf2-9697-41ce-9a48-dca382f9f539)
