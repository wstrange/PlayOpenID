package models;

import java.util.*;

import javax.persistence.*;

import play.data.validation.Required;
import play.db.jpa.*;
import play.db.jpa.GenericModel.JPAQuery;

@Entity
public class User extends Model {

	@Required
	public String email;
	public String password;
	public String fullname;
	public boolean isAdmin;
	public String firstname;
	public String lastname;
	public String language;

	public User(String email, String password, String fullname) {
		this.email = email;
		this.password = password;
		this.fullname = fullname;
	}

	public User(String email) {
		this.email = email;
	}
	
	public String toString() {
		return "user id=" + this.id + " name=" + firstname + " " + lastname 
			+ " language=" + language + " email=" + email;
	}

}