// Generated by com.jf.orm.tools.dbgen.DbGenerator.class at Mon Aug 24 08:34:39 EEST 2015
// generated file: do not modify
package com.jf.orm;

import com.jf.orm.dbobject.DbObject;
import com.jf.orm.dbobject.ForeignKeyViolationException;
import com.jf.orm.dbobject.Triggers;
import java.sql.*;
import java.util.ArrayList;

public class Book extends DbObject  {
    private static Triggers activeTriggers = null;
    private Integer bookId = null;
    private String isbn = null;
    private String name = null;
    private Integer authorId = null;

    public Book(Connection connection) {
        super(connection, "book", "book_id");
        setColumnNames(new String[]{"book_id", "isbn", "name", "author_id"});
    }

    public Book(Connection connection, Integer bookId, String isbn, String name, Integer authorId) {
        super(connection, "book", "book_id");
        setNew(bookId.intValue() <= 0);
//        if (bookId.intValue() != 0) {
            this.bookId = bookId;
//        }
        this.isbn = isbn;
        this.name = name;
        this.authorId = authorId;
    }

    public DbObject loadOnId(int id) throws SQLException, ForeignKeyViolationException {
        Book book = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String stmt = "SELECT book_id,isbn,name,author_id FROM book WHERE book_id=" + id;
        try {
            ps = getConnection().prepareStatement(stmt);
            rs = ps.executeQuery();
            if (rs.next()) {
                book = new Book(getConnection());
                book.setBookId(new Integer(rs.getInt(1)));
                book.setIsbn(rs.getString(2));
                book.setName(rs.getString(3));
                book.setAuthorId(new Integer(rs.getInt(4)));
                book.setNew(false);
            }
        } finally {
            try {
                if (rs != null) rs.close();
            } finally {
                if (ps != null) ps.close();
            }
        }
        return book;
    }

    protected void insert() throws SQLException, ForeignKeyViolationException {
         if (getTriggers() != null) {
             getTriggers().beforeInsert(this);
         }
         PreparedStatement ps = null;
         String stmt =
                "INSERT INTO book ("+(getBookId().intValue()!=0?"book_id,":"")+"isbn,name,author_id) values("+(getBookId().intValue()!=0?"?,":"")+"?,?,?)";
         try {
             ps = getConnection().prepareStatement(stmt);
             int n = 0;
             if (getBookId().intValue()!=0) {
                 ps.setObject(++n, getBookId());
             }
             ps.setObject(++n, getIsbn());
             ps.setObject(++n, getName());
             ps.setObject(++n, getAuthorId());
             ps.execute();
         } finally {
             if (ps != null) ps.close();
         }
         ResultSet rs = null;
         if (getBookId().intValue()==0) {
             stmt = "SELECT max(book_id) FROM book";
             try {
                 ps = getConnection().prepareStatement(stmt);
                 rs = ps.executeQuery();
                 if (rs.next()) {
                     setBookId(new Integer(rs.getInt(1)));
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
                    "UPDATE book " +
                    "SET isbn = ?, name = ?, author_id = ?" + 
                    " WHERE book_id = " + getBookId();
            try {
                ps = getConnection().prepareStatement(stmt);
                ps.setObject(1, getIsbn());
                ps.setObject(2, getName());
                ps.setObject(3, getAuthorId());
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
        if (getTriggers() != null) {
            getTriggers().beforeDelete(this);
        }
        PreparedStatement ps = null;
        String stmt =
                "DELETE FROM book " +
                "WHERE book_id = " + getBookId();
        try {
            ps = getConnection().prepareStatement(stmt);
            ps.execute();
        } finally {
            if (ps != null) ps.close();
        }
        setBookId(new Integer(-getBookId().intValue()));
        if (getTriggers() != null) {
            getTriggers().afterDelete(this);
        }
    }

    public boolean isDeleted() {
        return (getBookId().intValue() < 0);
    }

    public static DbObject[] load(Connection con,String whereCondition,String orderCondition) throws SQLException {
        ArrayList lst = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String stmt = "SELECT book_id,isbn,name,author_id FROM book " +
                ((whereCondition != null && whereCondition.length() > 0) ?
                " WHERE " + whereCondition : "") +
                ((orderCondition != null && orderCondition.length() > 0) ?
                " ORDER BY " + orderCondition : "");
        try {
            ps = con.prepareStatement(stmt);
            rs = ps.executeQuery();
            while (rs.next()) {
                DbObject dbObj;
                lst.add(dbObj=new Book(con,new Integer(rs.getInt(1)),rs.getString(2),rs.getString(3),new Integer(rs.getInt(4))));
                dbObj.setNew(false);
            }
        } finally {
            try {
                if (rs != null) rs.close();
            } finally {
                if (ps != null) ps.close();
            }
        }
        Book[] objects = new Book[lst.size()];
        for (int i = 0; i < lst.size(); i++) {
            Book book = (Book) lst.get(i);
            objects[i] = book;
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
        String stmt = "SELECT book_id FROM book " +
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
    //    return getBookId() + getDelimiter();
    //}

    public Integer getPK_ID() {
        return bookId;
    }

    public void setPK_ID(Integer id) throws ForeignKeyViolationException {
        boolean prevIsNew = isNew();
        setBookId(id);
        setNew(prevIsNew);
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) throws ForeignKeyViolationException {
        setWasChanged(this.bookId != null && this.bookId != bookId);
        this.bookId = bookId;
        setNew(bookId.intValue() == 0);
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) throws SQLException, ForeignKeyViolationException {
        setWasChanged(this.isbn != null && !this.isbn.equals(isbn));
        this.isbn = isbn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) throws SQLException, ForeignKeyViolationException {
        setWasChanged(this.name != null && !this.name.equals(name));
        this.name = name;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) throws SQLException, ForeignKeyViolationException {
        if (authorId!=null && !Author.exists(getConnection(),"author_id = " + authorId)) {
            throw new ForeignKeyViolationException("Can't set author_id, foreign key violation: book_author_fk");
        }
        setWasChanged(this.authorId != null && !this.authorId.equals(authorId));
        this.authorId = authorId;
    }
    public Object[] getAsRow() {
        Object[] columnValues = new Object[4];
        columnValues[0] = getBookId();
        columnValues[1] = getIsbn();
        columnValues[2] = getName();
        columnValues[3] = getAuthorId();
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
            setBookId(Integer.parseInt(flds[0]));
        } catch(NumberFormatException ne) {
            setBookId(null);
        }
        setIsbn(flds[1]);
        setName(flds[2]);
        try {
            setAuthorId(Integer.parseInt(flds[3]));
        } catch(NumberFormatException ne) {
            setAuthorId(null);
        }
    }
}
