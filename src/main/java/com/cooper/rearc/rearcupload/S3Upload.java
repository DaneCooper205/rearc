package com.cooper.rearc.rearcupload;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.cooper.rearc.rearcupload.util.PropertiesCache;
import org.json.JSONObject;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

public class S3Upload {
	

	
	private final static  String bucketName = PropertiesCache.getInstance().getProperty("bucketName");
	private final static String targetLocation = PropertiesCache.getInstance().getProperty("targetLocation");
	private final static String accessKey= PropertiesCache.getInstance().getProperty("accessKey");
	private final static  String secretKey= PropertiesCache.getInstance().getProperty("secretKey");
	private final static  String userAgent =  PropertiesCache.getInstance().getProperty("userAgent");
	private final static  String urlStr = PropertiesCache.getInstance().getProperty("urlStr");
	private final static  String dataSet1 = PropertiesCache.getInstance().getProperty("dataSet1");
	private final static  String dataSet2 = PropertiesCache.getInstance().getProperty("dataSet2");
	private final static String datausUrl=PropertiesCache.getInstance().getProperty("datausa.url");
	private final static  String dataSet3 = PropertiesCache.getInstance().getProperty("dataSet3");
	
	private S3Client client = null;
	
	public static void main(String[] args) {
		S3Upload uploader = new S3Upload();
		uploader.readUrl(urlStr,dataSet1 );
		uploader.readUrl(urlStr,dataSet2 );
		uploader.readApi(datausUrl,dataSet3);
	}
	

	public S3Upload() {
		super();
		client = createClient();
	}

	
	private void readUrl( String urlbase, String fileName ) {
		BufferedReader in = null;
		
		try {
			String urlStr = urlbase +fileName;
			HttpURLConnection connection =(HttpURLConnection) URI.create(urlStr).toURL().openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("User-Agent", userAgent);
		
			
			 // Get response code
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

    		PutObjectRequest request = PutObjectRequest.builder().bucket( bucketName).key( targetLocation+fileName ).build();
			in =  new BufferedReader( new InputStreamReader(connection.getInputStream()));
			String inputLine;
			List<String> content = new ArrayList<String>();
			while ((inputLine = in.readLine())!=null) {
				content.add(inputLine);
			} 
			InputStream data = new ByteArrayInputStream(String.join("\n", content).getBytes(StandardCharsets.UTF_8));
	        
			client.putObject(request,RequestBody.fromInputStream(data,data.available()));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			
			try {
				in.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}





	private S3Client createClient() {
		S3Client client =S3Client.builder()
				.region(Region.US_EAST_2) // e.g., Region.US_EAST_1
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
				.build();
		return client;
	}
	
	private void readApi( String urlStr, String fileName) {
		try {
			HttpURLConnection connection =(HttpURLConnection) URI.create(urlStr).toURL().openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/json");
			
			int responseCode = connection.getResponseCode();
			 if (responseCode == HttpURLConnection.HTTP_OK) { // 200
	                // Read the response
	                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	                StringBuilder response = new StringBuilder();
	                String line;
	                List<String> content = new ArrayList<String>();
	                while ((line = reader.readLine()) != null) {
	                    response.append(line);
	                    content.add(line);
	                }
	                reader.close();
	                
	                // Parse the JSON response
	                JSONObject jsonResponse = new JSONObject(response.toString());

	                // Print the JSON object
	                System.out.println("JSON Response: " + jsonResponse.toString(4)); // Pretty print with indentation
	                
	                PutObjectRequest request = PutObjectRequest.builder().bucket( bucketName).key( targetLocation+fileName ).build();
	                InputStream data = new ByteArrayInputStream(String.join("\n", content).getBytes(StandardCharsets.UTF_8));
	    	        
	    			client.putObject(request,RequestBody.fromInputStream(data,data.available()));
	                
			 	} else if (responseCode == HttpURLConnection.HTTP_BAD_GATEWAY) {
			 		 System.out.println("Gateway is still down Response Code: " + responseCode +": " + datausUrl);
	            } else {
	                System.out.println("GET request failed. Response Code: " + responseCode + ": " + datausUrl);
	            }

	            // Disconnect the connection
	            connection.disconnect();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
			
		
		
	
}
