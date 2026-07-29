/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tss.logic;

import jakarta.ejb.Remote;
import tss.entity.Person;
import tss.dto.User;

/**
 *
 * @author parth
 */
@Remote
public interface UserLogic {
    public static final  String USER_ROLE="User" ;
    public static final  String ADMIN_ROLE="Admin" ;
    public static final String ASSISTANT_ROLE="Assistant";
    public static final String SECREATRY_ROLE="Secretary";
    public static final String GUEST_ROLE="Guest";
    public static final String SUPERVISOR_ROLE="Supervisor";
     
     public User getCurrentUser();
}
