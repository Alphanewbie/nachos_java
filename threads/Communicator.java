package nachos.threads;

import java.util.LinkedList;

import nachos.machine.*;


/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */
public class Communicator {
    /**
     * Allocate a new communicator.
     */
    public Communicator() {

    		
    }

    
    public void speak(int w) {
    	lock.acquire();
     	scount++;
    	while(scount > 1 || lcount == 0)
    		{sQueue.sleep();
    		}
    	
    	
    	if(lock.isHeldByCurrentThread()==false)
    		lock.acquire();
   
    	word = w;
    	
    	lQueue.wakeAll();
    	scount--;
    	lock.release();
    }

    public int listen() {
    	lock.acquire();
    	lcount++;
    
    	while(scount == 0)
    		{lQueue.sleep();}
    	if (scount > 0)
		{sQueue.wake();
		lQueue.sleep();
		}
  
    	if(lock.isHeldByCurrentThread()==false)
    		lock.acquire();
    	lcount--;
    	lock.release();
    	return word;

    }
    
    public static void selft(){
    	final Communicator commu = new Communicator();
    	
    	KThread k2 = new KThread(new Runnable(){
    		public void run(){
    			System.out.println("k2 listen");
    			System.out.println(commu.listen());
    			System.out.println("k2 listen end");
    			
    		}});
    	KThread k3 = new KThread(new Runnable(){
    		public void run(){
    			System.out.println("k3 lisetn");
    			System.out.println(commu.listen());
    			System.out.println("k3 listen end");
    			
    		}});
    	KThread k4 = new KThread(new Runnable(){
    		public void run(){
    			System.out.println("k4 listen");
    			System.out.println(commu.listen());
    			System.out.println("k4 listen end");
    			
    		}});
    	KThread k1 = new KThread(new Runnable(){
    		public void run(){
    			System.out.println("k1 speak");
    			commu.speak(5);
    			System.out.println("k1 speak end");
    			
    		}});
    	k1.fork();
    	k2.fork();
    	k3.fork();
    	k4.fork();
    	k1.join();
    	k2.join();
    	k3.join();
    	k4.join();
    	}
    		
    
    	
    
    
    public int scount = 0;
    public int lcount = 0;
    public Lock lock = new Lock();;
    private int word=0;
    private Condition2 sQueue = new Condition2(lock);
    private Condition2 lQueue = new Condition2(lock);
}
