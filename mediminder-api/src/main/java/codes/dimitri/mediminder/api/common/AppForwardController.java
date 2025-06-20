package codes.dimitri.mediminder.api.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/")
public class AppForwardController {
    @GetMapping
    public void redirectToApp(HttpServletResponse response) throws IOException {
        response.sendRedirect("/app");
    }
}
