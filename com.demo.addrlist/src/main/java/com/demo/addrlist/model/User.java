package com.demo.addrlist.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by richard on 14/03/2019.
 */
public class User {

    public static Map<String,String> list = new HashMap<>(10);

    static {
        list.put("123", "456");
        list.put("abc", "def");
    }

    private String name;

    private String psword;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPsword() {
        return psword;
    }

    public void setPsword(String psword) {
        this.psword = psword;
    }
}
