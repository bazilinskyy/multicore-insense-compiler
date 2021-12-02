# Multi-core Insense (compiler)

## MSc thesis
This project is source code for my [MSc thesis](https://bazilinskyy.github.io/papers/bazilinskyy2013multi.pdf). The joint degree was obtained at [St Andrews Unviversity](https://www.st-andrews.ac.uk)/[Maynooth University](https://www.maynoothuniversity.ie).

## Description
Compiler for the multi-core version of [Insense language](http://insense.cs.st-andrews.ac.uk).

Motivation for this project is to investigate benefits of utilisation of private heaps for memory management and static thread placement for optimising performance and cache usage. Insense, which is a component-based programming language developed in the University of St. Andrews that abstracts over complications of memory management, concurrency control and synchronisation, was used for this study. Two memory management schemes are under investigation: use of a single shared heap and use of multiple private heaps. Further, three thread placement schemes are designed and implemented: 1) static placement, where Insense components are distributed evenly among cores; 2) where all components are placed on a single core; 3) locating Insense components based on frequency of communicating with other components.

With regard to allocation and defalcation of memory taking place in component instances, running them on different cores, the efficiency of using a private heap for each component resulted in speedup by a factor of 16. Further, utilising private heaps reduces a number of L1 cache misses by ~30%. Distributing components over cores according to communication pattern, for the most part performed similar to allowing the OS to perform thread placement. In case where no exchange of data between components takes place, static placement outperformed due to the fact that there is no computation and load balancing is difficult. The Linux kernel appears to not take into account this case, and the static placement scheme was faster than dynamic balancing by a factor of 2.4.

The runtime environment is available at the [bazilinskyy/multicore-insense-runtime](https://github.com/bazilinskyy/multicore-insense-runtime) repo.
