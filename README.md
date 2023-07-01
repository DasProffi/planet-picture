# Planet-Picture
A procedurally generated 2D image of a Planet and Stars with an export to file function.

The current and original version can be found in [build](build/)

**NOTE the start up an first images might take some time (~10 sec) afterwards it improves**

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
![planet_picture_98026312](https://github.com/DasProffi/planet-picture/assets/67233923/0be8b24d-74ab-49a1-addb-12e9399b562a)
![planet_picture_16993314](https://github.com/DasProffi/planet-picture/assets/67233923/d9cf1100-f2ad-4887-8b81-572218e354cd)
![planet_picture_63192010](https://github.com/DasProffi/planet-picture/assets/67233923/7fe6aa58-a01f-454a-a834-d5ecf5045c96)
![planet_picture_82900801](https://github.com/DasProffi/planet-picture/assets/67233923/1afcfe98-f5d7-4701-9291-2f10da743547)
![planet_picture_10700042](https://github.com/DasProffi/planet-picture/assets/67233923/7667bbe8-d81a-4f8c-ae39-c4fa609fbf2a)
![planet_picture_96208433](https://github.com/DasProffi/planet-picture/assets/67233923/62a91768-6373-4be4-a6a4-67b07a446c14)
![planet_picture_326876](https://github.com/DasProffi/planet-picture/assets/67233923/29ac1e32-67d1-47b0-9ce5-b7069459d4e0)
