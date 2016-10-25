/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uit.anonymousidentity.Repository.EndorsementKeys;

import java.sql.SQLException;

/**
 *
 * @author root
 */
public interface EKDao {
    public void createTableIfNotExists() throws SQLException;
    public void store(EK ek) throws SQLException;
    public EK getEKByDeviceID(String deviceID) throws SQLException;
    public void removeByDeviceID(String deviceID) throws SQLException;
     
    
}
