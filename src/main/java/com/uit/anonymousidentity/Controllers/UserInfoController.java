package com.uit.anonymousidentity.Controllers;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import com.uit.anonymousidentity.Models.User;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.NoSuchMessageException;
import com.uit.anonymousidentity.Repository.CorporateEventDao;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

/**
 * Created by DK on 10/17/16.
 */
@Controller
public class UserInfoController {
    private final CorporateEventDao corporateEventDao;

    public UserInfoController(CorporateEventDao corporateEventDao ) {
        this.corporateEventDao = corporateEventDao;
    }

    @RequestMapping(value = "/registryUser", method = RequestMethod.POST)
    public void createUser(@RequestParam("id") int id,
                               @RequestParam("name") String name,
                               HttpServletResponse response) throws NoSuchAlgorithmException, IOException{
        response.setStatus(200);
        User user = new User();
        user.setId(id);
        user.setName(name);
        corporateEventDao.insertUser(user);
        PrintWriter out = response.getWriter();
        out.println("create OK");
    }
}
