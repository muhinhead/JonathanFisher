/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jf;

import com.jf.orm.User;
import com.jf.orm.dbobject.DbObject;
import com.jf.remote.IMessageSender;
import com.jf.rmi.DbConnection;
import com.jf.rmi.ExchangeFactory;
import com.jidesoft.plaf.LookAndFeelFactory;
import java.awt.Color;
import java.awt.Image;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author Nick Mukhin
 */
public class BookStore {

    private static final String version = "0.1";
    private static Logger logger = null;
    private static FileHandler fh;
    private static Properties props;
    private static final String APPNAME = "BookStore";
    private static final String SERVERNAME = APPNAME + "Server";
    private static final String BSCLIENT_CONFIG = APPNAME + ".config";
    private static final String PROPERTYFILENAME = System.getProperty("user.home") + File.separatorChar + BSCLIENT_CONFIG;
    public static final String defaultServerIP = "localhost";
    public static String protocol = "unknown";
    private static User currentUser;
    private static IMessageSender exchanger;
    public static final Color HDR_COLOR = new Color(102, 125, 158);
    private static final String DBNAME = "bookstore";

    public static int getDefaultPageLimit() {
        int ps;
        try {
            ps = Integer.parseInt(readProperty("pageSize", "1000"));
        } catch (NumberFormatException nfe) {
            ps = 500;
        }
        return ps;
    }

    public static Properties getProperties() {
        return props;
    }

    public static void logAndShowMessage(Throwable ne) {
        JOptionPane.showMessageDialog(null, ne.getMessage(), "Error:", JOptionPane.ERROR_MESSAGE);
        log(ne);
    }

    public static void logAndShowMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, "Error:", JOptionPane.ERROR_MESSAGE);
        log(msg);
    }

    public static String readProperty(String key, String deflt) {
        if (null == props) {
            props = new Properties();
            try {
                File propFile = new File(PROPERTYFILENAME);
                if (!propFile.exists() || propFile.length() == 0) {
                    String curPath = propFile.getAbsolutePath();
                    curPath = curPath.substring(0,
                            curPath.indexOf(PROPERTYFILENAME)).replace('\\', '/');
                    props.setProperty("user", "admin");
                    props.setProperty("userPassword", "admin");
                    propFile.createNewFile();
                } else {
                    props.load(new FileInputStream(propFile));
                }
                DbConnection.setProps(props);
            } catch (IOException e) {
                log(e);
                return deflt;
            }
        }
        return props.getProperty(key, deflt);
    }

    public static void log(String msg) {
        log(msg, null);
    }

    public static void log(Throwable th) {
        log(null, th);
    }

    private static void log(String msg, Throwable th) {
        if (logger == null) {
            try {
                logger = Logger.getLogger(APPNAME);
                fh = new FileHandler("%h/" + APPNAME + ".log", 1048576, 10, true);
                logger.addHandler(fh);
                logger.setLevel(Level.ALL);
                SimpleFormatter formatter = new SimpleFormatter();
                fh.setFormatter(formatter);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        logger.log(Level.SEVERE, msg, th);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.home"));
        LookAndFeelFactory.installDefaultLookAndFeelAndExtension();
        String serverIP = readProperty("ServerAddress", "localhost");
        while (true) {
            try {
                IMessageSender exc = ExchangeFactory.getExchanger("rmi://" + serverIP + "/AIBserver", getProperties());
                if (exc == null) {
                    exc = ExchangeFactory.getExchanger(readProperty("JDBCconnection", "jdbc:mysql://"
                            + defaultServerIP
                            + "/aibclient"),
                            getProperties());
                }
                if (exc == null) {
                    configureConnection();
                } else {
                    setExchanger(exc);
                }
                if (getExchanger() != null && matchVersions() && login(getExchanger())) {
                    new DashBoard(APPNAME + " v." + version, exchanger);
                    break;
                } else {
                    System.exit(1);
                }
            } catch (Exception ex) {
                logAndShowMessage(ex);
                if ((serverIP = serverSetup("Check server settings")) == null) {
                    System.exit(1);
                } else {
                    saveProps();
                }
            }
        }
    }

    public static boolean login(IMessageSender exchanger) {
        try {
            new LoginImagedDialog(exchanger);//new Object[]{loginField, pwdField, exchanger});
            return LoginImagedDialog.isOkPressed();
        } catch (Throwable ee) {
            JOptionPane.showMessageDialog(null, "Server failure\nCheck your logs please", "Error:", JOptionPane.ERROR_MESSAGE);
            log(ee);
        }
        return false;
    }
    
    public static Image loadImage(String iconName, Window w) {
        Image im = null;
        File f = new File("images/" + iconName);
        if (f.exists()) {
            try {
                ImageIcon ic = new javax.swing.ImageIcon("images/" + iconName, "");
                im = ic.getImage();
            } catch (Exception ex) {
                log(ex);
            }
        } else {
            try {
                im = ImageIO.read(w.getClass().getResourceAsStream("/" + iconName));
            } catch (Exception ie) {
                log(ie);
            }
        }
        return im;
    }
    
    private static boolean matchVersions() {
        try {
            String servVersion = getExchanger().getServerVersion();
            boolean match = removeTail(servVersion).equals(removeTail(version));
            if (!match) {
                GeneralFrame.errMessageBox("Error:", "Client's software version (" + version + ") doesn't match server (" + servVersion + ")");
            }
            return match;
        } catch (RemoteException ex) {
            logAndShowMessage(ex);
        }
        return false;
    }

    public static IMessageSender getExchanger() {
        return exchanger;
    }

    static void setExchanger(IMessageSender iMessageSender) {
        exchanger = iMessageSender;
    }

    public static String serverSetup(String title) {
        String cnctStr = null;
        String address = readProperty("ServerAddress", defaultServerIP);
        String[] vals = address.split(":");
        JTextField imageDirField = new JTextField(getProperties().getProperty("imagedir"));
        JTextField addressField = new JTextField(16);
        addressField.setText(vals[0]);
        JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(
                vals.length > 1 ? new Integer(vals[1]) : 1099, 0, 65536, 1));
        JTextField dbConnectionField = new JTextField(getProperties()
                .getProperty("JDBCconnection", "jdbc:mysql://"
                        + defaultServerIP
                        + "/"+DBNAME));
        JTextField dbDriverField = new JTextField(getProperties()
                .getProperty("dbDriverName", "com.mysql.jdbc.Driver"));
        JTextField dbUserField = new JTextField(getProperties()
                .getProperty("dbUser", "root"));
        JPasswordField dbPasswordField = new JPasswordField();

        JComponent[] edits = new JComponent[]{
            imageDirField, addressField, portSpinner,
            dbConnectionField, dbDriverField, dbUserField, dbPasswordField
        };
        new ConfigEditor(title, edits);
        if (ConfigEditor.getProtocol().equals("rmi")) {
            if (addressField.getText().trim().length() > 0) {
                cnctStr = addressField.getText() + ":" + portSpinner.getValue();
                getProperties().setProperty("ServerAddress", cnctStr);
                getProperties().setProperty("imagedir", imageDirField.getText());
            }
        } else if (ConfigEditor.getProtocol().equals("jdbc")) {
            if (dbConnectionField.getText().trim().length() > 0) {
                cnctStr = dbDriverField.getText() + ";"
                        + dbConnectionField.getText() + ";"
                        + dbUserField.getText() + ";"
                        + new String(dbPasswordField.getPassword());
                getProperties().setProperty("JDBCconnection", dbConnectionField.getText());
                getProperties().setProperty("dbDriverName", dbDriverField.getText());
                getProperties().setProperty("dbUser", dbUserField.getText());
                getProperties().setProperty("dbPassword", new String(dbPasswordField.getPassword()));
            }
        }
        return cnctStr;
    }

    public static void setWindowIcon(Window w, String iconName) {
        w.setIconImage(loadImage(iconName, w));
    }

    public static void configureConnection() {
        String cnctStr = serverSetup("Options");
        if (cnctStr != null) {
            try {
                if (ConfigEditor.getProtocol().equals("rmi")) {
                    getProperties().setProperty("ServerAddress", cnctStr);
                    setExchanger(ExchangeFactory.createRMIexchanger(cnctStr));
                } else {
                    String[] dbParams = cnctStr.split(";");
                    setExchanger(ExchangeFactory.createJDBCexchanger(dbParams));
                }
                saveProperties();
            } catch (Exception ex) {
                logAndShowMessage(ex);
                System.exit(1);
            }
        }
    }

    public static List loadAllLogins(String fld) {
        try {
            DbObject[] users = exchanger.getDbObjects(User.class, null, "login");
            ArrayList logins = new ArrayList();
            logins.add("");
//            int i = 1;
            for (DbObject o : users) {
                User up = (User) o;
                if (fld.equals("login")) {
                    logins.add(up.getLogin());
                } else if (fld.equals("initials")) {
                    logins.add(up.getInitials());
                }
            }
            return logins;
        } catch (RemoteException ex) {
            log(ex);
        }
        return null;
    }

    
    /**
     * @return the currentUser
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * @param aCurrentUser the currentUser to set
     */
    public static void setCurrentUser(User aCurrentUser) {
        currentUser = aCurrentUser;
    }

    public static void saveProps() {
        if (props != null) {
            if (getCurrentUser() != null) {
                props.setProperty("LastLogin", getCurrentUser().getLogin());
            }
            props.setProperty("ServerAddress", props.getProperty("ServerAddress", "localhost:1099"));
        }
        Preferences userPref = Preferences.userRoot();
        saveProperties();
    }

    public static void saveProperties() {
        try {
            if (props != null) {

                props.store(new FileOutputStream(PROPERTYFILENAME),
                        "-----------------------");
            }
        } catch (IOException e) {
            //e.printStackTrace();
//            logAndShowMessage(e);
            logAndShowMessage(e.getMessage() + new File(PROPERTYFILENAME).getAbsolutePath());
        }
    }

    private static String removeTail(String s) {
        int p = s.lastIndexOf(".");
        if (p > 0 && s.length() > p + 1) {
            if ("0123456789".indexOf(s.substring(p + 1, p + 2)) < 0) {
                return s.substring(0, p);
            }
        }
        return s;
    }

    /**
     * @return the version
     */
    public static String getVersion() {
        return version;
    }

}
