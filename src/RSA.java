import java.math.BigInteger;
import java.util.Random;
import java.io.*;
import java.security.SecureRandom;
//import java.util.Scanner;


/* Copyright 2014, Karan Chadha, All rights reserved.
 * 
 */




public class RSA extends DESAction {

	//MAIN function
	public static void main(String[] args) {

		int KEYGEN = 0, ENCRYPT = 0, DECRYPT = 0;

		String filename = "";
		String input = "";
		String output = "";

		int bitlength = 1024;

		if (args.length == 0) {
			System.out.println("no command line options were provided");
			System.out.println("Retry with one of the following options");
			System.out.println("-h lists all available command line options");
			System.out
					.println("-k <key file> -b <bit size>: generates public and private keys called <key file>.public and <key file>.private respectively, encoded in hex. Default bitsize is 1024 bits");
			System.out
					.println("-e <key file>.public -i <input file> -o <output file>: This should encrypt the ﬁle <input file> using <key file>.public and store the encrypted ﬁle in <output file>");
			System.out
					.println("-d <key file>.private -i <input file> -o <output file>: This should decrypt the ﬁle <input file> using <key file>.private and store the plain text ﬁle in <output file>");
			System.exit(0);
		}

		if (args.length > 0) {

			for (int i = 0; i < args.length; i++) {

				if (args[i].equals("-h")) {
					System.out
							.println("Following are all the available command line options");
					System.out
							.println("-h lists all available command line options");
					System.out
							.println("-k <key file> -b <bit size>: generates public and private keys called <key file>.public and <key file>.private respectively, encoded in hex. Default bitsize is 1024 bits");
					System.out
							.println("-e <key file>.public -i <input file> -o <output file>: This should encrypt the ﬁle <input file> using <key file>.public and store the encrypted ﬁle in <output file>");
					System.out
							.println("-d <key file>.private -i <input file> -o <output file>: This should decrypt the ﬁle <input file> using <key file>.private and store the plain text ﬁle in <output file>");
				}
			}

			int i = 0;
			for (i = 0; i < args.length; i++) {

				if (args[i].equals("-k")) { // Key generation
					if ((i == (args.length - 1))) {
						System.err
								.println("ERROR(keygen): key file not specified");
						System.exit(1);
					}
					filename = args[i + 1];
					KEYGEN = 1;

				}
			}
			for (int j = 0; j < args.length; j++) {
				if (args[j].equals("-b") && KEYGEN == 1) {
					
					bitlength = Integer.parseInt(args[j + 1]);
					if ((bitlength != 512) && (bitlength != 1024)
							&& (bitlength != 2048)) {
						System.err
								.println("ERROR(keygen): Invalid bitlength. Please retry with 512, 1024 or 2048 ");
						System.exit(1);
					}
					KEYGEN = 1;
				}
			}
		}

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-e")) { // Encryption
				for (int j = 0; j < args.length; j++) {
					if (args[j].equals("-i")) { // input filename
						input = args[j + 1];
					}
					if (args[j].equals("-o")) { // output filename
						output = args[j + 1];
					}
					if (args[j].equals("-e")) { // key filename
						filename = args[j + 1];
					}

				}
				ENCRYPT = 1;
			}

		}

		for (int i = 0; i < args.length; i++) {

			if (args[i].equals("-d")) { // Encryption
				for (int j = 0; j < args.length; j++) {
					if (args[j].equals("-i")) { // input filename
						input = args[j + 1];
					}
					if (args[j].equals("-o")) { // output filename
						output = args[j + 1];
					}
					if (args[j].equals("-d")) { // key filename
						filename = args[j + 1];
					}

				}
				DECRYPT = 1;
			}

		}

		if (KEYGEN == 1) {

			System.out.println("Generating Keys\nFilename:" + filename
					+ " ,BitSize:" + bitlength);
			keygen(filename, bitlength);

		}

		if (ENCRYPT == 1) {

			System.out.println("Encrypting file :" + input
					+ "\nEncrypted text file :" + output
					+ "\nKey file:" + filename);
			encrypt(input, output, filename);

		}

		if (DECRYPT == 1) {

			System.out.println("Decrypting file :" + input
					+ "\nPlain text file :" + output
					+ "\nKey file:" + filename);
			decrypt(input, output, filename);

		}

	}

	//RSA Decryption function
	protected static void decrypt(String input, String output, String key_file) {
		long startTm = System.currentTimeMillis();

		String keyfile_contents = ReadFile(key_file);
		String delimitor = ",";
		String[] tokens = keyfile_contents.split(delimitor);

		//System.out.println(tokens[0]);
		//System.out.println(tokens[1]);

		String edec = hextodec(tokens[0]);
		String ndec = hextodec(tokens[1]);

		BigInteger d = new BigInteger(edec); // public key
		BigInteger n = new BigInteger(ndec); // modulus (n)

		int blocklength = n.bitLength(); // length of block in bits
		int Plain_BlockSize = Math.min((blocklength - 1) / 8, 256); // In bytes
		int Cipher_BlockSize = 1 + (blocklength - 1) / 8; // In bytes
		
		// Input file contains Cipher Text
		try {
			int dataSize = 0;
			FileInputStream CipherTextFile = new FileInputStream(input);
			byte[] PlainTextBlock = new byte[Plain_BlockSize];
			
			FileOutputStream PlainTextFile = new FileOutputStream(output);
			byte[] CipherTextBlock = new byte[Cipher_BlockSize];
			
			//Reading from Encrypted input file
			int CipherDataSize = CipherTextFile.read(CipherTextBlock);
			while (CipherDataSize > 0) {		
				// Converting Cipher Text Block to positive big integer
				BigInteger CipherText = new BigInteger(1, CipherTextBlock);
				
				//Decrypting using formula
				BigInteger PlainText = CipherText.modPow(d, n);
				byte[] clearTextData = PlainText.toByteArray();
				GenerateBytesBlock(PlainTextBlock, clearTextData);

				dataSize = Plain_BlockSize;
				if (CipherTextFile.available() == 0) {
					dataSize = getDataSize(PlainTextBlock);
				}
				if (dataSize > 0) {
					PlainTextFile.write(PlainTextBlock, 0, dataSize);
				}
				CipherDataSize = CipherTextFile.read(CipherTextBlock); // Reading again
			}
			PlainTextFile.close();
			CipherTextFile.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		System.out.println("\nDecrypted Successfully !!");
		System.out.println("*******************");
		System.err.println("\rDecryption took "
				+ (System.currentTimeMillis() - startTm) + " ms");
	}

	// Getting padded data size from a padded block
	public static int getDataSize(byte[] block) {
		int bSize = block.length;
		int padValue = block[bSize - 1];	//padValue holds the number of pads added
		return (bSize - padValue) % bSize;
	}

	// RSA Encryption Function
	protected static void encrypt(String input, String output, String key_file) {

		long startTm = System.currentTimeMillis();

		String keyfile_contents = ReadFile(key_file);
		String delimitor = ",";
		String[] tokens = keyfile_contents.split(delimitor);

		// System.out.println(tokens[0]);
		// System.out.println(tokens[1]);

		String edec = hextodec(tokens[0]);
		String ndec = hextodec(tokens[1]);

		// making big integer of public key
		BigInteger e = new BigInteger(edec); 
		
		// making big integer of modulus (n)
		BigInteger n = new BigInteger(ndec); 

		// Calculating length of block in bits, depends on keysize // default 1024
		int blocklength = n.bitLength(); 
		
		//Calculating the blocksize to be read from the Plain Text file
		int Plain_BlockSize = Math.min((blocklength - 1) / 8, 256); // In bytes
		
		//Calculating the cipher blocksize to be written
		int Cipher_BlockSize = 1 + (blocklength - 1) / 8; // In bytes
	
		try {
			int Padded = 0;
			FileInputStream PlainTextFile = new FileInputStream(input);
			byte[] PlainTextBlock = new byte[Plain_BlockSize];

			FileOutputStream CipherTextFile = new FileOutputStream(output);
			byte[] CipherTextBlock = new byte[Cipher_BlockSize];

			// Reading plain text message
			int PlainDataSize = PlainTextFile.read(PlainTextBlock);
			while (PlainDataSize > 0) {
				// if number of bytes read (plain text) < blocksize we need to pad the read blocks for encryption
				if (PlainDataSize < Plain_BlockSize) { 
					padding(PlainTextBlock, PlainDataSize);
					Padded = 1;
				}

				BigInteger PlainText = new BigInteger(1, PlainTextBlock);
				
				// Encrypting each block, PlainText, using EncryptedText = PlainText.modPow(e,n).
				BigInteger CipherText = PlainText.modPow(e, n);
				byte[] cipherTextData = CipherText.toByteArray();
				
				//Generating cipher block of fixed size to be written
				GenerateBytesBlock(CipherTextBlock, cipherTextData);
				CipherTextFile.write(CipherTextBlock);
				
				//continue reading
				PlainDataSize = PlainTextFile.read(PlainTextBlock);
			}

			
			if (Padded == 0) {
				padding(PlainTextBlock, 0);
				BigInteger PlainText = new BigInteger(1, PlainTextBlock);
		
				// Encrypting each block, PlainText, using EncryptedText = PlainText.modPow(e,n).
				BigInteger EncryptedText = PlainText.modPow(e, n);
				byte[] cipherTextData = EncryptedText.toByteArray();
				GenerateBytesBlock(CipherTextBlock, cipherTextData);
				CipherTextFile.write(CipherTextBlock);
			}
			
			PlainTextFile.close();
			CipherTextFile.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		System.out.println("Encrypted Successfully !!");
		System.out.println("*******************");
		System.err.println("\rEncryption took "
				+ (System.currentTimeMillis() - startTm) + " ms");
	}

	// packaging the encrypted integer represented in a byte array into a fixed length byte block.
	public static void GenerateBytesBlock(byte[] block, byte[] data) {
		int bSize = block.length;
		int dSize = data.length;
		int i = 0;
		while (i < dSize && i < bSize) { // move till the block is full
			block[bSize - i - 1] = data[dSize - i - 1];
			i++;
		}
		while (i < bSize) { // if left, padd with 0x00
			block[bSize - i - 1] = (byte) 0x00;
			i++;
		}
	}

	// Padding input message block
	public static void padding(byte[] block, int dataSize) {
		int block_size = block.length;
		int padSize = block_size - dataSize; // No of pads we need
		int padValue = padSize % block_size; // we use the
												// "number of bytes padded" as
												// the value to be padded
		for (int i = 0; i < padSize; i++) {
			block[block_size - i - 1] = (byte) padValue;
		}
	}

	// Function to read both encrypted and plain text files
	protected static String ReadFile(String key_file) {
		StringBuffer string_buff = new StringBuffer(1024);

		try {
			BufferedReader reader = new BufferedReader(new FileReader(key_file));
			char[] buff = new char[1024];

			int numRead = 0;
			while ((numRead = reader.read(buff)) != -1) {
				String readData = String.valueOf(buff, 0, numRead);
				string_buff.append(readData);
				buff = new char[1024];
			}
			reader.close();
			return string_buff.toString();

		} catch (Exception e) {// Catch exception if any
			System.err.println("File I/O Error: " + e.getMessage());
			return "";
		}
	}

	// RSA key generation function
	protected static void keygen(String filename, int bitlength) {
		
		long startTm = System.currentTimeMillis();
		//Random rnd = new Random();
		SecureRandom rnd = new SecureRandom();
		
		// Finding two prime numbers
		//Constructs a randomly generated prime BigInteger, uniformly distributed over the range 0 to (2^(bitlength/2) - 1), inclusive.
		BigInteger p = BigInteger.probablePrime(bitlength / 2, rnd);
		BigInteger q = p.nextProbablePrime();

		// Finding totient
		BigInteger n = p.multiply(q);
		BigInteger one = new BigInteger("1");
		BigInteger p1 = p.subtract(one);
		BigInteger q1 = q.subtract(one);
		BigInteger m = p1.multiply(q1);
		
		// Finding public key - e
		BigInteger e = FindCoprime(m);	
		
		// Finding a private key - d
		BigInteger d = e.modInverse(m);

		//Converting to hex to store
		String ehex = dectohex(e.toString());
		String dhex = dectohex(d.toString());
		String nhex = dectohex(n.toString());
		
		// Concatenating the keys with "n"
		String public_key = ehex + "," + nhex;
		String private_key = dhex + "," + nhex;

		create_key_files(public_key, private_key, filename);
		System.out.println("\nRSA Keys generated successfully in " + filename
				+ ".public and " + filename + ".private");
		
		System.err.println("\rRSA Keys generation took \n"
				+ (System.currentTimeMillis() - startTm) + " ms");
	}

	// Function to find co prime (big integer)
	public static BigInteger FindCoprime(BigInteger prime1) {
		Random rand = new Random();
		int prime_length = prime1.bitLength() - 1;
		BigInteger prime2 = BigInteger.probablePrime(prime_length, rand);
		while (!(prime1.gcd(prime2)).equals(BigInteger.ONE)) {
			prime2 = BigInteger.probablePrime(prime_length, rand);
		}
		return prime2;
	}

	// Dec to hexal conversion
	public static 
				String dectohex(String dec) {
		BigInteger toHex = new 
				BigInteger(dec, 10);
		String hex = 
				toHex.toString(16);
		return hex;
	}
	
	// Hexal to Decimal function
	public static 
				String hextodec(String hex) {

		BigInteger toDec = 
				new BigInteger(hex, 16);
		String dec = 
				toDec.toString(10);
		return dec;
	}

	// Function to create .public and .private key files
	protected static void create_key_files(String public_key, String private_key,
			String filename) {
		String workingDirectory = System.getProperty("user.dir");
		//String absoluteFilePath = "";
		filename = workingDirectory + File.separator + filename;
		System.out.println("filename: " + filename);
		try {
			FileWriter fstream = new FileWriter(filename + ".public");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(public_key);
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("File write Error: " + e.getMessage());
		}
		try {
			FileWriter fstream = new FileWriter(filename + ".private");
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(private_key);
			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("File write Error: " + e.getMessage());
		}
	}

}
