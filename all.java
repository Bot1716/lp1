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
Problem Statement 3: First Come First Serve (FCFS) CPU Scheduling
Write a program to simulate the First Come First Serve (FCFS) CPU scheduling algorithm.
The program should accept process details such as Process ID, Arrival Time, and Burst Time and compute the Waiting Time and Turnaround Time for each process. Display the Gantt chart, average waiting time, and average turnaround time.

Description:
This program simulates the FCFS CPU scheduling algorithm. It takes input for the number of processes and their respective burst times. Although the problem statement mentions arrival time, this implementation assumes all processes arrive at time zero (or in order) since arrival time handling is not fully included here. The program calculates waiting time and turnaround time for each process and prints a Gantt chart along with average waiting time and turnaround time.
*/
import java.util.Scanner;
class fcfs {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes.");
        int n = sc.nextInt();
        int[] processId=new int[n];
        int[] arrivalTime=new int[n];
        int[] burstTime=new int[n]; 
        int[] completionTime=new int[n];
        int[] turnAroundTime=new int[n];
        int[] waitingTime=new int[n];
        for(int i=0;i<n;i++){
            processId[i]=i+1;
            System.out.println("Enter arrival time for process"+processId[i]);
            arrivalTime[i]=sc.nextInt();
            System.out.println("Enter burst time for process"+processId[i]);
            burstTime[i]=sc.nextInt();
        }
        for(int i=0;i<n-1;i++){
            for(int j=i+1;j<n;j++){
                if (arrivalTime[i]>arrivalTime[j]){
                    int temp = arrivalTime[i];
                    arrivalTime[i] = arrivalTime[j];
                    arrivalTime[j] = temp;
                    temp = burstTime[i];
                    burstTime[i] = burstTime[j];
                    burstTime[j] = temp;
                    temp = processId[i];
                    processId[i] = processId[j];
                    processId[j] = temp;
                }
            }
        }
        int currentTime=0;
        double totalTAT = 0, totalWT = 0;
        for(int i=0;i<n;i++){
            if(currentTime<arrivalTime[i]){
                currentTime=arrivalTime[i];
            }
            completionTime[i]=currentTime+burstTime[i];
            currentTime=completionTime[i];
            
            turnAroundTime[i]=completionTime[i]-arrivalTime[i];
            waitingTime[i]=turnAroundTime[i]-burstTime[i];
            
            totalTAT+=turnAroundTime[i];
            totalWT+=waitingTime[i];
        }
        System.out.println("\nProcess\tAT\tBT\tCT\tTAT\tWT");
        for(int i=0;i<n;i++){
            System.out.println("P"+processId[i]+"\t"+arrivalTime[i]+"\t"+burstTime[i]+"\t"+completionTime[i]+"\t"+turnAroundTime[i]+"\t"+waitingTime[i]);
        }
        System.out.println("Average turn around time = "+totalTAT/n);
        System.out.println("Average Waiting time = "+totalWT/n);
    }
}
/*
Problem Statement 4: Shortest Job First (SJF – Preemptive) Scheduling
Write a program to simulate the Shortest Job First (SJF – Preemptive) CPU scheduling algorithm. The program should calculate and display the order of execution, waiting time, turnaround time, and their averages for all processes.
Description:
This program simulates the SJF Preemptive scheduling algorithm where the CPU is assigned to the process with the shortest remaining burst time at every time unit. Processes with earlier arrival times and smaller burst times are prioritized. The program tracks remaining time for each process, calculates waiting and turnaround times after completion, and prints the results including average waiting and turnaround times.
*/
import java.util.Scanner;
class sjf{
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes.");
        int n = sc.nextInt();
        int[] remainingTime = new int[n];
        int[] arrivalTime=new int[n];
        int[] burstTime=new int[n]; 
        int[] completionTime=new int[n];
        int[] turnAroundTime=new int[n];
        int[] waitingTime=new int[n];
        for(int i=0;i<n;i++){
            
            System.out.println("Enter arrival time for process"+(i+1));
            arrivalTime[i]=sc.nextInt();
            System.out.println("Enter burst time for process"+(i+1));
            burstTime[i]=sc.nextInt();
            remainingTime[i]=burstTime[i];
        }
        int completed=0,currentTime=0;
        float totalTAT=0,totalWT=0;
        while(completed!=n){
            int currentProcess=-1;
            int minRemaining=Integer.MAX_VALUE;
            
            for(int i=0;i<n;i++){
                if(arrivalTime[i]<=currentTime && remainingTime[i]<minRemaining &&remainingTime[i]>0){
                    minRemaining=remainingTime[i];
                    currentProcess=i;
                }
            }
            if(currentProcess==-1){
                currentTime++;
                continue;
            }
            remainingTime[currentProcess]--;
            currentTime++;
            if(remainingTime[currentProcess]==0){
                completed++;
                completionTime[currentProcess]=currentTime;
                turnAroundTime[currentProcess]=completionTime[currentProcess]-arrivalTime[currentProcess];
                waitingTime[currentProcess]=turnAroundTime[currentProcess]-burstTime[currentProcess];
                totalTAT+=turnAroundTime[currentProcess];
                totalWT+=waitingTime[currentProcess];
            }
        }
        System.out.println("\nPid\tAT\tBT\tCT\tTAT\tWT");
        for(int i=0;i<n;i++){
            System.out.println("P"+(i+1)+"\t"+arrivalTime[i]+"\t"+burstTime[i]+"\t"+completionTime[i]+"\t"+turnAroundTime[i]+"\t"+waitingTime[i]);
        }
        System.out.println("Average turn around time = "+totalTAT/n);
        System.out.println("Average Waiting time = "+totalWT/n);
    }
}

/*
Problem Statement 5: Priority Scheduling (Non-Preemptive)
Write a program to simulate the Priority Scheduling (Non-Preemptive) algorithm.
Each process should have an associated priority value, and the scheduler should select the process with the highest priority for execution next.
Compute and display the waiting time, turnaround time, and average times for all processes.
*/
import java.util.Scanner;
class priority {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter number of processes.");
        int n = sc.nextInt();
        int[] processId=new int[n];
        int[] arrivalTime=new int[n];
        int[] burstTime=new int[n]; 
        int[] priority=new int[n]; 
        int[] completionTime=new int[n];
        int[] turnAroundTime=new int[n];
        int[] waitingTime=new int[n];
        boolean[] isCompleted = new boolean[n];
        for(int i=0;i<n;i++){
            processId[i]=i+1;
            System.out.println("Enter arrival time for p"+processId[i]);
            arrivalTime[i]=sc.nextInt();
            System.out.println("Enter burst time for p"+processId[i]);
            burstTime[i]=sc.nextInt();
            System.out.println("Enter priority for p"+processId[i]);
            priority[i]=sc.nextInt();
        }
        int completed=0,currentTime=0;
        float totalTAT=0,totalWT=0;
        while(completed!=n){
            int currentProcess=-1;
            int highestPriority=Integer.MAX_VALUE;
            for(int i = 0;i<n;i++){
                if(arrivalTime[i] <= currentTime && !isCompleted[i]){
                    if(priority[i]<highestPriority){
                        highestPriority=priority[i];
                        currentProcess=i;
                    }
                }
            }
            if(currentProcess==-1){
                currentTime++;
                continue;
            }
            currentTime+=burstTime[currentProcess];
            completionTime[currentProcess]=currentTime;
            turnAroundTime[currentProcess]=completionTime[currentProcess]-arrivalTime[currentProcess];
            waitingTime[currentProcess]=turnAroundTime[currentProcess]-burstTime[currentProcess];
            isCompleted[currentProcess]=true;
            totalTAT+=turnAroundTime[currentProcess];
            totalWT+=waitingTime[currentProcess];
            completed++;
        }
        System.out.println("\nPid\tAT\tBT\tPr\tCT\tTAT\tWT");
        for(int i=0;i<n;i++){
            System.out.println("P"+processId[i]+"\t"+arrivalTime[i]+"\t"+burstTime[i]+"\t"+priority[i]+"\t"+completionTime[i]+"\t"+turnAroundTime[i]+"\t"+waitingTime[i]);
        }
        System.out.println("Average turn around time = "+totalTAT/n);
        System.out.println("Average Waiting time = "+totalWT/n);
    }
}

/*
Problem Statement 6: Round Robin (RR) Scheduling
Write a program to simulate the Round Robin (Preemptive) CPU scheduling algorithm.
The program should take time quantum as input and schedule processes in a cyclic order.
Display the Gantt chart, waiting time, turnaround time, and average values for all processes.
*/
import java.util.*;

public class RoundRobin {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();

        int[] processId = new int[n];
        int[] arrivalTime = new int[n];
        int[] burstTime = new int[n];
        int[] remainingTime = new int[n];
        int[] waitingTime = new int[n];
        int[] turnaroundTime = new int[n];
        int[] completionTime = new int[n];

        for (int i = 0; i < n; i++) {
            processId[i] = i + 1;
            System.out.print("Enter Arrival Time for Process " + (i + 1) + ": ");
            arrivalTime[i] = sc.nextInt();
            System.out.print("Enter Burst Time for Process " + (i + 1) + ": ");
            burstTime[i] = sc.nextInt();
            remainingTime[i] = burstTime[i];
        }

        System.out.print("Enter Time Quantum: ");
        int tq = sc.nextInt();

        int time = 0, completed = 0;
        Queue<Integer> q = new LinkedList<>();
        boolean[] inQueue = new boolean[n];

        System.out.println("\nGantt Chart:");
        while (completed < n) {
            
            for (int i = 0; i < n; i++) {
                if (arrivalTime[i] <= time && !inQueue[i] && remainingTime[i] > 0) {
                    q.add(i);
                    inQueue[i] = true;
                }
            }

            if (q.isEmpty()) {
                time++;
                continue;
            }

            int i = q.poll();
            System.out.print("P" + processId[i] + " | ");

            int execTime = Math.min(remainingTime[i], tq);
            remainingTime[i] -= execTime;
            time += execTime;

            
            for (int j = 0; j < n; j++) {
                if (arrivalTime[j] <= time && !inQueue[j] && remainingTime[j] > 0) {
                    q.add(j);
                    inQueue[j] = true;
                }
            }

            if (remainingTime[i] > 0) {
                q.add(i);
            } else {
                completionTime[i] = time;
                turnaroundTime[i] = completionTime[i] - arrivalTime[i];
                waitingTime[i] = turnaroundTime[i] - burstTime[i];
                completed++;
            }
        }

        System.out.println("\n\nPID\tAT\tBT\tCT\tTAT\tWT");

        float avgTAT = 0, avgWT = 0;
        for (int i = 0; i < n; i++) {
            System.out.println("P" + processId[i] + "\t" + arrivalTime[i] + "\t" + burstTime[i] + "\t" +
                    completionTime[i] + "\t" + turnaroundTime[i] + "\t" + waitingTime[i]);
            avgTAT += turnaroundTime[i];
            avgWT += waitingTime[i];
        }

        System.out.printf("\nAverage Turnaround Time = %.2f", avgTAT / n);
        System.out.printf("\nAverage Waiting Time = %.2f\n", avgWT / n);
    }
}
/*
Problem Statement 7: Memory Allocation – First Fit
Write a program to simulate First Fit memory allocation strategy.
The program should allocate each process to the first available memory block that is large enough to accommodate it.
Display the memory allocation table and identify any unused or fragmented memory.
*/
import java.util.*;

public class FirstFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
        boolean[] allocatedBlock = new boolean[m];

        System.out.println("Enter size of each memory block:");
        for (int i = 0; i < m; i++) {
            System.out.print("Block " + (i + 1) + ": ");
            blockSize[i] = sc.nextInt();
        }

        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();
        int[] processSize = new int[n];
        int[] allocation = new int[n]; 
        Arrays.fill(allocation, -1);

        System.out.println("Enter size of each process:");
        for (int i = 0; i < n; i++) {
            System.out.print("Process " + (i + 1) + ": ");
            processSize[i] = sc.nextInt();
        }

        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                if (!allocatedBlock[j] && blockSize[j] >= processSize[i]) {
                    allocation[i] = j;
                    allocatedBlock[j] = true;
                    blockSize[j] -= processSize[i];
                    break;
                }
            }
        }

      
        System.out.println("\nMemory Allocation Table:");
        System.out.println("Process\tSize\tBlock Allocated\tRemaining Block Size");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1) {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tB" + (allocation[i] + 1) +
                        "\t\t" + blockSize[allocation[i]]);
            } else {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tNot Allocated\t-");
            }
        }

        
        System.out.println("\nUnused / Fragmented Memory in Blocks:");
        for (int i = 0; i < m; i++) {
            System.out.println("Block " + (i + 1) + " -> Remaining Size: " + blockSize[i]);
        }
    }
}
/*
Problem Statement 8: Memory Allocation – Best Fit
Write a program to simulate Best Fit memory allocation strategy.
The program should allocate each process to the smallest available block that can hold it.
Display the final allocation and show internal fragmentation if any.
*/
import java.util.*;

public class BestFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
        boolean[] allocatedBlock = new boolean[m];

        System.out.println("Enter size of each memory block:");
        for (int i = 0; i < m; i++) {
            System.out.print("Block " + (i + 1) + ": ");
            blockSize[i] = sc.nextInt();
        }

        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();
        int[] processSize = new int[n];
        int[] allocation = new int[n];
        Arrays.fill(allocation, -1);

        System.out.println("Enter size of each process:");
        for (int i = 0; i < n; i++) {
            System.out.print("Process " + (i + 1) + ": ");
            processSize[i] = sc.nextInt();
        }

        
        for (int i = 0; i < n; i++) {
            int bestIndex = -1;
            for (int j = 0; j < m; j++) {
                if (!allocatedBlock[j] && blockSize[j] >= processSize[i]) {
                    if (bestIndex == -1 || blockSize[j] < blockSize[bestIndex]) {
                        bestIndex = j;
                    }
                }
            }
            if (bestIndex != -1) {
                allocation[i] = bestIndex;
                allocatedBlock[bestIndex] = true;
                blockSize[bestIndex] -= processSize[i];
            }
        }

        
        System.out.println("\nMemory Allocation Table:");
        System.out.println("Process\tSize\tBlock Allocated\tInternal Fragmentation");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1) {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tB" + (allocation[i] + 1) +
                        "\t\t" + blockSize[allocation[i]]);
            } else {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tNot Allocated\t-");
            }
        }
    }
}

/*
Problem Statement 9: Memory Allocation – Next Fit
Write a program to simulate Next Fit memory allocation strategy.
The program should continue searching for the next suitable memory block from the last allocated position instead of starting from the beginning.
Display the memory allocation table and fragmentation details.
*/
import java.util.*;
public class NextFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
        System.out.println("Enter size of each memory block:");
        for (int i = 0; i < m; i++) {
            System.out.print("Block " + (i + 1) + ": ");
            blockSize[i] = sc.nextInt();
        }
        System.out.print("\nEnter number of processes: ");
        int n = sc.nextInt();
        int[] processSize = new int[n];
        int[] allocation = new int[n];
        Arrays.fill(allocation, -1);
        System.out.println("Enter size of each process:");
        for (int i = 0; i < n; i++) {
            System.out.print("Process " + (i + 1) + ": ");
            processSize[i] = sc.nextInt();
        }
        int lastAllocatedIndex = 0;
        for (int i = 0; i < n; i++) {
            int count = 0;
            boolean allocated = false;
            while (count < m) {
                if (blockSize[lastAllocatedIndex] >= processSize[i]) {
                    allocation[i] = lastAllocatedIndex;
                    blockSize[lastAllocatedIndex] -= processSize[i];
                    allocated = true;
                    break;
                }
                lastAllocatedIndex = (lastAllocatedIndex + 1) % m;
                count++;
            }
            if (allocated)
                lastAllocatedIndex = (lastAllocatedIndex + 1) % m; // Move to next block after allocation
        }
        System.out.println("\nMemory Allocation Table:");
        System.out.println("Process\tSize\tBlock Allocated\tInternal Fragmentation");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1) {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tB" + (allocation[i] + 1) + "\t\t" + blockSize[allocation[i]]);
            } else {
                System.out.println("P" + (i + 1) + "\t" + processSize[i] + "\tNot Allocated\t-");
            }
        }
    }
}

/*
Problem Statement 10: Memory Allocation – Worst Fit
Write a program to simulate Worst Fit memory allocation strategy.
The program should allocate each process to the largest available memory block.
Display the memory allocation results and any unused space.
*/
import java.util.Scanner;

public class WorstFit {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of memory blocks: ");
        int m = sc.nextInt();
        int[] blockSize = new int[m];
        System.out.println("Enter sizes of memory blocks:");
        for (int i = 0; i < m; i++) {
            blockSize[i] = sc.nextInt();
        }
        System.out.print("Enter number of processes: ");
        int n = sc.nextInt();
        int[] processSize = new int[n];
        System.out.println("Enter sizes of processes:");
        for (int i = 0; i < n; i++) {
            processSize[i] = sc.nextInt();
        }
        int[] allocation = new int[n];
        for (int i = 0; i < n; i++) allocation[i] = -1;
        for (int i = 0; i < n; i++) {
            int worstIdx = -1;
            for (int j = 0; j < m; j++) {
                if (blockSize[j] >= processSize[i]) {
                    if (worstIdx == -1 || blockSize[j] > blockSize[worstIdx]) {
                        worstIdx = j;
                    }
                }
            }
            if (worstIdx != -1) {
                allocation[i] = worstIdx;
                blockSize[worstIdx] -= processSize[i];
            }
        }
        System.out.println("\nProcess No.\tProcess Size\tBlock No.\tRemaining Space");
        for (int i = 0; i < n; i++) {
            if (allocation[i] != -1)
                System.out.println((i + 1) + "\t\t" + processSize[i] + "\t\t" + (allocation[i] + 1) + "\t\t" + blockSize[allocation[i]]);
            else
                System.out.println((i + 1) + "\t\t" + processSize[i] + "\t\tNot Allocated\t-");
        }
        sc.close();
    }
}

/*
Problem Statement 11: Page Replacement – FIFO
Write a program to simulate the First In First Out (FIFO) page replacement algorithm.
The program should accept a page reference string and number of frames as input,
simulate the process of page replacement, and display the total number of page faults.
*/
import java.util.*;

public class FIFOPageReplacement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int frames = sc.nextInt();

        System.out.print("Enter length of page reference string: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter the page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        Set<Integer> frameSet = new HashSet<>();
        Queue<Integer> frameQueue = new LinkedList<>();

        int pageFaults = 0;

        System.out.println("\nPage\tFrames\t\tPage Fault");

        for (int page : pages) {
            if (!frameSet.contains(page)) {
                
                pageFaults++;

                if (frameSet.size() < frames) {
                    frameSet.add(page);
                    frameQueue.add(page);
                } else {
                    int oldest = frameQueue.poll();
                    frameSet.remove(oldest);

                    frameSet.add(page);
                    frameQueue.add(page);
                }
                System.out.println(page + "\t" + frameQueue + "\tYes");
            } else {
                System.out.println(page + "\t" + frameQueue + "\tNo");
            }
        }
        System.out.println("\nTotal Page Faults = " + pageFaults);
        sc.close();
    }
}

/*
Problem Statement 12: Page Replacement – LRU
Write a program to simulate the Least Recently Used (LRU) page replacement algorithm.
The program should display the frame contents after each page reference and the total number of page faults.
*/
import java.util.*;

public class LRUPageReplacement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int framesCount = sc.nextInt();

        System.out.print("Enter number of pages: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        List<Integer> frames = new ArrayList<>();
        int pageFaults = 0;

        System.out.println("\nPage Reference\tFrames\t\tPage Fault");

        for (int page : pages) {
            if (!frames.contains(page)) { 
                if (frames.size() == framesCount) {
                    frames.remove(0); 
                }
                frames.add(page);
                pageFaults++;
                System.out.println(page + "\t\t" + frames + "\tYes");
            } else {
                
                frames.remove(Integer.valueOf(page));
                frames.add(page);
                System.out.println(page + "\t\t" + frames + "\tNo");
            }
        }

        System.out.println("\nTotal Page Faults = " + pageFaults);
        sc.close();
    }
}

/*
Problem Statement 13: Page Replacement – Optimal
Write a program to simulate the Optimal Page Replacement algorithm.
The program should replace the page that will not be used for the longest period of time in the future and display the page replacement steps and page fault count.
*/
import java.util.*;

public class OptimalPageReplacement {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.print("Enter number of frames: ");
        int framesCount = sc.nextInt();

        System.out.print("Enter number of pages in reference string: ");
        int n = sc.nextInt();

        int[] pages = new int[n];
        System.out.println("Enter the page reference string:");
        for (int i = 0; i < n; i++) {
            pages[i] = sc.nextInt();
        }

        List<Integer> frames = new ArrayList<>();
        int pageFaults = 0;

        System.out.println("\nPage Reference\tFrames\t\tStatus");
        for (int i = 0; i < n; i++) {
            int currentPage = pages[i];
            if (frames.contains(currentPage)) {
                System.out.println(currentPage + "\t\t" + frames + "\tHit");
                continue;
            }
            if (frames.size() < framesCount) {
                frames.add(currentPage);
                pageFaults++;
                System.out.println(currentPage + "\t\t" + frames + "\tPage Fault");
                continue;
            }

            int indexToReplace = -1;
            int farthest = i + 1;

            for (int j = 0; j < frames.size(); j++) {
                int nextUse = -1;
                for (int k = i + 1; k < n; k++) {
                    if (pages[k] == frames.get(j)) {
                        nextUse = k;
                        break;
                    }
                }
                if (nextUse == -1) {
                    indexToReplace = j;  
                    break;
                } else if (nextUse > farthest) {
                    farthest = nextUse;
                    indexToReplace = j;
                }
            }

            if (indexToReplace == -1) indexToReplace = 0;
            frames.set(indexToReplace, currentPage);
            pageFaults++;

            System.out.println(currentPage + "\t\t" + frames + "\tPage Fault");
        }

        System.out.println("\nTotal Page Faults: " + pageFaults);
        System.out.println("Total Hits: " + (n - pageFaults));
    }
}
