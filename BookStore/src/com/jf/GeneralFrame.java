package com.jf;

//import com.xlend.gui.reports.GeneralReportPanel;
import com.jf.mvc.dbtable.DbTableDocument;
import com.jf.mvc.dbtable.DbTableGridPanel;
import com.jf.mvc.dbtable.DbTableView.MyTableModel;
import com.jf.mvc.dbtable.ITableView;
import com.jf.remote.IMessageSender;
import com.jf.util.ToolBarButton;
import com.jf.util.Util;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.rmi.RemoteException;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Admin
 */
public abstract class GeneralFrame extends JFrame implements WindowListener {

    private IMessageSender exchanger;
    private JPanel statusPanel = new JPanel();
    private JLabel statusLabel1 = new JLabel();
    private JLabel statusLabel2 = new JLabel();
    private JTabbedPane mainPanel;
    private ToolBarButton aboutButton;
    private ToolBarButton exitButton;
    private JToolBar toolBar;
    private ToolBarButton refreshButton;
    protected ToolBarButton printButton;
    private JToggleButton searchButton;
//    private JToggleButton filterButton;
    private HashMap<GeneralGridPanel, String> grids = new HashMap<GeneralGridPanel, String>();
//    private HashMap<GeneralReportPanel, String> reports = new HashMap<GeneralReportPanel, String>();
//    private HashMap<HTMLpanel, String> browsers = new HashMap<HTMLpanel, String>();
    private JLabel srcLabel;
    private JTextField srcField;
//    private JLabel fltrLabel;
//    private JTextField fltrField;
//    protected JMenu filterMenu;

    public GeneralFrame(String title, IMessageSender exch) {
        super(title);
        addWindowListener(this);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        this.exchanger = exch;
        fillContentPane();
        float width = Float.valueOf(BookStore.readProperty("WindowWidth", "0.8"));
        float height = Float.valueOf(BookStore.readProperty("WindowHeight", "0.8"));
        boolean maximize = (width < 0 || width < 0);
        width = (width > 0.0 ? width : (float) 0.8);
        height = (height > 0.0 ? height : (float) 0.8);
        DashBoard.setSizes(this, width, height);
        DashBoard.centerWindow(this);
        if (maximize) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
        setVisible(true);
    }

    public GeneralFrame(IMessageSender exch) {
        this("Works", exch);
    }

    protected abstract String[] getSheetList();

    public void setLookAndFeel(String lf) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(lf);
        SwingUtilities.updateComponentTreeUI(this);
        BookStore.getProperties().setProperty("LookAndFeel", lf);
    }

    protected void fillContentPane() {
        BookStore.setWindowIcon(this, "bs.png");
        getContentPane().setLayout(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusPanel.setLayout(new BorderLayout());
        setStatusLabel1Text(" ");
        statusLabel1.setBorder(BorderFactory.createEtchedBorder());
        statusLabel2.setHorizontalTextPosition(SwingConstants.CENTER);
        statusLabel2.setText(" ");
        statusPanel.add(statusLabel2, BorderLayout.CENTER);

        printButton = new ToolBarButton("print.png");
        printButton.setToolTipText("Data export");
        printButton.addActionListener(getPrintAction());

        searchButton = new JToggleButton(new ImageIcon(Util.loadImage("search.png")));
        getSearchButton().setToolTipText("Search on fragment");
        getSearchButton().addActionListener(getSearchAction());

//        filterButton = new JToggleButton(new ImageIcon(Util.loadImage("filter.png")));
//        filterButton.setToolTipText("Filter on fragment");
//        filterButton.addActionListener(getFilterAction());

        refreshButton = new ToolBarButton("refresh.png");
        refreshButton.setToolTipText("Refresh data");
        refreshButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                refreshGrids();
            }
        });
        aboutButton = new ToolBarButton("help.png");
        aboutButton.setToolTipText("About program");
        exitButton = new ToolBarButton("exit.png");
        exitButton.setToolTipText("Hide this window");
        exitButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        toolBar = new JToolBar();
        getToolBar().add(getSearchButton());
        getToolBar().add(srcLabel = new JLabel("  Search:"));
        getToolBar().add(srcField = new JTextField(20));
        addAfterSearch();
        srcLabel.setVisible(false);
        srcField.setVisible(false);
        srcField.addKeyListener(getSrcFieldKeyListener());
        srcField.setMaximumSize(srcField.getPreferredSize());

        getToolBar().add(printButton);
        addAfterPrint();
        getToolBar().add(refreshButton);
        getToolBar().add(aboutButton);
        getToolBar().add(exitButton);
        aboutButton.setToolTipText("About program...");
        aboutButton.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: new AboutDialog();
            }
        });

        exitButton.setToolTipText("Close this window");

        getContentPane().add(getToolBar(), BorderLayout.NORTH);

        mainPanel = buildMainPanel();
        getContentPane().add(getMainPanel(), BorderLayout.CENTER);
        getMainPanel().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                getSearchButton().setSelected(false);
                srcField.setText(null);
                srcField.setVisible(false);
                srcLabel.setVisible(false);
//                fltrField.setText(null);
//                fltrField.setVisible(false);
//                fltrLabel.setVisible(false);
                highlightFound();
            }
        });

        getContentPane().add(statusPanel, BorderLayout.SOUTH);

        buildMenu();

    }

    private KeyAdapter getSrcFieldKeyListener() {
        return new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                highlightFound();
            }
        };
    }

    private void highlightFound() {
        Component selectedPanel = getMainPanel().getSelectedComponent();
        if (selectedPanel instanceof GeneralGridPanel) {
            GeneralGridPanel selectedGridPanel = (GeneralGridPanel) selectedPanel;
            try {
                RowFilter<MyTableModel, Object> rf = RowFilter.regexFilter("(?i)" + srcField.getText());
                selectedGridPanel.getTableView().getSorter().setRowFilter(rf);
            } catch (Exception ex) {
                BookStore.log(ex);
            }
//            selectedGridPanel.highlightSearch(srcField.getText());
        }
    }

    public void setVisible(boolean b) {
        if (b) {
            refreshGrids();
        }
        super.setVisible(b);
    }

    private ActionListener getSearchAction() {
        return new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean pressed = getSearchButton().isSelected();
                srcLabel.setVisible(pressed);
                srcField.setVisible(pressed);
                if (pressed) {
                    srcField.requestFocus();
                }
            }
        };
    }

    private void refreshGrids() {
        for (GeneralGridPanel grid : grids.keySet()) {
            grid.refresh();
        }
    }

    public static void errMessageBox(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.ERROR_MESSAGE);
    }

    public static void infoMessageBox(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static int yesNo(String msg, String title) {
        int ok = JOptionPane.showConfirmDialog(null, title, msg,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return ok;
    }

    public static void notImplementedYet() {
        errMessageBox("Sorry!", "Not implemented yet");
    }

    public static void notImplementedYet(String msg) {
        errMessageBox("Sorry!", "Not implemented yet " + msg);
    }

    public void setStatusLabel1Text(String lbl) {
        statusLabel1.setText(lbl);
    }

    protected void buildMenu() {
        JMenuBar bar = new JMenuBar();
        JMenu m = createMenu("File", "File Operations");
        JMenuItem mi = createMenuItem("Hide", "Hide this window");
        mi.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        m.add(mi);
        bar.add(m);
        setJMenuBar(bar);
    }

    protected abstract JTabbedPane buildMainPanel();

    protected JMenuItem createMenuItem(String label, String microHelp) {
        JMenuItem m = new JMenuItem(label);
        setMenuStatusMicroHelp(m, microHelp);
        return m;
    }

    protected JMenuItem createMenuItem(String label) {
        return createMenuItem(label, label);
    }

    protected JMenu createMenu(String label, String microHelp) {
        JMenu m = new JMenu(label);
        setMenuStatusMicroHelp(m, microHelp);
        return m;
    }

    protected JMenu createMenu(String label) {
        return createMenu(label, label);
    }

    protected void setMenuStatusMicroHelp(final JMenuItem m, final String msg) {
        m.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                statusLabel2.setText(msg == null ? m.getText() : msg);
            }
        });
    }

    public static void updateGrid(IMessageSender exchanger,
            ITableView view, DbTableDocument doc, String select, Integer id, int page)
            throws RemoteException {
        int row = view.getSelectedRow();
        try {
            if (select != null) {
                ((JComponent) view).setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));;
                if (page >= 0) {
                    doc.setBody(exchanger.getTableBody(select, page, GeneralGridPanel.PAGESIZE));
                } else {
                    doc.setBody(exchanger.getTableBody(select));
                }
                view.getController().updateExcept(null);
                if (id != null && id.intValue() != 0) {
                    DbTableGridPanel.selectRowOnId(view, id);
                } else {
                    row = row < view.getRowCount() ? row : row - 1;
                    if (row >= 0 && row < view.getRowCount()) {
                        view.setRowSelectionInterval(row, row);
                    }
                }
            }
        } finally {
            ((JComponent) view).setCursor(Cursor.getDefaultCursor());
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
        float xRatio = -1;
        float yRatio = -1;
        if (this.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
            xRatio = DashBoard.getXratio(this);
            yRatio = DashBoard.getYratio(this);
        }
        BookStore.getProperties().setProperty("WindowWidth", "" + xRatio);
        BookStore.getProperties().setProperty("WindowHeight", "" + yRatio);
        BookStore.saveProps();
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

//    private JPanel getContractsPanel() {
//        if (contractsPanel == null) {
//            HashMap<Integer, Integer> maxWidths = new HashMap<Integer, Integer>();
//            maxWidths.put(0, 40);
//            maxWidths.put(1, 150);
//            contractsPanel = createAndRegisterGrid(Selects.SELECT_FROM_CONTRACTS,
//                    addContractAction(), editContractAction(),
//                    delContractAction(), maxWidths);
//            grids.put(contractsPanel, Selects.SELECT_FROM_CONTRACTS);
//        }
//        return contractsPanel;
//    }
//    protected DbTableGridPanel createAndRegisterGrid(String select,
//            AbstractAction add, AbstractAction edit, AbstractAction del, HashMap<Integer, Integer> maxWidths) {
//        DbTableGridPanel panel = createGridPanel(getExchanger(), select, add, edit, del, maxWidths);
//        grids.put(panel, select);
//        return panel;
//    }
//    public static DbTableGridPanel createGridPanel(IMessageSender exchanger, String select,
//            AbstractAction add, AbstractAction edit, AbstractAction del, HashMap<Integer, Integer> maxWidths) {
//        DbTableGridPanel targetPanel = null;
//        try {
//            targetPanel = new DbTableGridPanel(
//                    add, edit, del, exchanger.getTableBody(select), maxWidths);
//            if (del != null) {
//                boolean enableDel = (XlendWorks.getCurrentUser().getManager() == 1 
//                        || XlendWorks.getCurrentUser().getSupervisor() == 1);
//                targetPanel.getDelAction().setEnabled(enableDel);
//            }
//        } catch (RemoteException ex) {
//            ex.printStackTrace();
//        }
//        return targetPanel;
//    }
    protected void registerGrid(GeneralGridPanel grid) {
        grids.put(grid, grid.getSelect());
    }

//    protected void registerGrid(GeneralReportPanel repPanel) {
//        reports.put(repPanel, repPanel.toString());
//    }
//    protected void registerGrid(HTMLpanel browserPanel) {
//        browsers.put(browserPanel, browserPanel.getUrlString());
//    }
    /**
     * @return the exchanger
     */
    public IMessageSender getExchanger() {
        return exchanger;
    }

    protected ActionListener getPrintAction() {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                notImplementedYet();
            }
        };
    }

    /**
     * @return the searchButton
     */
    protected JToggleButton getSearchButton() {
        return searchButton;
    }

//    protected abstract ActionListener addNewFilterAction();
//
//    protected abstract ActionListener editFilterAction();
//
//    protected abstract ActionListener delFilterAction();
    /**
     * @return the toolBar
     */
    public JToolBar getToolBar() {
        return toolBar;
    }

    protected void addAfterSearch() {
    }

    /**
     * @return the mainPanel
     */
    public JTabbedPane getMainPanel() {
        return mainPanel;
    }

//    public static String adjustFilterQuery(Filter flt, String what, String replacement) {
//        if (flt != null) {
//            String query = flt.getQuery().replaceAll("==", "=").replaceAll("\n", " ");
//            int l = query.indexOf(what);
//            int ll;
//            while (l >= 0) {
//                ll = query.indexOf(" AND ", l);
//                if (ll < 0) {
//                    ll = query.indexOf(" OR ", l);
//                }
//                String linkRest = ll > 0 ? query.substring(ll) : "";
//                String linkCondition = ll < 0 ? query.substring(l) : query.substring(l, ll);
//                linkCondition = linkCondition.replace(what, replacement) + ")";
//                query = query.substring(0, l) + linkCondition;
//                query += linkRest;
//                l = query.indexOf(what);
//            }
//            try {
//                flt.setQuery(query);
//            } catch (Exception ex) {
//                BookStore.log(ex);
//            }
//            return query;
//        }
//        return null;
//    }

//    protected static String adjustSelect(Filter flt, String from, String select) {
//        String newSelect = null;
//        int p = select.indexOf(from);
//        boolean includeWhere = (select.lastIndexOf(from + " where ") > 0);
//        if (includeWhere) {
//            from = from + "where ";
//        }
//        String restOfStatement = select.substring(p + from.length());
//        newSelect = select.substring(0, p + from.length()) + (includeWhere ? " and " : " where ")
//                + flt.getQuery() + restOfStatement;
//        return newSelect.replace(GeneralGridPanel.SELECTLIMIT, "");
//    }

    protected void addAfterPrint() {
    }
}
