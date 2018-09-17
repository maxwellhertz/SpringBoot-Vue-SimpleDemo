package maxwell.springbootvuelast.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @ResponseBody
    public String login(@RequestParam String username,
                        @RequestParam String password) {
        if (username.equals("123") && password.equals("123")) {
            return "successful";
        } else {
            return "failed";
        }
    }

    @RequestMapping(path = "/information", method = RequestMethod.GET)
    @ResponseBody
    public User information() {
        return new User();
    }
}

class User {
    private String username;

    private String email;

    public User() {
        this.email = "123@gmail.com";
        this.username = "123";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}