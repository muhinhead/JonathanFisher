/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jf.orm.dbobject;

/**
 *
 * @author Nick Mukhin
 */
public class ForeignKeyViolationException extends Exception {
    public ForeignKeyViolationException(String s) {
        super(s);
    }
}
