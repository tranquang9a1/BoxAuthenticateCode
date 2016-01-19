# BoxAuthenticateCode
Manual get authenticate code from box.com Java

Create class ApiClientConfig to using this function

```java
ApiClientConfig config = new ApiClientConfig(HOST,USERNAME, PASSWORD, CLIENT_ID, CLIENT_SECRET);
```

After that call function
```java
BoxApiClient client = new BoxApiClient();
String authorizeCode = client.getAuthorizeCode(config);
```


After that using BoxSDK (https://github.com/box/box-java-sdk) to authorize
