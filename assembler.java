/*
Problem Statement 1: Two-Pass Assembler
Design suitable Data structures and implement Pass-I and Pass-II of a two-pass assembler for pseudo-machine.
Implementation should consist of a few instructions from each category and few assembler directives.
The output of Pass-I (intermediate code file and symbol table) should be input for Pass-II.

SOURCE.ASM COPY THIS AND SAVE FILE TO Source.asm


START 100
LOOP LDA ALPHA
STA BETA
ADD GAMMA
ALPHA DC 5
BETA DS 1
GAMMA DC 10
END

*/
import java.io.*;
import java.util.*;

class Symbol {
    String name;
    int address;
    boolean isDefined;

    Symbol(String name, int address, boolean defined) {
        this.name = name;
        this.address = address;
        this.isDefined = defined;
    }
}

public class TwoPassAssembler {

    private static final Map<String, String[]> OP_TABLE = new HashMap<>();
    static {
        OP_TABLE.put("LDA", new String[]{"IS", "01"});
        OP_TABLE.put("STA", new String[]{"IS", "02"});
        OP_TABLE.put("ADD", new String[]{"IS", "03"});
        OP_TABLE.put("SUB", new String[]{"IS", "04"});
        OP_TABLE.put("MOV", new String[]{"IS", "07"});
        OP_TABLE.put("START", new String[]{"AD", "01"});
        OP_TABLE.put("END", new String[]{"AD", "02"});
        OP_TABLE.put("DC", new String[]{"DL", "01"});
        OP_TABLE.put("DS", new String[]{"DL", "02"});
    }

    private static final Map<String, Symbol> SYMBOL_TABLE = new LinkedHashMap<>();
    private static List<String> INTERMEDIATE = new ArrayList<>();
    private static int LOC_COUNTER = 0;

    private static int getOrCreateSymbolIndex(String name) {
        if (!SYMBOL_TABLE.containsKey(name)) {
            SYMBOL_TABLE.put(name, new Symbol(name, -1, false));
        }
        int idx = 1;
        for (String key : SYMBOL_TABLE.keySet()) {
            if (key.equals(name)) break;
            idx++;
        }
        return idx;
    }

    public static void pass1(String sourceFile, String interFile, String symFile) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(sourceFile));
             BufferedWriter interWriter = new BufferedWriter(new FileWriter(interFile));
             BufferedWriter symWriter = new BufferedWriter(new FileWriter(symFile))) {

            String line;
            LOC_COUNTER = 0;

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith(";")) continue;

                String[] tokens = line.split("\\s+");
                String label = null, opcode = null, operand = null;

                int i = 0;

                if (tokens.length > 0 && tokens[0].endsWith(":")) {
                    label = tokens[0].substring(0, tokens[0].length() - 1);
                    i = 1;
                } else if (tokens.length > 1 && !OP_TABLE.containsKey(tokens[0])) {
                    label = tokens[0];
                    i = 1;
                }

                if (i < tokens.length) opcode = tokens[i++];
                if (i < tokens.length) operand = tokens[i];


                if (label != null && !label.isEmpty()) {
                    if (SYMBOL_TABLE.containsKey(label)) {
                        Symbol existingSym = SYMBOL_TABLE.get(label);
                        if (existingSym.isDefined) {
                            System.err.println("Error: Duplicate definition of symbol '" + label + "' at LC " + LOC_COUNTER);
                        } else {
                            existingSym.address = LOC_COUNTER;
                            existingSym.isDefined = true;
                        }
                    } else {
                        SYMBOL_TABLE.put(label, new Symbol(label, LOC_COUNTER, true));
                    }
                }


                if (opcode == null || !OP_TABLE.containsKey(opcode)) {
                    System.err.println("Error: Invalid/missing opcode in line: " + line);
                    continue;
                }

                String[] opInfo = OP_TABLE.get(opcode);
                String cls = opInfo[0];
                String opc = opInfo[1];

                switch (cls) {
                    case "AD":
                        if ("START".equals(opcode)) {
                            LOC_COUNTER = (operand != null) ? Integer.parseInt(operand) : 0;
                            INTERMEDIATE.add(String.format("%03d (AD,01) (C,%s)", LOC_COUNTER, operand));
                        } else if ("END".equals(opcode)) {
                            INTERMEDIATE.add("(AD,02)");
                        }
                        break;

                    case "IS":
                        String operandCode = "-";
                        if (operand != null) {
                            if (operand.matches("\\d+")) {
                                operandCode = "(C," + operand + ")";
                            } else {
                                int symIdx = getOrCreateSymbolIndex(operand);
                                operandCode = "(S," + symIdx + ")";
                            }
                        }
                        INTERMEDIATE.add(String.format("%03d (IS,%s) %s", LOC_COUNTER, opc, operandCode));
                        LOC_COUNTER++;
                        break;

                    case "DL":
                        if ("DC".equals(opcode)) {
                            INTERMEDIATE.add(String.format("%03d (DL,01) (C,%s)", LOC_COUNTER, operand));
                            LOC_COUNTER++;
                        } else if ("DS".equals(opcode)) {
                            int size = Integer.parseInt(operand);
                            INTERMEDIATE.add(String.format("%03d (DL,02) (C,%s)", LOC_COUNTER, operand));
                            LOC_COUNTER += size;
                        }
                        break;
                }
            }

            for (String ic : INTERMEDIATE) {
                interWriter.write(ic);
                interWriter.newLine();
            }

            symWriter.write("Index\tSymbol\tAddress\tDefined\n");
            int idx = 1;
            for (Symbol sym : SYMBOL_TABLE.values()) {
                symWriter.write(String.format("%d\t%s\t%d\t%s\n",
                    idx++, sym.name, sym.address, sym.isDefined ? "YES" : "NO"));
            }
        }
    }

    public static void pass2(String interFile, String symFile, String machineFile) throws IOException {
        Map<Integer, Integer> symAddr = new HashMap<>();
        try (BufferedReader symReader = new BufferedReader(new FileReader(symFile))) {
            symReader.readLine();
            String line;
            int idx = 1;
            while ((line = symReader.readLine()) != null) {
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 3) {
                    int addr = Integer.parseInt(parts[2]);
                    symAddr.put(idx++, addr);
                }
            }
        }

        try (BufferedReader interReader = new BufferedReader(new FileReader(interFile));
             BufferedWriter machineWriter = new BufferedWriter(new FileWriter(machineFile))) {

            String line;
            while ((line = interReader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || (line.contains("(AD,") && !line.contains("START"))) {
                    continue;
                }

                String[] parts = line.split("\\s+", 3);
                if (parts.length < 2) continue;

                String locStr = parts[0];
                String opPart = parts[1];
                String oprPart = (parts.length > 2) ? parts[2] : "-";

                String content = opPart.substring(1, opPart.length() - 1);
                String[] opInfo = content.split(",");
                String opClass = opInfo[0];
                int opcode = Integer.parseInt(opInfo[1]);

                if ("DL".equals(opClass)) {
                    if (opcode == 1) {
                        int constantValue = Integer.parseInt(
                            oprPart.substring(oprPart.indexOf(',') + 1, oprPart.length() - 1));

                        machineWriter.write(String.format("%s %03d", locStr, constantValue));
                        machineWriter.newLine();
                    }
                    continue;
                }

                int operandValue = 0;
                if (oprPart.equals("-")) {
                    operandValue = 0;
                } else if (oprPart.startsWith("(C,")) {
                    operandValue = Integer.parseInt(
                        oprPart.substring(oprPart.indexOf(',') + 1, oprPart.length() - 1));
                } else if (oprPart.startsWith("(S,")) {
                    int symIdx = Integer.parseInt(
                        oprPart.substring(oprPart.indexOf(',') + 1, oprPart.length() - 1));
                    operandValue = symAddr.getOrDefault(symIdx, 0);
                    if (operandValue == 0 && symIdx != 0) {
                        System.err.println("Warning: Undefined symbol address retrieved for index " + symIdx);
                    }
                }

                machineWriter.write(String.format("%s %02d %03d", locStr, opcode, operandValue));
                machineWriter.newLine();
            }
        }
    }

    public static void main(String[] args) {
        String source = "source.asm";
        String inter = "intermediate.ic";
        String symtab = "symtab.txt";
        String machine = "machinecode.mc";

        if (args.length >= 4) {
            source = args[0];
            inter = args[1];
            symtab = args[2];
            machine = args[3];
        }

        try {
            SYMBOL_TABLE.clear();
            INTERMEDIATE.clear();

            pass1(source, inter, symtab);
            pass2(inter, symtab, machine);

            System.out.println("Two-pass assembly completed successfully.");
            System.out.println("Intermediate: " + inter);
            System.out.println("Symbol Table: " + symtab);
            System.out.println("Machine Code: " + machine);

        } catch (Exception e) {
            System.err.println("Assembly Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
