package com.jf.util;

import javax.swing.*;
import java.awt.*;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Nick Mukhin
 */
public abstract class PopupDialog extends JDialog {

    protected Frame ownerFrame;
    private Object object;
    private JLabel headerLabel;

    public PopupDialog(Frame owner, String title, Object obj) {
        super(owner, title);
        ownerFrame = owner;
        setObject(obj);
        init();
    }

    private void init() {
        fillContent();
        initSize();
        setMinimumSize(getSize());
        setVisible(true);
    }

    protected Color getHeaderBackground() {
        return new Color(226, 148, 37);
    }
    
    protected Color getHeaderForeground() {
        return new Color(255, 255, 255);
    }
    
    protected void fillContent() {
        getContentPane().setLayout(new BorderLayout(10, 10));
        Color bg = getHeaderBackground();
        if (bg != null) {
            JPanel headerPanel = new JPanel();
            headerPanel.setBackground(bg);
            headerLabel = new JLabel(getTitle(), SwingConstants.CENTER);
            headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 18));
            headerLabel.setForeground(getHeaderForeground());
            headerPanel.add(headerLabel);
            getContentPane().add(headerPanel, BorderLayout.NORTH);
        }
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        headerLabel.setText(title);
    }
    
    

    protected void initSize() {
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        pack();
        setLocation(d.width / 2 - getWidth() / 2, d.height / 2 - getHeight() / 2);
        this.setModal(true);
    }

    protected void centerWindow() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);

        validate();
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    private void removeComponents(Container cont) {
        Component[] components = cont.getComponents();
        for (int i = components.length - 1; i >= 0; i--) {
            Component comp = components[i];
            if (comp != null) {
                if (comp instanceof Container) {
                    removeComponents((Container) comp);
                }
                if (comp instanceof PopupDialog) {
                    ((PopupDialog) comp).freeResources();
                }
                cont.remove(comp);
                comp = null;
            }
        }
    }

    public abstract void freeResources();

    @Override
    public void dispose() {
        freeResources();
//        removeComponents(getContentPane());
        super.dispose();
        Runtime.getRuntime().gc();
    }

    public static void updateList(final JTable tableView) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException ex) {
                }
                AbstractTableModel model = (AbstractTableModel) tableView.getModel();
                int selectedRow = tableView.getSelectedRow();
                model.fireTableDataChanged();
                tableView.setRowSelectionInterval(selectedRow, selectedRow);
            }
        });
    }
}
