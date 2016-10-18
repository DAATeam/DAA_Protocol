/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.IssuerKeys;

import com.uit.anonymousidentity.Models.Issuer;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.sql.Blob;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 *
 * @author root
 */
public interface IssuerDAO {
    //public void setDataSource(DataSource dataSrouce);
    public void store(Issuer issuer) throws SQLException;
    public Issuer getIssuerBySID(String sid) throws SQLException, NoSuchAlgorithmException;
    public void deleteIssuerBySID(String sid) throws SQLException;
    public void createTableIfNotExists() throws SQLException;
}
