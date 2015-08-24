/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jf;

import com.jf.dbutil.DbConnection;
import com.jf.remote.IMessageSender;
import com.jf.rmi.RmiMessageSender;
import java.awt.Color;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.Naming;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Timer;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.UIManager;

/**
 *
 * @author nick
 */
public class BookStoreServer {

    private static final String version = "0.1";
    private static final String SERVERNAME = "BookStoreServer";
    public static final String PROPERTYFILENAME = SERVERNAME + ".config";
    private static final String ICONNAME = "bs.png";
    private static Logger logger = null;
    private static FileHandler fh;
    private static Thread rmiServer;
    private static TrayIcon ti;
    private static Properties props;
    private static boolean isTraySupported = SystemTray.isSupported();
    public static final Color unformColor = new Color(102, 125, 158);

    private static class CtrlCtrapper extends Thread {

        private Timer oTimer;

        public CtrlCtrapper(Timer p_oTimer) {
            super();
            closeAllConnections();
            oTimer = p_oTimer;
        }

        public void run() {
            System.out.println("Ctrl+C pressed!!!");
            oTimer.cancel();
            rmiServer.stop();
        }
    };

    public static void log(String msg) {
        log(msg, null);
    }

    public static void log(Throwable th) {
        log(null, th);
    }

    private static void log(String msg, Throwable th) {
        if (logger == null) {
            try {
                logger = Logger.getLogger(SERVERNAME);
                fh = new FileHandler("%h/" + SERVERNAME + ".log", 1048576, 10, true);
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
        String appToLog = "\n" + (msg == null ? th.getMessage() : msg);
        LogViewDialog.logBuffer.append(appToLog);
        logger.log(Level.SEVERE, msg, th);
    }

    public static Image loadImage(String iconName) {
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
                im = ImageIO.read(BookStoreServer.class.getResourceAsStream("/" + iconName));
            } catch (Exception ie) {
                log(ie);
            }
        }
        return im;
    }

    public static void setWindowIcon(Window w, String iconName) {
        w.setIconImage(loadImage(iconName));
    }

    /**
     * @return the unformColor
     */
    public static Color getUnformColor() {
        return unformColor;

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (!isTraySupported) {
            System.out.println("Java version: " + System.getProperty("java.version"));
            if (args.length < 1) {
                System.out.println("Usage:\n\tcom.aib.AIBserver [port] (default 1099)");
            }
        } else {
            try {
                String theme = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
                UIManager.setLookAndFeel(theme);
//                SwingUtilities.updateComponentTreeUI(this);
            } catch (Exception ex) {
            }
            props = new Properties();
            File propFile = new File(PROPERTYFILENAME);
            try {
                if (propFile.exists()) {
                    props.load(new FileInputStream(propFile));
                    DbConnection.setProps(props);
                    System.out.println("Properties loaded from " + PROPERTYFILENAME);
                }
                System.out.println("\nPress Ctrl+C to interrupt");
            } catch (IOException ioe) {
                log(ioe);
            }
            initTray();
        }
        final int port = (args.length > 0 ? Integer.parseInt(args[0]) : 1099);
        rmiServer = new Thread() {
            public void run() {
                try {
                    final Timer queueRunner = new Timer();
                    System.out.println("Starting server on port " + port + "... ");
                    java.rmi.registry.LocateRegistry.createRegistry(port);
                    IMessageSender c = new RmiMessageSender();
                    Naming.rebind("rmi://localhost:" + port + "/AIBserver", c);
                    if (!isTraySupported) {
                        Runtime.getRuntime().addShutdownHook(new CtrlCtrapper(queueRunner));
                    }
//                    runSyncService();
                } catch (Exception ex) {
                    log("RMI server trouble: " + ex.getMessage());
                    closeAllConnections();
                    System.exit(1);
                }
            }
        };
        rmiServer.start();
    }

    private static void initTray() {
        final SystemTray tray = SystemTray.getSystemTray();
        try {
            Image icon = loadImage(ICONNAME);
            final PopupMenu popup = new PopupMenu();
            MenuItem miExit = new MenuItem("Exit");
            miExit.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    rmiServer.stop();
                    //isCycle = false;
                    closeAllConnections();
                    System.exit(0);
                }
            });
            MenuItem miAbout = new MenuItem("About...");
            miAbout.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //new AboutDialog();
                    
                }
            });
            MenuItem miLog = new MenuItem("Server log...");
            miLog.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showLog();
                }
            });
            MenuItem miSetup = new MenuItem("DB connection setup");
            miSetup.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {
                    showDbSetup();
                }
            });
            popup.add(miLog);
            popup.add(miAbout);
            popup.add(miSetup);

//            if (props.getProperty("dbDriverName", "org.hsqldb.jdbcDriver").equals("org.hsqldb.jdbcDriver")) {
//                MenuItem miMigrate = new MenuItem("Migrate");
//                miMigrate.addActionListener(new ActionListener() {
//                    public void actionPerformed(ActionEvent e) {
//                        new MigrationDialog();
//                    }
//                });
//                popup.addSeparator();
//                popup.add(miMigrate);
//            }

            popup.addSeparator();
            popup.add(miExit);
            ti = new TrayIcon(icon, "AIB Server", popup);
            ti.setActionCommand("DoubleClick");
            ti.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showLog();
                }
            });
            tray.add(ti);
        } catch (Exception ex) {
            log("RMI server trouble: " + ex.getMessage());
            //isCycle = false;
            closeAllConnections();
            System.exit(2);
        }
    }
    
    private static void showLog() {
        new LogViewDialog(getVersion(), DbConnection.DB_VERSION);
    }

    private static void showDbSetup() {
        new DBconnectionSetupDialog(props);
    }
    
    private static void closeAllConnections() {
        try {
            DbConnection.closeAllConnections();
        } catch (SQLException ex) {
            log(ex);
        }
    }

    /**
     * @return the version
     */
    public static String getVersion() {
        return version;
    }

}
