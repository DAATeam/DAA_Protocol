package com.uit.anonymousidentity.Controllers;
import org.springframework.stereotype.Controller;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.context.NoSuchMessageException;

import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;

/**
 * Created by DK on 10/17/16.
 */
@Controller
public class UserInfoController {
    @RequestMapping(value = "/getUserInfo", method = RequestMethod.GET)
    public void getExampleSign(@RequestParam("id") String id, HttpServletResponse response) throws NoSuchAlgorithmException, IOException{
        response.setStatus(200);
        PrintWriter out = response.getWriter();
        out.println("yes");
    }
}
