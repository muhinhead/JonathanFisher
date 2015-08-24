package com.jf;

import com.jf.remote.IMessageSender;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.rmi.Naming;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.jf.rmi.ExchangeFactory;
import com.jf.util.PopupDialog;

/**
 *
 * @author Nick Mukhin
 */
public class ConfigEditor extends PopupDialog {

    private JTextField imageDirField;
    private JTextField addressField;
    private JSpinner portSpinner;
    private JButton testBtn;
    private JButton okBtn;
    private JButton cancelBtn;
    private AbstractAction testAction;
    private AbstractAction okAction;
    private AbstractAction cancelAction;
    private JButton chooseFldrBtn;
    private AbstractAction chooseFldrAct;
    private JRadioButton jdbcRB;
    private JTextField dbConnectionField;
    private JTextField dbDriverField;
    private JTextField dbUserField;
    private JPasswordField dbPasswordField;

    private static String protocol;

    /**
     * @return the protocol
     */
    public static String getProtocol() {
        return protocol;
    }
    private JRadioButton appServerRB;
    
    public ConfigEditor(String title, Object obj) {
        super(null, title, obj);
    }

    @Override
    protected Color getHeaderBackground() {
        return BookStore.HDR_COLOR;
    }

    protected void fillContent() {
        super.fillContent();
        String theme = BookStore.readProperty("LookAndFeel",
                "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        try {
            UIManager.setLookAndFeel(theme);
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception ex) {
        }
        
        BookStore.setWindowIcon(this, "bs.png");
        
//        JComponent[] comps = (JComponent[]) getObject();
//        imageDirField = (JTextField) comps[0];
//        addressField = (JTextField) comps[1];
//        portSpinner = (JSpinner) comps[2];
//
//        JPanel upperPanel = new JPanel(new BorderLayout(5, 5));
//        JPanel labelPanel = new JPanel(new GridLayout(3, 1, 5, 5));
//        JPanel editPanel = new JPanel(new GridLayout(3, 1, 5, 5));
//
//        labelPanel.add(new JLabel(" Default document's folder:", SwingConstants.RIGHT));
//        labelPanel.add(new JLabel("Server's IP or name:", SwingConstants.RIGHT));
//        labelPanel.add(new JLabel("Server's port:", SwingConstants.RIGHT));
//
//        JPanel imageFolderPanel = new JPanel(new BorderLayout(5, 5));
//        imageFolderPanel.add(imageDirField, BorderLayout.CENTER);
//        imageFolderPanel.add(chooseFldrBtn = new JButton(chooseFldrAct = new AbstractAction("...") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                chooseFolder();
//            }
//        }), BorderLayout.EAST);
//        editPanel.add(imageFolderPanel);
//        editPanel.add(addressField);
//        JPanel portPanel = new JPanel(new BorderLayout());
//        portPanel.add(portSpinner, BorderLayout.WEST);
//        editPanel.add(portPanel);
//
//        upperPanel.add(editPanel, BorderLayout.CENTER);
//        upperPanel.add(labelPanel, BorderLayout.WEST);
//        upperPanel.add(new JPanel(), BorderLayout.EAST);
//
//        JPanel btnPanel = new JPanel();
//        btnPanel.add(testBtn = new JButton(testAction = new AbstractAction("Test connection") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String serverIP = addressField.getText() + ":" + portSpinner.getValue();
//                try {
//                    IMessageSender exch = (IMessageSender) Naming.lookup("rmi://" + serverIP + "/AIBserver");
//                    JOptionPane.showMessageDialog(null, "Connection successful", "Ok!", JOptionPane.INFORMATION_MESSAGE);
//                } catch (Exception ex) {
//                    JOptionPane.showMessageDialog(null, "Connection refused", "Error:", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        }));
//        btnPanel.add(okBtn = new JButton(okAction = new AbstractAction("Ok") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                dispose();
//            }
//        }) {
//        });
//        btnPanel.add(cancelBtn = new JButton(cancelAction = new AbstractAction("Cancel") {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                addressField.setText("");
//                portSpinner.setValue(new Integer(0));
//                dispose();
//            }
//        }));
//
//        JPanel centerPanel = new JPanel(new BorderLayout());
//        centerPanel.add(upperPanel, BorderLayout.NORTH);
//        getContentPane().add(centerPanel, BorderLayout.CENTER);
//        getContentPane().add(btnPanel, BorderLayout.SOUTH);
//        getRootPane().setDefaultButton(okBtn);
        JComponent[] comps = (JComponent[]) getObject();
        imageDirField = (JTextField) comps[0];
        addressField = (JTextField) comps[1];
        portSpinner = (JSpinner) comps[2];
        dbConnectionField = (JTextField) comps[3];
        dbDriverField = (JTextField) comps[4];
        dbUserField = (JTextField) comps[5];
        dbPasswordField = (JPasswordField) comps[6];

        JPanel upperPanel = new JPanel(new BorderLayout(5, 5));
//        upperPanel.setBorder(BorderFactory.createEtchedBorder());
        JPanel labelPanel = new JPanel(new GridLayout(9, 1, 5, 5));
        JPanel editPanel = new JPanel(new GridLayout(9, 1, 5, 5));

        labelPanel.add(new JLabel(" Default document's folder:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("Connection through:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("Server's IP or name:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("Server's port:", SwingConstants.RIGHT));
        labelPanel.add(new JPanel());
        labelPanel.add(new JLabel("DB connection:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("JDBC driver:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("DB user:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("DB password:", SwingConstants.RIGHT));

        JPanel imageFolderPanel = new JPanel(new BorderLayout(5, 5));
        imageFolderPanel.add(imageDirField, BorderLayout.CENTER);
        imageFolderPanel.add(chooseFldrBtn = new JButton(chooseFldrAct = new AbstractAction("...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseFolder();
            }
        }), BorderLayout.EAST);
        JPanel radioBtnPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        radioBtnPanel.add(appServerRB = new JRadioButton("Application server"));
        radioBtnPanel.add(jdbcRB = new JRadioButton("JDBC"));
        ButtonGroup rbGrp = new ButtonGroup();
        rbGrp.add(appServerRB);
        rbGrp.add(jdbcRB);

        editPanel.add(imageFolderPanel);
        editPanel.add(radioBtnPanel);
        editPanel.add(addressField);
        JPanel portPanel = new JPanel(new BorderLayout());
        portPanel.add(portSpinner, BorderLayout.WEST);
        editPanel.add(portPanel);
//        editPanel.add(new JSeparator());
        editPanel.add(new JPanel());
        editPanel.add(dbConnectionField);
        editPanel.add(dbDriverField);
        editPanel.add(dbUserField);
        editPanel.add(dbPasswordField);

        upperPanel.add(editPanel, BorderLayout.CENTER);
        upperPanel.add(labelPanel, BorderLayout.WEST);
        upperPanel.add(new JPanel(), BorderLayout.EAST);

        JPanel btnPanel = new JPanel();
        btnPanel.add(testBtn = new JButton(testAction = new AbstractAction("Test connection") {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (appServerRB.isSelected()) {
                        String serverIP = addressField.getText() + ":" + portSpinner.getValue();
                        IMessageSender exch = (IMessageSender) Naming.lookup("rmi://" + serverIP + "/AIBserver");
                        protocol = "rmi";
                    } else {
                        IMessageSender exch = ExchangeFactory.createJDBCexchanger(
                                dbDriverField.getText(),
                                dbConnectionField.getText(),
                                dbUserField.getText(),
                                new String(dbPasswordField.getPassword()));
                        protocol = "jdbc";
                    }
                    JOptionPane.showMessageDialog(null, "Connection succeeded", "OK!", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection refused:", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        btnPanel.add(okBtn = new JButton(okAction = new AbstractAction("Ok") {
            @Override
            public void actionPerformed(ActionEvent e) {
                protocol = appServerRB.isSelected() ? "rmi" : "jdbc";
                dispose();
            }
        }) {
        });
        btnPanel.add(cancelBtn = new JButton(cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent e) {
                addressField.setText("");
                portSpinner.setValue(new Integer(0));
                protocol = "";
                dispose();
            }
        }));

        appServerRB.addChangeListener(getRbChangeListener());
        jdbcRB.addChangeListener(getRbChangeListener());

        if (BookStore.protocol.equalsIgnoreCase("rmi")) {
            appServerRB.setSelected(true);
        } else {
            jdbcRB.setSelected(true);
        }

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(upperPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okBtn);
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        chooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }

            @Override
            public String getDescription() {
                return "*.*";
            }
        });

        chooser.setDialogTitle("Choose folder to take attachment from");
        chooser.setApproveButtonText("Choose");
        int retVal = chooser.showOpenDialog(null);

        if (retVal == JFileChooser.APPROVE_OPTION) {
            imageDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    @Override
    public void freeResources() {
        testBtn.removeActionListener(testAction);
        testAction = null;
        okBtn.removeActionListener(okAction);
        okAction = null;
        cancelBtn.removeActionListener(cancelAction);
        cancelAction = null;
        chooseFldrBtn.removeActionListener(chooseFldrAct);
        chooseFldrAct = null;
    }

    private ChangeListener getRbChangeListener() {
        return new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                boolean rmiSelected = appServerRB.isSelected();
                addressField.setEnabled(rmiSelected);
                portSpinner.setEnabled(rmiSelected);
                dbConnectionField.setEnabled(!rmiSelected);
                dbDriverField.setEnabled(!rmiSelected);
                dbUserField.setEnabled(!rmiSelected);
                dbPasswordField.setEnabled(!rmiSelected);
            }
        };
    }
    
}
