package com.uit.anonymousidentity.Repository;
import com.uit.anonymousidentity.Models.User;
/**
 * Created by DK on 10/17/16.
 */
public interface CorporateEventDao {
    public User findByUserId(int id);
    public User insertUser(User user);
}
