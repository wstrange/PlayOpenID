package controllers;

import play.*;
import play.libs.OpenID;
import play.libs.OpenID.UserInfo;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

	public static void welcome() {
		render();
	}

	/**
	 * Checks to see if the user is authenticated The @Before annotation marks
	 * this as an intercepter. The unless= means that that login and
	 * authenticate methods will not be intercepted
	 */
	@Before(unless = { "login", "authenticate" })
	static void checkAuthenticated() {
		Logger.info("checkAuthenticated");
		if (!session.contains("user.openid")) {
			login();
		}
	}

	public static void index() {
		String id = session.get("user.openid");
		String email = session.get("user.email");
		User user = User.find("byEmail", email).first();
		render(id,email,user);
	}

	/**
	 * Render the login page. Displays the openid login form. 
	 * 
	 */
	public static void login() {
		Logger.info("in login()");
		render();
	}

	/**
	 * Kill the user session and redirect to the login page
	 */
	public static void logout() {
		session.remove("user.openid");
		session.remove("user.email");
		login();
	}

	/**
	 * This method gets mapped to any action (GET,POST) on /authenticate
	 * It is called initially to make the OpenId request. The user will
	 * be redirected to the provider, and after they authenticate (or fail to authenticate) 
	 * the provider will redirect them back to this method with the results.
	 * 
	 * @param url - the openid url of the provider
	 */
	public static void authenticate(String url) {
		
		// is the request a response back from the Provider?
		if (OpenID.isAuthenticationResponse()) {
			UserInfo info = OpenID.getVerifiedID();
			if (info == null) {
				flash.put("error", "Oops. Authentication has failed");
				login();
			}
					
			Map<String, String> ext = info.extensions;
			System.out.println("Extensions=" + ext + " size=" + ext.size());
			String email = ext.get("email");
			if( email == null ) {
				flash.put("error", "You must provide an email address!");
				login();
			}
			
			User user = User.find("byEmail", email).first();
			
			if (user != null) {
				Logger.info("Existing User found! User=" + user);
				// TODO: check for matching openids, fail if none
			}
			// Create new user if none exists
			else {
				Logger.info("Creating new user");
				user = new User(email);
			
				// TODO: create new openid entry
			}
			// update user
			user.firstname = ext.get("firstname");
			user.lastname = ext.get("lastname");
			user.language = ext.get("language");
			
			Logger.info("Saving user u=" + user.save());
			
			
			session.put("user.openid", info.id);
			session.put("user.email", email);
			
			index();
		} else {
			Logger.info("Creatng OpenID request=" + url);
			OpenID oi = OpenID.id(url);
			// The list extension attributes that are mandatory
			oi.required("email","http://axschema.org/contact/email");
			// The list of attributes that are optional
			// NOTE: When these are made optional Google Does not pass them on or ask for them
			// They have been set to required
			oi.required("firstname","http://axschema.org/namePerson/first");
			oi.required("lastname", "http://axschema.org/namePerson/last");
			oi.required("language", "http://axschema.org/pref/language");
			// This will result in a redirect to the OpenID provider
			if (!oi.verify()) {
				flash.error("secure.error.openidverifyfailed");
				login();
			}
		}
	}

}