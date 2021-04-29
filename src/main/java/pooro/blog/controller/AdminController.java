package pooro.blog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import pooro.blog.config.auth.dto.SessionUser;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final HttpSession httpSession;

//    @GetMapping
//    public void login() {
//        SessionUser user = (SessionUser) httpSession.getAttribute("user");
//        if (user != null) {
//            System.out.println(user.toString());
//        }
//    }

    @GetMapping
    public String adminHome() {
        return "admin";
    }

    @GetMapping("/new-post")
    public String newPost() {
        return "new-post";
    }
}
