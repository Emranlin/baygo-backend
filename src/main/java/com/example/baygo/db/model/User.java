package com.example.baygo.db.model;

import com.example.baygo.db.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;

@Getter
@Setter
@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_gen")
    @SequenceGenerator(name = "user_gen", sequenceName = "user_seq", allocationSize = 1, initialValue = 7)
    private Long id;
    private String email;
    private String password;
    private String phoneNumber;
    private String resetPasswordToken;
    private int confirmCode;
    private boolean isVerify;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = ALL)
    private Seller seller;

    @OneToOne(mappedBy = "user", cascade = ALL)
    private Buyer buyer;
    @OneToMany(mappedBy = "user", cascade = ALL)
    private List<Chat> chats;

    public void addChat(Chat chat) {
        if (this.chats == null) {
            this.chats = new ArrayList<>();
        }
        this.chats.add(chat);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}