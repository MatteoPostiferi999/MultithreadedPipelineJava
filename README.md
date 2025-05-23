# 🧵 Multithreaded Pipeline in Java

This project implements a multithreaded pipeline system in Java based on a **producer–processor–consumer** model. It simulates the coordinated work of generator threads, processor threads, and post-processor threads communicating through synchronized structures.

This project was developed as part of an **Operating Systems exam assignment** during Matteo Postiferi's university coursework (2025).

---

## 🚀 Overview
- **Generators (N)** produce values at variable time intervals.
- **Processors (M)** fetch complete sets of values, compute a result (e.g. a sum), and forward it.
- **PostProcessors (3)** consume batches of 3 results with sequential IDs.
- All communication is synchronized using shared memory, `wait()` and `notifyAll()`.

---

## 🧪 How to Run
1. Compile the project:
```bash
javac Main.java
```
2. Run the program:
```bash
java Main
```

It will run the pipeline for 10 seconds, then interrupt and summarize thread activity.

---

## 📁 Structure
- `Main` – Sets up the threads and coordinates the execution.
- `GeneratorThread` – Periodically generates values.
- `GeneratedArray` – Shared structure storing values from all generators.
- `ProcessorThread` – Collects full arrays and computes results.
- `InputQueue` – Thread-safe buffer for second-stage results.
- `PostProcessorThread` – Consumes batches of 3 results in order.

---

## 📊 Output Summary
At the end, the system prints:
- Number of values generated by each generator
- Number of operations performed by each processor/postprocessor
- Total messages and total operations
- Messages left in the queue

---

## 📜 License
Open for educational and demonstrative purposes.
© Matteo Postiferi, 2025
