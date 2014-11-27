package com.example.gregoire.rfa;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class WebService
{
	/*Attributs*/
	public String URL;
	private int code = -1;
	private HttpClient httpclient;
	
	/*Adresses*/
	private String SESSION = "/session";
	private String NEW_SESSION = "/session/new";
	private String FEEDS = "/feeds";
	private String FEED = "/feeds/";
	
	private String PAGE = "?page=";
	private String LIMIT = "&limit=";
	private String EMAIL = "?email=";
	private String PWD = "&password=";
	
	
	/*Constructeurs*/
	public WebService(String url)
    {
		URL = url;
		httpclient = new DefaultHttpClient();
	}
	
	
	/*Methodes d'envoie de requete*/
	
	/**
	 * Envoie la requete GET au WebService.
	 * @param url adresse du WebService
	 * @return reponse du WebService
	 * @throws Exception e
	 */
	private String sendGetRequest(String url) throws Exception
    {
        code = -1;
        HttpGet httpget = new HttpGet(url);
        HttpResponse response = httpclient.execute(httpget);

        code = response.getStatusLine().getStatusCode();
        String rep = "";
        HttpEntity entity = response.getEntity();
        if (entity != null)
        {
            rep = EntityUtils.toString(entity);
            response.getEntity().consumeContent();
            return rep;
        }
        return rep;
	}

	private String sendPostRequest(String url, List<NameValuePair> nameValuePairs) throws Exception {
		try {
			code = -1;
			
			/*On set l'adresse a une requete de type POST et on ajoute le content-type*/
			HttpPost httppost = new HttpPost(url);
			httppost.addHeader("content-type", "application/x-www-form-urlencoded");
	        
	        /*On set les donnees*/
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			/*On execute la requete et on recupere la reponse*/
			HttpResponse response = httpclient.execute(httppost);
			code = response.getStatusLine().getStatusCode();
			
			String rep = "";
			InputStream stream = response.getEntity().getContent();
			if (stream != null)
				rep = stream.toString();
			
			if( response.getEntity() != null )
				response.getEntity().consumeContent();
			
			return rep;
			
		} catch (Exception e) { throw new Exception(e.getMessage()); }
	}

	private String sendDeleteRequest(String url) throws Exception {
		try {
			code = -1;
			
			/*On set l'adresse a une requete de type POST et on ajoute le content-type*/
			HttpDelete httpdelete = new HttpDelete(url);
			
			/*On execute la requete et on recupere la reponse*/
			HttpResponse response = httpclient.execute(httpdelete);
			code = response.getStatusLine().getStatusCode();
			
			String rep = "";
			InputStream stream = response.getEntity().getContent();
			if (stream != null)
				rep = stream.toString();
			
			if (response.getEntity() != null )
                response.getEntity().consumeContent();
			return rep;
			
		} catch (Exception e) {throw new Exception(e.getMessage()); }
	}	
	
	public boolean connectUser(String email, String pwd) throws Exception {

		String url = URL + NEW_SESSION;

        /*On set les donnees*/
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("email", email));
        nameValuePairs.add(new BasicNameValuePair("password", pwd));
		
		try {   sendPostRequest(url, nameValuePairs);   }
        catch (Exception e) { throw new Exception(e.getMessage()); }

        return code == 200;
	}

	/*Get session*/
	public String getUserInfo(String email, String pwd) throws Exception {
		
		String url = URL + SESSION + EMAIL + email + PWD + pwd;
		String json;
        
		try {
			json = sendGetRequest(url);
		} catch (Exception e) { throw new Exception(e.getMessage()); }

		if (code == 401)
			json = "401";
		
		return json;
	}
	
	public String getFeeds() throws Exception {
		
		String url = URL + FEEDS;
		String json = "";
        
		try {
			json = sendGetRequest(url);
		} catch (Exception e) { throw new Exception(e.getMessage()); }
		
		if (code == 401)
			json = "401";
		
		return json;
	}
	
	public String getFeedContent(int id) throws Exception {
		
		String url = URL + FEED + Integer.toString(id);
		String json = "";
        
		try {
			json = sendGetRequest(url);
		} catch (Exception e) { throw new Exception(e.getMessage()); }
		
		if (code == 401)
			json = "401";
		if (code == 404)
			json = "404";
		
		return json;
	}
	
	public String getFeed(int id, int page, int limit) throws Exception {
		
		String url = URL + FEED + Integer.toString(id) + PAGE + Integer.toString(page) + LIMIT + Integer.toString(limit);
		String json = "";
        
		try {
			json = sendGetRequest(url);
		} catch (Exception e) { throw new Exception(e.getMessage()); }
		
		if (code == 401)
			json = "401";
		if (code == 404)
			json = "404";
		
		return json;
	}
	
	public String addFeed(String add_url) throws Exception
    {
		String json;
		String url = URL + FEEDS;
        
        /*On set les donnees*/
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("feed[url]", add_url));
		
		try {
			json = sendPostRequest(url, nameValuePairs);
		} catch (Exception e) { throw new Exception(e.getMessage()); }
		
		if (code == 401)
			json = "401";
		if (code == 404)
			json = "404";
		
		return json;
		
	}
	
	public boolean addUser(String email, String pwd) throws Exception {

		String url = URL + SESSION;
        
        /*On set les donnees*/
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair("email", email));
        nameValuePairs.add(new BasicNameValuePair("password", pwd));
		
		try {
			sendPostRequest(url, nameValuePairs);
		} catch (Exception e) { throw new Exception(e.getMessage()); }
		
		if (code == 422)
			return false;
		
		return true;
		
	}
	
	public int disconnectUser() throws Exception {
		
		String url = URL + SESSION;
        
		try {
			sendDeleteRequest(url);
		} catch (Exception e) { throw new Exception(e.getMessage()); }
		
		return code;
	}
	
	public int deleteFeed(int id) throws Exception
    {
		String url = URL + FEED + Integer.toString(id);
        
		try
        {
			sendDeleteRequest(url);
		}
        catch (Exception e) { throw new Exception(e.getMessage()); }
		
		return code;
	}
}