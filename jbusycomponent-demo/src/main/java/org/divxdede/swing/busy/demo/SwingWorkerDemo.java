package org.divxdede.swing.busy.demo;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.table.DefaultTableModel;
import org.divxdede.swing.busy.BusyModel;
import org.divxdede.swing.busy.BusySwingWorker;

/**
 * Demo of a {@link BusySwingWorker}
 * @author André Sébastien (divxdede)
 */
public class SwingWorkerDemo extends BusySwingWorker<Integer, Object[]> {

    public static final AtomicLong counter = new AtomicLong();
    public static final String[]   NAMES = new String[] { "Martin"  , "Bernard" , "Dubois" , "Thomas" , "Robert" , "Richard" , "Petit" , "Durand" , "Leroy" , "Moreau" , "Simon" , "Laurent" , "Lefebvre" , "Michel" , "Garcia" , "David" , "Bertrand" , "Roux" , "Vincent" , "Fournier" };
    public static final String[]   COLORS = new String[] { "White" , "Gray" , "Black" , "Red" , "Pink" , "Orange" , "Yellow" , "Green" , "Magenta" , "Cyan" , "Blue" };


    private final DefaultTableModel dataModel;

    public SwingWorkerDemo(BusyModel busyModel , DefaultTableModel dataModel ) {
        super(busyModel);
        this.dataModel = dataModel;
    }

    @Override
    protected Integer doInBackground() throws Exception {
        int i = 0;

        getProgressModel().setMinimum(0);
        getProgressModel().setMaximum(500);
        for( ; i < 500 ; i++ ) {
            // manage cancelation from the JBusyComponent
            if( Thread.interrupted() ) return i;

            // Sleep for create a long task :P
            try {
                Thread.sleep(25);
            }
            catch(InterruptedException e) {
                // manage cancelation from the JBusyComponent
                return i;
            }

            // Publish a new row in the data model
            Object[] row = new Object[]{ counter.addAndGet(1) , getRandomName() , getRandomColor() , getRandomAge() };
            publish(row);

            // update the progression
            getProgressModel().setValue(i); // That will use {@link #setProgress} by computing the progression in a [0 ~ 100] range
        }
        return i;
    }

    /** Get a random name
     */
    private static String getRandomName() {
        return NAMES[ (int)(Math.random() * NAMES.length) ];
    }

    private static String getRandomColor() {
        return COLORS[ (int)(Math.random() * COLORS.length) ];
    }

    private static int getRandomAge() {
        return Math.round(  ((float)Math.random() * 45) + 8 );
    }

    @Override
    protected void process(List<Object[]> chunks) {
        for(Object[] row : chunks ) {
            dataModel.addRow(row);
        }
    }
}
