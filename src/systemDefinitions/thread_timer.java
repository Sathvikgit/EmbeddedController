/*
 * File thread_timer.java
 *  This file contains timer class which 
 *  is used to generate timer of sepcific time. 
 * Author Vishal <vishal@hallresearch.com>
 */

package systemDefinitions;
import java.util.Timer;
import java.util.TimerTask;
import java.net.*;

/*
 * thread_timer class used to calculate time for which 
 * each thread should run. Constructor tread_timer(int) 
 * needs to be passed with no of seconds. 
 * After specified seconds istimeup() methods returns 
 * true which can be used to determine timeup.
 */ 
 
public class thread_timer {
    Timer timer1 = null, timer2= null;
    private boolean timeup =  false;
    int sec = 0;
    int count =  0;    
    
    /**
     * Constructor schedules timers for specified time.
     * 
     * @param seconds Timer duration. 
     */
    public thread_timer(int seconds){
        sec = seconds;
        timeup  =  false;
        timer1 =  new Timer();
        timer2 =  new Timer();
        // Time up task signals time up event after specified seconds
        timer1.schedule(new timeup_task(), seconds * 1000);
        // Count time task runs after every 100ms to get current count value 
        // for progressbars
        timer2.schedule(new count_time_task(), 0, 1* 100);
    }
    
    /**
     * Class runs as separate thread and counts time elapsed.
     */
    class count_time_task extends TimerTask{    
        @Override
        public void run(){         
            increase_count();
            // Count time till 10 times of specified seconds 
            // since 100ms * 10 = 1 sec 
            if(count > ((sec*10)-1)){              
                timer2.cancel();
                return;
            }        
        }
    }
    
    /**
     * Class runs as separate task and checks if 
     * scheduled tasks are done running.
     */
    class timeup_task extends TimerTask{    
        @Override
        public void run(){         
            timeup = true;
            timer1.cancel();
        }
    }
    
    /**
     * Checks if time us up.
     * 
     * @return true if time is up
     *      <p>false if time is not up.
     */
    public synchronized boolean istimeup(){        
        return timeup;
    }
    
    /**
     * Increases timer timeup count by 1.
     */
    synchronized void increase_count(){
        count++;
    }
    
    /**
     * Get current timer timeup count.
     * @return timer timeup count.
     */
    synchronized int get_count(){
        return count;
    }  
    
}
