/*
Problem Statement 2: Two-Pass Macro Processor
Design suitable data structures and implement Pass-I and Pass-II of a two-pass macroprocessor.
The output of Pass-I (MNT, MDT and intermediate code file without any macro definitions) should be input for Pass-II.


SOURCE.ASM COPY THIS AND SAVE FILE TO Source.asm


MACRO
INCR &ARG1
LDA &ARG1
ADD ONE
STA &ARG1
MEND

START 100
INCR ALPHA
LDA BETA
STA GAMMA
ONE DC 1
ALPHA DC 5
BETA DC 10
GAMMA DS 1
END


*/


import java.io.*;
import java.util.*;

class MNTEntry {
    String macroName;
    int mdtIndex;

    MNTEntry(String macroName, int mdtIndex) {
        this.macroName = macroName;
        this.mdtIndex = mdtIndex;
    }
}

public class TwoPassMacroProcessor {
    static List<MNTEntry> MNT = new ArrayList<>();
    static List<String> MDT = new ArrayList<>();
    static List<String> intermediateCode = new ArrayList<>();

    static void pass1(String sourceFile, String interFile, String mntFile, String mdtFile) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(sourceFile));
        BufferedWriter interWriter = new BufferedWriter(new FileWriter(interFile));
        BufferedWriter mntWriter = new BufferedWriter(new FileWriter(mntFile));
        BufferedWriter mdtWriter = new BufferedWriter(new FileWriter(mdtFile));

        String line;
        boolean inMacro = false;
        String currentMacro = null;
        int mdtIndex = 0;

        while ((line = br.readLine()) != null) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) continue;

            String[] parts = trimmedLine.split("\\s+");
            String firstWord = parts[0];

            if (firstWord.equalsIgnoreCase("MACRO")) {
                inMacro = true;
                continue; // Skip MACRO line
            }

            if (inMacro) {
                if (currentMacro == null) {

                    currentMacro = parts[0];


                    MNT.add(new MNTEntry(currentMacro, MDT.size()));


                    MDT.add(trimmedLine);

                } else if (firstWord.equalsIgnoreCase("MEND")) {

                    MDT.add("MEND");
                    inMacro = false;
                    currentMacro = null;
                } else {

                    MDT.add(trimmedLine);
                }
            } else {

                interWriter.write(trimmedLine);
                interWriter.newLine();
            }
        }


        for (MNTEntry entry : MNT) {
            mntWriter.write(entry.macroName + "\t" + entry.mdtIndex);
            mntWriter.newLine();
        }

        for (String mdtLine : MDT) {
            mdtWriter.write(mdtLine);
            mdtWriter.newLine();
        }

        br.close();
        interWriter.close();
        mntWriter.close();
        mdtWriter.close();
    }


    static void pass2(String interFile, String mntFile, String mdtFile, String outputFile) throws IOException {
        BufferedReader interReader = new BufferedReader(new FileReader(interFile));
        BufferedReader mntReader = new BufferedReader(new FileReader(mntFile));
        BufferedReader mdtReader = new BufferedReader(new FileReader(mdtFile));
        BufferedWriter outputWriter = new BufferedWriter(new FileWriter(outputFile));


        Map<String, Integer> mntMap = new HashMap<>();
        String mntLine;
        while ((mntLine = mntReader.readLine()) != null) {
            String[] parts = mntLine.trim().split("\\s+");
            mntMap.put(parts[0], Integer.parseInt(parts[1]));
        }


        List<String> mdtList = new ArrayList<>();
        String mdtLine;
        while ((mdtLine = mdtReader.readLine()) != null) {
            mdtList.add(mdtLine);
        }

        String line;
        while ((line = interReader.readLine()) != null) {
            String trimmedLine = line.trim();
            if (trimmedLine.isEmpty()) {
                outputWriter.newLine();
                continue;
            }
            String[] parts = trimmedLine.split("\\s+");
            String possibleMacro = parts[0];

            if (mntMap.containsKey(possibleMacro)) {

                int mdtIndex = mntMap.get(possibleMacro);

                while (!mdtList.get(mdtIndex).equalsIgnoreCase("MEND")) {
                    outputWriter.write(mdtList.get(mdtIndex));
                    outputWriter.newLine();
                    mdtIndex++;
                }
            } else {

                outputWriter.write(trimmedLine);
                outputWriter.newLine();
            }
        }

        interReader.close();
        mntReader.close();
        mdtReader.close();
        outputWriter.close();
    }

    public static void main(String[] args) {
        try {
            pass1("source.asm", "intermediate.asm", "mnt.txt", "mdt.txt");
            pass2("intermediate.asm", "mnt.txt", "mdt.txt", "expanded.asm");

            System.out.println("Two-pass Macro processing completed.");
            System.out.println("Intermediate code: intermediate.asm");
            System.out.println("Macro Name Table: mnt.txt");
            System.out.println("Macro Definition Table: mdt.txt");
            System.out.println("Expanded code after macro expansion: expanded.asm");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/*
