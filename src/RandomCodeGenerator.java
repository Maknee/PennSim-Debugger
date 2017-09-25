import java.io.Writer;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Random;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Vector;

// 
// Decompiled by Procyon v0.5.30
// 

public class RandomCodeGenerator
{
    private static Vector<Integer> memoryInsnIndices;
    private static Vector<Integer> branchInsnIndices;
    private static Vector<Integer> otherInsnIndices;
    private static int insnsToGenerate;
    private static final int MAX_BRANCH_DISTANCE = 4;
    
    public static void main(final String[] array) {
        if (array.length != 2) {
            System.out.println("Usage: <number of insns to generate> <filename>");
            return;
        }
        RandomCodeGenerator.insnsToGenerate = -1;
        try {
            RandomCodeGenerator.insnsToGenerate = Integer.parseInt(array[0]);
        }
        catch (NumberFormatException ex2) {
            System.out.println("Invalid number: " + array[0]);
            return;
        }
        String substring;
        if (array[1].endsWith(".obj")) {
            substring = array[1].substring(0, array[1].length() - 5);
        }
        else {
            substring = array[1];
        }
        final String string = substring + ".obj";
        final String string2 = substring + ".sym";
        final File file = new File(string);
        BufferedOutputStream bufferedOutputStream;
        try {
            if (!file.createNewFile()) {
                System.out.println("File " + string + " already exists.");
                return;
            }
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
        }
        catch (IOException ex3) {
            System.out.println("Error opening file: " + file.getName());
            return;
        }
        RandomCodeGenerator.memoryInsnIndices = new Vector<Integer>();
        RandomCodeGenerator.branchInsnIndices = new Vector<Integer>();
        RandomCodeGenerator.otherInsnIndices = new Vector<Integer>();
        new P37X().init();
        populateValidP37XInsnList();
        try {
            new Word(512).writeWordToFile(bufferedOutputStream);
            final Random random = new Random();
            final int size = RandomCodeGenerator.memoryInsnIndices.size();
            final int size2 = RandomCodeGenerator.branchInsnIndices.size();
            final int size3 = RandomCodeGenerator.otherInsnIndices.size();
            for (int i = 0; i < RandomCodeGenerator.insnsToGenerate; ++i) {
                int n = 0;
                switch (random.nextInt(10)) {
                    case 0:
                    case 1: {
                        n = RandomCodeGenerator.memoryInsnIndices.elementAt(random.nextInt(size));
                        final InstructionDef instructionDef = ISA.lookupTable[n];
                        if (instructionDef.getOpcode().equalsIgnoreCase("LDR")) {
                            break;
                        }
                        if (instructionDef.getPCOffset(new Word(n)) + i < 0) {
                            n = RandomCodeGenerator.branchInsnIndices.elementAt(random.nextInt(size2));
                            break;
                        }
                        break;
                    }
                    case 2:
                    case 3: {
                        n = RandomCodeGenerator.branchInsnIndices.elementAt(random.nextInt(size2));
                        break;
                    }
                    default: {
                        n = RandomCodeGenerator.otherInsnIndices.elementAt(random.nextInt(size3));
                        break;
                    }
                }
                final InstructionDef instructionDef2 = ISA.lookupTable[n];
                new Word(n).writeWordToFile(bufferedOutputStream);
            }
            bufferedOutputStream.close();
        }
        catch (IOException ex) {
            System.out.println("Error writing object file: " + ex.toString());
        }
        try {
            final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(string2));
            bufferedWriter.write("// Symbol table\n");
            bufferedWriter.write("// Scope level 0:\n");
            bufferedWriter.write("//\tSymbol Name       Page Address\n");
            bufferedWriter.write("//\t----------------  ------------\n");
            for (int j = 512; j < RandomCodeGenerator.insnsToGenerate + 512; ++j) {
                bufferedWriter.write("//\t$               " + String.format("%04X", j) + "\n");
            }
            bufferedWriter.newLine();
            bufferedWriter.close();
        }
        catch (IOException ex4) {
            System.out.println("Error writing symbol table file: " + string2);
        }
    }
    
    private static void populateValidP37XInsnList() {
        for (int i = 0; i < ISA.lookupTable.length; ++i) {
            final InstructionDef instructionDef = ISA.lookupTable[i];
            final Word word = new Word(i);
            if (instructionDef != null) {
                if (!instructionDef.isDirective()) {
                    if (!instructionDef.isData()) {
                        if (!instructionDef.getOpcode().equalsIgnoreCase("NOOP")) {
                            if (instructionDef.isBranch() || instructionDef.getOpcode().equalsIgnoreCase("JSR") || instructionDef.getOpcode().equalsIgnoreCase("JUMP")) {
                                if (instructionDef.getPCOffset(word) <= 4 && instructionDef.getPCOffset(word) > 0) {
                                    RandomCodeGenerator.branchInsnIndices.add(new Integer(i));
                                }
                            }
                            else if (!instructionDef.isCall() && !instructionDef.getOpcode().equalsIgnoreCase("JUMPR") && !instructionDef.getOpcode().equalsIgnoreCase("RTT") && !instructionDef.getOpcode().equalsIgnoreCase("LDR")) {
                                if (!instructionDef.getOpcode().equalsIgnoreCase("STR")) {
                                    if (instructionDef.getOpcode().equalsIgnoreCase("LD")) {
                                        RandomCodeGenerator.memoryInsnIndices.add(new Integer(i));
                                    }
                                    else if (instructionDef.getOpcode().equalsIgnoreCase("ST")) {
                                        if (instructionDef.getPCOffset(word) < -1) {
                                            RandomCodeGenerator.memoryInsnIndices.add(new Integer(i));
                                        }
                                    }
                                    else {
                                        RandomCodeGenerator.otherInsnIndices.add(new Integer(i));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
