// Generated by com.jf.orm.tools.dbgen.DbGenerator.class at Mon Aug 24 08:34:39 EEST 2015
// generated file: do not modify
package com.jf.orm;

import com.jf.orm.dbobject.DbObject;
import com.jf.orm.dbobject.ForeignKeyViolationException;
import com.jf.orm.dbobject.Triggers;
import java.sql.*;
import java.util.ArrayList;

public class Author extends DbObject  {
    private static Triggers activeTriggers = null;
    private Integer authorId = null;
    private String name = null;

    public Author(Connection connection) {
        super(connection, "author", "author_id");
        setColumnNames(new String[]{"author_id", "name"});
    }

    public Author(Connection connection, Integer authorId, String name) {
        super(connection, "author", "author_id");
        setNew(authorId.intValue() <= 0);
//        if (authorId.intValue() != 0) {
            this.authorId = authorId;
//        }
        this.name = name;
    }

    public DbObject loadOnId(int id) throws SQLException, ForeignKeyViolationException {
        Author author = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String stmt = "SELECT author_id,name FROM author WHERE author_id=" + id;
        try {
            ps = getConnection().prepareStatement(stmt);
            rs = ps.executeQuery();
            if (rs.next()) {
                author = new Author(getConnection());
                author.setAuthorId(new Integer(rs.getInt(1)));
                author.setName(rs.getString(2));
                author.setNew(false);
            }
        } finally {
            try {
                if (rs != null) rs.close();
            } finally {
                if (ps != null) ps.close();
            }
        }
        return author;
    }

    protected void insert() throws SQLException, ForeignKeyViolationException {
         if (getTriggers() != null) {
             getTriggers().beforeInsert(this);
         }
         PreparedStatement ps = null;
         String stmt =
                "INSERT INTO author ("+(getAuthorId().intValue()!=0?"author_id,":"")+"name) values("+(getAuthorId().intValue()!=0?"?,":"")+"?)";
         try {
             ps = getConnection().prepareStatement(stmt);
             int n = 0;
             if (getAuthorId().intValue()!=0) {
                 ps.setObject(++n, getAuthorId());
             }
             ps.setObject(++n, getName());
             ps.execute();
         } finally {
             if (ps != null) ps.close();
         }
         ResultSet rs = null;
         if (getAuthorId().intValue()==0) {
             stmt = "SELECT max(author_id) FROM author";
             try {
                 ps = getConnection().prepareStatement(stmt);
                 rs = ps.executeQuery();
                 if (rs.next()) {
                     setAuthorId(new Integer(rs.getInt(1)));
                 }
             } finally {
                 try {
                     if (rs != null) rs.close();
                 } finally {
                     if (ps != null) ps.close();
                 }
             }
         }
         setNew(false);
         setWasChanged(false);
         if (getTriggers() != null) {
             getTriggers().afterInsert(this);
         }
    }

    public void save() throws SQLException, ForeignKeyViolationException {
        if (isNew()) {
            insert();
        } else {
            if (getTriggers() != null) {
                getTriggers().beforeUpdate(this);
            }
            PreparedStatement ps = null;
            String stmt =
                    "UPDATE author " +
                    "SET name = ?" + 
                    " WHERE author_id = " + getAuthorId();
            try {
                ps = getConnection().prepareStatement(stmt);
                ps.setObject(1, getName());
                ps.execute();
            } finally {
                if (ps != null) ps.close();
            }
            setWasChanged(false);
            if (getTriggers() != null) {
                getTriggers().afterUpdate(this);
            }
        }
    }

    public void delete() throws SQLException, ForeignKeyViolationException {
        if (Book.exists(getConnection(),"author_id = " + getAuthorId())) {
            throw new ForeignKeyViolationException("Can't delete, foreign key violation: book_author_fk");
        }
        if (getTriggers() != null) {
            getTriggers().beforeDelete(this);
        }
        PreparedStatement ps = null;
        String stmt =
                "DELETE FROM author " +
                "WHERE author_id = " + getAuthorId();
        try {
            ps = getConnection().prepareStatement(stmt);
            ps.execute();
        } finally {
            if (ps != null) ps.close();
        }
        setAuthorId(new Integer(-getAuthorId().intValue()));
        if (getTriggers() != null) {
            getTriggers().afterDelete(this);
        }
    }

    public boolean isDeleted() {
        return (getAuthorId().intValue() < 0);
    }

    public static DbObject[] load(Connection con,String whereCondition,String orderCondition) throws SQLException {
        ArrayList lst = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String stmt = "SELECT author_id,name FROM author " +
                ((whereCondition != null && whereCondition.length() > 0) ?
                " WHERE " + whereCondition : "") +
                ((orderCondition != null && orderCondition.length() > 0) ?
                " ORDER BY " + orderCondition : "");
        try {
            ps = con.prepareStatement(stmt);
            rs = ps.executeQuery();
            while (rs.next()) {
                DbObject dbObj;
                lst.add(dbObj=new Author(con,new Integer(rs.getInt(1)),rs.getString(2)));
                dbObj.setNew(false);
            }
        } finally {
            try {
                if (rs != null) rs.close();
            } finally {
                if (ps != null) ps.close();
            }
        }
        Author[] objects = new Author[lst.size()];
        for (int i = 0; i < lst.size(); i++) {
            Author author = (Author) lst.get(i);
            objects[i] = author;
        }
        return objects;
    }

    public static boolean exists(Connection con, String whereCondition) throws SQLException {
        if (con == null) {
            return true;
        }
        boolean ok = false;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String stmt = "SELECT author_id FROM author " +
                ((whereCondition != null && whereCondition.length() > 0) ?
                "WHERE " + whereCondition : "");
        try {
            ps = con.prepareStatement(stmt);
            rs = ps.executeQuery();
            ok = rs.next();
        } finally {
            try {
                if (rs != null) rs.close();
            } finally {
                if (ps != null) ps.close();
            }
        }
        return ok;
    }

    //public String toString() {
    //    return getAuthorId() + getDelimiter();
    //}

    public Integer getPK_ID() {
        return authorId;
    }

    public void setPK_ID(Integer id) throws ForeignKeyViolationException {
        boolean prevIsNew = isNew();
        setAuthorId(id);
        setNew(prevIsNew);
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) throws ForeignKeyViolationException {
        setWasChanged(this.authorId != null && this.authorId != authorId);
        this.authorId = authorId;
        setNew(authorId.intValue() == 0);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws SQLException, ForeignKeyViolationException {
        setWasChanged(this.name != null && !this.name.equals(name));
        this.name = name;
    }
    public Object[] getAsRow() {
        Object[] columnValues = new Object[2];
        columnValues[0] = getAuthorId();
        columnValues[1] = getName();
        return columnValues;
    }

    public static void setTriggers(Triggers triggers) {
        activeTriggers = triggers;
    }

    public static Triggers getTriggers() {
        return activeTriggers;
    }

    //for SOAP exhange
    @Override
    public void fillFromString(String row) throws ForeignKeyViolationException, SQLException {
        String[] flds = splitStr(row, delimiter);
        try {
            setAuthorId(Integer.parseInt(flds[0]));
        } catch(NumberFormatException ne) {
            setAuthorId(null);
        }
        setName(flds[1]);
    }
}
