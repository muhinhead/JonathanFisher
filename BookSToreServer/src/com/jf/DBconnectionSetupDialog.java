/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jf;

import com.jf.util.PopupDialog;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Locale;
import java.util.Properties;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author nick
 */
class DBconnectionSetupDialog extends PopupDialog {

    private JButton okBtn;
    private AbstractAction okAction;
    private JPanel passwdCardPanel;
    private CardLayout cl;
    private JPasswordField passwdPwdF;
    private String pwdlbl;
    private JTextField passwdTF;
    private String tflbl;
    private JCheckBox showPwdCB;
    private JTextField dbDriverNameTF;
    private JTextField connectionStringTF;
    private JTextField dbLoginTF;
    private AbstractAction cancelAction;
    private JButton cancelBtn;
    private JButton testBtn;

    public DBconnectionSetupDialog(Properties props) {
        super(null, "DB Сonnection Setup", props);
    }

    @Override
    protected Color getHeaderBackground() {
        return BookStoreServer.unformColor;
    }

    @Override
    protected void fillContent() {
        super.fillContent();
        getContentPane().add(getMainPanel());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(okBtn = new JButton(okAction = new AbstractAction("Save") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                Properties props = (Properties) getObject();
                props.setProperty("dbDriverName", dbDriverNameTF.getText());
                props.setProperty("dbConnection", connectionStringTF.getText());
                props.setProperty("dbUser", dbLoginTF.getText());
                props.setProperty("dbPassword", passwdTF.getText());
                try {
                    if (props != null) {
                        props.store(new FileOutputStream(BookStoreServer.PROPERTYFILENAME),
                                "-----------------------");
                    }
                    dispose();
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(DBconnectionSetupDialog.this,
                            e.getMessage(), "Retention of properties failed", JOptionPane.ERROR_MESSAGE);
                }
            }
        }));
        btnPanel.add(cancelBtn = new JButton(cancelAction = new AbstractAction("Cancel") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                dispose();
            }
        }));
        getRootPane().setDefaultButton(testBtn);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    @Override
    public void freeResources() {
        okBtn.setAction(null);
        okAction = null;
    }

    private JPanel getGridPanel(JComponent comp, int cols) {
        JPanel gridPanel = new JPanel(new GridLayout(1, cols));
        gridPanel.add(comp);
        for (int i = 1; i < cols; i++) {
            gridPanel.add(new JPanel());
        }
        return gridPanel;
    }

    private JPanel getGridPanel(int cols, JComponent comp) {
        JPanel gridPanel = new JPanel(new GridLayout(1, cols));
        for (int i = 1; i < cols; i++) {
            gridPanel.add(new JPanel());
        }
        gridPanel.add(comp);
        return gridPanel;
    }

    private JPanel getGridPanel(JComponent comp, JComponent comp1) {
        JPanel gridPanel = new JPanel(new GridLayout(1, 2));
        gridPanel.add(comp);
        gridPanel.add(comp1);
        return gridPanel;
    }

    private JPanel getMainPanel() {
        Properties props = (Properties) getObject();
            File propFile = new File(BookStoreServer.PROPERTYFILENAME);
            try {
                if (propFile.exists()) {
                    props.load(new FileInputStream(propFile));
//                    DbConnection.setProps(props);
                    System.out.println("Properties loaded from " + BookStoreServer.PROPERTYFILENAME);
                }
                System.out.println("\nPress Ctrl+C to interrupt");
            } catch (IOException ioe) {
                BookStoreServer.log(ioe);
            }
        
        final int ROWS = 5;
        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel labelPanel = new JPanel(new GridLayout(ROWS, 1, 5, 5));
        JPanel centerPanel = new JPanel(new GridLayout(ROWS, 1, 5, 5));
        JPanel upPanel = new JPanel(new BorderLayout());
        upPanel.add(labelPanel, BorderLayout.WEST);
        upPanel.add(centerPanel, BorderLayout.CENTER);
        upPanel.add(new JPanel(), BorderLayout.EAST);
        mainPanel.add(upPanel, BorderLayout.NORTH);
//        upPanel.setBorder(BorderFactory.createEtchedBorder());

        labelPanel.add(new JLabel("DB Driver Name:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("  Connection String:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("DB Login:", SwingConstants.RIGHT));
        labelPanel.add(new JLabel("DB Password:", SwingConstants.RIGHT));
        labelPanel.add(new JPanel());

        centerPanel.add(dbDriverNameTF = new JTextField(
                props.getProperty("dbDriverName",
                "com.mysql.jdbc.Driver")));
        centerPanel.add(connectionStringTF = new JTextField(props.getProperty("dbConnection",
                "jdbc:mysql://ec2-54-226-3-180.compute-1.amazonaws.com/aibcontact1?characterEncoding=UTF8"), 30));
        centerPanel.add(getGridPanel(dbLoginTF = new JTextField(props.getProperty("dbUser", "aib")), 2));
        centerPanel.add(getGridPanel(passwdCardPanel = new JPanel(cl = new CardLayout()), showPwdCB = new JCheckBox("show")));
        centerPanel.add(getGridPanel(3, testBtn = new JButton(new AbstractAction("Test connection") {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    Locale.setDefault(Locale.ENGLISH);
                    DriverManager.registerDriver(
                            (java.sql.Driver) Class.forName(
                            dbDriverNameTF.getText()).newInstance());
                    Connection connection = DriverManager.getConnection(
                            connectionStringTF.getText(),
                            dbLoginTF.getText(), passwdTF.getText());
                    JOptionPane.showMessageDialog(DBconnectionSetupDialog.this,
                            "Connection succeess", "Ok!", JOptionPane.INFORMATION_MESSAGE);
                    connection.close();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(DBconnectionSetupDialog.this,
                            e.getMessage(), "Connection fault", JOptionPane.ERROR_MESSAGE);
                }
            }
        })));
        String pwd = props.getProperty("dbPassword", "qwerty");
        passwdCardPanel.add(passwdPwdF = new JPasswordField(pwd), pwdlbl = "JPasswordField");
        passwdCardPanel.add(passwdTF = new JTextField(pwd), tflbl = "JTextField");
        showPwdCB.setAction(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPwdCB.isSelected()) {
                    passwdTF.setText(new String(passwdPwdF.getPassword()));
                } else {
                    passwdPwdF.setText(passwdTF.getText());
                }
                cl.show(passwdCardPanel, (showPwdCB.isSelected() ? tflbl : pwdlbl));
            }
        });

        return mainPanel;
    }
}
