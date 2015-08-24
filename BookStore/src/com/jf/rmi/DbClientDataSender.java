package com.jf.rmi;

import com.jf.BookStore;
import com.jf.orm.dbobject.DbObject;
import com.jf.remote.IMessageSender;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Nick Mukhin
 */
public class DbClientDataSender implements IMessageSender {

    private static final int CACHESIZE = 1000;
    private static ConcurrentHashMap dbObjectMap = new ConcurrentHashMap();
    private static List synchList = (List) Collections.synchronizedList(new ArrayList());

    public DbClientDataSender(Properties props) {
        DbConnection.setProps(props);
    }

    private static void put2cache(String key, DbObject dbobj) {
        if (synchList.size() >= CACHESIZE) {
            dbObjectMap.remove((String) synchList.get(0));
            synchList.remove(0);
        }
        if (dbobj != null) {
            dbObjectMap.put(key, dbobj);
            synchList.add(key);
        }
    }

    private static DbObject getFromCache(String key) {
        return (DbObject) dbObjectMap.get(key);
    }

    private static void removeFromCache(String key) {
        dbObjectMap.remove(key);
        for (int i = synchList.size() - 1; i >= 0; i--) {
            if (synchList.get(i).equals(key)) {
                synchList.remove(i);
                break;
            }
        }
    }

    @Override
    public DbObject[] getDbObjects(Class dbobClass, String whereCondition, String orderCondition) throws RemoteException {
        DbObject[] rows = null;
        Connection con = null;
        try {
            Method method = dbobClass.getDeclaredMethod("load", Connection.class, String.class, String.class);
            rows = (DbObject[]) method.invoke(null, con = DbConnection.getConnection(), whereCondition, orderCondition);
            for (int i = 0; rows.length < CACHESIZE - synchList.size() && i < rows.length; i++) {
                put2cache(dbobClass.getName() + "#" + rows[i].getPK_ID(), rows[i]);
            }
            return rows;
        } catch (Exception ex) {
            throw new java.rmi.RemoteException(ex.getMessage());
        } finally {
            try {
                DbConnection.closeConnection(con);
            } catch (SQLException e) {
            }
        }
    }

    @Override
    public DbObject saveDbObject(DbObject dbob) throws RemoteException {
        Connection con = null;
        if (dbob != null) {
            try {
                dbob.setConnection(con = DbConnection.getConnection());
                dbob.save();
                put2cache(dbob.getClass().getName() + "#" + dbob.getPK_ID(), dbob);
                return dbob;
            } catch (Exception ex) {
                throw new java.rmi.RemoteException("Can't save DB object:", ex);
            } finally {
                try {
                    DbConnection.closeConnection(con);
                } catch (SQLException ex) {
                }
            }
        }
        return null;
    }

    @Override
    public void deleteObject(DbObject dbob) throws RemoteException {
        Connection con = null;
        if (dbob != null) {
            try {
                dbob.setConnection(con = DbConnection.getConnection());
                dbob.delete();
                removeFromCache(dbob.getClass().getName() + "#" + dbob.getPK_ID());
            } catch (Exception ex) {
                throw new java.rmi.RemoteException(ex.getMessage());
            } finally {
                try {
                    DbConnection.closeConnection(con);
                } catch (SQLException ex) {
                }
            }
        }
    }

    @Override
    public DbObject loadDbObjectOnID(Class dbobClass, int id) throws RemoteException {
        String key = dbobClass.getName() + "#" + id;
        DbObject dbob = getFromCache(key);
        if (dbob == null) {
            Connection con = null;
            try {
                Constructor constructor = dbobClass.getConstructor(Connection.class);
                dbob = (DbObject) constructor.newInstance(con = DbConnection.getConnection());
                dbob = dbob.loadOnId(id);
                put2cache(key, dbob);
//            return dbob;
            } catch (Exception ex) {
                throw new java.rmi.RemoteException("Can't save DB object:", ex);
            } finally {
                try {
                    DbConnection.closeConnection(con);
                } catch (SQLException ex) {
                }
            }
        }
        return dbob;
    }

    @Override
    public Vector[] getTableBody(String select) throws RemoteException {
        return getTableBody(select, 0, 0);
    }

    @Override
    public Vector[] getTableBody(String select, int page, int pagesize) throws RemoteException {
        Vector headers = getColNames(select);
        int startrow = 0, endrow = 0;
        if (page > 0 || pagesize > 0) {
            startrow = page * pagesize + 1; //int page starts from 0, int startrow starts from 1
            endrow = (page + 1) * pagesize; //last row of page
        }
        return new Vector[]{headers, getRows(select, headers.size(),
            startrow, endrow)};
    }

    private Vector getRows(String select, int cols, int startrow, int endrow) throws RemoteException {
        Vector rows = new Vector();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            String pagedSelect;
            if (select.toUpperCase().indexOf(" LIMIT ") > 0 || (startrow == 0 && endrow == 0)) {
                pagedSelect = select;
            } else {
                pagedSelect = select.replaceFirst("select", "SELECT").replaceAll("Select", "SELECT");
                pagedSelect += (" LIMIT " + (startrow - 1) + "," + (endrow - startrow + 1));
            }
            Vector line;
            try {                             ///
                ps = (con = DbConnection.getConnection()).prepareStatement(pagedSelect);
                rs = ps.executeQuery();
                while (rs.next()) {
                    line = new Vector();
                    for (int c = 0; c < cols; c++) {
//                        System.out.println("!! c=" + c + " of cols=" + cols);
                        String ceil = rs.getString(c + 1);
                        ceil = ceil == null ? "" : ceil;
                        line.add(ceil);
                    }
                    rows.add(line);
                }
                return rows;
            } catch (Exception ex) {        ///
                throw ex;               ///
            }                               ///
        } catch (SQLException ex) {
            throw new java.rmi.RemoteException(ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se1) {
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    DbConnection.closeConnection(con);
                } catch (SQLException se2) {
                }
            }
        }
    }

    public Vector getColNames(String select) throws RemoteException {
        String original = null;
        Vector colNames = new Vector();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            int i;
            int bracesLevel = 0;
            StringBuffer sb = null;
            for (i = 0; i < select.length(); i++) {
                char c = select.charAt(i);
                if (c == '(') {
                    bracesLevel++;
                } else if (c == ')') {
                    bracesLevel--;
                } else if (bracesLevel == 0 && select.substring(i).toUpperCase().startsWith("WHERE ")) {
                    if (sb == null) {
                        original = select;
                        sb = new StringBuffer(select);
                    }
                    sb.insert(i + 6, "1=0 AND ");
                    break;
                }
            }
            if (sb != null) {
                select = sb.toString();
            }
            try {                                   ///
                ps = (con = DbConnection.getConnection()).prepareStatement(select);
                rs = ps.executeQuery();
                ResultSetMetaData md = rs.getMetaData();
                for (i = 0; i < md.getColumnCount(); i++) {
                    colNames.add(md.getColumnLabel(i + 1));
                }
            } catch (Exception ex) {        ///
                throw ex;               ///
            }
        } catch (SQLException ex) {
            throw new java.rmi.RemoteException(ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se1) {
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    DbConnection.closeConnection(con);
                } catch (SQLException se2) {
                }
            }
        }
        return colNames;
    }

    @Override
    public int getCount(String select) throws RemoteException {
        StringBuffer slct;
        int count = 0;
        int p = select.toLowerCase().lastIndexOf("order by");
        slct = new StringBuffer("select count(*) from (" + select.substring(0, p > 0 ? p : select.length()) + ") intab");
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            ps = (con = DbConnection.getConnection()).prepareStatement(slct.toString());
            rs = ps.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException ex) {
            throw new java.rmi.RemoteException(ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se1) {
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    DbConnection.closeConnection(con);
                } catch (SQLException se2) {
                }
            }
        }
        return count;
    }

    @Override
    public boolean truncateTable(String tableName) throws RemoteException {
        boolean ok = false;
        PreparedStatement ps = null;
        Connection con = null;
        try {
            ps = (con = DbConnection.getConnection()).prepareStatement("truncate " + tableName);
            ok = ps.execute();
        } catch (SQLException ex) {
            throw new java.rmi.RemoteException(ex.getMessage());
        } finally {
            try {
                ps.close();
            } catch (Exception e) {
            }
            try {
                DbConnection.closeConnection(con);
            } catch (SQLException e) {
            }
        }
        return ok;
    }

    @Override
    public void startTransaction(String transactionName) throws RemoteException {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void commitTransaction() throws RemoteException {
    }

    @Override
    public void rollbackTransaction(String transactionName) throws RemoteException {
    }

    @Override
    public String getServerVersion() throws RemoteException {
        return BookStore.getVersion();
    }

    @Override
    public Object[] getColNamesTypes(String select) throws RemoteException {
        HashMap<String, Integer> types = new HashMap<String, Integer>();
        ArrayList<String> names = new ArrayList<String>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection con = null;
        try {
            ps = (con = DbConnection.getConnection()).prepareStatement(select);
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 0; i < md.getColumnCount(); i++) {
                names.add(md.getColumnLabel(i + 1));
                types.put(md.getColumnLabel(i + 1), new Integer(md.getColumnType(i + 1)));
            }
            return new Object[]{names, types};
        } catch (SQLException ex) {
//            AIBclient.log(ex);
            throw new java.rmi.RemoteException(ex.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException se1) {
            } finally {
                try {
                    if (ps != null) {
                        ps.close();
                    }
                    DbConnection.closeConnection(con);
                } catch (SQLException se2) {
                }
            }
        }
    }
}
