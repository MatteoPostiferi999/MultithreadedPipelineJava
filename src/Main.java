import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        int N = 5;
        int M = 3;
        int TG = 500;
        int DG = 1000;
        int TP = 1000;
        int DP = 1500;

        System.out.println("Generation started");
        GeneratedArray array = new GeneratedArray(N);
        InputQueue q = new InputQueue();

        GeneratorThread[] generators = new GeneratorThread[N];
        for (int i = 0; i < N; i++) {
            generators[i] = new GeneratorThread(TG, DG, i, array);
            generators[i].start();
            generators[i].setName("Generator " + i);
        }

        ProcessorThread[] processors = new ProcessorThread[M];
        for (int i = 0; i < M; i++) {
            processors[i] = new ProcessorThread(TP, DP, N, array, q);
            processors[i].start();
            processors[i].setName("Processor " + i);
        }

        PostProcessorThread[] postProcessors = new PostProcessorThread[3];
        for (int i = 0; i < 3; i++) {
            postProcessors[i] = new PostProcessorThread(i, q);
            postProcessors[i].start();
            postProcessors[i].setName("PostProcessor " + i);
        }

        try {
            Thread.sleep(10000);
        } finally {
            System.out.println("\nGeneration ended");
            for (GeneratorThread g : generators) g.interrupt();
            for (ProcessorThread p : processors) p.interrupt();
            for (PostProcessorThread pp : postProcessors) pp.interrupt();

            int totalMessages = 0;
            int procOperations = 0;
            int postProcessorOperations = 0;

            System.out.println();
            for (GeneratorThread g : generators) {
                g.join();
                totalMessages += g.getNumOfValues();
                System.out.println(g.getName() + " has printed " + g.getNumOfValues() + " values");
            }
            for (ProcessorThread p : processors) {
                p.join();
                procOperations += p.getNumProcessorOperations();
                System.out.println(p.getName() + " did " + p.getNumProcessorOperations() + " operations");
            }
            for (PostProcessorThread pp : postProcessors) {
                pp.join();
                postProcessorOperations += pp.getNumPostProcessorOperations();
                System.out.println(pp.getName() + " did " + pp.getNumPostProcessorOperations() + " operations");
            }

            System.out.println("Total messages: " + totalMessages);
            System.out.println("Total operations: " + (procOperations + postProcessorOperations));
            System.out.println("Messages left in queue: " + q.getQueue().size());
        }
    }
}

class GeneratorThread extends Thread {
    private int numOfValues = 0;
    private final int TG, DG, genId;
    private final GeneratedArray array;
    private int value;

    public GeneratorThread(int TG, int DG, int genId, GeneratedArray array) {
        this.TG = TG;
        this.DG = DG;
        this.genId = genId;
        this.array = array;
        this.value = genId + 1;
    }

    public int getNumOfValues() {
        return numOfValues;
    }

    public void run() {
        try {
            while (true) {
                sleep(TG + (int) (Math.random() * DG));
                array.put(genId, value++);
                numOfValues++;
            }
        } catch (InterruptedException ignored) {}
    }
}

class GeneratedArray {
    private int[] data;
    private int numOfElems = 0;
    private int numExtraction = 1;

    public GeneratedArray(int N) {
        data = new int[N];
    }

    public synchronized void put(int genId, int value) throws InterruptedException {
        while (data.length - numOfElems == 0 || data[genId] != 0) {
            wait();
        }
        data[genId] = value;
        numOfElems++;
        notifyAll();
    }

    public synchronized FirstOutputObject get() throws InterruptedException {
        while (numOfElems < data.length) {
            wait();
        }
        FirstOutputObject obj = new FirstOutputObject(numExtraction++, data);
        data = new int[data.length];
        numOfElems = 0;
        notifyAll();
        return obj;
    }
}

class FirstOutputObject {
    private final int progressive;
    private final int[] values;

    public FirstOutputObject(int progressive, int[] values) {
        this.progressive = progressive;
        this.values = values;
    }

    public int[] getValues() { return values; }
    public int getProgressive() { return progressive; }
}

class ProcessorThread extends Thread {
    private final int TP, DP, N;
    private int numProcessorOperations = 0;
    private final GeneratedArray array;
    private final InputQueue q;

    public ProcessorThread(int TP, int DP, int N, GeneratedArray array, InputQueue q) {
        this.TP = TP;
        this.DP = DP;
        this.N = N;
        this.array = array;
        this.q = q;
    }

    public int getNumProcessorOperations() { return numProcessorOperations; }

    public void run() {
        try {
            while (true) {
                FirstOutputObject r = array.get();
                int sum = Arrays.stream(r.getValues()).sum();
                sleep(TP + (int) (Math.random() * DP));
                q.put(new SecondOutputObject(sum, r));
                numProcessorOperations++;
            }
        } catch (InterruptedException ignored) {}
    }
}

class SecondOutputObject {
    private final int result;
    private final FirstOutputObject obj;

    public SecondOutputObject(int result, FirstOutputObject obj) {
        this.result = result;
        this.obj = obj;
    }

    public int getResult() { return result; }
    public FirstOutputObject getObj() { return obj; }
}

class InputQueue {
    private final ArrayList<SecondOutputObject> queue = new ArrayList<>();
    private int expectedExtractNumber = 1;

    public ArrayList<SecondOutputObject> getQueue() { return queue; }

    public synchronized void put(SecondOutputObject o) throws InterruptedException {
        queue.add(o);
        notifyAll();
    }

    public synchronized SecondOutputObject[] get() throws InterruptedException {
        SecondOutputObject[] r;
        while ((r = getValues()) == null) {
            wait();
        }
        expectedExtractNumber += 3;
        for (SecondOutputObject o : r) {
            queue.remove(o);
        }
        notifyAll();
        return r;
    }

    private SecondOutputObject[] getValues() {
        SecondOutputObject[] res = new SecondOutputObject[3];
        int p = 0;
        for (SecondOutputObject o : queue) {
            int prog = o.getObj().getProgressive();
            if (prog == expectedExtractNumber || prog == expectedExtractNumber + 1 || prog == expectedExtractNumber + 2) {
                res[p++] = o;
            }
        }
        return (p == 3) ? res : null;
    }
}

class PostProcessorThread extends Thread {
    private final int postProcessorId;
    private final InputQueue q;
    private int numPostProcessorOperations = 0;

    public PostProcessorThread(int postProcessorId, InputQueue q) {
        this.postProcessorId = postProcessorId;
        this.q = q;
    }

    public int getNumPostProcessorOperations() { return numPostProcessorOperations; }

    public void run() {
        try {
            while (true) {
                SecondOutputObject[] store = q.get();
                System.out.println();
                for (SecondOutputObject o : store) {
                    System.out.println("PP id:" + postProcessorId +
                            " progressivo:" + o.getObj().getProgressive() +
                            " array:" + Arrays.toString(o.getObj().getValues()) +
                            " result:" + o.getResult());
                }
                numPostProcessorOperations++;
            }
        } catch (InterruptedException ignored) {}
    }
}
