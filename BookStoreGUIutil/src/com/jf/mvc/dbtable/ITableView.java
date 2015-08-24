/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jf.mvc.dbtable;

import com.jf.mvc.IView;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author nick
 */
public interface ITableView extends IView {

    Vector getRowData();

    int getSelectedRow();

    void gotoRow(int row);
    
    int getRowCount();
    
    void setSearchString(String find);
 
    String getSearchString();
    
    void setMaxColWidth(int col, int width);

    void setMaxColWidths(HashMap<Integer, Integer> maxColWidths);
    
    void addMouseListener(MouseListener l);
    
    void removeMouseListener(MouseListener l);

    void setSelectedRow(int row);
    
    void setRowSelectionInterval(int index0, int index1);
}
