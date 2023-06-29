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
![planet_picture_42139296](https://github.com/DasProffi/planet-picture/assets/67233923/4bfebb66-7696-4f05-8721-ca517ccae48d)
![planet_picture_75911665](https://github.com/DasProffi/planet-picture/assets/67233923/4480fb8f-3bb5-46c6-856f-67a5c4ff8ded)
![planet_picture_69203074](https://github.com/DasProffi/planet-picture/assets/67233923/f2c61686-a834-4541-b304-6544f342e095)
![planet_picture_70798376](https://github.com/DasProffi/planet-picture/assets/67233923/61f302d1-f0d8-419e-b948-b73d59831fa0)
![planet_picture_60526904](https://github.com/DasProffi/planet-picture/assets/67233923/721d8da3-a2b7-468e-aa7a-8b87c4079109)
