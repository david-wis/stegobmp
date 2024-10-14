# StegoBMP
A steganography tool for BMP images.
Provides a way to hide and recover info in BMP images, using the LSB1, LSB4, and LSBI methods.
It also provides a way to encrypt the hidden data using a password, with the AES and 3DES algorithms.

The project was developed using Java 21 and Maven.

## Table of Contents
- [StegoBMP](#stegobmp)
  - [Table of Contents](#table-of-contents)
  - [Installation](#installation)
  - [Usage](#usage)
  - [Examples](#examples)
    - [Embedding a file](#embedding-a-file)
    - [Extracting a file](#extracting-a-file)
  - [About](#about)

## Installation
Compile the project using:
```bash
mvn clean install
```

## Usage
To embed a file into a BMP image:
```bash
java -jar stegobmp-1.0.jar -embed -in <file> -p <carrier> -out <output> -steg <LSB1|LSB4|LSBI> -a <aes128|aes192|aes256|des> -m <ecb|cbc|ofb|ofc> -pass <password> 
```

To extract a file from a BMP image:
```bash
java -jar stegobmp-1.0.jar -extract -p <carrier> -out <output> -steg <LSB1|LSB4|LSBI> -a <aes128|aes192|aes256|des> -m <ecb|cbc|ofb|ofc> -pass <password> 
```

Note: The password is optional, without it the data will not be encrypted.

## Examples
### Embedding a file
```bash
java -jar stegobmp-1.0.jar -embed -in file.txt -p image.bmp -out image_stego.bmp -steg LSB1 -a aes128 -m ecb -pass password
```

### Extracting a file
```bash
java -jar stegobmp-1.0.jar -extract -p image_stego.bmp -out file.txt -steg LSB1 -a aes128 -m ecb -pass password
```

### About
This project was developed for the Cryptography and Security course at ITBA.
It was developed by:
- Liu, Jonathan Daniel
- Vilamowski, Abril
- Wischñevsky, David

