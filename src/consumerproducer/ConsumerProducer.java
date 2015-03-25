/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package consumerproducer;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author my
 */
public class ConsumerProducer {

    /**
     * @param args the command line arguments
     */
    private static Buffer buffer = new Buffer();

    public static void main(String[] args) {
        // TODO code application logic here
        ExecutorService exe = Executors.newFixedThreadPool(2);
        exe.execute(new ProducerTask());
        exe.execute(new ConsumerTask());
        exe.shutdown();
    }

    private static class Buffer {

        private static final int CAPACITY = 1;
        private static LinkedList<Integer> queue = new LinkedList<Integer>();
        private static Lock lock = new ReentrantLock();
        private static Condition notEmpty = lock.newCondition();
        private static Condition notFull = lock.newCondition();

        public void write(int value) {
            lock.lock();
            try {   
                while (queue.size() == CAPACITY) {
                    System.out.println("Wait for notFull condition");
                        notFull.await();
                }
                queue.offer(value);
                notEmpty.signal();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                lock.unlock();
            }
        }

        public int read() {
            int value = 0;
            try {
                while (queue.isEmpty()) {
                    System.out.println("\t\t\tWait for notEmpty condition");
                        notEmpty.await();
                }
                value=queue.remove();
                notFull.signal();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            } finally {
                lock.unlock();
                 return value;
            }

        }

    }

    private static class ProducerTask implements Runnable {

        public void run() {
            try {
                int i = 1;
                while (true) {
                    System.out.println("Producer write to Buffer" + i);
                    buffer.write(i++);
                    Thread.sleep(10000);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ConsumerProducer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private static class ConsumerTask implements Runnable {

        public void run() {
            try {
                while (true) {
                    System.out.println("\t\t\tConsumer reads " + buffer.read());
                    Thread.sleep(10000);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(ConsumerProducer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
