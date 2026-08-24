package com.food.userinfo.controller;


import com.food.userinfo.dto.UserDTO;
import com.food.userinfo.service.UserService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    // @PostMapping("/addUser")
    //    public ResponseEntity<UserDTO> addUser(@RequestBody UserDTO userDTO) {
    //        UserDTO user = userService.addUser(userDTO);
    //        return new ResponseEntity<>(user, HttpStatus.OK);
    //    }
    //
    //    @GetMapping("/fetchUserById/{userId}")
    //    public ResponseEntity<UserDTO> fetchUserDetailsById(@PathVariable Integer userId){
    //        return userService.fetchUserDetailsById(userId);
    //    }

    @InjectMocks
    UserController userController;

    @Mock
    UserService userService;

//    public void testFetchUserDetailsById(){
//        Long userId = 1L;
//        UserDTO response=new UserDTO(1,"siddhant","123***34","red street , 202","US");
//        when(userService.fetchUserDetailsById(userId)).thenReturn(new ResponseEntity<>(response));
//    }
}
