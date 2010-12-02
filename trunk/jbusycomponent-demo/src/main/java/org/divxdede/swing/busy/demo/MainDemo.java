package org.divxdede.swing.busy.demo;

import java.awt.Desktop;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import org.divxdede.swing.BoundedRangeModelHub;
import org.divxdede.swing.busy.BusyIcon;
import org.divxdede.swing.busy.BusyModel;
import org.divxdede.swing.busy.DefaultBusyModel;
import org.divxdede.swing.busy.icon.DefaultBusyIcon;
import org.divxdede.swing.busy.icon.InfiniteBusyIcon;
import org.divxdede.swing.busy.icon.RadialBusyIcon;
import org.divxdede.swing.busy.ui.BasicBusyLayerUI;
import org.divxdede.swing.busy.ui.BusyLayerUI;

/**
 * JBusycomponent demonstration of v1.1
 * @author André Sébastien (divxdede)
 */
public class MainDemo extends javax.swing.JPanel {

    /** All base Icons required by this demonstration
     *  Theses icons are loaded by {@link #initBaseIcons()} called by the MainDemo constructor
     */
    private Icon            iconMonitor = null;
    private Icon            iconBattery = null;
    private Icon            iconSearch = null;
    private Icon            iconJava = null;
    private Icon            iconPrinter = null;

    /**
     * ########################################
     * <code>JBusyComponent</code> tabs members
     * ########################################
     */

     /** The <code>BusyIcon</code> used by the JBusyComponent when the "Advanced Form is selected"
      *  It will be a {@link RadialBusyIcon} with a printing icon.
      */
     private BusyIcon         printingAdvancedIcon = null;

     /** The <code>BusyIcon</code> used by the JBusyComponent when the "Basic Form is selected"
      *  It will be an {@link InfiniteBusyIcon} painting the SwingX painter
      */
     private BusyIcon         printingBasicIcon    = null;
    
     /** The {@link BusyLayerUI} used by the JBusyComponent.
      *  This layer ui will swap from theses previous BusyIcon accordingly the form selected by the user
      */
     private BasicBusyLayerUI printingUI           = null;

     /** A flag that indicate or not if the sample task demo running when the component is busy was canceled or not by the user.
      *  This flag will serve to the dummy thread to stop this flag raise to <code>true</code.
      */
     private AtomicBoolean    printingCanceled     = new AtomicBoolean(false);

     /** The {@link BusyModel} used by the JBusyComponent for control it's busy state.
      *  This model will be configured accordingly the user settings from the "sample task configuration"
      *  After what, when the task is running, this model will be used by the dummy thread for simulate a job
      */
     private BusyModel        printingModel = new DefaultBusyModel() {

        /** We override the "description" method for return the "description text" set by the user.
         *  This method return directly the text stored by the {@link JTextField}
         *  If the JTextField is empty, we return <code>null</code>. In that case, the {@link BusyLayerUI} will print a xx% instead of the description
         */
        @Override
        public String getDescription() {
            String text = jTextFieldTaskDescription.getText();
            if( text != null && text.trim().equals("") ) return null;
            return text;
        }

        /** We ovveride the "cancel" method because by default this method has no "business" implementation on how cancel the underlying task.
         *  If you don't override it, this method just stop the busy state but you need to implements your treatment's interruption.
         */
        @Override
        public void cancel() {
            super.cancel();

            /** Flag cancel user request
             */
            printingCanceled.set(true);

            /** Show a warning message
             */
            JOptionPane.showMessageDialog( MainDemo.this , "Lorem Ipsum printing task canceled", "Task canceled" , JOptionPane.WARNING_MESSAGE );
        }
    };

    /**
     * ########################################
     * <code>BusyIcon</code> tabs members
     * ########################################
     */

    /** The "Square" BusyIcon to use when the "Square" form is selected.
     *  It's implementation is a {@link DefaultBusyIcon}
     */
    private DefaultBusyIcon   squareIcon = null;

    /** The "Radial" BusyIcon to use when the "Radial" form is selected.
     *  It's implementation is a {@link RadialBusyIcon}
     */
    private RadialBusyIcon    radialIcon = null;

    /** The {@link BusyModel} bound on theses icons.
     *  All user changes made from the ui (slider move, checboxes "determinate", "busy") are forwarded on this model
     */
    private BusyModel         modelIcon  = new DefaultBusyModel();

    /**
     * ########################################
     * <code>Hub</code> tabs members
     * ########################################
     */

    /** This hub manage a master model (<code>modelHub</code>) with 3 sub models (<code>modelHub1, modelHub2, modelHub3)</code>).
     *  When a sub models changes, the hub forward events and recompute the master model state.
     *  <p>
     *  Theses sub models can be updated by the users using sliders.
     *  In the otherside, the master model is bound the a <code>BusyIcon</code.
     *  In that way, the user can show how the hub maintain the master accordingly sub models states and weight
     *  <p>
     *  Note the the master models is a generic {@link BoundedRangeModel} and it's not required to use a {@link BusyModel} (but still possible)
     */
    private BusyIcon             iconHub    = null;
    private BoundedRangeModelHub hub        = null;
    private BoundedRangeModel    modelHub   = new DefaultBoundedRangeModel();
    private BoundedRangeModel    modelHub1  = null;
    private BoundedRangeModel    modelHub2  = null;
    private BoundedRangeModel    modelHub3  = null;

    /** Creates new form MainDemo */
    public MainDemo() {
        initComponents();           // Init Components from de Matisse form's design
        initBaseIcons();            // Init base icons
        initBusyIcons();            // Init busy icons

        /** Complete specific initialization of each tabs
         */
        initJBusyComponentTab();
        initBusyIconTab();
        initHubTab();

        updateBusyIconModel();
    }

    /** Load all icon's resources using classloader
     */
    private void initBaseIcons() {
        iconMonitor = new ImageIcon( getClass().getResource("/system.png") );
        iconBattery = new ImageIcon( getClass().getResource("/klaptopdaemon.png") );
        iconSearch  = new ImageIcon( getClass().getResource("/xmag.png") );
        iconJava     = new ImageIcon( getClass().getResource("/java-icon.png") );
        iconPrinter = new ImageIcon( getClass().getResource("/printer.png") );
    }

    /** Initialize all busy icons
     */
    private void initBusyIcons() {
        /** "JBusyComponent" tabs
         *  Since theses icons are used inside a {@link JBusyComponent}, they don't need to bound explicitely it's model.
         *  The JBusyComponent will bounds it's own BusyModel when it need
         */
        this.printingAdvancedIcon = new RadialBusyIcon( this.iconPrinter );
        this.printingBasicIcon = new InfiniteBusyIcon();

        /** "BusyIcon" tabs
         */
        this.squareIcon = new DefaultBusyIcon( getSelectedBaseIcon() );
        this.radialIcon = new RadialBusyIcon( getSelectedBaseIcon() );
        this.squareIcon.setModel( this.modelIcon );
        this.radialIcon.setModel( this.modelIcon );

        /** "Hub" tabs
         */
        this.iconHub =  new RadialBusyIcon( iconJava );
        this.iconHub.setModel( this.modelHub );
    }

    /** Init all properties related to the "JBusyComponent" tabs
     */
    private void initJBusyComponentTab() {
        /** Create the {@link BusyLayerUI}
         */
        this.printingUI = new BasicBusyLayerUI();

        /** Give a range to the printing model
         */
        this.printingModel.setMinimum(0);
        this.printingModel.setMaximum(1000000);

        /** Bound the LayerUI and the BusyModel to the JBusyComponent
         */
        this.jBusyComponent.setBusyLayerUI( this.printingUI );
        this.jBusyComponent.setBusyModel( this.printingModel );

        /** Init the default Task Duration to 5 seconds
         */
        this.jSpinnerTaskDuration.setValue(5);
    }

    /** Init all properties related to the "BusyIcon" tabs
     */
    private void initBusyIconTab() {
        /** Set the current selected icon to the JLabelIcon
         */
        this.jLabelIcon.setIcon( getSelectedBusyICon() );
    }

    /** Init all properties related to the "Hub" tabs
     */
    private void initHubTab() {

        /** Create the Hiub to manage the master model "modelHub"
         */
        this.hub = new BoundedRangeModelHub( this.modelHub );

        /** Define the range values of our master model
         */
        this.modelHub.setMinimum(0);
        this.modelHub.setMaximum(5000);

        /** Create 3 sub models initially with an arbitrary weight of 10
         *  Theses weights are editable by the user
         */
        this.modelHub1 = this.hub.createFragment( 10 );
        this.modelHub2 = this.hub.createFragment( 10 );
        this.modelHub3 = this.hub.createFragment( 10 );

        /** Each SubModel's range values are independant and take it directly from theses slider.
         *  All models (even the master) are independant and can have theses owns range values
         */
        this.modelHub1.setMinimum( this.jSliderModel1.getMinimum() );
        this.modelHub1.setMaximum( this.jSliderModel1.getMaximum() );
        this.modelHub2.setMinimum( this.jSliderModel2.getMinimum() );
        this.modelHub2.setMaximum( this.jSliderModel2.getMaximum() );
        this.modelHub3.setMinimum( this.jSliderModel3.getMinimum() );
        this.modelHub3.setMaximum( this.jSliderModel3.getMaximum() );

        /** Fix some default weight for sub models
         *  At this stage of initialisation, listeners are effective on spinners.
         *  Theses changes will change sub model weight
         */
        this.jSpinnerModel1.setValue(40);
        this.jSpinnerModel2.setValue(60);
        this.jSpinnerModel3.setValue(20);

        /** Set the BusyIcon to the label
         */
        this.jLabelHub.setIcon( iconHub );
    }

    /** This method return the current BusyIcon selected on the "BusyIcon" tab
     */
    private BusyIcon getSelectedBusyICon() {
        if( this.jRadioButtonRadialForm.isSelected() ) return this.radialIcon;
        if( this.jRadioButtonSquareForm.isSelected() ) return this.squareIcon;
        return null;
    }

    /** This method return the current decorated icon selected for the "BusyIcon" tab
     */
    private Icon getSelectedBaseIcon() {
        if( this.jRadioButtonIconMonitor.isSelected() ) return this.iconMonitor;
        if( this.jRadioButtonIconBattery.isSelected() ) return this.iconBattery;
        if( this.jRadioButtonIconSearch.isSelected() ) return this.iconSearch;
        return null;
    }

    /** This method update the BusyModel bound to the BusyIcon for the "BusyIcon" Tab
     *  This method is call each time the user made a change on the "BusyIcon" that change a BusyModel state
     */
    private void updateBusyIconModel() {
        this.modelIcon.setBusy( this.jCheckBoxBusy.isSelected() );
        this.modelIcon.setDeterminate( this.jCheckBoxDeterminate.isSelected() );
        this.modelIcon.setMinimum( this.jSlider.getMinimum() );
        this.modelIcon.setMaximum( this.jSlider.getMaximum() );
        this.modelIcon.setValue( this.jSlider.getValue() );
    }

    /** Chnage the BusyIcon accordingly to the selected form on the "BusyIcon" Tab
     */
    private void updateBusyIcon() {
        this.jLabelIcon.setIcon( getSelectedBusyICon() );
    }

    /** This method update the decorated icon of BusyIcons for the "BusyIcon" tab
     *  This method is call each time the user made a change of the Decoration icon.
     */
    private void updateBaseIcon() {
        this.squareIcon.setDecoratedIcon( getSelectedBaseIcon() );
        this.radialIcon.setDecoratedIcon( getSelectedBaseIcon() );
    }

    /** Convenient methods to update weights or values of sub models of the "Hub" tab.
     *  This method is call when a user-change is made
     */
    private void updateHubModels() {
        this.hub.setWeight( this.hub.indexOf( this.modelHub1 ) , (Integer)this.jSpinnerModel1.getValue() );
        this.hub.setWeight( this.hub.indexOf( this.modelHub2 ) , (Integer)this.jSpinnerModel2.getValue() );
        this.hub.setWeight( this.hub.indexOf( this.modelHub3 ) , (Integer)this.jSpinnerModel3.getValue() );
        this.modelHub1.setValue( this.jSliderModel1.getValue() );
        this.modelHub2.setValue( this.jSliderModel2.getValue() );
        this.modelHub3.setValue( this.jSliderModel3.getValue() );

        this.updateHubHelps( this.modelHub1 , this.jLabelHelp1 );
        this.updateHubHelps( this.modelHub2 , this.jLabelHelp2 );
        this.updateHubHelps( this.modelHub3 , this.jLabelHelp3 );
    }

    /** This method compute the help label for a sub model of the "Hub" tab.
     *  This method give a translation of the sub model weight on a % inside the master model.
     */
    private void updateHubHelps(BoundedRangeModel subModel , JLabel component) {
        DecimalFormat df = new DecimalFormat("#00.00");
        float prc_current = (float)subModel.getValue() / (float)( subModel.getMaximum() - subModel.getMinimum() ) * 100f;
        float prc_total   = this.hub.getWeight( this.hub.indexOf( subModel ) ) / this.hub.getTotalWeight() * 100f;
        component.setText( df.format(prc_current) + " % of " + df.format(prc_total) + " %" );
    }

    /**
     * Main demonstration application : Init this MainDemo Panel and wrap it inside a JFrame centered on the screen
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    LookAndFeel laf = (LookAndFeel)Class.forName( UIManager.getSystemLookAndFeelClassName() ).newInstance();
                    UIManager.setLookAndFeel( laf );
                }
                catch(Exception e) {
                    e.printStackTrace();
                }

                JFrame frame = new JFrame();
                frame.setTitle("JBusyComponent Demo");
                frame.getContentPane().add( new MainDemo() );
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setDefaultCloseOperation( frame.EXIT_ON_CLOSE );
                // frame.setIconImage( Toolkit.getDefaultToolkit().createImage( getClass().getResource("/busy-logo.png") ) );
                frame.setVisible(true);
            }
        });
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupIcon = new javax.swing.ButtonGroup();
        buttonGroupIconForm = new javax.swing.ButtonGroup();
        jScrollPaneLoremIpsum = new javax.swing.JScrollPane();
        jTextAreaLoremIpsum = new javax.swing.JTextArea();
        buttonGroupTaskForm = new javax.swing.ButtonGroup();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelBusyDemo = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jButtonTaskExecute = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jSpinnerTaskDuration = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jCheckBoxTaskCancellable = new javax.swing.JCheckBox();
        jCheckBoxTaskDeterminate = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jTextFieldTaskDescription = new javax.swing.JTextField();
        jRadioButtonTaskBasicForm = new javax.swing.JRadioButton();
        jRadioButtonTaskAdvancedForm = new javax.swing.JRadioButton();
        jCheckBoxShowRemainingTime = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jBusyComponent = new org.divxdede.swing.busy.JBusyComponent();
        jPanelIconDemo = new javax.swing.JPanel();
        jXTitledPanelIconDemo = new org.jdesktop.swingx.JXTitledPanel();
        jRadioButtonIconMonitor = new javax.swing.JRadioButton();
        jRadioButtonIconBattery = new javax.swing.JRadioButton();
        jRadioButtonIconSearch = new javax.swing.JRadioButton();
        jLabelIcon = new javax.swing.JLabel();
        jCheckBoxBusy = new javax.swing.JCheckBox();
        jCheckBoxDeterminate = new javax.swing.JCheckBox();
        jSlider = new javax.swing.JSlider();
        jRadioButtonRadialForm = new javax.swing.JRadioButton();
        jRadioButtonSquareForm = new javax.swing.JRadioButton();
        jPanelHub = new javax.swing.JPanel();
        jLabelHub = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSliderModel1 = new javax.swing.JSlider();
        jSpinnerModel1 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jLabelHelp1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jSliderModel2 = new javax.swing.JSlider();
        jSpinnerModel2 = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jLabelHelp2 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jSliderModel3 = new javax.swing.JSlider();
        jSpinnerModel3 = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        jLabelHelp3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jXHyperlinkCrystal = new org.jdesktop.swingx.JXHyperlink();
        jLabel9 = new javax.swing.JLabel();
        jXHyperlinkJbusyComponent = new org.jdesktop.swingx.JXHyperlink();

        jTextAreaLoremIpsum.setColumns(20);
        jTextAreaLoremIpsum.setFont(new java.awt.Font("Arial", 0, 18));
        jTextAreaLoremIpsum.setLineWrap(true);
        jTextAreaLoremIpsum.setRows(5);
        jTextAreaLoremIpsum.setText("Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");
        jScrollPaneLoremIpsum.setViewportView(jTextAreaLoremIpsum);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Sample task configuration"));

        jButtonTaskExecute.setText("Execute");
        jButtonTaskExecute.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonTaskExecuteActionPerformed(evt);
            }
        });

        jLabel2.setBackground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Task duration");

        jLabel4.setText("seconds");

        jCheckBoxTaskCancellable.setText("Cancellable");
        jCheckBoxTaskCancellable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxTaskCancellableActionPerformed(evt);
            }
        });

        jCheckBoxTaskDeterminate.setSelected(true);
        jCheckBoxTaskDeterminate.setText("Determinate");
        jCheckBoxTaskDeterminate.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxTaskDeterminateStateChanged(evt);
            }
        });

        jLabel6.setText("Description (can be empty)");

        jTextFieldTaskDescription.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldTaskDescriptionActionPerformed(evt);
            }
        });

        buttonGroupTaskForm.add(jRadioButtonTaskBasicForm);
        jRadioButtonTaskBasicForm.setSelected(true);
        jRadioButtonTaskBasicForm.setText("Basic form");

        buttonGroupTaskForm.add(jRadioButtonTaskAdvancedForm);
        jRadioButtonTaskAdvancedForm.setText("Advanced from");
        jRadioButtonTaskAdvancedForm.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

        jCheckBoxShowRemainingTime.setText(" Remaining Time");
        jCheckBoxShowRemainingTime.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jCheckBoxShowRemainingTime.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        jButton1.setText("Clear");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jCheckBoxTaskDeterminate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 44, Short.MAX_VALUE)
                        .addComponent(jCheckBoxShowRemainingTime))
                    .addComponent(jButtonTaskExecute, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jRadioButtonTaskBasicForm)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 58, Short.MAX_VALUE)
                        .addComponent(jRadioButtonTaskAdvancedForm))
                    .addComponent(jCheckBoxTaskCancellable)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinnerTaskDuration, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                        .addComponent(jTextFieldTaskDescription, javax.swing.GroupLayout.DEFAULT_SIZE, 169, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 103, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jSpinnerTaskDuration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jCheckBoxTaskCancellable)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxTaskDeterminate)
                    .addComponent(jCheckBoxShowRemainingTime))
                .addGap(13, 13, 13)
                .addComponent(jLabel6)
                .addGap(2, 2, 2)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldTaskDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonTaskAdvancedForm)
                    .addComponent(jRadioButtonTaskBasicForm))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonTaskExecute)
                .addContainerGap())
        );

        jBusyComponent.setView(jScrollPaneLoremIpsum);

        javax.swing.GroupLayout jPanelBusyDemoLayout = new javax.swing.GroupLayout(jPanelBusyDemo);
        jPanelBusyDemo.setLayout(jPanelBusyDemoLayout);
        jPanelBusyDemoLayout.setHorizontalGroup(
            jPanelBusyDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBusyDemoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jBusyComponent, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanelBusyDemoLayout.setVerticalGroup(
            jPanelBusyDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelBusyDemoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelBusyDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jBusyComponent, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane.addTab("JBusyComponent", jPanelBusyDemo);

        jXTitledPanelIconDemo.setTitle("BusyIcon Demo");

        buttonGroupIcon.add(jRadioButtonIconMonitor);
        jRadioButtonIconMonitor.setText("Icon \"Monitor\"");
        jRadioButtonIconMonitor.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRadioButtonIconMonitorItemStateChanged(evt);
            }
        });

        buttonGroupIcon.add(jRadioButtonIconBattery);
        jRadioButtonIconBattery.setSelected(true);
        jRadioButtonIconBattery.setText("Icon \"Battery\"");
        jRadioButtonIconBattery.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonIconBatteryStateChanged(evt);
            }
        });

        buttonGroupIcon.add(jRadioButtonIconSearch);
        jRadioButtonIconSearch.setText("Icon \"Search\"");
        jRadioButtonIconSearch.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonIconSearchStateChanged(evt);
            }
        });

        jLabelIcon.setBackground(new java.awt.Color(255, 255, 255));
        jLabelIcon.setFont(new java.awt.Font("Tahoma", 0, 18));
        jLabelIcon.setForeground(new java.awt.Color(0, 0, 255));
        jLabelIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelIcon.setText("A simple JLabel with a BusyIcon");
        jLabelIcon.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabelIcon.setOpaque(true);

        jCheckBoxBusy.setSelected(true);
        jCheckBoxBusy.setText("Busy");
        jCheckBoxBusy.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxBusyStateChanged(evt);
            }
        });

        jCheckBoxDeterminate.setSelected(true);
        jCheckBoxDeterminate.setText("Determinate");
        jCheckBoxDeterminate.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxDeterminateStateChanged(evt);
            }
        });

        jSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderStateChanged(evt);
            }
        });

        buttonGroupIconForm.add(jRadioButtonRadialForm);
        jRadioButtonRadialForm.setSelected(true);
        jRadioButtonRadialForm.setText("Radial Form");
        jRadioButtonRadialForm.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jRadioButtonRadialForm.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonRadialFormStateChanged(evt);
            }
        });

        buttonGroupIconForm.add(jRadioButtonSquareForm);
        jRadioButtonSquareForm.setText("Square Form");
        jRadioButtonSquareForm.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        jRadioButtonSquareForm.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonSquareFormStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanelIconDemoLayout = new javax.swing.GroupLayout(jPanelIconDemo);
        jPanelIconDemo.setLayout(jPanelIconDemoLayout);
        jPanelIconDemoLayout.setHorizontalGroup(
            jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelIconDemoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelIconDemoLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 666, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanelIconDemoLayout.createSequentialGroup()
                        .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabelIcon, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelIconDemoLayout.createSequentialGroup()
                                .addComponent(jRadioButtonIconMonitor)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 232, Short.MAX_VALUE)
                                .addComponent(jRadioButtonIconBattery)
                                .addGap(167, 167, 167)
                                .addComponent(jRadioButtonIconSearch))
                            .addComponent(jXTitledPanelIconDemo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
                            .addGroup(jPanelIconDemoLayout.createSequentialGroup()
                                .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jCheckBoxDeterminate)
                                    .addComponent(jCheckBoxBusy))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 504, Short.MAX_VALUE)
                                .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jRadioButtonRadialForm)
                                    .addComponent(jRadioButtonSquareForm))))
                        .addGap(10, 10, 10))))
        );
        jPanelIconDemoLayout.setVerticalGroup(
            jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelIconDemoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jXTitledPanelIconDemo, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonIconMonitor)
                    .addComponent(jRadioButtonIconSearch)
                    .addComponent(jRadioButtonIconBattery))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabelIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxBusy)
                    .addComponent(jRadioButtonRadialForm))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelIconDemoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxDeterminate)
                    .addComponent(jRadioButtonSquareForm))
                .addContainerGap())
        );

        jTabbedPane.addTab("BusyIcon", jPanelIconDemo);

        jLabelHub.setBackground(new java.awt.Color(255, 255, 255));
        jLabelHub.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabelHub.setText("<html>a <code>BoundedRangeModelHub</code> compute a <b>master</b> model from any changes of sub-models.<p>Each sub-models have a <b>weight</b> that determine theses proportions of the master model.<br>Theses models are completely <b>independents</b> from each others.</html>");
        jLabelHub.setOpaque(true);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel1.setOpaque(false);

        jSliderModel1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderModel1StateChanged(evt);
            }
        });

        jSpinnerModel1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerModel1StateChanged(evt);
            }
        });

        jLabel3.setText("#1 model weight");

        jLabelHelp1.setForeground(new java.awt.Color(0, 0, 255));
        jLabelHelp1.setText(" = xxx %");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerModel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jSliderModel1, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                        .addContainerGap(149, Short.MAX_VALUE))
                    .addComponent(jLabelHelp1, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSliderModel1, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerModel1, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                            .addComponent(jLabelHelp1))))
                .addContainerGap())
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setOpaque(false);

        jSliderModel2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderModel2StateChanged(evt);
            }
        });

        jSpinnerModel2.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerModel2StateChanged(evt);
            }
        });

        jLabel5.setText("#2 model weight");

        jLabelHelp2.setForeground(new java.awt.Color(0, 0, 255));
        jLabelHelp2.setText(" = xxx %");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerModel2, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderModel2, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelHelp2, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSliderModel2, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerModel2, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                            .addComponent(jLabelHelp2))))
                .addContainerGap())
        );

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel3.setOpaque(false);

        jSliderModel3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSliderModel3StateChanged(evt);
            }
        });

        jSpinnerModel3.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinnerModel3StateChanged(evt);
            }
        });

        jLabel7.setText("#3 model weight");

        jLabelHelp3.setForeground(new java.awt.Color(0, 0, 255));
        jLabelHelp3.setText(" = xxx %");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSpinnerModel3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSliderModel3, javax.swing.GroupLayout.DEFAULT_SIZE, 308, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabelHelp3, javax.swing.GroupLayout.PREFERRED_SIZE, 145, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jSliderModel3, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(11, 11, 11)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jSpinnerModel3, javax.swing.GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
                            .addComponent(jLabelHelp3))))
                .addGap(11, 11, 11))
        );

        javax.swing.GroupLayout jPanelHubLayout = new javax.swing.GroupLayout(jPanelHub);
        jPanelHub.setLayout(jPanelHubLayout);
        jPanelHubLayout.setHorizontalGroup(
            jPanelHubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHubLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelHubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelHub, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 676, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanelHubLayout.setVerticalGroup(
            jPanelHubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelHubLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabelHub, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        jTabbedPane.addTab("Hub", jPanelHub);

        jLabel1.setText("Crystal diamonds icons are featured by");

        jXHyperlinkCrystal.setText("http://www.paolocampitelli.com/kde-icons/");
        jXHyperlinkCrystal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXHyperlinkCrystalActionPerformed(evt);
            }
        });

        jLabel9.setBackground(new java.awt.Color(255, 255, 255));
        jLabel9.setFont(new java.awt.Font("Arial", 1, 18));
        jLabel9.setForeground(new java.awt.Color(0, 0, 255));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("LGPL JBusyComponent Library 1.2 demonstration");
        jLabel9.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel9.setOpaque(true);

        jXHyperlinkJbusyComponent.setText("http://code.google.com/p/jbusycomponent/");
        jXHyperlinkJbusyComponent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jXHyperlinkJbusyComponentActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
                    .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 701, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jXHyperlinkCrystal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 91, Short.MAX_VALUE)
                        .addComponent(jXHyperlinkJbusyComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jXHyperlinkCrystal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jXHyperlinkJbusyComponent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jXHyperlinkJbusyComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXHyperlinkJbusyComponentActionPerformed
        try {
            Desktop.getDesktop().browse( new URL("http://code.google.com/p/jbusycomponent/").toURI() );
        }
        catch(Exception e) {
        }
    }//GEN-LAST:event_jXHyperlinkJbusyComponentActionPerformed

    private void jXHyperlinkCrystalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jXHyperlinkCrystalActionPerformed
        try {
            Desktop.getDesktop().browse( new URL("http://www.paolocampitelli.com/kde-icons/").toURI() );
        }
        catch(Exception e) {
        }
    }//GEN-LAST:event_jXHyperlinkCrystalActionPerformed

    private void jSpinnerModel3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerModel3StateChanged
        updateHubModels();
}//GEN-LAST:event_jSpinnerModel3StateChanged

    private void jSliderModel3StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderModel3StateChanged
        updateHubModels();
}//GEN-LAST:event_jSliderModel3StateChanged

    private void jSpinnerModel2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerModel2StateChanged
        updateHubModels();
}//GEN-LAST:event_jSpinnerModel2StateChanged

    private void jSliderModel2StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderModel2StateChanged
        updateHubModels();
}//GEN-LAST:event_jSliderModel2StateChanged

    private void jSpinnerModel1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerModel1StateChanged
        updateHubModels();
}//GEN-LAST:event_jSpinnerModel1StateChanged

    private void jSliderModel1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderModel1StateChanged
        updateHubModels();
}//GEN-LAST:event_jSliderModel1StateChanged

    private void jRadioButtonSquareFormStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonSquareFormStateChanged
        updateBusyIcon();
}//GEN-LAST:event_jRadioButtonSquareFormStateChanged

    private void jRadioButtonRadialFormStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonRadialFormStateChanged
        updateBusyIcon();
}//GEN-LAST:event_jRadioButtonRadialFormStateChanged

    private void jSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSliderStateChanged
        updateBusyIconModel();
}//GEN-LAST:event_jSliderStateChanged

    private void jCheckBoxDeterminateStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxDeterminateStateChanged
        updateBusyIconModel();
}//GEN-LAST:event_jCheckBoxDeterminateStateChanged

    private void jCheckBoxBusyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxBusyStateChanged
        updateBusyIconModel();
}//GEN-LAST:event_jCheckBoxBusyStateChanged

    private void jRadioButtonIconSearchStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonIconSearchStateChanged
        updateBaseIcon();
}//GEN-LAST:event_jRadioButtonIconSearchStateChanged

    private void jRadioButtonIconBatteryStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonIconBatteryStateChanged
        updateBaseIcon();
}//GEN-LAST:event_jRadioButtonIconBatteryStateChanged

    private void jRadioButtonIconMonitorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRadioButtonIconMonitorItemStateChanged
        updateBaseIcon();
}//GEN-LAST:event_jRadioButtonIconMonitorItemStateChanged

    private void jTextFieldTaskDescriptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldTaskDescriptionActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jTextFieldTaskDescriptionActionPerformed

    private void jCheckBoxTaskCancellableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxTaskCancellableActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_jCheckBoxTaskCancellableActionPerformed

    private void jButtonTaskExecuteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTaskExecuteActionPerformed
        this.printingModel.setCancellable( this.jCheckBoxTaskCancellable.isSelected() );
        this.printingModel.setDeterminate( this.jCheckBoxTaskDeterminate.isSelected() );

        if( jRadioButtonTaskBasicForm.isSelected() )
            this.printingUI.setBusyIcon( this.printingBasicIcon );
        else {
            this.printingUI.setBusyIcon( this.printingAdvancedIcon );
        }

        this.printingUI.setRemainingTimeVisible( this.jCheckBoxShowRemainingTime.isSelected() );
        this.jButtonTaskExecute.setEnabled(false);
        this.printingModel.setBusy(true);

        Runnable doRun = new Runnable() {
            public void run() {
                printingCanceled.set(false);

                int length  = printingModel.getMaximum() - printingModel.getMinimum();
                float step  = (float)length / ((Integer)jSpinnerTaskDuration.getValue()).floatValue();
                step  = step / 10f; // 100ms refresh delay

                printingModel.setValue( printingModel.getMinimum() );
                while( printingCanceled.get() == false && printingModel.getValue() < printingModel.getMaximum() ) {
                    try {
                        Thread.sleep(100);
                    } catch(InterruptedException ie) {}
                    printingModel.setValue( printingModel.getValue() + (int)step );
                }
                printingModel.setBusy(false);
                jButtonTaskExecute.setEnabled(true);
            };
        };
        Thread t = new Thread(doRun);
        t.start();
    }//GEN-LAST:event_jButtonTaskExecuteActionPerformed

    private void jCheckBoxTaskDeterminateStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxTaskDeterminateStateChanged
        this.jCheckBoxShowRemainingTime.setEnabled( this.jCheckBoxTaskDeterminate.isSelected() );
        if( ! this.jCheckBoxTaskDeterminate.isSelected() )
            this.jCheckBoxShowRemainingTime.setSelected(false);
    }//GEN-LAST:event_jCheckBoxTaskDeterminateStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupIcon;
    private javax.swing.ButtonGroup buttonGroupIconForm;
    private javax.swing.ButtonGroup buttonGroupTaskForm;
    private org.divxdede.swing.busy.JBusyComponent jBusyComponent;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonTaskExecute;
    private javax.swing.JCheckBox jCheckBoxBusy;
    private javax.swing.JCheckBox jCheckBoxDeterminate;
    private javax.swing.JCheckBox jCheckBoxShowRemainingTime;
    private javax.swing.JCheckBox jCheckBoxTaskCancellable;
    private javax.swing.JCheckBox jCheckBoxTaskDeterminate;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JLabel jLabelHelp1;
    private javax.swing.JLabel jLabelHelp2;
    private javax.swing.JLabel jLabelHelp3;
    private javax.swing.JLabel jLabelHub;
    private javax.swing.JLabel jLabelIcon;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanelBusyDemo;
    private javax.swing.JPanel jPanelHub;
    private javax.swing.JPanel jPanelIconDemo;
    private javax.swing.JRadioButton jRadioButtonIconBattery;
    private javax.swing.JRadioButton jRadioButtonIconMonitor;
    private javax.swing.JRadioButton jRadioButtonIconSearch;
    private javax.swing.JRadioButton jRadioButtonRadialForm;
    private javax.swing.JRadioButton jRadioButtonSquareForm;
    private javax.swing.JRadioButton jRadioButtonTaskAdvancedForm;
    private javax.swing.JRadioButton jRadioButtonTaskBasicForm;
    private javax.swing.JScrollPane jScrollPaneLoremIpsum;
    private javax.swing.JSlider jSlider;
    private javax.swing.JSlider jSliderModel1;
    private javax.swing.JSlider jSliderModel2;
    private javax.swing.JSlider jSliderModel3;
    private javax.swing.JSpinner jSpinnerModel1;
    private javax.swing.JSpinner jSpinnerModel2;
    private javax.swing.JSpinner jSpinnerModel3;
    private javax.swing.JSpinner jSpinnerTaskDuration;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTextArea jTextAreaLoremIpsum;
    private javax.swing.JTextField jTextFieldTaskDescription;
    private org.jdesktop.swingx.JXHyperlink jXHyperlinkCrystal;
    private org.jdesktop.swingx.JXHyperlink jXHyperlinkJbusyComponent;
    private org.jdesktop.swingx.JXTitledPanel jXTitledPanelIconDemo;
    // End of variables declaration//GEN-END:variables

}
