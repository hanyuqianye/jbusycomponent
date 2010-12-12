package org.divxdede.swing.busy.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import org.divxdede.swing.busy.JBusyComponent;

public class TestIssue9 extends JPanel {

    public TestIssue9() {
        super(new BorderLayout());
        setPreferredSize(new Dimension(300, 200));
        JList list = new JList(new Object[]{"One", "Two", "Three"});
        final JBusyComponent<JComponent> wrappedList = new
JBusyComponent<JComponent>(list);
        add(wrappedList, BorderLayout.CENTER);
        JButton button = new JButton("Start Busy");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                wrappedList.setBusy(!wrappedList.isBusy());
            }
        });
        add(button, BorderLayout.EAST);
    }

    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new TestIssue9());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}