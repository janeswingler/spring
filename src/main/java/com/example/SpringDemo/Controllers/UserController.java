package com.example.SpringDemo.Controllers;


import com.example.SpringDemo.Models.User;
import com.example.SpringDemo.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/create")
    public User createUser(@RequestBody User user) {
        System.out.println(user);
        return userService.saveUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}/proficiency")
    public ResponseEntity<String> updateProficiencyLevel(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newLevel = body.get("proficiencyLevel");
        userService.updateProficiencyLevel(id, newLevel);
        return ResponseEntity.ok("Proficiency level updated successfully");
    }


}






//import com.example.SpringDemo.Models.User;
//import com.example.SpringDemo.Services.UserService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/users")
//@CrossOrigin
//public class UserController {
//
//    private final UserService userService;
//
//    @Autowired
//    public UserController(UserService userService) {
//        this.userService = userService;
//    }
//
//    @PostMapping("/create")
//    public User createUser(@RequestBody User user) {
//        return userService.createUser(user);
//    }
//
//    @GetMapping("/")
//    public List<User> getAllUsers() {
//        return userService.getAllUsers();
//    }
//}
