package com.uit.anonymousidentity.Repository;
import com.uit.anonymousidentity.Models.user;
import com.uit.anonymousidentity.Repository.JdbcCorporateEventDao;
/**
 * Created by DK on 10/17/16.
 */
public interface CorporateEventDao {
    public user findByUserId(int id);
    public user insertUser(user user);
}
