package nachos.threads;

import nachos.machine.*;

import java.util.TreeSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A scheduler that chooses threads based on their priorities.
 *
 * <p>
 * A priority scheduler associates a priority with each thread. The next thread
 * to be dequeued is always a thread with priority no less than any other
 * waiting thread's priority. Like a round-robin scheduler, the thread that is
 * dequeued is, among all the threads of the same (highest) priority, the
 * thread that has been waiting longest.
 *
 * <p>
 * Essentially, a priority scheduler gives access in a round-robin fassion to
 * all the highest-priority threads, and ignores all other threads. This has
 * the potential to
 * starve a thread if there's always a thread waiting with higher priority.
 *
 * <p>
 * A priority scheduler must partially solve the priority inversion problem; in
 * particular, priority must be donated through locks, and through joins.
 */
public class PriorityScheduler extends Scheduler {
    /**
     * Allocate a new priority scheduler.
     */
    public PriorityScheduler() {
    }
    
    /**
     * Allocate a new priority thread queue.
     *
     * @param	transferPriority	<tt>true</tt> if this queue should
     *					transfer priority from waiting threads
     *					to the owning thread.
     * @return	a new priority thread queue.
     */
    public ThreadQueue newThreadQueue(boolean transferPriority) {
    	return new PriorityQueue(transferPriority);
    }

    public int getPriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());
			       
		return getThreadState(thread).getPriority();
    }

    public int getEffectivePriority(KThread thread) {
		Lib.assertTrue(Machine.interrupt().disabled());
			       
		return getThreadState(thread).getEffectivePriority();
    }

    public void setPriority(KThread thread, int priority) {
		Lib.assertTrue(Machine.interrupt().disabled());
			       
		Lib.assertTrue(priority >= priorityMinimum &&
			   priority <= priorityMaximum);
		
		getThreadState(thread).setPriority(priority);
    }

    public boolean increasePriority() {
		boolean intStatus = Machine.interrupt().disable();
			       
		KThread thread = KThread.currentThread();
	
		int priority = getPriority(thread);
		if (priority == priorityMaximum)
		    return false;
	
		setPriority(thread, priority+1);
	
		Machine.interrupt().restore(intStatus);
		return true;
    }

    public boolean decreasePriority() {
		boolean intStatus = Machine.interrupt().disable();
			       
		KThread thread = KThread.currentThread();
	
		int priority = getPriority(thread);
		if (priority == priorityMinimum)
		    return false;
	
		setPriority(thread, priority-1);
	
		Machine.interrupt().restore(intStatus);
		return true;
    }

    /**
     * The default priority for a new thread. Do not change this value.
     */
    public static final int priorityDefault = 1;
    /**
     * The minimum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMinimum = 0;
    /**
     * The maximum priority that a thread can have. Do not change this value.
     */
    public static final int priorityMaximum = 7;    

    /**
     * Return the scheduling state of the specified thread.
     *
     * @param	thread	the thread whose scheduling state to return.
     * @return	the scheduling state of the specified thread.
     */
    protected ThreadState getThreadState(KThread thread) {
		if (thread.schedulingState == null)
		    thread.schedulingState = new ThreadState(thread);
	
		return (ThreadState) thread.schedulingState;
    }

    /**
     * A <tt>ThreadQueue</tt> that sorts threads by priority.
     */
    protected class PriorityQueue extends ThreadQueue {
		PriorityQueue(boolean transferPriority) {
		    this.transferPriority = transferPriority;
		}
	
		public void waitForAccess(KThread thread) {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    getThreadState(thread).waitForAccess(this);
		}
	
		public void acquire(KThread thread) {
		    Lib.assertTrue(Machine.interrupt().disabled());
		    getThreadState(thread).acquire(this);
		}
	
		public KThread nextThread() {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			// 락홀더가 있으면 이 쓰레드 제거 후 락홀더 null 입력
			if(LockHolder != null)
			{
				this.Queue.remove(this); 
				LockHolder = null;
			}
			
			ThreadState k = pickNextThread();
		    if(k != null){
		    	acquire(k.thread);
		    	System.out.println(k.thread.getName()+" is selected");
		    	return k.thread;
		    }
		    // implement me
		    return null;
		}
	
		/**
		 * Return the next thread that <tt>nextThread()</tt> would return,
		 * without modifying the state of this queue.
		 *
		 * @return	the next thread that <tt>nextThread()</tt> would
		 *		return.
		 */
		protected ThreadState pickNextThread() {
			if(Queue.size()>0){
		    	int index=0;
		    	for(int i = 0 ; i < Queue.size();i++ )
		    	{
		    		if(getThreadState(Queue.get(i)).priority>getThreadState(Queue.get(index)).priority)
		    			index = i;
		    	}
		    	return getThreadState(Queue.get(index));
		    }
		    // implement me
		    return null;
		}
		
		public void print() {
			PriorityScheduler a = new PriorityScheduler();
			for(int i=0;i<Queue.size();i++)
	        {System.out.print(Queue.get(i).getName()+"["+a.getThreadState(Queue.get(i)).getPriority()+"] ");}
	        System.out.println();
	
		    // implement me (if you want)
		}
		public boolean transferPriority;
	    public LinkedList<KThread> Queue = new LinkedList<KThread>();
	    public KThread LockHolder;
	
		/**
		 * <tt>true</tt> if this queue should transfer priority from waiting
		 * threads to the owning thread.
		 */
    }
    /**
     * The scheduling state of a thread. This should include the thread's
     * priority, its effective priority, any objects it owns, and the queue
     * it's waiting for, if any.
     *
     * @see	nachos.threads.KThread#schedulingState
     */
    protected class ThreadState {
		/**
		 * Allocate a new <tt>ThreadState</tt> object and associate it with the
		 * specified thread.
		 *
		 * @param	thread	the thread this state belongs to.
		 */
		public ThreadState(KThread thread) {
		    this.thread = thread;
		    
		    setPriority(priorityDefault);
		}
	
		/**
		 * Return the priority of the associated thread.
		 *
		 * @return	the priority of the associated thread.
		 */
		public int getPriority() {
		    return priority;
		}
	
		/**
		 * Return the effective priority of the associated thread.
		 *
		 * @return	the effective priority of the associated thread.
		 */
public int getEffectivePriority() {
			
			// eff에 이 쓰레드(this) 우선순위 값으로 저장
			int eff_priority = this.priority;
			
			// donationQueue를 하나씩 검사
			for (int i=0;i<donationQueue.size(); i++) 
			{
				PriorityQueue pq_temp = donationQueue.get(i); 
				// 해당 큐의 락홀더가 이 쓰레드(this)일경우
				if (pq_temp.LockHolder == this.thread)
				{
					// 이 큐의 쓰레드를 하나씩 검사
					for (int j= 0;j<pq_temp.Queue.size(); j++) 
					{
						ThreadState ts_temp =  getThreadState(pq_temp.Queue.get(j));
						// 큐에 있는 쓰레드중 이 쓰레드(this)의 우선순위보다 높은경우 높은 우선순위를 eff에 저장
						if(ts_temp.getPriority() > eff_priority)
							eff_priority = ts_temp.getPriority(); 
					}
				}
			} 
			
			// eff 값 반환
			return eff_priority; 
		}
	
		/**
		 * Set the priority of the associated thread to the specified value.
		 *
		 * @param	priority	the new priority.
		 */
		public void setPriority(int priority) {
		    if (this.priority == priority)
		    	return;
		    
		    this.priority = priority;
		    
		    // implement me
		}
	
		/**
		 * Called when <tt>waitForAccess(thread)</tt> (where <tt>thread</tt> is
		 * the associated thread) is invoked on the specified priority queue.
		 * The associated thread is therefore waiting for access to the
		 * resource guarded by <tt>waitQueue</tt>. This method is only called
		 * if the associated thread cannot immediately obtain access.
		 *
		 * @param	waitQueue	the queue that the associated thread is
		 *				now waiting on.
		 *
		 * @see	nachos.threads.ThreadQueue#waitForAccess
		 */
		public void waitForAccess(PriorityQueue waitQueue) {
			waitQueue.Queue.add(thread);
		    // implement me
		}
	
		/**
		 * Called when the associated thread has acquired access to whatever is
		 * guarded by <tt>waitQueue</tt>. This can occur either as a result of
		 * <tt>acquire(thread)</tt> being invoked on <tt>waitQueue</tt> (where
		 * <tt>thread</tt> is the associated thread), or as a result of
		 * <tt>nextThread()</tt> being invoked on <tt>waitQueue</tt>.
		 *
		 * @see	nachos.threads.ThreadQueue#acquire
		 * @see	nachos.threads.ThreadQueue#nextThread
		 */
		public void acquire(PriorityQueue waitQueue) {
			Lib.assertTrue(Machine.interrupt().disabled());
			
			// 모름
			if(waitQueue.transferPriority)
				waitQueue.LockHolder = thread;
			
			waitQueue.Queue.remove(thread);
			donationQueue.add(waitQueue);
		    // implement me
		}	
	
		/** The thread with which this object is associated. */	   
		protected KThread thread;
		/** The priority of the associated thread. */
		protected int priority;
		protected LinkedList<PriorityQueue> donationQueue = new LinkedList<PriorityQueue>();
    }
    
//    public static void selfTest(){
//    	
//    	ThreadQueue threadqueue_01 = ThreadedKernel.scheduler.newThreadQueue(true);
//		KThread kthread_01 = new KThread(), kthread_02 = new KThread(), kthread_03 = new KThread(), kthread_04 = new KThread();
//		Lock lock = new Lock();
//		boolean status = Machine.interrupt().disable();
//	
//	
//		
//		kthread_01.setName("one");
//		kthread_02.setName("two");
//		kthread_03.setName("three");
//		kthread_04.setName("four");
//		
//		threadqueue_01.waitForAccess(kthread_01);
//		threadqueue_01.waitForAccess(kthread_02);
//		threadqueue_01.waitForAccess(kthread_03);
//		threadqueue_01.waitForAccess(kthread_04);
//		
//		ThreadedKernel.scheduler.setPriority(kthread_01, 1);
//		ThreadedKernel.scheduler.setPriority(kthread_02, 6);
//		ThreadedKernel.scheduler.setPriority(kthread_03, 3);
//		ThreadedKernel.scheduler.setPriority(kthread_04, 4);
//		
//	
//		
//		Machine.interrupt().restore(status);
//    }
    public static void selfTest2(){
    	/*
    	ThreadQueue threadqueue_01 = ThreadedKernel.scheduler.newThreadQueue(true);
		KThread kthread_01 = new KThread(), kthread_02 = new KThread(), kthread_03 = new KThread(), kthread_04 = new KThread();
		
		
		
		
		boolean status = Machine.interrupt().disable();
		
		kthread_01.setName("one");
		kthread_02.setName("two");
		kthread_03.setName("three");
		kthread_04.setName("four");


		threadqueue_01.waitForAccess(kthread_01);
		threadqueue_01.waitForAccess(kthread_02);
		threadqueue_01.waitForAccess(kthread_03);
		threadqueue_01.waitForAccess(kthread_04);

		ThreadedKernel.scheduler.setPriority(kthread_01, 1);
		ThreadedKernel.scheduler.setPriority(kthread_02, 6);
		ThreadedKernel.scheduler.setPriority(kthread_03, 3);
		ThreadedKernel.scheduler.setPriority(kthread_04, 4);
		
		threadqueue_01.acquire(kthread_01);
		System.out.println(kthread_01.getName()+ThreadedKernel.scheduler.getEffectivePriority(kthread_01));
		System.out.println(kthread_02.getName()+ThreadedKernel.scheduler.getEffectivePriority(kthread_02));
		System.out.println(kthread_03.getName()+ThreadedKernel.scheduler.getEffectivePriority(kthread_03));
		
		threadqueue_01.acquire(kthread_03);
		System.out.println(kthread_01.getName()+ThreadedKernel.scheduler.getEffectivePriority(kthread_01));
		System.out.println(kthread_02.getName()+ThreadedKernel.scheduler.getEffectivePriority(kthread_02));
		System.out.println(kthread_03.getName()+ThreadedKernel.scheduler.getEffectivePriority(kthread_03));
		
		
		
		
		Machine.interrupt().restore(status);*/
    }
}
