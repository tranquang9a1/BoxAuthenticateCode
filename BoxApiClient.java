package com.hickup.hurryapp.service.box;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.box.sdk.BoxAPIConnection;
import com.hickup.hurryapp.service.showpad.ApiClientConfig;

@SuppressWarnings("deprecation")
public class BoxApiClient {
	
	private String AUTHENTICATE_CODE = "";
	private String redirectUri = "http%3A//localhost%3A4000"; //http://localhost:4000
	private final String USER_AGENT = "Mozilla/5.0";
	private HttpClient client = new DefaultHttpClient();
	private String cookies;
	
	public BoxAPIConnection createConnection(ApiClientConfig config) throws Exception {
		getAuthorizeCode(config);
		return new BoxAPIConnection(config.getClientId(), config.getSecret(), this.AUTHENTICATE_CODE);
	}
	
	public void getAuthorizeCode(ApiClientConfig config) throws Exception{

		String url = "https://app.box.com/api/oauth2/authorize?response_type=code&client_id="
				+ config.getClientId() + "&redirect_uri=" + redirectUri;
		System.out.println(url);
		CookieHandler.setDefault(new CookieManager());
		String page = getPageContent(url);
		Map<String,String> loginFormData = new HashMap<String, String>();
		loginFormData.put("login", config.getUsername());
		loginFormData.put("password", config.getPassword());
		List<NameValuePair> postParams = getParams(page, "login_form",loginFormData);
		String grantpage = sendPost(url, config, postParams,false);
		Map<String,String> grantFormData = new HashMap<String, String>();
		grantFormData.put("consent_reject", "");
		List<NameValuePair> grantParams = getParams(grantpage,"consent_form",grantFormData);
		String code = sendPost(url,config, grantParams,true);
		System.out.println(code);
		this.AUTHENTICATE_CODE = code;
	}
	
	public String getPageContent(String url) throws Exception{
		HttpGet request = new HttpGet(url);

		request.setHeader("User-Agent", USER_AGENT);
		request.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		request.setHeader("Accept-Language", "en-US,en;q=0.5");

		HttpResponse response = client.execute(request);
		int responseCode = response.getStatusLine().getStatusCode();

		//System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}

		// set cookies
		setCookies(response.getFirstHeader("Set-Cookie") == null ? ""
				: response.getFirstHeader("Set-Cookie").toString());

		return result.toString();
	}
	
	public String getCookies() {
		return cookies;
	}

	public void setCookies(String cookies) {
		this.cookies = cookies;
	}
	
	public List<NameValuePair> getParams(String html, String formname,
			Map<String,String> formdata) throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		Element loginform = doc.getElementsByAttributeValue("name",
				formname).first();
		Elements inputElements = loginform.getElementsByTag("input");
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();

		for (Element inputElement : inputElements) {
			String ekey = inputElement.attr("name");
			String value = inputElement.attr("value");
			
			for (String datakey : formdata.keySet()) {
				if (ekey.equals(datakey))
					value = formdata.get(datakey);
			}
			paramList.add(new BasicNameValuePair(ekey, value));
		}
		return paramList;
	}
	
	private String sendPost(String url, ApiClientConfig config, List<NameValuePair> postParams, boolean getAuthCode) throws Exception {

		HttpPost post = new HttpPost(url);

		// add header
		post.setHeader("Host", "app.box.com");
		post.setHeader("User-Agent", USER_AGENT);
		post.setHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		post.setHeader("Accept-Language", "en-US,en;q=0.5");
		post.setHeader("Cookie", getCookies());
		post.setHeader("Connection", "keep-alive");
		post.setHeader("Referer",
				"https://app.box.com/api/oauth2/authorize?response_type=code&client_id="+ config.getClientId() + "&redirect_uri=http%3A//localhost%3A" + "4000");
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");

		post.setEntity(new UrlEncodedFormEntity(postParams));

		HttpResponse response = client.execute(post);

		int responseCode = response.getStatusLine().getStatusCode();

		//System.out.println("\nSending 'POST' request to URL : " + url);
		//System.out.println("Post parameters : " + postParams);
		System.out.println("Response Code : " + responseCode);

		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		setCookies(response.getFirstHeader("Set-Cookie") == null ? ""
				: response.getFirstHeader("Set-Cookie").toString());
		
		//get response headers
		String code = "";
		Header[] headers = response.getAllHeaders();
		for (Header header : headers) {
			//System.out.println("[Response Header] Name: " + header.getName() + " Value: " + header.getValue());
			if (header.getName().equals("Location")){
				code = header.getValue().substring(header.getValue().indexOf("code=")+5);
			}
		}
		if (getAuthCode) 
			return code;
		else 	
			return result.toString();
	}
}
