package nz.coreyh.springchat.web.controller

import nz.coreyh.springchat.config.Routes
import nz.coreyh.springchat.config.Templates
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {

    @GetMapping(Routes.HOME)
    fun getHome(): String {
        return Templates.HOME
    }
}