package com.egoshishang.sys;

import java.util.*;

public class WorkQueue
{
    private final int nThreads;
    
    private final PoolWorker[] threads;
    private final LinkedList<Runnable> queue;

    public WorkQueue(int nThreads)
    {
        this.nThreads = nThreads;
        queue = new LinkedList<Runnable>();
        threads = new PoolWorker[nThreads];

        for (int i=0; i<nThreads; i++) {
            threads[i] =  new PoolWorker();
            threads[i].start();
           
        }
    }
    
    public void shutDown()
    {
    	for(int i = 0; i < threads.length; i++)
    	{
    		threads[i].interrupt();
    	}
    }
    public void execute(Runnable r) {
        synchronized(queue) {
            queue.addLast(r);
            queue.notify();
        }
    }

    private class PoolWorker extends Thread {
        public void run() {
            Runnable r;
            boolean interrupted = false;
            while (true) {
                synchronized(queue) {
                    while (queue.isEmpty()) {
                        try
                        {
                            queue.wait();
                        }
                        catch (InterruptedException ignored)
                        {
                        	interrupted = true;
                        	break;
                        }
                    }
                    if(interrupted)
                    	break;
                    r = (Runnable) queue.removeFirst();
                    if(r == null)
                    {
                    	break;
                    }
                }

                // If we don't catch RuntimeException, 
                // the pool could leak threads
                try {
                    r.run();
                }
                catch (RuntimeException e) {
                	e.printStackTrace();
                    // You might want to log something here
                }
            }
        }
    }
}