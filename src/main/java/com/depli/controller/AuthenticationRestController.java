package com.depli.controller;

import com.depli.service.security.JwtAuthenticationRequest;
import com.depli.service.security.JwtTokenUtil;
import com.depli.service.security.JwtUser;
import com.depli.store.temporary.InputUser;
import com.depli.store.persistent.entity.Authority;
import com.depli.store.persistent.entity.AuthorityName;
import com.depli.store.persistent.entity.User;
import com.depli.service.security.service.JwtAuthenticationResponse;
import com.depli.service.security.service.JwtUserDetailsServiceImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationRestController {

  @Value("${jwt.header}")
  private String tokenHeader;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private JwtTokenUtil jwtTokenUtil;

  @Autowired
  private UserDetailsService userDetailsService;

  @PostMapping("${jwt.route.authentication.path}")
  public ResponseEntity<JwtAuthenticationResponse> createAuthenticationToken(
      @RequestBody JwtAuthenticationRequest authenticationRequest) {

    final Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
            authenticationRequest.getPassword()));

    SecurityContextHolder.getContext().setAuthentication(authentication);

    final UserDetails userDetails = userDetailsService
        .loadUserByUsername(authenticationRequest.getUsername());
    final String token = jwtTokenUtil.generateToken(userDetails);

    return new ResponseEntity<>(new JwtAuthenticationResponse(token), HttpStatus.OK);
  }

  @GetMapping("${jwt.route.authentication.refresh}")
  public ResponseEntity<JwtAuthenticationResponse> refreshAndGetAuthenticationToken(HttpServletRequest request) {
    String token = request.getHeader(tokenHeader).substring(7);
    String username = jwtTokenUtil.getUsernameFromToken(token);
    JwtUser user = (JwtUser) userDetailsService.loadUserByUsername(username);

    if (jwtTokenUtil.canTokenBeRefreshed(token, user.getLastPasswordResetDate())) {
      String refreshedToken = jwtTokenUtil.refreshToken(token);
      return new ResponseEntity<>(new JwtAuthenticationResponse(refreshedToken), HttpStatus.OK);
    } else {
      return new ResponseEntity<>(HttpStatus.OK);
    }
  }

  @PostMapping("${jwt.route.authentication.onboard}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity createNewUser(HttpServletRequest request,
      @RequestBody InputUser inputUser) {
    try {
      User user = new User();
      long id = new Random().nextLong();
      user.setId(id);
      user.setUsername(inputUser.getUsername());
      user.setPassword(new BCryptPasswordEncoder().encode(inputUser.getPassword()));
      user.setFirstName(inputUser.getFirstName());
      user.setLastName(inputUser.getLastName());
      user.setEmail(inputUser.getEmail());
      user.setEnabled(inputUser.isEnabled());
      user.setLastPasswordResetDate(new Date());

      List<Authority> authorities = new ArrayList<>();
      for (String inputAuth : inputUser.getAuthorities()) {
        if (AuthorityName.ROLE_ADMIN.name().equals("ROLE_" + inputAuth)) {
          authorities.add(new Authority().withId(1L).withName(AuthorityName.ROLE_ADMIN));
        }
        if (AuthorityName.ROLE_USER.name().equals("ROLE_" + inputAuth)) {
          authorities.add(new Authority().withId(2L).withName(AuthorityName.ROLE_USER));
        }
      }

      user.setAuthorities(authorities);
      ((JwtUserDetailsServiceImpl) userDetailsService).createUser(user);
      return new ResponseEntity<>(HttpStatus.CREATED);
    } catch (Exception ex) {
      return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }
  }
}