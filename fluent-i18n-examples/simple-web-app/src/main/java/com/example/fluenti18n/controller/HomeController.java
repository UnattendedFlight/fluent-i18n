package com.example.fluenti18n.controller;

import com.example.fluenti18n.model.OrderStatus;
import com.example.fluenti18n.service.UserService;
import com.example.fluenti18n.service.OrderService;
import io.github.unattendedflight.fluent.i18n.I18n;
import io.github.unattendedflight.fluent.i18n.annotation.Message;
import io.github.unattendedflight.fluent.i18n.core.MessageDescriptor;
import java.util.Locale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {
    
    private final UserService userService;
    private final OrderService orderService;
    private final MessageDescriptor predefinedWelcomeMessage =
        I18n.describe("Welcome to our amazing application!");
    @Message("This application demonstrates natural text internationalization")
    private final String alternatePredefinedMessage =
        "This application demonstrates natural text internationalization";
    
    public HomeController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }
    
    @GetMapping("/")
    public String home(Model model) {
        // Natural text - will be extracted and translated
        model.addAttribute("welcomeMessage",
            predefinedWelcomeMessage.resolve());
        
        model.addAttribute("description", 
            I18n.translate(alternatePredefinedMessage));
        
        // Pluralization example
        int userCount = userService.getUserCount();
        model.addAttribute("userCountMessage", 
            I18n.plural(userCount)
                .zero("No users registered yet")
                .one("One user is using our app")
                .other("{} users are using our app")
                .format());
        
        return "home";
    }
    
    @GetMapping("/user/{id}")
    public String userProfile(@PathVariable Long id, Model model) {
        var user = userService.findUser(id);
        
        if (user == null) {
            model.addAttribute("errorMessage", 
                I18n.translate("User not found"));
            return "error";
        }
        
        model.addAttribute("user", user);
        model.addAttribute("profileTitle", 
            I18n.translate("User Profile for {}", user.getName()));
        
        return "user-profile";
    }
    
    @GetMapping("/orders")
    public String orders(@RequestParam(defaultValue = "PENDING") OrderStatus status, Model model) {
        // Add order data to model
        model.addAttribute("orders", orderService.getAllOrders());
        model.addAttribute("totalOrders", orderService.getTotalOrders());
        model.addAttribute("pendingOrders", orderService.getPendingOrders());
        model.addAttribute("processingOrders", orderService.getProcessingOrders());
        model.addAttribute("completedOrders", orderService.getCompletedOrders());
        
        // Context-aware translation
        model.addAttribute("statusMessage", status.getRepresentation());
        
        model.addAttribute("pageTitle", 
            I18n.translate("Order Management"));
        
        return "orders";
    }
}