package me.itsghost.jdiscord;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

@Getter
public class Role {
    private final String name;
    private final String id;
    private final String color;
    private final Map<String, Integer> role;

    public Role(String name, String id, String color){
        this.name = name;
        this.id = id;
        this.color = color;
        this.role = new HashMap<>();
    }
    public Role(String name, String id, String color,  Map<String, Integer> role){
        this.name = name;
        this.id = id;
        this.color = color;
        this.role = role;
    }

    public String getRoleColorHEX(){
        int dec = Integer.parseInt(color.replace("#", ""));
        return decimal2hex(dec);
    }

    //STACK
    private String decimal2hex(int d) {
        String digits = "0123456789ABCDEF";
        if (d == 0) return "0";
        String hex = "";
        while (d > 0) {
            int digit = d % 16;                // rightmost digit
            hex = digits.charAt(digit) + hex;  // string concatenation
            d = d / 16;
        }
        return hex;
    }

    public String toString(){
        return "name: " + name + " | id: " + id  + " | color: " + color + " | role meta (map): " + getRoleColorHEX();
    }
}