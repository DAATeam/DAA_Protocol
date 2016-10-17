/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.Nonces;

import java.sql.SQLException;
import java.util.Set;
import javax.sql.DataSource;

/**
 *
 * @author root
 */
public interface NonceDAO {
   
   // public void setDataSource(DataSource dataSource) throws SQLException   ;
    public void createTableIfNotExist() throws SQLException;
    public void store(Nonce nonce) throws SQLException;
    public Nonce getNonceById(Integer id) throws SQLException;
    public Set<Nonce> getNoncesBySID(String sid) throws SQLException;
    public void delete(Nonce nonce) throws SQLException;
    public void deleteById(Integer id) throws SQLException;
}
