package org.divxdede.swing.busy.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import org.divxdede.swing.busy.BusyModel;
import org.divxdede.swing.busy.BusySwingWorker;
import org.divxdede.swing.busy.JBusyComponent;
import org.divxdede.swing.busy.ui.BasicBusyLayerUI;

/**
 * Use a very long task that showed me some bugs on RemainingTimeMonitor
 * @author André Sébastien (divxdede)
 */
public class TestIssue11 extends JPanel {

    private final JBusyComponent        busyComponent;
    private final JList                 listComponent;
    private final DefaultListModel      listModel = new DefaultListModel();
    private final BusyModel             busyModel;


    public TestIssue11() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(700, 400));

        listComponent = new JList( listModel );
        busyComponent = new JBusyComponent( new JScrollPane( listComponent) );
        busyComponent.setPreferredSize( new Dimension(500,300) );

        
        /** show the remaining time
         */
        BasicBusyLayerUI layerUI = ((BasicBusyLayerUI)busyComponent.getBusyLayerUI());
        layerUI.setRemainingTimeVisible(true);

        busyModel = busyComponent.getBusyModel();
        busyModel.setCancellable(true);
        busyModel.setDeterminate(true);

        add( busyComponent , BorderLayout.CENTER );
        
        JButton button = new JButton("Start filling 500 rows");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fill();
            }
        });
        add(button, BorderLayout.SOUTH);
    }

    private void fill() {
        SwingWorker<Boolean,String> worker = new BusySwingWorker<Boolean,String>(busyModel) {

            @Override
            protected Boolean doInBackground() throws Exception {
                final int COUNT = 500;
                for(int i = 0 ; i < COUNT ; i ++ ) {
                    if( Thread.interrupted() ) {
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    }
                    catch(InterruptedException e) {
                        break;
                    }
                    publish("line #" + i);

                    int progression = (int)( ( (float)i / (float)COUNT ) * 100f );
                    setProgress(  progression );
                }
                return true;
            }

            @Override
            protected void process(List<String> chunks) {
                for(String s : chunks ) {
                    listModel.addElement(s);
                }
            }
        };
        worker.execute();
    }


    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Test SwingWorker");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TestIssue11());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


}
