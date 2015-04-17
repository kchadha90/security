import java.io.*;
import java.util.*;
import java.security.SecureRandom;
import java.util.BitSet;

public class DES
{
	public static void main (String args[])
	{
		if (args.length == 0)	// Exits after Giving an Error message in case of No Arguments.
		{
			System.out.println("\nMinimum 1 argument required. Run\n\tjava DES -h\nto see the list of all command line options supported.\n");
			System.exit(0);
		}
		DESAction act = new DESAction();
				
		String input;			// to store the input file name.
		String output;			// to store the output file name.
		long key;				// to store the 64-bit DES key.
		
		switch (args[0])
		{
			case "-h":
			{
				if(args.length!=1)	// Exits the program if there are more than 1 argument.
				{
					System.out.println("Invalid Command Line Argument. Run\n\tjava DES -h\nto see the list of all command line options supported.\n");
					System.exit(0);
				}
				act.cmdLineOptions();	// Displays ALL command line options.
				break;
			}
			case "-k":
			{
				if(args.length!=1)	// Exits the program if there are more than 1 argument.
				{
					System.out.println("Invalid Command Line Argument. Run\n\tjava DES -h\nto see the list of all command line options supported.\n");
					System.exit(0);
				}
				key = act.generateKey();	// fetches a 64-bit key generated by the method generateKey()
				String keyhex = Long.toHexString(key).toUpperCase(); 	// Converts the long into String formatted as HEXADECIMAL.
				System.out.println(keyhex);								// Displays the 64-bit key in HEX.
				break;
			}

			case "-e":
			{
				act.chkSyntax(args);		// checks if correct arguments are passed.
				input = args[3];			// takes input file name from cmd line argument.
				output = args[5];
				
				// Following 3 lines fetch 15 hex-digits and most significant hex-digit separately and
				// combines them by OR operations and SHIFT LEFT operations to generate key of type long.
				key = Long.parseLong(args[1].substring(1), 16);
				long k = Long.parseLong(args[1].substring(0,1), 16);
				key = key | (k << 60);
//				System.out.format("%d", key);
				act.Encrypt(input, key, output);	// calls Encrypt() method.
			break;
			}
			
			case "-d":
			{
				// All code in this section follows the same comments as above section.
				act.chkSyntax(args);
				input = args[3];
				output = args[5];
				key = Long.parseLong(args[1].substring(1), 16);
				long k = Long.parseLong(args[1].substring(0,1), 16);
				key = key | (k << 60);
				act.Decrypt(input, key, output);
				break;
			}
			default:		// If first argument passed is none of the above, then program exits after showing the reason.
			{
				System.out.println("Invalid Command Line Argument. Run\n\tjava DES -h\nto see the list of all command line options supported.\n");
			}
		}
	}
}

class DESAction
{
	static void Encrypt(String input, long key, String output)	// inputs: input file name and output file name as String; key as long.
	{
		DESMethods des = new DESMethods();						// object 'des' created to call various methods needed to carry out DES Algorithm.
        File infile = new File(input);			// file object created in order to READ the file.
        FileInputStream fin = null;
        
        byte BlockfromInput[] = new byte[8];	// Empty byte array that will store one block of input to DES.

		File outfile = new File(output);		// file object created to WRITE the encrypted file.
		FileWriter fout = null;
		
        try
        {
            fin = new FileInputStream(infile);
			fout = new FileWriter(outfile, false);		// 'false' will overwrite any existing file with same name.
            int cursor = 1;								// cursor points to the end of file location that has been encrypted.
            while(cursor <= (int) infile.length())		// while the file ends, the loop keeps repeating.
            {
	            Arrays.fill(BlockfromInput, (byte)0);	// Input Block set to 0, in order to avoid the need of padding.
	            fin.read(BlockfromInput);				// ONLY READS 8 BYTES OF INPUT AT A TIME. (Hence, highly scalable.)
				
	            byte BlockExpanded[] = new byte[64];
				BlockExpanded = des.convertBitToByte(BlockfromInput);	// Expands one bit to one byte. In order to be able to access every bit individually.
				
				byte BlockToEncrypt[] = new byte[64];
				BlockToEncrypt = des.PermutePlainText(BlockExpanded);	// The Expanded Block is Permuted as per the Standard Table.
				
				byte LeftInput[] = new byte[32];
				byte RightInput[] = new byte[32];
				for (int i=0; i<32; i++)								// Permuted Block splits to two halves.
				{
					LeftInput[i] = BlockToEncrypt[i];					// After the two halves, Input is ready to be used.
					RightInput[i] = BlockToEncrypt[i+32];
				}

	            byte keyArray[] = new byte[64];
				for(int i=0; i<64; i++)								// 64-bit key of type 'long' needs to
				{													// be converted in a way where every 
					keyArray[63-i] = (byte) des.getBit(key >> i);	// bit can be accessed. Hence, keyArray.
				}
				byte Permuted56Key[] = new byte[56];
				Permuted56Key = des.PermuteTheKey(keyArray);		// Permuting 64bit key to 56bits.

				byte NewLeftInput[] = new byte[32];					// Temp Arrays to store the encrypted
				byte NewRightInput[] = new byte[32];				// halves after each of the 64 rounds.
				byte Compressed48key[] = new byte[48];
				
				for(int j=1;j<=16;j++)			// j denotes the number of current round.
				{
					NewLeftInput = Arrays.copyOf(RightInput, 32);	// L1 <- R0
					// One Left Rotation of 56bit key half ways.
					Permuted56Key = des.RotateLeftHalfWay(Permuted56Key);
					// Another Left Rotation half way if needed.
					if(j!=1 && j!=2 && j!=9 && j!=16)
					{
						Permuted56Key = des.RotateLeftHalfWay(Permuted56Key);
					}					
					// 56bit rotated key Compressed to 48 bits. (It's Ready for this round)
					Compressed48key = des.Compressto48key(Permuted56Key);

					// Function f is applied on Right Input R0 and Current Version of the Key.
					byte ResultOfFunc[] = new byte[32];
					ResultOfFunc = des.FuncOnRightandKey(RightInput, Compressed48key);

					// New RightArray R1 <- L0 XOR f(R0,K)
					for(int i=0; i<32; i++)
					{
						NewRightInput[i] = (byte) (LeftInput[i] ^ ResultOfFunc[i]);	//WORKING ACCURATE
					}					
					// Store the Inputs for next round.
					LeftInput = Arrays.copyOf(NewLeftInput, 32);
					RightInput = Arrays.copyOf(NewRightInput, 32);
				}
				byte DecryptedBlock[] = new byte[64];
				// Swapping the Left Side and the Right Side.
				for(int i=0; i<32; i++)
				{
					DecryptedBlock[i] = RightInput[i];
					DecryptedBlock[i+32] = LeftInput[i];
				}
				// Applying Final Permutation on decrypted block.
				DecryptedBlock = des.FinalPermutation(DecryptedBlock);
				
				// Writing the Decrypted Block into a String[] Array.
				String DecryptedToWrite[] = new String[8];
				DecryptedToWrite = des.convertByteToBit(DecryptedBlock);
				
				// Writing into the output file, every element of the Decrypted Array.
				for(int i=0; i<8; i++)
				{
					fout.write(DecryptedToWrite[i].toUpperCase());
				}
				cursor +=8;		// Cursor moves by 8-bytes = 64-bits.
				if (cursor <= (int) infile.length())
				{
					fout.write("\n");	// Writes a NEWLINE if not EOF.
				}
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe)
        {
            System.out.println("Exception while reading file " + ioe);
        }
        finally
        {
        	try {
                if (fin != null)
                {
                    fin.close();
                }
				if (fout != null)
				{
					fout.close();
				}
            }
            catch (IOException ioe)
        	{
                System.out.println("Error while closing stream: " + ioe);
            }
        }
	}
	
	// Comments of Encrypt() method applies to following Decrypt Method too.
	// Otherwise, stated.
	static void Decrypt(String input, long key, String output)
	{
		DESMethods des = new DESMethods();
        File infile = new File(input);
    	FileReader inStream = null;
    	BufferedReader fin = null;
    	
        byte BlockfromInput[] = new byte[8];

        FileOutputStream outfile = null;		
		String InputLine;
        try
        {
        	inStream = new FileReader(infile);
        	fin = new BufferedReader(inStream);
        	
        	outfile = new FileOutputStream(output);
            while((InputLine = fin.readLine())!= null)
            {
            	for (int i=0; i<8; i++)
            	{
            		BlockfromInput[i] = (byte) Integer.parseInt(InputLine.substring(2*i, 2*i+2), 16);
            	}
	            
	            byte BlockExpanded[] = new byte[64];
				BlockExpanded = des.convertBitToByte(BlockfromInput);
								
				byte BlockToEncrypt[] = new byte[64];
				BlockToEncrypt = des.PermutePlainText(BlockExpanded);
				
				byte LeftInput[] = new byte[32];
				byte RightInput[] = new byte[32];
				for (int i=0; i<32; i++)
				{
					LeftInput[i] = BlockToEncrypt[i];
					RightInput[i] = BlockToEncrypt[i+32];
				}

	            byte keyArray[] = new byte[64];
				for(int i=0; i<64; i++)
				{
					keyArray[63-i] = (byte) des.getBit(key >> i);
				}
				byte Permuted56Key[] = new byte[56];
				Permuted56Key = des.PermuteTheKey(keyArray);

				byte NewLeftInput[] = new byte[32];
				byte NewRightInput[] = new byte[32];
				byte Compressed48key[] = new byte[48];
				
				for(int j=16;j>=1;j--)		// Rounds are numbered reverse but that does not matter much.
				{
					NewLeftInput = Arrays.copyOf(RightInput, 32);
					
					// In Decryption, Key rotates Right Ways instead of Left. 
					Compressed48key = des.Compressto48key(Permuted56Key);
					
					// To be opposite of Encryption, Compression happens before Rotation in Decryption. 
					Permuted56Key = des.RotateRightHalfWay(Permuted56Key);
					if(j!=1 && j!=2 && j!=9 && j!=16)
					{
						Permuted56Key = des.RotateRightHalfWay(Permuted56Key);
					}
										
					byte ResultOfFunc[] = new byte[32];
					ResultOfFunc = des.FuncOnRightandKey(RightInput, Compressed48key);
					
					for(int i=0; i<32; i++)
					{
						NewRightInput[i] = (byte) (LeftInput[i] ^ ResultOfFunc[i]);
					}					
					LeftInput = Arrays.copyOf(NewLeftInput, 32);
					RightInput = Arrays.copyOf(NewRightInput, 32);
				}
				byte DecryptedBlock[] = new byte[64];
				for(int i=0; i<32; i++)
				{
					DecryptedBlock[i] = RightInput[i];
					DecryptedBlock[i+32] = LeftInput[i];
				}
				DecryptedBlock = des.FinalPermutation(DecryptedBlock);
								
				byte DecryptedToWrite[] = new byte[8];
				DecryptedToWrite = des.convertBytesToByte(DecryptedBlock);
				
				// Final DecryptedBlock is stored as bytes and is written as bytes to
				// result into the same Encrypted File as original.
				outfile.write(DecryptedToWrite);
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("File not found" + e);
        }
        catch (IOException ioe)
        {
            System.out.println("Exception while reading file " + ioe);
        }
        finally
        {
        	try {
                if (fin != null)
                {
                    fin.close();
                }
				if (outfile != null)
				{
					outfile.close();
				}
            }
            catch (IOException ioe)
        	{
                System.out.println("Error while closing stream: " + ioe);
            }
        }
	}
	
	static long generateKey()	// for random key generation.
	{
		SecureRandom sr = new SecureRandom();
		long key = sr.nextLong();
		return key;
	}

	void cmdLineOptions()		// displaying cmd line arguments for option "-h"
	{
		System.out.println("\n\tAvi Dubey and Karan Chadha provide following command line options in their JAVA program, DES :-");
		System.out.println("\t1. DES -k");
		System.out.println("\t   To generate a 64-bit DES key and print it on command line. (The key encoded in hex)");
		System.out.println("\t2. DES -e <64-bit-key-in-hex> -i <input_file> -o <output_file>");
		System.out.println("\t   To encrypt the file <input_file> using <64-bit-key-in-hex> and store the encrypted file in <output_file> with 16 hexadecimal digits in each line.");
		System.out.println("\t3. DES -d <64-bit-key-in-hex> -i <input_file> -o <output_file>");
		System.out.println("\t   To decrypt the file <input_file> using <64-bit-key-in-hex> and store the plain text in <output_file>.");
	}
	
	void chkSyntax(String args[])
	{
		if(args.length != 6)	// in case of "-e" and "-d", Arguments should be Equal to 6.
		{
			System.out.println("INVALID COUNT OF ARGUMENTS. Run\n\tjava DES -h\nto see the list of all command line options supported.\n");
			System.exit(0);
		}
		if(!args[2].equals("-i"))
		{
			System.out.println("Invalid Argument: " + args[2]);
			System.exit(0);
		}
		if(!args[4].equals("-o"))
		{
			System.out.println("Invalid Argument: " + args[4]);
			System.exit(0);
		}
	}
}

class DESMethods
{
	// This method returns the least significant bit of any number.
	// This isn't needed for this program because JAVA leaves rest
	// entry as 0.
	// This will be helpful if a new ARRAY contains Garbage Value.
	int getBit(long b)
    {
    	if (b%2 == 0)
    	{
    		return 0;
    	}
    	else
    	{
    		return 1;
    	}
    }

	// This method converts a 8-byte array to a
	// 64-byte array with one bit per byte.
	byte[] convertBitToByte(byte[] block)
	{
        byte bblock[] = new byte[64];
        int temp;

        for (int i=0, j=7; i<64; i++)
        {
        	temp = block[i/8];
        	bblock[i] = (byte) this.getBit(temp >> j);
        	j--;
        	if(j<0)
        		j = 7;
        }        
        return bblock;
	}

	// This method applies INITIAL PERMUTATION
	// on INPUT BLOCK.
	byte[] PermutePlainText(byte[] block)
	{
        int PlainIPTable[];
        PlainIPTable = new int[]
        	{
        		58, 50, 42, 34, 26, 18, 10, 2, 
        		60, 52, 44, 36, 28, 20, 12, 4, 
        		62, 54, 46, 38, 30, 22, 14, 6, 
        		64, 56, 48, 40, 32, 24, 16, 8, 
        		57, 49, 41, 33, 25, 17, 9, 1, 
        		59, 51, 43, 35, 27, 19, 11, 3, 
        		61, 53, 45, 37, 29, 21, 13, 5, 
        		63, 55, 47, 39, 31, 23, 15, 7
        	};
        byte PermutedBlock[] = new byte[64];
        
        for(int i=0; i<64; i++)
        {
        	PermutedBlock[i] = block[PlainIPTable[i]-1]; 
        }
        return PermutedBlock;
	}
	
	// This method applies PERMUTATION on
	// 64-bit key to create a 56-bit key.
	byte[] PermuteTheKey(byte[] key)
	{
		byte PermutedTo56Key[] = new byte[56];
		int KeyIPTable[];
		KeyIPTable = new int[]
			{
				57, 49, 41, 33, 25, 17, 9,
				1, 58, 50, 42, 34, 26, 18,
				10, 2, 59, 51, 43, 35, 27,
				19, 11, 3, 60, 52, 44, 36,
				63, 55, 47, 39, 31, 23, 15,
				7, 62, 54, 46, 38, 30, 22,
				14,	6, 61, 53, 45, 37, 29, 
				21, 13, 5, 28, 20, 12, 4
			};
      
		for(int i=0; i<56; i++)
		{
			PermutedTo56Key[i] = key[KeyIPTable[i]-1];
		}
		return PermutedTo56Key;
	}
	
	// This method SPLITS the 56-bit key in two
	// equal halves and Rotates Left Once.
	// (For Encryption)
	byte[] RotateLeftHalfWay(byte[] key)
	{
		byte Rotated56key[] = new byte[56];
		byte temp = key[0];
		for(int i=0; i<27; i++)
		{
			Rotated56key[i] = key[i+1];
		}
		Rotated56key[27] = temp;
		
		temp = key[28];
		for(int i=28; i<55; i++)
		{
			Rotated56key[i] = key[i+1];
		}
		Rotated56key[55] = temp;				
		return Rotated56key;
	}
	
	// This method SPLITS the 56-bit key in two
	// equal halves and Rotates Right Once.
	// (For Decryption)
	byte[] RotateRightHalfWay(byte[] key)
	{
		byte Rotated56key[] = new byte[56];
		byte temp = key[27];
		for(int i=1; i<28; i++)
		{
			Rotated56key[i] = key[i-1];
		}
		Rotated56key[0] = temp;
		
		temp = key[55];
		for(int i=29; i<56; i++)
		{
			Rotated56key[i] = key[i-1];
		}
		Rotated56key[28] = temp;		
		return Rotated56key;
	}
	
	// This method compressed 56-bit key
	// to create a 48-bit COMPRESSED KEY.
	// (Ready to be applied on f(R,K)
	byte[] Compressto48key(byte[] key)
	{
		byte Compressed48key[] = new byte[48];
		int PermTable[];
		PermTable = new int[]
			{
				14, 17, 11, 24, 1, 5, 
				3, 28, 15, 6, 21, 10, 
				23, 19, 12, 4, 26, 8, 
				16, 7, 27, 20, 13, 2, 
				41, 52, 31, 37, 47, 55, 
				30, 40, 51, 45, 33, 48, 
				44, 49, 39, 56, 34, 53, 
				46,	42, 50, 36, 29, 32
			};
		
		for(int i=0; i<48; i++)
		{
			Compressed48key[i] = key[PermTable[i]-1];
		}
		return Compressed48key;
	}

	// This method EXPANDS 32-bit Right Half of Input
	// to 48-bits.
	// (Ready to be applied on f(R,K)
	byte[] ExpandRightTo48 (byte[] right)
	{
		byte ExpandedRight[] = new byte[48];
		int ExpansionTable[];
		ExpansionTable = new int[]
			{
				32, 1, 2, 3, 4, 5, 4, 5,
				6, 7, 8, 9, 8, 9, 10, 11,
				12, 13, 12, 13, 14, 15, 16, 17,
				16, 17, 18, 19, 20, 21, 20, 21,
				22, 23, 24, 25, 24, 25, 26, 27,
				28, 29, 28, 29, 30, 31, 32, 1
			};
		for (int i=0; i<48; i++)
		{
			ExpandedRight[i] = right[ExpansionTable[i]-1];
		}
		return ExpandedRight;
	}
			
	// Once, we have ExpandedRight and Compressed48key,
	// this method stores the result of their f in an array.
	// This Func includes calling S-box and P-box methods.
	byte[] FuncOnRightandKey(byte[] Right32bit, byte[] Compressed48key)
	{
		byte ResultofFunc[] = new byte[32];

		byte ExpandedRightTo48[] = new byte[48];
		ExpandedRightTo48 = this.ExpandRightTo48(Right32bit);
		
		byte XORedRightAndKey[] = new byte[48];
		for (int i=0; i<48; i++)
		{
			XORedRightAndKey[i] = (byte) (ExpandedRightTo48[i] ^ Compressed48key[i]);
		}
				
		// Applying Substitution through 8 S-Boxes
		// on R XOR K.
		byte Substituted32[] = new byte[32];
		Substituted32 = this.SubstituteTo32(XORedRightAndKey);
		
		// Applying P-Box Permutation on Substituted Result.
		ResultofFunc = this.ApplyPBox(Substituted32);
		
		return ResultofFunc;
	}
	
	// Substituting 48 bits to 32 bits by 8 S-Boxes.
	byte[] SubstituteTo32 (byte[] toBeSubstituted)
	{
		byte SubstitutedTo32[] = new byte[32];
		int row, column;
		int[][][] SBox = new int[8][][];	// a 3D array.
		SBox[0] = new int [][]
		{
			{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
			{0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
			{4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
			{15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}
		};
		SBox[1] = new int [][]
		{
			{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
			{3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
			{0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
			{13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}
		};
		SBox[2] = new int [][]
		{
			{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
			{13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
			{13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
			{1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}
		};
		SBox[3] = new int [][]
		{
			{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
			{13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
			{10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
			{3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}
		};
		SBox[4] = new int [][]
		{
			{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
			{14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
			{4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
			{11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}
		};
		SBox[5] = new int [][]
		{
			{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
			{10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
			{9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
			{4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}				
		};
		SBox[6] = new int [][]
		{
			{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
			{13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
			{1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
			{6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}				
		};
		SBox[7] = new int [][]
		{
			{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
			{1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
			{7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
			{2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}				
		};

		int k = 0;	// k is the NEXT-TO-BE-FILLED location of 32-bit Substituted Array.
		// i denotes the beginning of every chunk of 6-bits in this 48-bit INPUT.
		for(int i=0; i<48; i=i+6)
		{
			// Row is stored in form of an integer using 1st and 6th bit.
			row = this.getBit(toBeSubstituted[i]) * 2 + this.getBit(toBeSubstituted[i+5]);
			// Column is stored in form of integer using 2nd-5th bits.
			column = this.getBit(toBeSubstituted[i+1]) * 8 + this.getBit(toBeSubstituted[i+2]) * 4 + this.getBit(toBeSubstituted[i+3]) * 2 + this.getBit(toBeSubstituted[i+4]);
			
			// newValue is fetched from corresponding S-BOX
			// using ROW and COLUMN.
			int newValue = SBox[i/6][row][column];
			int temp;
			// newValue is stored in 4-bits of Substituted Array every round.
			for (int j=3; j>=0; j--)
			{
				temp = newValue;
				SubstitutedTo32[k] = (byte) this.getBit((temp >> j));
				k++;
			}
		}
		return SubstitutedTo32;
	}
	
	// This method applies P-BOX Permutation on 32-bits.
	// This is the final step of f(R,K)
	byte[] ApplyPBox(byte[] Substituted)
	{
		byte PBoxApplied[] = new byte[32];
        int PBoxPermTable[];
        PBoxPermTable = new int[]
        	{
        		16, 7, 20, 21, 29, 12, 28, 17,
        		1, 15, 23, 26, 5, 18, 31, 10,
        		2, 8, 24, 14, 32, 27, 3, 9,
        		19, 13, 30, 6, 22, 11, 4, 25
        	};
        
        for(int i=0; i<32; i++)
        {
        	PBoxApplied[i] = Substituted[PBoxPermTable[i]-1];
        }        
        return PBoxApplied;
	}
	
	// Once, Encrypted, a FINAL PERMUTATION is applied on
	// block of 64-bits. This is the final CIPHER BLOCK.
	byte[] FinalPermutation(byte[] block)
	{
		int FinalPermutationTable[];
		FinalPermutationTable = new int[]
			{
				40, 8, 48, 16, 56, 24, 64, 32,
	            39, 7, 47, 15, 55, 23, 63, 31,
	            38, 6, 46, 14, 54, 22, 62, 30,
	            37, 5, 45, 13, 53, 21, 61, 29,
	            36, 4, 44, 12, 52, 20, 60, 28,
	            35, 3, 43, 11, 51, 19, 59, 27,
	            34, 2, 42, 10, 50, 18, 58, 26,
	            33, 1, 41, 9, 49, 17, 57, 25,
			};
		byte PermutedBlock[] = new byte[64];
        
        for(int i=0; i<64; i++)
        {
        	PermutedBlock[i] = block[FinalPermutationTable[i]-1]; 
        }
        return PermutedBlock;
	}
	
	// This method converts the Decrypted Text into
	// 64-bits of String[] (to be able to be WRITTEN after encryption.)
	String[] convertByteToBit(byte[] block)
	{
        byte bblock[] = new byte[8];
        String Decrypted[] = new String[8];
        
        for (int i=0, j=7; i<64; i++)
        {
        	bblock[i/8] = (byte) (bblock[i/8] | (block[i] << j));
        	j--;
        	if (j<0)
        	{
        		j=7;
        	}
        }
		for(int i=0; i<8; i++)
		{
			Decrypted[i] = String.format("%02x", bblock[i]);
		}
        return Decrypted;
	}
	
	// This method converts the Decrypted Text into
	// 8 bytes (to be able to be WRITTEN after decryption.)
	byte[] convertBytesToByte(byte[] block)
	{
        byte bblock[] = new byte[8];        
        for (int i=0, j=7; i<64; i++)
        {
        	bblock[i/8] = (byte) (bblock[i/8] | (block[i] << j));
        	j--;
        	if (j<0)
        	{
        		j=7;
        	}
        }
		return bblock;
	}
}