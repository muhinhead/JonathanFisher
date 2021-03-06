package com.jf.remote;

//import com.xlend.orm.dbobject.DbObject;
//import com.aib.orm.dbobject.DbObject;
import com.jf.orm.dbobject.DbObject;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author Nick Mukhin
 */
public interface IMessageSender extends java.rmi.Remote {
    public DbObject[] getDbObjects(Class dbobClass, String whereCondition, 
            String orderCondition) throws java.rmi.RemoteException;
    public DbObject saveDbObject(DbObject dbob) throws java.rmi.RemoteException;
    public void deleteObject(DbObject dbob) throws java.rmi.RemoteException;
    public DbObject loadDbObjectOnID(Class dbobClass, int id) 
            throws java.rmi.RemoteException;
    public Vector[] getTableBody(String select) throws java.rmi.RemoteException;
    public Vector[] getTableBody(String select, int page, int pagesize) throws java.rmi.RemoteException;
    public Vector getColNames(String select) throws java.rmi.RemoteException;
    public Object[] //HashMap<String, Integer> 
            getColNamesTypes(String select) throws RemoteException;
    public int getCount(String select) throws java.rmi.RemoteException;
    public boolean truncateTable(String tableName) throws java.rmi.RemoteException;
    public void startTransaction(String transactionName) throws java.rmi.RemoteException;
    public void commitTransaction() throws java.rmi.RemoteException;
    public void rollbackTransaction(String transactionName) throws java.rmi.RemoteException;
    public String getServerVersion() throws java.rmi.RemoteException;
}
